package nl.yellowbrick.data.dao;


import nl.yellowbrick.data.domain.WhitelistEntry;

import java.util.Optional;

public interface WhitelistImportDao {

    void markAllAsObsolete();

    void deleteAllObsolete();

    Optional<WhitelistEntry> findByTravelcardNumber(String tcNumber);

    void updateEntry(WhitelistEntry entry);

    void createEntry(WhitelistEntry entry);
}
