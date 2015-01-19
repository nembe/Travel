package nl.yellowbrick.admin.controller;

import nl.yellowbrick.activation.service.AccountActivationService;
import nl.yellowbrick.activation.validation.AccountRegistrationValidator;
import nl.yellowbrick.admin.exceptions.InconsistentDataException;
import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.admin.form.BusinessAccountProvisioningForm;
import nl.yellowbrick.admin.form.FormData;
import nl.yellowbrick.admin.form.PersonalAccountProvisioningForm;
import nl.yellowbrick.admin.service.RateTranslationService;
import nl.yellowbrick.admin.util.MessageHelper;
import nl.yellowbrick.data.dao.*;
import nl.yellowbrick.data.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/provisioning/accounts")
public class AccountProvisioningController {

    private static final Logger LOG = LoggerFactory.getLogger(AccountProvisioningController.class);
    private static final String FORM_ERRORS = BindingResult.MODEL_KEY_PREFIX + "form";

    // collaborators
    @Autowired private CustomerDao customerDao;
    @Autowired private CustomerAddressDao addressDao;
    @Autowired private PriceModelDao priceModelDao;
    @Autowired private MarketingActionDao marketingActionDao;
    @Autowired private DirectDebitDetailsDao directDebitDetailsDao;
    @Autowired private SubscriptionDao subscriptionDao;
    @Autowired private AccountActivationService accountActivationService;
    @Autowired private RateTranslationService rateTranslationService;

    // validators
    @Autowired private List<AccountRegistrationValidator> accountRegistrationValidators;

    // formatters
    @Autowired private ConversionService conversionService;

    @RequestMapping(method = RequestMethod.GET)
    public String pendingValidation(Model model) {
        model.addAttribute("customers", customersPendingManualValidation());

        return "provisioning/accounts/index";
    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public String validate(ModelMap model, @PathVariable("id") int id, Locale locale) {
        Customer customer = customerById(id);
        CustomerAddress address = addressForCustomer(id);

        FormData form;
        if(model.containsAttribute("form")) {
            form = (FormData) model.get("form");
        } else if(customer.isBusinessCustomer()) {
            Optional<CustomerAddress> billingAddress = billingAddressForCustomer(id);
            List<BusinessIdentifier> businessIdentifiers = customerDao.getBusinessIdentifiers(id);
            form = new BusinessAccountProvisioningForm(customer, address, billingAddress, businessIdentifiers);
        } else {
            form = new PersonalAccountProvisioningForm(customer, address);
        }

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "form");
        errors.initConversion(conversionService);

        if(model.containsAttribute(FORM_ERRORS)) {
            BindingResult previousErrors = (BindingResult) model.get(FORM_ERRORS);
            errors.addAllErrors(previousErrors);
        }

        for(Validator validator: accountRegistrationValidators) {
            ValidationUtils.invokeValidator(validator, customer, errors);
        }

        model.addAttribute("form", form);
        model.addAttribute(FORM_ERRORS, errors);
        model.addAttribute("customer", customer);
        model.addAttribute("specialRateDescription", rateTranslationService.describeRateForCustomer(customer, locale));
        model.addAttribute("priceModel", priceModelForCustomer(id, customer.getActionCode()));

        addPaymentData(model, customer);

        subscriptionDao.findForCustomer(id).ifPresent((subscription) -> {
            model.addAttribute("activeSubscription", subscription.isSubscriptionActive());
        });

        if(customer.isBusinessCustomer())
            return "provisioning/accounts/validate_business";
        return "provisioning/accounts/validate_personal";
    }

