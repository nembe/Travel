package nl.yellowbrick.domain;

public class Membership {

    private final Customer customer;
    private final PriceModel priceModel;

    public Membership(Customer customer, PriceModel priceModel) {
        this.customer = customer;
        this.priceModel = priceModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Membership that = (Membership) o;

        if (!customer.equals(that.customer)) return false;
        if (!priceModel.equals(that.priceModel)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = customer.hashCode();
        result = 31 * result + priceModel.hashCode();
        return result;
    }
}
