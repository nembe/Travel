package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.DirectDebitDetails;

import java.util.List;
import java.util.Optional;

public interface DirectDebitDetailsDao {

    Optional<DirectDebitDetails> findForCustomer(long customerId);

    List<DirectDebitDetails> findBySepaNumber(String sepaNumber);
}
