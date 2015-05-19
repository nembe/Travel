package nl.yellowbrick.data.errors;

import nl.yellowbrick.data.domain.Customer;

public class ExhaustedCardPoolException extends ActivationException {

    private final long productGroupId;

    public ExhaustedCardPoolException(Customer customer) {
        super(String.format(
                "Unable to assign cards to customer ID %s: Exhausted card pool for product group %s",
                customer.getCustomerId(),
                customer.getProductGroupId()
        ));

        this.productGroupId = customer.getProductGroupId();
    }

    public long getProductGroupId() {
        return productGroupId;
    }
}
