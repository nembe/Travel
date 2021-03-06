package nl.yellowbrick.data.domain;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;

import java.util.Date;

public class Customer {

    private String accountHolderName = "";
    private String accountNr = "";
    private String accountType = "";
    private String actionCode = "";
    private String agentName = "";
    private Date applicationDate = null;
    private long billingAgentId = 0;
    private String businessName = "";
    private long businessTypeId = 0;
    private String business = "N";
    private long creditLimit = 0;
    private long customerId = 0;
    private String customerNr = "";
    private Date dateOfBirth = null;
    private String email = "";
    private Date exitDate = null;
    private String fax = "";
    private String firstCardLicensePlate = null;
    private String firstCardMobile = null;
    private String firstName = "";
    private String gender = null;
    private String infix = "";
    private String initials = "";
    private String lastName = "";
    private Date memberDate = null;
    private int numberOfQCards = 0;
    private int numberOfRTPCards = 0;
    private int numberOfTCards = 0;
    private int parkadammerTotal = 0;
    private String phoneNr = "";
    private String pincode = "";
    private String productGroup = "";
    private int productGroupId = -1;
    private CustomerStatus status;
    private String invoiceAttn = "";
    private String invoiceEmail = "";
    private boolean extraInvoiceAnnotations = false;

    public PaymentMethod getPaymentMethod() {
        int billingAgent = Long.valueOf(this.getBillingAgentId()).intValue();

        return PaymentMethod.forCode(billingAgent);
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public String getAccountNr() {
        return accountNr;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getActionCode() {
        return actionCode;
    }

    public String getAgentName() {
        return agentName;
    }

    public Date getApplicationDate() {
        return applicationDate;
    }

    public long getBillingAgentId() {
        return billingAgentId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public long getBusinessTypeId() {
        return businessTypeId;
    }

    public String getBusiness() {
        return business;
    }

    public long getCreditLimit() {
        return creditLimit;
    }

    public long getCustomerId() {
        return customerId;
    }

    public String getCustomerNr() {
        return customerNr;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public Date getExitDate() {
        return exitDate;
    }

    public String getFax() {
        return fax;
    }

    public String getFirstCardLicensePlate() {
        return firstCardLicensePlate;
    }

    public String getFirstCardMobile() {
        return firstCardMobile;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFullName() {
        return isBusinessCustomer()
                ? businessName.trim()
                : Joiner.on(" ")
                    .skipNulls()
                    .join(firstName.trim(), infix.trim(), lastName.trim())
                    .replaceAll("\\s{2,}", " ");
    }

    public String getGender() {
        return gender;
    }

    public String getInfix() {
        return infix;
    }

    public String getInitials() {
        return initials;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getMemberDate() {
        return memberDate;
    }

    public int getNumberOfQCards() {
        return numberOfQCards;
    }

    public int getNumberOfRTPCards() {
        return numberOfRTPCards;
    }

    public int getNumberOfTCards() {
        return numberOfTCards;
    }

    public int getParkadammerTotal() {
        return parkadammerTotal;
    }

    public String getPhoneNr() {
        return phoneNr;
    }

    public String getPincode() {
        return pincode;
    }

    public String getProductGroup() {
        return productGroup;
    }

    public int getProductGroupId() {
        return productGroupId;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public boolean isBusinessCustomer() {
        return business.equals("Y");
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = (accountHolderName != null) ? accountHolderName : "";
    }

    public void setAccountNr(String accountNr) {
        this.accountNr = (accountNr != null) ? accountNr : "";
    }

    public void setAccountType(String accountType) {
        this.accountType = (accountType != null) ? accountType : "";
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
    }

    public void setBillingAgentId(long billingAgentId) {
        this.billingAgentId = billingAgentId;
    }

    public void setBusinessName(String businessName) {
        this.businessName = (businessName != null) ? businessName : "";
    }

    public void setBusinessTypeId(long businessTypeIdfk) {
        this.businessTypeId = businessTypeIdfk;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public void setCreditLimit(long creditLimit) {
        this.creditLimit = creditLimit;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public void setCustomerNr(String customerNr) {
        this.customerNr = (customerNr != null) ? customerNr.trim() : "";
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setEmail(String email) {
        this.email = (email != null) ? email : "";
    }

    public void setExitDate(Date exitDate) {
        this.exitDate = exitDate;
    }

    public void setFax(String fax) {
        this.fax = (fax != null) ? fax : "";
    }

    public void setFirstCardLicensePlate(String firstCardLicensePlate) {
        this.firstCardLicensePlate = firstCardLicensePlate;
    }

    public void setFirstCardMobile(String firstCardMobile) {
        this.firstCardMobile = firstCardMobile;
    }

    public void setFirstName(String firstName) {
        this.firstName = (firstName != null) ? firstName : "";
    }

    public void setGender(String gender) {
        this.gender = (gender != null) ? gender : "U";
    }

    public void setInfix(String infix) {
        this.infix = (infix != null) ? infix : "";
    }

    public void setInitials(String initials) {
        this.initials = (initials != null) ? initials : "";
    }

    public void setLastName(String lastName) {
        this.lastName = (lastName != null) ? lastName : "";
    }

    public void setMemberDate(Date memberDate) {
        this.memberDate = memberDate;
    }

    public void setNumberOfQCards(int numberOfQCards) {
        this.numberOfQCards = numberOfQCards;
    }

    public void setNumberOfRTPCards(int numberOfRTPCards) {
        this.numberOfRTPCards = numberOfRTPCards;
    }

    public void setNumberOfTCards(int numberOfTCards) {
        this.numberOfTCards = numberOfTCards;
    }

    public void setParkadammerTotal(int parkadammerTotal) {
        this.parkadammerTotal = parkadammerTotal;
    }

    public void setPhoneNr(String phoneNr) {
        this.phoneNr = (phoneNr != null) ? phoneNr : "";
    }

    public void setPincode(String pincode) {
        this.pincode = (pincode != null) ? pincode : "";
    }

    public void setProductGroup(String productGroup) {
        this.productGroup = productGroup;
    }

    public void setProductGroupId(int productGroupId) {
        this.productGroupId = productGroupId;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }

    public void setExtraInvoiceAnnotations(boolean extraInvoiceAnnotations) {
        this.extraInvoiceAnnotations = extraInvoiceAnnotations;
    }

    public boolean isExtraInvoiceAnnotations() {
        return extraInvoiceAnnotations;
    }

    public void setInvoiceEmail(String invoiceEmail) {
        this.invoiceEmail = invoiceEmail;
    }

    public String getInvoiceEmail() {
        return invoiceEmail;
    }

    public void setInvoiceAttn(String invoiceAttn) {
        this.invoiceAttn = invoiceAttn;
    }

    public String getInvoiceAttn() {
        return invoiceAttn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Customer customer = (Customer) o;

        if (billingAgentId != customer.billingAgentId) return false;
        if (businessTypeId != customer.businessTypeId) return false;
        if (creditLimit != customer.creditLimit) return false;
        if (customerId != customer.customerId) return false;
        if (extraInvoiceAnnotations != customer.extraInvoiceAnnotations) return false;
        if (numberOfQCards != customer.numberOfQCards) return false;
        if (numberOfRTPCards != customer.numberOfRTPCards) return false;
        if (numberOfTCards != customer.numberOfTCards) return false;
        if (parkadammerTotal != customer.parkadammerTotal) return false;
        if (productGroupId != customer.productGroupId) return false;
        if (accountHolderName != null ? !accountHolderName.equals(customer.accountHolderName) : customer.accountHolderName != null)
            return false;
        if (accountNr != null ? !accountNr.equals(customer.accountNr) : customer.accountNr != null) return false;
        if (accountType != null ? !accountType.equals(customer.accountType) : customer.accountType != null)
            return false;
        if (actionCode != null ? !actionCode.equals(customer.actionCode) : customer.actionCode != null) return false;
        if (agentName != null ? !agentName.equals(customer.agentName) : customer.agentName != null) return false;
        if (applicationDate != null ? !applicationDate.equals(customer.applicationDate) : customer.applicationDate != null)
            return false;
        if (business != null ? !business.equals(customer.business) : customer.business != null) return false;
        if (businessName != null ? !businessName.equals(customer.businessName) : customer.businessName != null)
            return false;
        if (customerNr != null ? !customerNr.equals(customer.customerNr) : customer.customerNr != null) return false;
        if (dateOfBirth != null ? !dateOfBirth.equals(customer.dateOfBirth) : customer.dateOfBirth != null)
            return false;
        if (email != null ? !email.equals(customer.email) : customer.email != null) return false;
        if (exitDate != null ? !exitDate.equals(customer.exitDate) : customer.exitDate != null) return false;
        if (fax != null ? !fax.equals(customer.fax) : customer.fax != null) return false;
        if (firstCardLicensePlate != null ? !firstCardLicensePlate.equals(customer.firstCardLicensePlate) : customer.firstCardLicensePlate != null)
            return false;
        if (firstCardMobile != null ? !firstCardMobile.equals(customer.firstCardMobile) : customer.firstCardMobile != null)
            return false;
        if (firstName != null ? !firstName.equals(customer.firstName) : customer.firstName != null) return false;
        if (gender != null ? !gender.equals(customer.gender) : customer.gender != null) return false;
        if (infix != null ? !infix.equals(customer.infix) : customer.infix != null) return false;
        if (initials != null ? !initials.equals(customer.initials) : customer.initials != null) return false;
        if (invoiceAttn != null ? !invoiceAttn.equals(customer.invoiceAttn) : customer.invoiceAttn != null)
            return false;
        if (invoiceEmail != null ? !invoiceEmail.equals(customer.invoiceEmail) : customer.invoiceEmail != null)
            return false;
        if (lastName != null ? !lastName.equals(customer.lastName) : customer.lastName != null) return false;
        if (memberDate != null ? !memberDate.equals(customer.memberDate) : customer.memberDate != null) return false;
        if (phoneNr != null ? !phoneNr.equals(customer.phoneNr) : customer.phoneNr != null) return false;
        if (pincode != null ? !pincode.equals(customer.pincode) : customer.pincode != null) return false;
        if (productGroup != null ? !productGroup.equals(customer.productGroup) : customer.productGroup != null)
            return false;
        if (status != customer.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = accountHolderName != null ? accountHolderName.hashCode() : 0;
        result = 31 * result + (accountNr != null ? accountNr.hashCode() : 0);
        result = 31 * result + (accountType != null ? accountType.hashCode() : 0);
        result = 31 * result + (actionCode != null ? actionCode.hashCode() : 0);
        result = 31 * result + (agentName != null ? agentName.hashCode() : 0);
        result = 31 * result + (applicationDate != null ? applicationDate.hashCode() : 0);
        result = 31 * result + (int) (billingAgentId ^ (billingAgentId >>> 32));
        result = 31 * result + (businessName != null ? businessName.hashCode() : 0);
        result = 31 * result + (int) (businessTypeId ^ (businessTypeId >>> 32));
        result = 31 * result + (business != null ? business.hashCode() : 0);
        result = 31 * result + (int) (creditLimit ^ (creditLimit >>> 32));
        result = 31 * result + (int) (customerId ^ (customerId >>> 32));
        result = 31 * result + (customerNr != null ? customerNr.hashCode() : 0);
        result = 31 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (exitDate != null ? exitDate.hashCode() : 0);
        result = 31 * result + (fax != null ? fax.hashCode() : 0);
        result = 31 * result + (firstCardLicensePlate != null ? firstCardLicensePlate.hashCode() : 0);
        result = 31 * result + (firstCardMobile != null ? firstCardMobile.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (infix != null ? infix.hashCode() : 0);
        result = 31 * result + (initials != null ? initials.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (memberDate != null ? memberDate.hashCode() : 0);
        result = 31 * result + numberOfQCards;
        result = 31 * result + numberOfRTPCards;
        result = 31 * result + numberOfTCards;
        result = 31 * result + parkadammerTotal;
        result = 31 * result + (phoneNr != null ? phoneNr.hashCode() : 0);
        result = 31 * result + (pincode != null ? pincode.hashCode() : 0);
        result = 31 * result + (productGroup != null ? productGroup.hashCode() : 0);
        result = 31 * result + productGroupId;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (invoiceAttn != null ? invoiceAttn.hashCode() : 0);
        result = 31 * result + (invoiceEmail != null ? invoiceEmail.hashCode() : 0);
        result = 31 * result + (extraInvoiceAnnotations ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accountHolderName", accountHolderName)
                .add("accountNr", accountNr)
                .add("accountType", accountType)
                .add("actionCode", actionCode)
                .add("agentName", agentName)
                .add("applicationDate", applicationDate)
                .add("billingAgentId", billingAgentId)
                .add("businessName", businessName)
                .add("businessTypeId", businessTypeId)
                .add("business", business)
                .add("creditLimit", creditLimit)
                .add("customerId", customerId)
                .add("customerNr", customerNr)
                .add("dateOfBirth", dateOfBirth)
                .add("email", email)
                .add("exitDate", exitDate)
                .add("fax", fax)
                .add("firstCardLicensePlate", firstCardLicensePlate)
                .add("firstCardMobile", firstCardMobile)
                .add("firstName", firstName)
                .add("gender", gender)
                .add("infix", infix)
                .add("initials", initials)
                .add("lastName", lastName)
                .add("memberDate", memberDate)
                .add("numberOfQCards", numberOfQCards)
                .add("numberOfRTPCards", numberOfRTPCards)
                .add("numberOfTCards", numberOfTCards)
                .add("parkadammerTotal", parkadammerTotal)
                .add("phoneNr", phoneNr)
                .add("pincode", pincode)
                .add("productGroup", productGroup)
                .add("productGroupId", productGroupId)
                .add("status", status)
                .add("invoiceAttn", invoiceAttn)
                .add("invoiceEmail", invoiceEmail)
                .add("extraInvoiceAnnotations", extraInvoiceAnnotations)
                .toString();
    }
}
