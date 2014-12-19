package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.Customer;

import java.util.List;
import java.util.Optional;

public interface CardOrderDao {

    public void saveSpecialTarifIfApplicable(Customer customer);

    public void validateCardOrders(Customer customer);

    public List<String> nextTransponderCardNumbers(int productGroupId, int numberOfCards, Optional<String> lastUsedCardNumber);
}
