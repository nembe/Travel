package nl.yellowbrick.data.domain;

public class WhitelistEntry {

    private String travelcardNumber;
    private String licensePlate;

    public WhitelistEntry(String travelcardNumber, String licensePlate) {
        this.travelcardNumber = travelcardNumber;
        this.licensePlate = licensePlate;
    }

    public String getTravelcardNumber() {
        return travelcardNumber;
    }

    public void setTravelcardNumber(String travelcardNumber) {
        this.travelcardNumber = travelcardNumber;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WhitelistEntry that = (WhitelistEntry) o;

        if (licensePlate != null ? !licensePlate.equals(that.licensePlate) : that.licensePlate != null) return false;
        if (travelcardNumber != null ? !travelcardNumber.equals(that.travelcardNumber) : that.travelcardNumber != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = travelcardNumber != null ? travelcardNumber.hashCode() : 0;
        result = 31 * result + (licensePlate != null ? licensePlate.hashCode() : 0);
        return result;
    }
}
