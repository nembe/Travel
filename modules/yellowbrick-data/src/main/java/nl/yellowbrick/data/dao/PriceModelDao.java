package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.PriceModel;

import java.util.Optional;

public interface PriceModelDao {

    public Optional<PriceModel> findForCustomer(long customer);
}
