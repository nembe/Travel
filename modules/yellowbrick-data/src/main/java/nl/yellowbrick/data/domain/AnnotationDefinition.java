package nl.yellowbrick.data.domain;

public class AnnotationDefinition {

    private Long id;
    private int position;
    private long customerId;
    private AnnotationType type;
    private String name = "";
    private boolean defaultAnnotation;
    private boolean freeInput;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public AnnotationType getType() {
        return type;
    }

    public void setType(AnnotationType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefaultAnnotation() {
        return defaultAnnotation;
    }

    public void setDefaultAnnotation(boolean defaultAnnotation) {
        this.defaultAnnotation = defaultAnnotation;
    }

    public boolean isFreeInput() {
        return freeInput;
    }

    public void setFreeInput(boolean freeInput) {
        this.freeInput = freeInput;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnnotationDefinition that = (AnnotationDefinition) o;

        if (customerId != that.customerId) return false;
        if (defaultAnnotation != that.defaultAnnotation) return false;
        if (freeInput != that.freeInput) return false;
        if (position != that.position) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + position;
        result = 31 * result + (int) (customerId ^ (customerId >>> 32));
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (defaultAnnotation ? 1 : 0);
        result = 31 * result + (freeInput ? 1 : 0);
        return result;
    }
}
