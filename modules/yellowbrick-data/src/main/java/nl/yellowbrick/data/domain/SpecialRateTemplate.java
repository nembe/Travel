package nl.yellowbrick.data.domain;

public class SpecialRateTemplate {

    public enum TRANSACTION_TYPE {
        STREET_PARKING(1), SUBSCRIPTION(5);

        private int code;

        TRANSACTION_TYPE(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

    private long id;
    private long productGroupId;
    private long balanceTotal;
    private long specialRateNumber;
    private String specialRateBase;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProductGroupId() {
        return productGroupId;
    }

    public void setProductGroupId(long productGroupId) {
        this.productGroupId = productGroupId;
    }

    public long getBalanceTotal() {
        return balanceTotal;
    }

    public void setBalanceTotal(long balanceTotal) {
        this.balanceTotal = balanceTotal;
    }

    public long getSpecialRateNumber() {
        return specialRateNumber;
    }

    public void setSpecialRateNumber(long specialRateNumber) {
        this.specialRateNumber = specialRateNumber;
    }

    public String getSpecialRateBase() {
        return specialRateBase;
    }

    public void setSpecialRateBase(String specialRateBase) {
        this.specialRateBase = specialRateBase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpecialRateTemplate that = (SpecialRateTemplate) o;

        if (balanceTotal != that.balanceTotal) return false;
        if (id != that.id) return false;
        if (productGroupId != that.productGroupId) return false;
        if (specialRateNumber != that.specialRateNumber) return false;
        if (specialRateBase != null ? !specialRateBase.equals(that.specialRateBase) : that.specialRateBase != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (productGroupId ^ (productGroupId >>> 32));
        result = 31 * result + (int) (balanceTotal ^ (balanceTotal >>> 32));
        result = 31 * result + (int) (specialRateNumber ^ (specialRateNumber >>> 32));
        result = 31 * result + (specialRateBase != null ? specialRateBase.hashCode() : 0);
        return result;
    }
}
