package nl.yellowbrick.admin.form;

import com.google.common.base.Strings;
import nl.yellowbrick.data.domain.BusinessIdentifier;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerAddress;
import nl.yellowbrick.data.domain.PriceModel;

import java.util.List;
import java.util.Optional;

public class BusinessAccountProvisioningForm extends PersonalAccountProvisioningForm {

    private String businessName;
    private List<BusinessIdentifier> businessIdentifiers;
    private boolean billingAddressSameAsMailingAddress;
    private boolean billingAddressIsPoBox;
    private String billingAddressPoBox;
    private String billingAddressStreet;
    private String billingAddressHouseNr;
    private String billingAddressSupplement;
    private String billingAddressPostalCode;
    private String billingAddressCity;
    private String billingAddressCountry;

    // 0-arg constructor for javabeans compliance
    public BusinessAccountProvisioningForm() {
    }

    public BusinessAccountProvisioningForm(Customer customer,
                                           CustomerAddress address,
                                           PriceModel priceModel,
                                           Optional<CustomerAddress> billingAddress,
                                           List<BusinessIdentifier> businessIdentifiers) {
        super(customer, address, priceModel);
        setBusinessName(customer.getBusinessName());
        setBusinessIdentifiers(businessIdentifiers);

        billingAddress.ifPresent((ba) -> {
            setBillingAddressIsPoBox(!Strings.isNullOrEmpty(ba.getPoBox()));
            setBillingAddressPoBox(ba.getPoBox());
            setBillingAddressStreet(ba.getAddress());
            setBillingAddressHouseNr(ba.getHouseNr());
            setBillingAddressSupplement(ba.getSupplement());
            setBillingAddressPostalCode(ba.getZipCode());
            setBillingAddressCity(ba.getCity());
            setBillingAddressCountry(ba.getCountryCode());
        });

        setBillingAddressSameAsMailingAddress(!billingAddress.isPresent());
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public List<BusinessIdentifier> getBusinessIdentifiers() {
        return businessIdentifiers;
    }

    public void setBusinessIdentifiers(List<BusinessIdentifier> businessIdentifiers) {
        this.businessIdentifiers = businessIdentifiers;
    }

    public boolean isBillingAddressSameAsMailingAddress() {
        return billingAddressSameAsMailingAddress;
    }

    public void setBillingAddressSameAsMailingAddress(boolean billingAddressSameAsMailingAddress) {
        this.billingAddressSameAsMailingAddress = billingAddressSameAsMailingAddress;
    }

    public boolean isBillingAddressIsPoBox() {
        return billingAddressIsPoBox;
    }

    public void setBillingAddressIsPoBox(boolean billingAddressIsPoBox) {
        this.billingAddressIsPoBox = billingAddressIsPoBox;
    }

    public String getBillingAddressPoBox() {
        return billingAddressPoBox;
    }

    public void setBillingAddressPoBox(String billingAddressPoBox) {
        this.billingAddressPoBox = billingAddressPoBox;
    }

    public String getBillingAddressStreet() {
        return billingAddressStreet;
    }

    public void setBillingAddressStreet(String billingAddressStreet) {
        this.billingAddressStreet = billingAddressStreet;
    }

    public String getBillingAddressHouseNr() {
        return billingAddressHouseNr;
    }

    public void setBillingAddressHouseNr(String billingAddressHouseNr) {
        this.billingAddressHouseNr = billingAddressHouseNr;
    }

    public String getBillingAddressSupplement() {
        return billingAddressSupplement;
    }

    public void setBillingAddressSupplement(String billingAddressSupplement) {
        this.billingAddressSupplement = billingAddressSupplement;
    }

    public String getBillingAddressPostalCode() {
        return billingAddressPostalCode;
    }

    public void setBillingAddressPostalCode(String billingAddressPostalCode) {
        this.billingAddressPostalCode = billingAddressPostalCode;
    }

    public String getBillingAddressCity() {
        return billingAddressCity;
    }

    public void setBillingAddressCity(String billingAddressCity) {
        this.billingAddressCity = billingAddressCity;
    }

    public String getBillingAddressCountry() {
        return billingAddressCountry;
    }

    public void setBillingAddressCountry(String billingAddressCountry) {
        this.billingAddressCountry = billingAddressCountry;
    }
}
