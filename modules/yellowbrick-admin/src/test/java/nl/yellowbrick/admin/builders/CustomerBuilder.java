package nl.yellowbrick.admin.builders;

import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerStatus;
import nl.yellowbrick.data.domain.ProductGroup;

public class CustomerBuilder {

    private final Customer customer = new Customer();

    public CustomerBuilder(ProductGroup productGroup) {
        withProductGroup(productGroup);
    }

    public CustomerBuilder withProductGroup(ProductGroup productGroup) {
        customer.setProductGroup(productGroup.getDescription());
        customer.setProductGroupId(productGroup.getId().intValue());

        return this;
    }

    public CustomerBuilder withStatus(CustomerStatus status) {
        customer.setStatus(status);

        return this;
    }

    public Customer build() {
        return customer;
    }
}
