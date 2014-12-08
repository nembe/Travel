package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.Customer;

public interface CardOrderDao {

    public void saveSpecialTarifIfApplicable(Customer customer);

    public void validateCardOrders(Customer customer);
}
