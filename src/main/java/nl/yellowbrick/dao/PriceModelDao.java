package nl.yellowbrick.dao;

import nl.yellowbrick.domain.Customer;
import nl.yellowbrick.domain.PriceModel;

import java.util.Optional;

public interface PriceModelDao {

    public Optional<PriceModel> findForCustomer(Customer customer);
}
