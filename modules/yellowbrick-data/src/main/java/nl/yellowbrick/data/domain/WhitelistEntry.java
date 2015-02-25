package nl.yellowbrick.data.domain;

public class WhitelistEntry {

    private String travelcardNumber;
    private String licensePlate;
    private long transponderCardId;
    private boolean obsolete;

    public WhitelistEntry(String travelcardNumber, String licensePlate) {
        this.travelcardNumber = travelcardNumber;
        this.licensePlate = licensePlate;
    }

    public WhitelistEntry(String travelcardNumber, String licensePlate, long transponderCardId) {
        this(travelcardNumber, licensePlate);
        this.transponderCardId = transponderCardId;
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

    public long getTransponderCardId() {
        return transponderCardId;
    }

    public void setTransponderCardId(long transponderCardId) {
        this.transponderCardId = transponderCardId;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WhitelistEntry that = (WhitelistEntry) o;

        if (obsolete != that.obsolete) return false;
        if (transponderCardId != that.transponderCardId) return false;
        if (licensePlate != null ? !licensePlate.equals(that.licensePlate) : that.licensePlate != null) return false;
        if (travelcardNumber != null ? !travelcardNumber.equals(that.travelcardNumber) : that.travelcardNumber != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = travelcardNumber != null ? travelcardNumber.hashCode() : 0;
        result = 31 * result + (licensePlate != null ? licensePlate.hashCode() : 0);
        result = 31 * result + (int) (transponderCardId ^ (transponderCardId >>> 32));
        result = 31 * result + (obsolete ? 1 : 0);
        return result;
    }
}
