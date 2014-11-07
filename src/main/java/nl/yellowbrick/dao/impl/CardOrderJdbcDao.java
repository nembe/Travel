package nl.yellowbrick.dao.impl;

import nl.yellowbrick.dao.CardOrderDao;
import nl.yellowbrick.domain.Customer;
import org.springframework.stereotype.Component;

@Component
public class CardOrderJdbcDao implements CardOrderDao {

    @Override
    public void saveSpecialTarifIfApplicable(Customer customer) {
        // TODO implement
    }

    @Override
    public void validateCardOrders(Customer customer) {
        // TODO implement
    }
}
