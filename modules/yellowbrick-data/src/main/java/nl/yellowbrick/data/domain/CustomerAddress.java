package nl.yellowbrick.data.domain;

public class CustomerAddress {

    private String address;
    private long customerAddressId;
    private String city;
    private String countryCode;
    private String extraInfo;
    private String houseNr;
    private String supplement;
    private String zipCode;
    private String poBox;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getCustomerAddressId() {
        return customerAddressId;
    }

    public void setCustomerAddressId(long customerAddressId) {
        this.customerAddressId = customerAddressId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
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

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getPoBox() {
        return poBox;
    }

    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomerAddress that = (CustomerAddress) o;

        if (customerAddressId != that.customerAddressId) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (city != null ? !city.equals(that.city) : that.city != null) return false;
        if (countryCode != null ? !countryCode.equals(that.countryCode) : that.countryCode != null) return false;
        if (extraInfo != null ? !extraInfo.equals(that.extraInfo) : that.extraInfo != null) return false;
        if (houseNr != null ? !houseNr.equals(that.houseNr) : that.houseNr != null) return false;
        if (poBox != null ? !poBox.equals(that.poBox) : that.poBox != null) return false;
        if (supplement != null ? !supplement.equals(that.supplement) : that.supplement != null) return false;
        if (zipCode != null ? !zipCode.equals(that.zipCode) : that.zipCode != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (int) (customerAddressId ^ (customerAddressId >>> 32));
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
        result = 31 * result + (extraInfo != null ? extraInfo.hashCode() : 0);
        result = 31 * result + (houseNr != null ? houseNr.hashCode() : 0);
        result = 31 * result + (supplement != null ? supplement.hashCode() : 0);
        result = 31 * result + (zipCode != null ? zipCode.hashCode() : 0);
        result = 31 * result + (poBox != null ? poBox.hashCode() : 0);
        return result;
    }
}
