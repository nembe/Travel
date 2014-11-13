package nl.yellowbrick.dao;

import nl.yellowbrick.domain.Customer;

public interface CardOrderDao {

    public void saveSpecialTarifIfApplicable(Customer customer);

    public void validateCardOrders(Customer customer);
}
