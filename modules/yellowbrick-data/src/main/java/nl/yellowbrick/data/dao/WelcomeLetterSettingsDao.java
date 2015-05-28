package nl.yellowbrick.data.dao;

import java.util.Optional;

public interface WelcomeLetterSettingsDao {

    Optional<Long> latestExportedCustomer();

    void updateLatestExportedCustomer(long customerId);
}
