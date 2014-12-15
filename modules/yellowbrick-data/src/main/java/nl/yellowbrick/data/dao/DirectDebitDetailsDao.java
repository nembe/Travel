package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.DirectDebitDetails;

import java.util.Optional;

public interface DirectDebitDetailsDao {

    Optional<DirectDebitDetails> findForCustomer(long customerId);
}
