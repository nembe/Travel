package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.SpecialRateTemplate;

import java.util.Optional;

import static nl.yellowbrick.data.domain.SpecialRateTemplate.*;

public interface SpecialRateTemplateDao {

    Optional<SpecialRateTemplate> findForProductGroup(long productGroupId, TRANSACTION_TYPE transactionType);
}