    private void addPaymentData(ModelMap model, Customer customer) {
        PaymentMethod payMethod = customer.getPaymentMethodType();

        if(payMethod.equals(PaymentMethod.DIRECT_DEBIT)) {
            directDebitDetailsDao.findForCustomer(customer.getCustomerId()).ifPresent((details) -> {
                model.addAttribute("iban", details.getSepaNumber());
            });
        }

        if(Arrays.asList(PaymentMethod.MASTERCARD, PaymentMethod.VISA).contains(payMethod)) {
            model.addAttribute("ccname", payMethod.name());
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "{id}", params = {"validatePersonalAccount"})
    public String saveValidatedPersonalAccount(
            @PathVariable("id") int id,
            @ModelAttribute("form") PersonalAccountProvisioningForm form,
            BindingResult bindingResult,
            ModelMap model,
            Locale locale,
            RedirectAttributes ra) {

        if(bindingResult.hasErrors())
            return validate(model, id, locale);
        else
            model.clear();

        Customer customer = customerById(id);
        CustomerAddress address = addressForCustomer(id);
        PriceModel priceModel = priceModelForCustomer(id, customer.getActionCode());

        updateCustomer(customer, form);
        updateAddress(address, form);

        model.clear();

        // save changes
        customerDao.savePrivateCustomer(customer);
        addressDao.savePrivateCustomerAddress(id, address);

        // and activate customer
        accountActivationService.activateCustomerAccount(customer, priceModel);

        MessageHelper.flash(ra, "account.validated");
        return "redirect:/provisioning/accounts";
    }

    @RequestMapping(method = RequestMethod.POST, value = "{id}", params = {"validateBusinessAccount"})
    public String saveValidatedBusinessAccount(
            @PathVariable("id") int id,
            @ModelAttribute("form") BusinessAccountProvisioningForm form,
            BindingResult bindingResult,
            ModelMap model,
            Locale locale,
            RedirectAttributes ra) {

        if(bindingResult.hasErrors())
            return validate(model, id, locale);
        else
            model.clear();

        Customer customer = customerById(id);
        CustomerAddress businessAddress = addressForCustomer(id);
        Optional<CustomerAddress> maybeBillingAddress = billingAddressForCustomer(id);

        PriceModel priceModel = priceModelForCustomer(id, customer.getActionCode());

        updateBusinessCustomer(customer, form);
        updateAddress(businessAddress, form);

        // save changes
        customerDao.saveBusinessCustomer(customer);
        addressDao.saveBusinessCustomerAddress(id, businessAddress, AddressType.MAIN);

        // save changes to or remove billing address
        if(form.isBillingAddressSameAsMailingAddress()) {
            maybeBillingAddress.ifPresent(addressDao::deleteAddress);
        } else {
            CustomerAddress billingAddress = maybeBillingAddress.orElse(new CustomerAddress());
            updateBillingAddress(billingAddress, form);
            addressDao.saveBusinessCustomerAddress(id, billingAddress, AddressType.BILLING);
        }

        // update business identifiers
        form.getBusinessIdentifiers().forEach(customerDao::updateBusinessIdentifier);

        // and activate customer
        accountActivationService.activateCustomerAccount(customer, priceModel);

        MessageHelper.flash(ra, "account.validated");
        return "redirect:/provisioning/accounts";
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
        Optional<CustomerAddress> address = addressDao.findByCustomerId(customerId, AddressType.MAIN);

        if(address.isPresent())
            return address.get();

        String error = String.format("couldn't find address for customer id: %s", customerId);

        LOG.error(error);
        throw new InconsistentDataException(error);
    }

    private Optional<CustomerAddress> billingAddressForCustomer(int customerId) {
        return addressDao.findByCustomerId(customerId, AddressType.BILLING);
    }

    private PriceModel priceModelForCustomer(int customerId, String actionCode) {
        Optional<PriceModel> priceModel = priceModelDao.findForCustomer(customerId);
        Optional<MarketingAction> marketingAction = marketingActionDao.findByActionCode(actionCode);

        Supplier<InconsistentDataException> inconsistentDataError = () -> {
            String error = "couldn't find price model for customer id: " + customerId;
            LOG.error(error);

            return new InconsistentDataException(error);
        };

        Function<PriceModel, PriceModel> applyDiscount = (pm) -> {
            marketingAction.ifPresent((action) -> pm.setRegistratiekosten(action.getRegistrationCost()));
            return pm;
        };

        return priceModel.map(applyDiscount).orElseThrow(inconsistentDataError);
    }

    private Customer updateCustomer(Customer customer, PersonalAccountProvisioningForm form) {
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

    private Customer updateBusinessCustomer(Customer customer, BusinessAccountProvisioningForm form) {
        customer.setBusinessName(form.getBusinessName());

        return updateCustomer(customer, form);
    }

    private void updateAddress(CustomerAddress address, PersonalAccountProvisioningForm form) {
        address.setAddress(form.getStreet());
        address.setHouseNr(form.getHouseNr());
        address.setSupplement(form.getSupplement());
        address.setZipCode(form.getPostalCode());
        address.setCity(form.getCity());
        address.setCountryCode(form.getCountry());
    }

    private void updateBillingAddress(CustomerAddress address, BusinessAccountProvisioningForm form) {
        if(form.isBillingAddressIsPoBox())
            address.setPoBox(form.getBillingAddressPoBox());

        address.setAddress(form.getBillingAddressStreet());
        address.setHouseNr(form.getBillingAddressHouseNr());
        address.setSupplement(form.getBillingAddressSupplement());
        address.setZipCode(form.getBillingAddressPostalCode());
        address.setCity(form.getBillingAddressCity());
        address.setCountryCode(form.getBillingAddressCountry());
    }
}
