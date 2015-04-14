package nl.yellowbrick.admin.controller;

import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerStatus;
import nl.yellowbrick.data.domain.ProductGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static nl.yellowbrick.admin.util.CommonRequestParams.PRODUCT_GROUP_KEY;
import static nl.yellowbrick.admin.util.CommonRequestParams.from;

@Controller
@RequestMapping("/provisioning/accounts")
public class AccountProvisioningListController {

    @Autowired private CustomerDao customerDao;

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

        model.addAttribute(PRODUCT_GROUP_KEY, productGroup);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("customers", customersPendingActivation(productGroup, statusFilter));

        return "provisioning/accounts/index";
    }

    private StatusFilter requestedStatusFilterOrDefault(Map<String, String> requestParams,
                                                        Map<String, StatusFilter> allStatuses) {
        if(!requestParams.containsKey("statusFilter"))
            return StatusFilter.ANY;

        return allStatuses.getOrDefault(requestParams.get("statusFilter"), StatusFilter.ANY);
    }

    private List<Customer> customersPendingActivation(ProductGroup productGroup, StatusFilter statusFilter) {
        return customerDao.findAllPendingActivation().stream()
                .filter(customer -> customer.getProductGroupId() == productGroup.getId())
                .filter(statusFilter.filter)
                .collect(Collectors.toList());
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
}
