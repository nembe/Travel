package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardOrderStatus;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Customer;

import java.util.List;
import java.util.Optional;

public interface CardOrderDao {

    public void saveSpecialTarifIfApplicable(Customer customer);

    public void validateCardOrder(CardOrder cardOrder);

    public void validateCardOrders(Customer customer, CardType... cardTypes);

    public List<String> nextTransponderCardNumbers(int productGroupId, int numberOfCards, Optional<String> lastUsedCardNumber);

    public List<CardOrder> findForCustomer(Customer customer, CardOrderStatus orderStatus, CardType cardType);

    public void processTransponderCard(String cardNumber, Customer customer, boolean updateMobileWithCard);
}
