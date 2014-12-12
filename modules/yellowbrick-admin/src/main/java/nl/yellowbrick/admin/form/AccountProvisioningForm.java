package nl.yellowbrick.admin.form;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class AccountProvisioningForm {

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

    // fees
    private double subscriptionFee;
    private double registrationFee;

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

    public double getSubscriptionFee() {
        return subscriptionFee;
    }

    public void setSubscriptionFee(double subscriptionFee) {
        this.subscriptionFee = subscriptionFee;
    }

    public double getRegistrationFee() {
        return registrationFee;
    }

    public void setRegistrationFee(double registrationFee) {
        this.registrationFee = registrationFee;
    }
}
