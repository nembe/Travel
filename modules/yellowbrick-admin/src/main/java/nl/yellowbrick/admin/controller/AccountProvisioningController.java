package nl.yellowbrick.admin.controller;

import nl.yellowbrick.activation.service.AccountActivationService;
import nl.yellowbrick.activation.validation.AccountRegistrationValidator;
import nl.yellowbrick.admin.exceptions.InconsistentDataException;
import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.admin.form.AccountProvisioningForm;
import nl.yellowbrick.data.dao.CustomerAddressDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerAddress;
import nl.yellowbrick.data.domain.CustomerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/provisioning")
public class AccountProvisioningController {

    private static final Logger log = LoggerFactory.getLogger(AccountProvisioningController.class);

    private CustomerDao customerDao;
    private CustomerAddressDao addressDao;
    private AccountActivationService accountActivationService;
    private AccountRegistrationValidator[] accountRegistrationValidators;
    private ConversionService conversionService;

    @Autowired
    public AccountProvisioningController(CustomerDao customerDao,
                                         CustomerAddressDao addressDao,
                                         AccountActivationService accountActivationService,
                                         ConversionService conversionService,
                                         AccountRegistrationValidator... accountRegistrationValidators) {
        this.customerDao = customerDao;
        this.addressDao = addressDao;
        this.accountActivationService = accountActivationService;
        this.conversionService = conversionService;
        this.accountRegistrationValidators = accountRegistrationValidators;
    }

    public void setCustomerDao(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public void setAddressDao(CustomerAddressDao addressDao) {
        this.addressDao = addressDao;
    }

    public void setAccountActivationService(AccountActivationService accountActivationService) {
        this.accountActivationService = accountActivationService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String pendingValidation(Model model) {
        model.addAttribute("customers", customersPendingManualValidation());

        return "provisioning/index";
    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public String validate(Model model, @PathVariable("id") int id) {
        Customer customer = customerById(id);
        CustomerAddress address = addressForCustomer(id);

        AccountProvisioningForm form = form(customer, address);

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "form");
        errors.initConversion(conversionService);

        for(Validator validator: accountRegistrationValidators) {
            ValidationUtils.invokeValidator(validator, customer, errors);
        }

        model.addAttribute("form", form);
        model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "form", errors);

        return "provisioning/validate";
    }

    @RequestMapping(method = RequestMethod.POST, value = "{id}", params = {"validate"})
    public String saveValidated(@ModelAttribute("form") AccountProvisioningForm form,
                                @PathVariable("id") int id,
                                BindingResult bindingResult,
                                ModelMap model) {

        // TODO run some additional validations here ?
        Customer customer = customerById(id);
        CustomerAddress address = addressForCustomer(id);

        updateCustomer(customer, form);
        updateAddress(address, form);

        model.clear();

        // save changes
        customerDao.savePrivateCustomer(customer);
        addressDao.savePrivateCustomerAddress(customer.getCustomerId(), address);

        // and activate customer
        accountActivationService.activateCustomerAccount(customer);

        return "redirect:/provisioning";
    }

    private List<Customer> customersPendingManualValidation() {
        return customerDao.findAllPendingActivation().stream()
                .filter((customer) -> customer.getCustomerStatusIdfk() == CustomerStatus.ACTIVATION_FAILED.code())
                .collect(Collectors.toList());
    }

    private Customer customerById(int customerId) {
        return customersPendingManualValidation().stream()
                .filter((cust) -> cust.getCustomerId() == customerId)
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }

    private CustomerAddress addressForCustomer(int customerId) {
        Optional<CustomerAddress> address = addressDao.findByCustomerId(customerId);

        if(address.isPresent())
            return address.get();

        String error = "couldn't find address for customer id: " + customerId;

        log.error(error);
        throw new InconsistentDataException(error);
    }

    private AccountProvisioningForm form(Customer customer, CustomerAddress address) {
        AccountProvisioningForm form = new AccountProvisioningForm();

        form.setGender(customer.getGender());
        form.setInitials(customer.getInitials());
        form.setFirstName(customer.getFirstName());
        form.setInfix(customer.getInfix());
        form.setLastName(customer.getLastName());
        form.setDateOfBirth(new Date());
        form.setEmail(customer.getEmail());
        form.setPhoneNr(customer.getPhoneNr());

        form.setStreet(address.getAddress());
        form.setHouseNr(address.getHouseNr());
        form.setSupplement(address.getSupplement());
        form.setPostalCode(address.getZipCode());
        form.setCity(address.getCity());
        form.setCountry(address.getCountryCode());

        form.setNumberOfTransponderCards(customer.getNumberOfTCards());
        form.setNumberOfPPlusCards(customer.getNumberOfQCards());

        return form;
    }

    private Customer updateCustomer(Customer customer, AccountProvisioningForm form) {
        customer.setGender(form.getGender());
        customer.setInitials(form.getInitials());
        customer.setFirstName(form.getFirstName());
        customer.setInfix(form.getInfix());
        customer.setLastName(form.getLastName());
        customer.setDateOfBirth(form.getDateOfBirth());
        customer.setEmail(form.getEmail());
        customer.setPhoneNr(form.getPhoneNr());

        customer.setNumberOfTCards(form.getNumberOfTransponderCards());
        customer.setNumberOfQCards(form.getNumberOfPPlusCards());

        return customer;
    }

    private void updateAddress(CustomerAddress address, AccountProvisioningForm form) {
        address.setAddress(form.getStreet());
        address.setHouseNr(form.getHouseNr());
        address.setSupplement(form.getSupplement());
        address.setZipCode(form.getPostalCode());
        address.setCity(form.getCity());
        address.setCountryCode(form.getCountry());
    }
}
