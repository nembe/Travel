package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.exceptions.InconsistentDataException;
import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerStatus;
import nl.yellowbrick.data.domain.ProductGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/provisioning/accounts")
public class AccountProvisioningListController {

    @Autowired private CustomerDao customerDao;
    @Autowired private ProductGroupDao productGroupDao;

    @RequestMapping(method = RequestMethod.GET, params = { "productGroup" })
    public String accountsPendingValidation(ModelMap model, @RequestParam("productGroup") long productGroupId) {
        List<ProductGroup> allProductGroups = productGroupDao.all();
        ProductGroup selectedProductGroup = allProductGroups.stream()
                .filter(pg -> pg.getId() == productGroupId)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("could not find product group with id " + productGroupId));

        return accountsPendingValidation(model, allProductGroups, selectedProductGroup);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String accountsPendingValidation(ModelMap model) {
        List<ProductGroup> allProductGroups = productGroupDao.all();
        ProductGroup defaultProductGroup = allProductGroups.stream()
                .findFirst()
                .orElseThrow(() -> new InconsistentDataException("no product groups"));

        return accountsPendingValidation(model, allProductGroups, defaultProductGroup);
    }

    private String accountsPendingValidation(ModelMap model,
                                             List<ProductGroup> allProductGroups,
                                             ProductGroup selectedProductGroup) {
        model.addAttribute("allProductGroups", allProductGroups);
        model.addAttribute("productGroup", selectedProductGroup);
        model.addAttribute("customers", customersPendingManualValidation()
                .filter(customer -> customer.getProductGroupId() == selectedProductGroup.getId())
                .collect(Collectors.toList()));

        return "provisioning/accounts/index";
    }

    private Stream<Customer> customersPendingManualValidation() {
        return customerDao.findAllPendingActivation().stream()
                .filter(customer -> customer.getCustomerStatusIdfk() == CustomerStatus.ACTIVATION_FAILED.code());
    }
}
