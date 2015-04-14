package nl.yellowbrick.data.domain;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class ProductGroup {

    private Long id;
    private String description;
    private boolean internalCardProvisioning;
    private String mutator;
    private Date mutationDate;
    private int maxAnnotations;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isInternalCardProvisioning() {
        return internalCardProvisioning;
    }

    public void setInternalCardProvisioning(boolean internalCardProvisioning) {
        this.internalCardProvisioning = internalCardProvisioning;
    }

    public String getMutator() {
        return mutator;
    }

    public void setMutator(String mutator) {
        this.mutator = mutator;
    }

    public Date getMutationDate() {
        return mutationDate;
    }

    public void setMutationDate(Date mutationDate) {
        this.mutationDate = mutationDate;
    }

    public int getMaxAnnotations() {
        return maxAnnotations;
    }

    public void setMaxAnnotations(int maxAnnotations) {
        this.maxAnnotations = maxAnnotations;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductGroup that = (ProductGroup) o;

        if (internalCardProvisioning != that.internalCardProvisioning) return false;
        if (maxAnnotations != that.maxAnnotations) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (endDate != null ? !endDate.equals(that.endDate) : that.endDate != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (mutationDate != null ? !mutationDate.equals(that.mutationDate) : that.mutationDate != null) return false;
        if (mutator != null ? !mutator.equals(that.mutator) : that.mutator != null) return false;
        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (internalCardProvisioning ? 1 : 0);
        result = 31 * result + (mutator != null ? mutator.hashCode() : 0);
        result = 31 * result + (mutationDate != null ? mutationDate.hashCode() : 0);
        result = 31 * result + maxAnnotations;
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }
}


