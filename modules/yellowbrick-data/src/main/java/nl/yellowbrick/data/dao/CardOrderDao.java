package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.*;

import java.util.List;
import java.util.Optional;

public interface CardOrderDao {

    void saveSpecialTarifIfApplicable(Customer customer);

    void validateCardOrder(CardOrder cardOrder);

    void validateCardOrders(Customer customer, CardType... cardTypes);

    List<String> nextTransponderCardNumbers(int productGroupId, int numberOfCards, Optional<String> lastUsedCardNumber);

    List<CardOrder> findForCustomer(Customer customer, CardOrderStatus orderStatus, CardType cardType);

    List<CardOrder> findTransponderCardsForCustomer(Customer customer);

    void processTransponderCard(String cardNumber, Customer customer, boolean updateMobileWithCard);

    List<CardOrder> findByStatusAndType(CardOrderStatus status, CardType cardType);

    List<CardOrder> findByStatus(CardOrderStatus status);

    List<CardOrder> findPendingExport(ProductGroup productGroup);

    Optional<CardOrder> findById(long id);

    void delete(long id);

    void updateCardNumber(long cardOrderId, String cardNumber);

    void updateOrderStatus(long cardOrderId, CardOrderStatus status);

    String nextQCardNumber(long customerId);
}
