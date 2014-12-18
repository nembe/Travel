package nl.yellowbrick.data.domain;

public class DirectDebitDetails {

    private long id;
    private String sepaNumber;
    private String bic;
    private boolean verified;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

        DirectDebitDetails that = (DirectDebitDetails) o;

        if (id != that.id) return false;
        if (verified != that.verified) return false;
        if (bic != null ? !bic.equals(that.bic) : that.bic != null) return false;
        if (sepaNumber != null ? !sepaNumber.equals(that.sepaNumber) : that.sepaNumber != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (sepaNumber != null ? sepaNumber.hashCode() : 0);
        result = 31 * result + (bic != null ? bic.hashCode() : 0);
        result = 31 * result + (verified ? 1 : 0);
        return result;
    }
}
