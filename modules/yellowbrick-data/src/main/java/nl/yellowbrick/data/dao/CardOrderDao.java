package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardOrderStatus;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Customer;

import java.util.List;
import java.util.Optional;

public interface CardOrderDao {

    void saveSpecialTarifIfApplicable(Customer customer);

    void validateCardOrder(CardOrder cardOrder);

    void validateCardOrders(Customer customer, CardType... cardTypes);

    List<String> nextTransponderCardNumbers(int productGroupId, int numberOfCards, Optional<String> lastUsedCardNumber);

    List<CardOrder> findForCustomer(Customer customer, CardOrderStatus orderStatus, CardType cardType);

    void processTransponderCard(String cardNumber, Customer customer, boolean updateMobileWithCard);

    List<CardOrder> findByStatusAndType(CardOrderStatus status, CardType cardType);

    List<CardOrder> findByStatus(CardOrderStatus status);

    Optional<CardOrder> findById(long id);
}
