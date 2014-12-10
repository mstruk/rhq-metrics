package org.rhq.metrics.core;

import com.google.common.base.Objects;

import java.util.HashSet;
import java.util.Set;

/**
 * @author John Sanda
 */
public class RawNumericMetric implements NumericMetric {

    private String bucket = "raw";

    private String id;

    private Double value;

    private long timestamp;

    private HashSet<String> tags;

    public RawNumericMetric() {
    }

    public RawNumericMetric(String id, Double value, long timestamp) {
        this.id = id;
        this.value = value;
        this.timestamp = timestamp;
    }

    public RawNumericMetric(String id, Double value, long timestamp, String [] tags) {
        this.id = id;
        this.value = value;
        this.timestamp = timestamp;

        if (tags != null) {
            this.tags = new HashSet();
            for (String tag: tags) {
                this.tags.add(tag);
            }
        }

    }

    public String getBucket() {
        return bucket;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getMin() {
        return value;
    }

    public Double getMax() {
        return value;
    }

    public Double getAvg() {
        return value;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = new HashSet(tags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RawNumericMetric that = (RawNumericMetric) o;

        if (timestamp != that.timestamp) return false;
        if (bucket != null ? !bucket.equals(that.bucket) : that.bucket != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (tags != null ? !tags.equals(that.tags) : that.tags != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = bucket != null ? bucket.hashCode() : 0;
        result = 47 * result + (id != null ? id.hashCode() : 0);
        result = 47 * result + (value != null ? value.hashCode() : 0);
        result = 47 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 47 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass().getSimpleName())
            .add("bucket", bucket)
            .add("id", id)
            .add("value", value)
            .add("timestamp", timestamp)
            .add("tags: ", tags)
            .toString();
    }
}
