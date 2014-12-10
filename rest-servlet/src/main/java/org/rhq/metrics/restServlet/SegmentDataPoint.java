package org.rhq.metrics.restServlet;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *  @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
@XmlRootElement
public class SegmentDataPoint extends BucketDataPoint {

    private String segment;
    private long duration;
    private long count;
    private double speed;
    private double sum;

    public SegmentDataPoint(String id, String segment, long timestamp, long duration, long count, double sum, double min, double max) {
        super(id, timestamp, min, count > 0 ? sum / (double) count : 0, max);
        this.segment = segment;
        this.duration = duration;
        this.count = count;
        this.sum = sum;

        this.speed = count > 0 ? sum / (double) duration * 1000 : 0;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "SegmentDataPoint{" +
                "segment=" + segment +
                ", count=" + count +
                ", duration=" + duration +
                ", sum=" + sum +
                ", speed=" + speed +
                ", min=" + getMin() +
                ", max=" + getMax() +
                ", avg=" + getAvg() +
                '}';
    }
}
