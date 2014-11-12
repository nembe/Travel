package nl.yellowbrick.domain;

import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Optional;

public class Config {

    private String field;
    private String value;
    private String description;
    private String section;
    private String title;


    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static final Optional<Config> findByField(Collection<Config> configs, String field) {
        Config config = Iterables.find(configs, (cfg) -> cfg.getField().equalsIgnoreCase(field), null);

        return Optional.ofNullable(config);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Config config = (Config) o;

        if (description != null ? !description.equals(config.description) : config.description != null) return false;
        if (field != null ? !field.equals(config.field) : config.field != null) return false;
        if (section != null ? !section.equals(config.section) : config.section != null) return false;
        if (title != null ? !title.equals(config.title) : config.title != null) return false;
        if (value != null ? !value.equals(config.value) : config.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (section != null ? section.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
}
