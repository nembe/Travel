package nl.yellowbrick.data.domain;

import java.util.Date;

public class TransponderCard {

    private long id;
    private long customerId;
    private String cardNumber;
    private CardStatus status;
    private String licenseplate;
    private String country;
    private String mutator;
    private Date mutationDate;
    private long orderId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public String getLicenseplate() {
        return licenseplate;
    }

    public void setLicenseplate(String licenseplate) {
        this.licenseplate = licenseplate;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getMutator() {
        return mutator;
    }

    public void setMutator(String mutator) {
        this.mutator = mutator;
    }

    public Date getMutationDate() {
        return mutationDate;
    }

    public void setMutationDate(Date mutationDate) {
        this.mutationDate = mutationDate;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransponderCard that = (TransponderCard) o;

        if (customerId != that.customerId) return false;
        if (id != that.id) return false;
        if (orderId != that.orderId) return false;
        if (cardNumber != null ? !cardNumber.equals(that.cardNumber) : that.cardNumber != null) return false;
        if (country != null ? !country.equals(that.country) : that.country != null) return false;
        if (licenseplate != null ? !licenseplate.equals(that.licenseplate) : that.licenseplate != null) return false;
        if (mutationDate != null ? !mutationDate.equals(that.mutationDate) : that.mutationDate != null) return false;
        if (mutator != null ? !mutator.equals(that.mutator) : that.mutator != null) return false;
        if (status != that.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (customerId ^ (customerId >>> 32));
        result = 31 * result + (cardNumber != null ? cardNumber.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (licenseplate != null ? licenseplate.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (mutator != null ? mutator.hashCode() : 0);
        result = 31 * result + (mutationDate != null ? mutationDate.hashCode() : 0);
        result = 31 * result + (int) (orderId ^ (orderId >>> 32));
        return result;
    }
}
