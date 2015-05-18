package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.*;

import java.util.List;
import java.util.Optional;

public interface CardOrderDao {

    void saveSpecialTarifIfApplicable(Customer customer);

    void validateCardOrder(CardOrder cardOrder);

    List<String> nextTransponderCardNumbers(int productGroupId, int numberOfCards, Optional<String> lastUsedCardNumber);

    List<CardOrder> findForCustomer(Customer customer, CardOrderStatus orderStatus, CardType cardType);

    void processTransponderCard(String cardNumber, Customer customer, CardOrder order, boolean updateMobileWithCard);

    List<CardOrder> findByStatusAndType(CardOrderStatus status, CardType cardType);

    List<CardOrder> findByStatus(CardOrderStatus status);

    List<CardOrder> findPendingExport(ProductGroup productGroup);

    Optional<CardOrder> findById(long id);

    void delete(long id);

    void updateOrderStatus(long cardOrderId, CardOrderStatus status);

    String nextQCardNumber(long customerId);

    int cardsAvailableForProductGroup(int productGroupId);
}
