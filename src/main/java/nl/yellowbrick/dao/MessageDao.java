package nl.yellowbrick.dao;

import java.util.Optional;

public interface MessageDao {

    Optional<String> getMessage(String key, String locale);

    Optional<String> getGroupSpecificMessage(String key, int groupId, String locale);
}
