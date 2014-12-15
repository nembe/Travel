package nl.yellowbrick.admin.controller;

import nl.yellowbrick.activation.service.AccountActivationService;
import nl.yellowbrick.activation.validation.AccountRegistrationValidator;
import nl.yellowbrick.admin.exceptions.InconsistentDataException;
import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.admin.form.AccountProvisioningForm;
import nl.yellowbrick.data.dao.*;
import nl.yellowbrick.data.domain.*;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/provisioning")
public class AccountProvisioningController {

    private static final Logger log = LoggerFactory.getLogger(AccountProvisioningController.class);

    // collaborators
    @Autowired private CustomerDao customerDao;
    @Autowired private CustomerAddressDao addressDao;
    @Autowired private PriceModelDao priceModelDao;
    @Autowired private MarketingActionDao marketingActionDao;
    @Autowired private DirectDebitDetailsDao directDebitDetailsDao;
    @Autowired private AccountActivationService accountActivationService;

    // validators
    @Autowired private List<AccountRegistrationValidator> accountRegistrationValidators;

    // formatters
    @Autowired private ConversionService conversionService;

    @RequestMapping(method = RequestMethod.GET)
    public String pendingValidation(Model model) {
        model.addAttribute("customers", customersPendingManualValidation());

        return "provisioning/index";
    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public String validate(Model model, @PathVariable("id") int id) {
        Customer customer = customerById(id);
        CustomerAddress address = addressForCustomer(id);
        PriceModel priceModel = priceModelForCustomer(id, customer.getActionCode());

        AccountProvisioningForm form = form(customer, address, priceModel);

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "form");
        errors.initConversion(conversionService);

        for(Validator validator: accountRegistrationValidators) {
            ValidationUtils.invokeValidator(validator, customer, errors);
        }

        model.addAttribute("form", form);
        model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "form", errors);
        model.addAttribute("customer", customer);

        addPaymentData(model, customer);

        return "provisioning/validate";
    }

    private void addPaymentData(Model model, Customer customer) {
        PaymentMethod payMethod = customer.getPaymentMethodType();

        if(payMethod.equals(PaymentMethod.DIRECT_DEBIT)) {
            directDebitDetailsDao.findForCustomer(customer.getCustomerId()).ifPresent((details) -> {
                model.addAttribute("iban", details.getSepaNumber());
            });
        }

        if(Arrays.asList(PaymentMethod.MASTERCARD, PaymentMethod.VISA).contains(payMethod)) {
            String accountNr = customer.getAccountNr();
            String last4digits = accountNr.length() > 3
                    ? accountNr.substring(accountNr.length() - 4)
                    : "";

            model.addAttribute("ccname", payMethod.name());
            model.addAttribute("ccdigits", last4digits);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "{id}", params = {"validate"})
    public String saveValidated(@ModelAttribute("form") AccountProvisioningForm form,
                                @PathVariable("id") int id,
                                BindingResult bindingResult,
                                ModelMap model) {

        // TODO run some additional validations here ?
        Customer customer = customerById(id);
        CustomerAddress address = addressForCustomer(id);
        PriceModel priceModel = priceModelForCustomer(id, customer.getActionCode());

        updateCustomer(customer, form);
        updateAddress(address, form);

        model.clear();

        // save changes
        customerDao.savePrivateCustomer(customer);
        addressDao.savePrivateCustomerAddress(customer.getCustomerId(), address);

        // and activate customer
        accountActivationService.activateCustomerAccount(customer, priceModel);

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

    private PriceModel priceModelForCustomer(int customerId, String actionCode) {
        Optional<PriceModel> priceModel = priceModelDao.findForCustomer(customerId);
        Optional<MarketingAction> marketingAction = marketingActionDao.findByActionCode(actionCode);

        Supplier<InconsistentDataException> inconsistentDataError = () -> {
            String error = "couldn't find price model for customer id: " + customerId;
            log.error(error);

            return new InconsistentDataException(error);
        };

        Function<PriceModel, PriceModel> applyDiscount = (pm) -> {
            marketingAction.ifPresent((action) -> pm.setRegistratiekosten(action.getRegistrationCost()));
            return pm;
        };

        return priceModel.map(applyDiscount).orElseThrow(inconsistentDataError);
    }

    private AccountProvisioningForm form(Customer customer, CustomerAddress address, PriceModel priceModel) {
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

        form.setSubscriptionFee(priceModel.getSubscriptionCostEuroCents() / 100);
        form.setRegistrationFee(priceModel.getRegistratiekosten() / 100);

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
