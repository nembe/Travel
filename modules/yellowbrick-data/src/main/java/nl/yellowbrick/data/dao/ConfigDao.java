package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.Config;

import java.util.List;
import java.util.Optional;

public interface ConfigDao {

    public List<Config> findAllBySection(ConfigSection section);

    public Optional<Config> findSectionField(ConfigSection section, String field);
}
