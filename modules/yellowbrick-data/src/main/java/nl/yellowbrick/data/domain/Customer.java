package nl.yellowbrick.data.domain;

import com.google.common.base.Joiner;

import java.util.Date;

public class Customer {

    private String accountCity = "";
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
    private String paymentMethod = "";
    private String phoneNr = "";
    private String pincode = "";
    private String productGroup = "";
    private int productGroupId = -1;
    private CustomerStatus status;
    private String invoiceAttn = "";
    private String invoiceEmail = "";
    private boolean extraInvoiceAnnotations = false;

    public PaymentMethod getPaymentMethodType() {
        int billingAgent = Long.valueOf(this.getBillingAgentId()).intValue();

        return PaymentMethod.forCode(billingAgent);
    }

    public String getAccountCity() {
        return accountCity;
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
                : Joiner.on(" ").skipNulls().join(firstName.trim(), infix.trim(), lastName.trim());
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

    public String getPaymentMethod() {
        return paymentMethod;
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

    public void setAccountCity(String accountCity) {
        this.accountCity = (accountCity != null) ? accountCity : "";
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

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = (paymentMethod != null) ? paymentMethod : "";
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
}
