package nl.yellowbrick.data.domain;

import java.util.Calendar;
import java.util.Date;

public class Subscription {

    public static final int TYPE_WEEKLY = 1;
    public static final int TYPE_AIRMILES = 2;

    private long id;
    private Date beginTime;
    private long customerId;
    private Date endTime;
    private String description;
    private long typeId;

    public boolean isSubscriptionActive() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date beginningOfDay = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = calendar.getTime();
        // active : no end time given or the subscription end date is before
        // midnight today.
        return (this.beginTime != null) && (this.beginTime.before(beginningOfDay) || this.beginTime.equals(beginningOfDay))
                && ((this.endTime == null) || this.endTime.after(endOfDay) || this.endTime.equals(endOfDay));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subscription that = (Subscription) o;

        if (customerId != that.customerId) return false;
        if (id != that.id) return false;
        if (typeId != that.typeId) return false;
        if (beginTime != null ? !beginTime.equals(that.beginTime) : that.beginTime != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (beginTime != null ? beginTime.hashCode() : 0);
        result = 31 * result + (int) (customerId ^ (customerId >>> 32));
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (int) (typeId ^ (typeId >>> 32));
        return result;
    }
}
