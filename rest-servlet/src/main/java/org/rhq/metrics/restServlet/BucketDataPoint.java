package org.rhq.metrics.restServlet;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * A point in time with some data for min/avg/max to express
 * that at this point in time multiple values were recorded.
 * @author Heiko W. Rupp
 */
@ApiClass(description = "A bucket is a time range with multiple data items represented by min/avg/max values" +
    "for that time span.")
@XmlRootElement
@JsonIgnoreProperties({"empty"})
public class BucketDataPoint extends IdDataPoint {

    private double min;
    private double max;
    private double avg;

    public BucketDataPoint() {
    }

    public BucketDataPoint(String id, long timestamp, double min, double avg, double max) {
        super();
        this.setId(id);
        this.setTimestamp(timestamp);
        this.min = min;
        this.max = max;
        this.avg = avg;
    }

    @ApiProperty("Minimum value during the time span of the bucket.")
    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    @ApiProperty("Maximum value during the time span of the bucket.")
    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    @ApiProperty("Average value during the time span of the bucket.")
    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    @XmlTransient
    public boolean isEmpty() {
        return min == 0 && max == 0 && avg == 0;
    }

    @Override
    public String toString() {
        return "BucketDataPoint{" +
            "min=" + min +
            ", max=" + max +
            ", avg=" + avg +
            '}';
    }
}
