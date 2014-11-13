package nl.yellowbrick.domain;

import java.util.Date;

public class PriceModel {

    private long id;
    private String description = "";
    private int subscriptionCostEuroCents;
    private int transactionCostMaximumEuroCents;
    private int transactionCostMinimumEuroCents;
    private int transactionCostPercentage;
    private int kortingenGeldigheidsduur;
    private int registratiekosten;
    private int sleevePrice;
    private int maxAmountCards;
    private int initRtpCardCost;
    private int rtpCardCost;
    private int initTranspCardCost;
    private int transpCardCost;
    private int qparkPassCost;
    private Date applyDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSubscriptionCostEuroCents() {
        return subscriptionCostEuroCents;
    }

    public void setSubscriptionCostEuroCents(int subscriptionCostEuroCents) {
        this.subscriptionCostEuroCents = subscriptionCostEuroCents;
    }

    public int getTransactionCostMaximumEuroCents() {
        return transactionCostMaximumEuroCents;
    }

    public void setTransactionCostMaximumEuroCents(int transactionCostMaximumEuroCents) {
        this.transactionCostMaximumEuroCents = transactionCostMaximumEuroCents;
    }

    public int getTransactionCostMinimumEuroCents() {
        return transactionCostMinimumEuroCents;
    }

    public void setTransactionCostMinimumEuroCents(int transactionCostMinimumEuroCents) {
        this.transactionCostMinimumEuroCents = transactionCostMinimumEuroCents;
    }

    public int getTransactionCostPercentage() {
        return transactionCostPercentage;
    }

    public void setTransactionCostPercentage(int transactionCostPercentage) {
        this.transactionCostPercentage = transactionCostPercentage;
    }

    public int getKortingenGeldigheidsduur() {
        return kortingenGeldigheidsduur;
    }

    public void setKortingenGeldigheidsduur(int kortingenGeldigheidsduur) {
        this.kortingenGeldigheidsduur = kortingenGeldigheidsduur;
    }

    public int getRegistratiekosten() {
        return registratiekosten;
    }

    public void setRegistratiekosten(int registratiekosten) {
        this.registratiekosten = registratiekosten;
    }

    public int getSleevePrice() {
        return sleevePrice;
    }

    public void setSleevePrice(int sleevePrice) {
        this.sleevePrice = sleevePrice;
    }

    public int getMaxAmountCards() {
        return maxAmountCards;
    }

    public void setMaxAmountCards(int maxAmountCards) {
        this.maxAmountCards = maxAmountCards;
    }

    public int getInitRtpCardCost() {
        return initRtpCardCost;
    }

    public void setInitRtpCardCost(int initRtpCardCost) {
        this.initRtpCardCost = initRtpCardCost;
    }

    public int getRtpCardCost() {
        return rtpCardCost;
    }

    public void setRtpCardCost(int rtpCardCost) {
        this.rtpCardCost = rtpCardCost;
    }

    public int getInitTranspCardCost() {
        return initTranspCardCost;
    }

    public void setInitTranspCardCost(int initTranspCardCost) {
        this.initTranspCardCost = initTranspCardCost;
    }

    public int getTranspCardCost() {
        return transpCardCost;
    }

    public void setTranspCardCost(int transpCardCost) {
        this.transpCardCost = transpCardCost;
    }

    public int getQparkPassCost() {
        return qparkPassCost;
    }

    public void setQparkPassCost(int qparkPassCost) {
        this.qparkPassCost = qparkPassCost;
    }

    public Date getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(Date applyDate) {
        this.applyDate = applyDate;
    }
}
