package nl.yellowbrick.data.domain;

import com.google.common.base.MoreObjects;

import java.util.Date;

public class CardOrder {

    private long id;
    private Date date;
    private CardOrderStatus status;
    private long customerId;
    private CardType cardType;
    private String briefCode;
    private int amount;
    private double pricePerCard;
    private double surcharge;
    private boolean export;
    private String cardNumber;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public CardOrderStatus getStatus() {
        return status;
    }

    public void setStatus(CardOrderStatus status) {
        this.status = status;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public String getBriefCode() {
        return briefCode;
    }

    public void setBriefCode(String briefCode) {
        this.briefCode = briefCode;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPricePerCard() {
        return pricePerCard;
    }

    public void setPricePerCard(double pricePerCard) {
        this.pricePerCard = pricePerCard;
    }

    public double getSurcharge() {
        return surcharge;
    }

    public void setSurcharge(double surcharge) {
        this.surcharge = surcharge;
    }

    public boolean isExport() {
        return export;
    }

    public void setExport(boolean export) {
        this.export = export;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CardOrder cardOrder = (CardOrder) o;

        if (amount != cardOrder.amount) return false;
        if (customerId != cardOrder.customerId) return false;
        if (export != cardOrder.export) return false;
        if (id != cardOrder.id) return false;
        if (Double.compare(cardOrder.pricePerCard, pricePerCard) != 0) return false;
        if (Double.compare(cardOrder.surcharge, surcharge) != 0) return false;
        if (briefCode != null ? !briefCode.equals(cardOrder.briefCode) : cardOrder.briefCode != null) return false;
        if (cardNumber != null ? !cardNumber.equals(cardOrder.cardNumber) : cardOrder.cardNumber != null) return false;
        if (cardType != cardOrder.cardType) return false;
        if (date != null ? !date.equals(cardOrder.date) : cardOrder.date != null) return false;
        if (status != cardOrder.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (int) (customerId ^ (customerId >>> 32));
        result = 31 * result + (cardType != null ? cardType.hashCode() : 0);
        result = 31 * result + (briefCode != null ? briefCode.hashCode() : 0);
        result = 31 * result + amount;
        temp = Double.doubleToLongBits(pricePerCard);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(surcharge);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (export ? 1 : 0);
        result = 31 * result + (cardNumber != null ? cardNumber.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("date", date)
                .add("status", status)
                .add("customerId", customerId)
                .add("cardType", cardType)
                .add("briefCode", briefCode)
                .add("amount", amount)
                .add("pricePerCard", pricePerCard)
                .add("surcharge", surcharge)
                .add("export", export)
                .add("cardNumber", cardNumber)
                .toString();
    }
}
