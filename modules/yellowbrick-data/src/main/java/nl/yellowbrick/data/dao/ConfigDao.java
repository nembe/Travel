package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.Config;

import java.util.List;
import java.util.Optional;

public interface ConfigDao {

    List<Config> findAllBySection(ConfigSection section);

    Optional<Config> findSectionField(ConfigSection section, String field);

    Config mustFindSectionField(ConfigSection section, String field);
}
