package nl.yellowbrick.data.domain;

import java.util.Date;

public class MarketingAction {

    private String actionCode;
    private int registrationCost;
    private Date validFrom;
    private Date validTo;

    public MarketingAction(String actionCode, int registrationCost, Date validFrom, Date validTo) {
        this.actionCode = actionCode;
        this.registrationCost = registrationCost;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public boolean isCurrentlyValid() {
        Date now = new Date();

        return now.after(validFrom) && now.before(validTo);
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public int getRegistrationCost() {
        return registrationCost;
    }

    public void setRegistrationCost(int registrationCost) {
        this.registrationCost = registrationCost;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }
}
