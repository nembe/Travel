package nl.yellowbrick.admin.form;

import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerAddress;
import nl.yellowbrick.data.domain.PaymentMethod;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class PersonalAccountProvisioningForm {

    // personal details
    private String gender;
    private String initials;
    private String firstName;
    private String infix;
    private String lastName;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date dateOfBirth;

    // contact details
    private String email;
    private String phoneNr;

    // address details
    private String street;
    private String houseNr;
    private String supplement;
    private String postalCode;
    private String city;
    private String country;

    // singnup details
    private int numberOfTransponderCards;
    private int numberOfPPlusCards;

    // Payment details
    private String iban;
    private String ccname;
    private PaymentMethod paymentMethod;

    // 0-arg constructor for javabeans compliance
    public PersonalAccountProvisioningForm() {
    }

    public PersonalAccountProvisioningForm(Customer customer, CustomerAddress address) {
        setGender(customer.getGender());
        setInitials(customer.getInitials());
        setFirstName(customer.getFirstName());
        setInfix(customer.getInfix());
        setLastName(customer.getLastName());
        setDateOfBirth(customer.getDateOfBirth());
        setEmail(customer.getEmail());
        setPhoneNr(customer.getPhoneNr());

        setStreet(address.getAddress());
        setHouseNr(address.getHouseNr());
        setSupplement(address.getSupplement());
        setPostalCode(address.getZipCode());
        setCity(address.getCity());
        setCountry(address.getCountryCode());

        setNumberOfTransponderCards(customer.getNumberOfTCards());
        setNumberOfPPlusCards(customer.getNumberOfQCards());

        setPaymentMethod(customer.getPaymentMethod());
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getInfix() {
        return infix;
    }

    public void setInfix(String infix) {
        this.infix = infix;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNr() {
        return phoneNr;
    }

    public void setPhoneNr(String phoneNr) {
        this.phoneNr = phoneNr;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNr() {
        return houseNr;
    }

    public void setHouseNr(String houseNr) {
        this.houseNr = houseNr;
    }

    public String getSupplement() {
        return supplement;
    }

    public void setSupplement(String supplement) {
        this.supplement = supplement;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getNumberOfTransponderCards() {
        return numberOfTransponderCards;
    }

    public void setNumberOfTransponderCards(int numberOfTransponderCards) {
        this.numberOfTransponderCards = numberOfTransponderCards;
    }

    public int getNumberOfPPlusCards() {
        return numberOfPPlusCards;
    }

    public void setNumberOfPPlusCards(int numberOfPPlusCards) {
        this.numberOfPPlusCards = numberOfPPlusCards;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getCcname() {
        return ccname;
    }

    public void setCcname(String ccname) {
        this.ccname = ccname;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
