package nl.yellowbrick.domain;

import org.apache.log4j.Logger;

import java.util.Date;

public class CardOrder {

    private static Logger logger = Logger.getLogger(CardOrder.class);

    // Brief Code constants
    public final static int BRIEF_CODE_NEW_CUST      = 1;
    public final static int BRIEF_CODE_EXISTING_CUST = 2;

    // Order status constants
    public final static int STATUS_INSERTED          = 1;
    public final static int STATUS_ACCEPTED          = 2;
    public final static int STATUS_EXPORTED          = 3;

    // Card types
    public final static int UNKNOWN_CARD             = 0;
    public final static int TRANSPONDER_CARD         = 1;
    public final static int RTP_CARD                 = 2;
    public final static int QPARK_CARD               = 3;
    public final static int SLEEVE                   = 4;

    private int             amount;
    private String          cardType                 = "";
    private long            customerId;
    private String          customerName             = "";
    private long            customerNr;
    private String          customerType             = "";
    private String          letterCode               = "";
    private Date orderDate;
    private String          orderId                  = "";
    private String          orderStatus              = "";
    private double          pricePerCard;
    private String          productGroup;

    /**
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * @return the cardType
     */
    public String getCardType() {
        return cardType;
    }

    /**
     * @return the cardTypeCode
     */
    public String getCardTypeCode() {
        logger.info("cardtype in CardOrder=" + cardType + "qcard".toLowerCase().equals(cardType) + "LITERAL=" + QPARK_CARD);
        if (cardType.toLowerCase().startsWith("transponder"))
            return TRANSPONDER_CARD + "";
        else if (cardType.toLowerCase().startsWith("rtp"))
            return RTP_CARD + "";
        else if ("qcard".toLowerCase().equalsIgnoreCase(cardType))
            return QPARK_CARD + "";
        else if ("hoesje".toLowerCase().equalsIgnoreCase(cardType))
            return SLEEVE + "";
        else
            return UNKNOWN_CARD + "";
    }

    /**
     * @return the customerId
     */
    public long getCustomerId() {
        return customerId;
    }

    /**
     * @return the customerName
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * @return the customerNr
     */
    public long getCustomerNr() {
        return customerNr;
    }

    /**
     * @return the customerType
     */
    public String getCustomerType() {
        return customerType;
    }

    /**
     * @return the letterCode
     */
    public String getLetterCode() {
        return letterCode;
    }

    /**
     * @return the orderDate
     */
    public Date getOrderDate() {
        return orderDate;
    }

    /**
     * @return the orderId
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * @return the orderStatus
     */
    public String getOrderStatus() {
        return orderStatus;
    }

    /**
     * @return the pricePerCard
     */
    public double getPricePerCard() {
        return pricePerCard;
    }

    public String getProductGroup() {
        return productGroup;
    }

    /**
     * @param amount
     *            the amount to set
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * @param cardType
     *            the cardType to set
     */
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    /**
     * @param customerId
     *            the customerId to set
     */
    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    /**
     * @param customerId
     *            the customerId to set
     */
    public void setCustomerId(Long customerId) {
        this.customerId = customerId.longValue();
    }

    /**
     * @param customerName
     *            the customerName to set
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * @param customerNr
     *            the customerNr to set
     */
    public void setCustomerNr(long customerNr) {
        this.customerNr = customerNr;
    }

    /**
     * @param customerType
     *            the customerType to set
     */
    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    /**
     * @param letterCode
     *            the letterCode to set
     */
    public void setLetterCode(String letterCode) {
        this.letterCode = letterCode;
    }

    /**
     * @param orderDate
     *            the orderDate to set
     */
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    /**
     * @param orderId
     *            the orderId to set
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * @param orderStatus
     *            the orderStatus to set
     */
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    /**
     * @param pricePerCard
     *            the pricePerCard to set
     */
    public void setPricePerCard(double pricePerCard) {
        this.pricePerCard = pricePerCard;
    }

    /**
     * @param pricePerCard
     *            the pricePerCard to set
     */
    public void setPricePerCard(Double pricePerCard) {
        this.pricePerCard = pricePerCard.doubleValue();
    }

    public void setProductGroup(String productGroup) {
        this.productGroup = productGroup;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("orderId = " + getOrderId() + "\n");
        sb.append("cardType = " + getCardType() + "\n");
        sb.append("letterCode = " + getLetterCode() + "\n");
        sb.append("orderStatus = " + getOrderStatus() + "\n");
        sb.append("customerId = " + getCustomerId() + "\n");
        sb.append("amount = " + getAmount() + "\n");
        sb.append("orderDate = " + getOrderDate() + "\n");
        sb.append("pricePerCard = " + getPricePerCard() + "\n");
        return sb.toString();
    }

}