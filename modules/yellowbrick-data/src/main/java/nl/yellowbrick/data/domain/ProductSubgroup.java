package nl.yellowbrick.data.domain;

public class ProductSubgroup {

    private Long id;
    private long productGroupId;
    private String description;
    private boolean business;
    private boolean defaultIssuePhysicalCard;
    private String theme;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getProductGroupId() {
        return productGroupId;
    }

    public void setProductGroupId(long productGroupId) {
        this.productGroupId = productGroupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBusiness() {
        return business;
    }

    public void setBusiness(boolean business) {
        this.business = business;
    }

    public boolean isDefaultIssuePhysicalCard() {
        return defaultIssuePhysicalCard;
    }

    public void setDefaultIssuePhysicalCard(boolean defaultIssuePhysicalCard) {
        this.defaultIssuePhysicalCard = defaultIssuePhysicalCard;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductSubgroup that = (ProductSubgroup) o;

        if (business != that.business) return false;
        if (defaultIssuePhysicalCard != that.defaultIssuePhysicalCard) return false;
        if (productGroupId != that.productGroupId) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (theme != null ? !theme.equals(that.theme) : that.theme != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (productGroupId ^ (productGroupId >>> 32));
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (business ? 1 : 0);
        result = 31 * result + (defaultIssuePhysicalCard ? 1 : 0);
        result = 31 * result + (theme != null ? theme.hashCode() : 0);
        return result;
    }
}
