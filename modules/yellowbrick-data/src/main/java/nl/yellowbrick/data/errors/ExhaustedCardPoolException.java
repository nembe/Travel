package nl.yellowbrick.data.errors;

import nl.yellowbrick.data.domain.Customer;

public class ExhaustedCardPoolException extends ActivationException {

    public ExhaustedCardPoolException(Customer customer) {
        super(String.format(
                "Unable to assign cards to customer ID %s: Exhausted card pool for product group %s",
                customer.getCustomerId(),
                customer.getProductGroupId()
        ));
    }
}
