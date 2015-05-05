package nl.yellowbrick.admin.controller;

import nl.yellowbrick.activation.service.AccountActivationService;
import nl.yellowbrick.activation.service.AccountValidationService;
import nl.yellowbrick.admin.exceptions.InconsistentDataException;
import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.admin.form.BusinessAccountProvisioningForm;
import nl.yellowbrick.admin.form.PersonalAccountProvisioningForm;
import nl.yellowbrick.admin.service.RateTranslationService;
import nl.yellowbrick.admin.util.MessageHelper;
import nl.yellowbrick.admin.validation.BusinessAccountProvisioningFormValidator;
import nl.yellowbrick.admin.validation.PersonalAccountProvisioningFormValidator;
import nl.yellowbrick.admin.validation.ValidatorChain;
import nl.yellowbrick.data.dao.*;
import nl.yellowbrick.data.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Controller
@RequestMapping("/provisioning/accounts/{id}")
public class AccountProvisioningFormController {

    private static final Logger LOG = LoggerFactory.getLogger(AccountProvisioningListController.class);
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
    @Autowired private AccountValidationService accountValidationService;

    // formatters
    @Autowired private ConversionService conversionService;

    @InitBinder("form")
    private void setValidators(WebDataBinder binder) {
        binder.addValidators(ValidatorChain.of(
                new PersonalAccountProvisioningFormValidator(), new BusinessAccountProvisioningFormValidator()
        ));
    }

    @RequestMapping(method = RequestMethod.GET)
    public String validate(ModelMap model, @PathVariable("id") int id, Locale locale) {
        Customer customer = customerById(id);
        CustomerAddress address = addressForCustomer(id);

        PersonalAccountProvisioningForm form;
        if(model.containsAttribute("form")) {
            form = (PersonalAccountProvisioningForm) model.get("form");
        } else if(customer.isBusinessCustomer()) {
            Optional<CustomerAddress> billingAddress = billingAddressForCustomer(id);
            List<BusinessIdentifier> businessIdentifiers = customerDao.getBusinessIdentifiers(id);
            form = new BusinessAccountProvisioningForm(customer, address, billingAddress, businessIdentifiers);
        } else {
            form = new PersonalAccountProvisioningForm(customer, address);
        }

        addPaymentData(form, customer);

        // form validation & binding errors
        BeanPropertyBindingResult formErrors = new BeanPropertyBindingResult(form, "form");
        formErrors.initConversion(conversionService);

        if(model.containsAttribute(FORM_ERRORS)) {
            BindingResult previousErrors = (BindingResult) model.get(FORM_ERRORS);
            formErrors.addAllErrors(previousErrors);
        }

        // account validation errors
        Errors activationErrors = accountValidationService.validate(customer, "form");
        formErrors.addAllErrors(activationErrors);

        model.addAttribute("form", form);
        model.addAttribute(FORM_ERRORS, formErrors);
        model.addAttribute("customer", customer);
        model.addAttribute("specialRateDescription", rateTranslationService.describeRateForCustomer(customer, locale));
        model.addAttribute("priceModel", priceModelForCustomer(id, customer.getActionCode()));

        subscriptionDao.findForCustomer(id).ifPresent((subscription) -> {
            model.addAttribute("activeSubscription", subscription.isSubscriptionActive());
        });

        if(customer.isBusinessCustomer())
            return "provisioning/accounts/validate_business";
        return "provisioning/accounts/validate_personal";
    }

    private void addPaymentData(PersonalAccountProvisioningForm form, Customer customer) {
        PaymentMethod payMethod = customer.getPaymentMethodType();

        if(payMethod.equals(PaymentMethod.DIRECT_DEBIT)) {
            directDebitDetailsDao.findForCustomer(customer.getCustomerId()).ifPresent((details) -> {
                form.setIban(details.getSepaNumber());
            });
        }

        if(Arrays.asList(PaymentMethod.MASTERCARD, PaymentMethod.VISA).contains(payMethod)) {
            form.setCcname(payMethod.name());
        }
    }

    @RequestMapping(method = RequestMethod.POST, params = {"validatePersonalAccount"})
    public String saveValidatedPersonalAccount(
            @PathVariable("id") int id,
            @ModelAttribute("form") @Valid PersonalAccountProvisioningForm form,
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
        return activateAccountAndRedirect(customer, priceModel, ra);
    }

    @RequestMapping(method = RequestMethod.POST, params = {"validateBusinessAccount"})
    public String saveValidatedBusinessAccount(
            @PathVariable("id") int id,
            @ModelAttribute("form") @Valid BusinessAccountProvisioningForm form,
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
        return activateAccountAndRedirect(customer, priceModel, ra);
    }

    private String activateAccountAndRedirect(Customer customer, PriceModel priceModel, RedirectAttributes ra) {
        try {
            accountActivationService.activateCustomerAccount(customer, priceModel);
            MessageHelper.flash(ra, "account.validated");
        } catch(Exception e) {
            LOG.error("error activating customer id " + customer.getCustomerId(), e);
            MessageHelper.flashWarning(ra, "account.unknownValidationError", e.getMessage());
        }
        return "redirect:/provisioning/accounts";
    }

    private Customer customerById(int customerId) {
        return customerDao.findAllPendingActivation()
                .stream()
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
