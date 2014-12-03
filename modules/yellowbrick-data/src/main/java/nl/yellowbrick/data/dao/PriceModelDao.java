package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.PriceModel;

import java.util.Optional;

public interface PriceModelDao {

    public Optional<PriceModel> findForCustomer(Customer customer);
}
