package nl.yellowbrick.domain;

public class Membership {

    private final Customer customer;
    private final PriceModel priceModel;

    public Membership(Customer customer, PriceModel priceModel) {
        this.customer = customer;
        this.priceModel = priceModel;
    }
}
