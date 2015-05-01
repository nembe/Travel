package nl.yellowbrick.admin.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.TransponderCardDao;
import nl.yellowbrick.data.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customers")
public class CustomerSearchController {

    @Autowired CustomerDao customerDao;
    @Autowired TransponderCardDao transponderCardDao;

    private static final SearchFilter EMPTY_FILTER = new SearchFilter();

    @ModelAttribute("filter")
    public SearchFilter filter() {
        return new SearchFilter();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String customersFiltered(ModelMap model, @ModelAttribute("filter") SearchFilter filter) {
        List<CustomerListItem> customers = EMPTY_FILTER.equals(filter)
                ? Lists.newArrayList()
                : searchCustomers(filter);

        model.put("customers", customers);

        return "customers/index";
    }

    private List<CustomerListItem> searchCustomers(SearchFilter filter) {
        Map<Long, Customer> results = Maps.newHashMap();

        searchByCustomerNumber(filter.getCustomerNumber(), results);
        searchByEmail(filter.getEmail(), results);
        searchByPhoneNumber(filter.getPhoneNumber(), results);
        searchByTransponderCardNumber(filter.getTransponderCardNumber(), results);

        return results.values().stream().map(CustomerListItem::new).collect(Collectors.toList());
    }

    private void searchByCustomerNumber(String customerNumber, Map<Long, Customer> results) {
        if(Strings.isNullOrEmpty(customerNumber))
            return;

        customerDao.findByCustomerNr(customerNumber).ifPresent(c -> results.put(c.getCustomerId(), c));
    }

    private void searchByEmail(String email, Map<Long, Customer> results) {
        if(Strings.isNullOrEmpty(email))
            return;

        customerDao.findAllByEmail(email).forEach(c -> results.put(c.getCustomerId(), c));
    }

    private void searchByPhoneNumber(String phoneNr, Map<Long, Customer> results) {
        if(Strings.isNullOrEmpty(phoneNr))
            return;

        customerDao.findAllByMobile(phoneNr).forEach(c -> results.put(c.getCustomerId(), c));
    }

    private void searchByTransponderCardNumber(String cardNumber, Map<Long, Customer> results) {
        if(Strings.isNullOrEmpty(cardNumber))
            return;

        transponderCardDao.findByCardNumber(cardNumber).ifPresent(t -> {
            customerDao.findById(t.getCustomerId()).ifPresent(c -> results.put(c.getCustomerId(), c));
        });
    }

    private static class SearchFilter {
        private String customerNumber;
        private String phoneNumber;
        private String email;
        private String transponderCardNumber;

        public String getCustomerNumber() {
            return customerNumber;
        }

        public void setCustomerNumber(String customerNumber) {
            this.customerNumber = customerNumber;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTransponderCardNumber() {
            return transponderCardNumber;
        }

        public void setTransponderCardNumber(String transponderCardNumber) {
            this.transponderCardNumber = transponderCardNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SearchFilter that = (SearchFilter) o;

            if (customerNumber != null ? !customerNumber.equals(that.customerNumber) : that.customerNumber != null)
                return false;
            if (email != null ? !email.equals(that.email) : that.email != null) return false;
            if (phoneNumber != null ? !phoneNumber.equals(that.phoneNumber) : that.phoneNumber != null) return false;
            if (transponderCardNumber != null ? !transponderCardNumber.equals(that.transponderCardNumber) : that.transponderCardNumber != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = customerNumber != null ? customerNumber.hashCode() : 0;
            result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
            result = 31 * result + (email != null ? email.hashCode() : 0);
            result = 31 * result + (transponderCardNumber != null ? transponderCardNumber.hashCode() : 0);
            return result;
        }
    }

    private static class CustomerListItem {

        public final String customerId;
        public final String customerType;
        public final String clientName;
        public final String productGroup;
        public final String status;

        private CustomerListItem(Customer customer) {
            this.customerId = String.valueOf(customer.getCustomerId());
            this.customerType = customer.isBusinessCustomer() ? "business" : "personal";
            this.clientName = customer.getFullName();
            this.productGroup = customer.getProductGroup();
            this.status = customer.getStatus().name();
        }
    }
}
