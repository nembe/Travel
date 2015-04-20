package nl.yellowbrick.admin.controller;

import nl.yellowbrick.data.dao.CustomerAddressDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.yellowbrick.admin.util.CommonRequestParams.PRODUCT_GROUP_KEY;
import static nl.yellowbrick.admin.util.CommonRequestParams.from;

@Controller
@RequestMapping("/provisioning/accounts")
public class AccountProvisioningListController {

    @Autowired private CustomerDao customerDao;
    @Autowired private CustomerAddressDao customerAddressDao;

    @ModelAttribute("allStatuses")
    public Map<String, StatusFilter> statuses() {
        return new HashMap<String, StatusFilter>() {{
            put("0", StatusFilter.ANY);
            put("1", StatusFilter.FAILED);
            put("2", StatusFilter.REGISTERED);
        }};
    }

    @RequestMapping(method = RequestMethod.GET)
    public String accountsPendingValidation(ModelMap model,
                                            @RequestParam Map<String, String> requestParams,
                                            @ModelAttribute("allProductGroups") List<ProductGroup> allProductGroups,
                                            @ModelAttribute("allStatuses") Map<String, StatusFilter> allStatuses) {

        ProductGroup productGroup = from(requestParams).productGroupOrDefault(allProductGroups);
        StatusFilter statusFilter = requestedStatusFilterOrDefault(requestParams, allStatuses);
        List<AccountListItem> customers = customersPendingActivation(productGroup, statusFilter)
                .map(this::accountListItem)
                .collect(Collectors.toList());

        model.addAttribute(PRODUCT_GROUP_KEY, productGroup);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("customers", customers);

        return "provisioning/accounts/index";
    }

    private StatusFilter requestedStatusFilterOrDefault(Map<String, String> requestParams,
                                                        Map<String, StatusFilter> allStatuses) {
        if(!requestParams.containsKey("statusFilter"))
            return StatusFilter.ANY;

        return allStatuses.getOrDefault(requestParams.get("statusFilter"), StatusFilter.ANY);
    }

    private Stream<Customer> customersPendingActivation(ProductGroup productGroup, StatusFilter statusFilter) {
        return customerDao.findAllPendingActivation().stream()
                .filter(customer -> customer.getProductGroupId() == productGroup.getId())
                .filter(statusFilter.filter);
    }

    private enum StatusFilter {
        ANY(c -> Arrays.asList(CustomerStatus.ACTIVATION_FAILED, CustomerStatus.REGISTERED).contains(c.getStatus())),
        FAILED(c -> c.getStatus() == CustomerStatus.ACTIVATION_FAILED),
        REGISTERED(c -> c.getStatus() == CustomerStatus.REGISTERED);

        final Predicate<Customer> filter;

        StatusFilter(Predicate<Customer> filter) {
            this.filter = filter;
        }
    }

    private AccountListItem accountListItem(Customer cust) {
        Optional<CustomerAddress> address = customerAddressDao.findByCustomerId(cust.getCustomerId(), AddressType.MAIN);

        return address.isPresent()
                ? new AccountListItem(cust, address.get())
                : new AccountListItem(cust);
    }

    private class AccountListItem {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public final String customerId;
        public final String registrationTime;
        public final String customerType;
        public final String clientName;
        public final String location;
        public final String productGroup;
        public final String status;

        private AccountListItem(Customer customer) {
            this(customer, "");
        }

        private AccountListItem(Customer customer, CustomerAddress address) {
            this(customer, address.getCity());
        }

        private AccountListItem(Customer customer, String location) {
            this.customerId = String.valueOf(customer.getCustomerId());
            this.registrationTime = customer.getApplicationDate() != null
                    ? dateFormat.format(customer.getApplicationDate())
                    : "";
            this.customerType = customer.isBusinessCustomer() ? "business" : "personal";
            this.clientName = customer.getFullName();
            this.productGroup = customer.getProductGroup();
            this.status = customer.getStatus().name();
            this.location = location;
        }
    }
}
