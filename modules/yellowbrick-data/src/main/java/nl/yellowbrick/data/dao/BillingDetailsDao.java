package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.DirectDebitDetails;

import java.util.List;
import java.util.Optional;

public interface BillingDetailsDao {

    Optional<DirectDebitDetails> findDirectDebitDetailsForCustomer(long customerId);

    List<DirectDebitDetails> findDirectDebitDetailsBySepaNumber(String sepaNumber);

    boolean existsCreditCardReferenceForCustomer(long customerId);
}
