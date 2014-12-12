package nl.yellowbrick.data.dao;


import nl.yellowbrick.data.domain.MarketingAction;

import java.util.Optional;

public interface MarketingActionDao {

    Optional<MarketingAction> findByActionCode(String actionCode);
}
