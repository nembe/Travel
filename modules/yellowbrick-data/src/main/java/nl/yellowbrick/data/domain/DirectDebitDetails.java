package nl.yellowbrick.data.domain;

public class DirectDebitDetails {

    private long id;
    private long customerId;
    private String sepaNumber;
    private String bic;
    private boolean verified;

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

    public String getSepaNumber() {
        return sepaNumber;
    }

    public void setSepaNumber(String sepaNumber) {
        this.sepaNumber = sepaNumber;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DirectDebitDetails details = (DirectDebitDetails) o;

        if (customerId != details.customerId) return false;
        if (id != details.id) return false;
        if (verified != details.verified) return false;
        if (bic != null ? !bic.equals(details.bic) : details.bic != null) return false;
        if (sepaNumber != null ? !sepaNumber.equals(details.sepaNumber) : details.sepaNumber != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (customerId ^ (customerId >>> 32));
        result = 31 * result + (sepaNumber != null ? sepaNumber.hashCode() : 0);
        result = 31 * result + (bic != null ? bic.hashCode() : 0);
        result = 31 * result + (verified ? 1 : 0);
        return result;
    }
}
