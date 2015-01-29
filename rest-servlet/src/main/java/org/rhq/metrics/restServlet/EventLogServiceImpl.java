package org.rhq.metrics.restServlet;

import org.rhq.metrics.core.LogEvent;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
@ApplicationScoped
public class EventLogServiceImpl implements EventLogService {

    private Map<String, Metric> metrics = new ConcurrentHashMap<>();

    {
        Metric metric = new Metric("requests");
        metrics.put(metric.getId(), metric);

        metric = new NotificationsMetric("notifications");
        metrics.put(metric.getId(), metric);

        metric = new UniqueUsersMetric("unique-users");
        metrics.put(metric.getId(), metric);

        metric = new BandwidthMetric("bandwidth");
        metrics.put(metric.getId(), metric);

        metric = new BandwidthByServiceMetric("bandwidth-by-service");
        metrics.put(metric.getId(), metric);

        metric = new BandwidthByPathMetric("bandwidth-by-path-");
        metrics.put(metric.getId(), metric);
    }

    @Override
    public boolean checkMetricId(String id) {
        if (id == null) {
            return true;
        }
        for (String rid: metrics.keySet()) {
            if (rid.equals(id)) {
                return true;
            }
            if (rid.endsWith("-") && id.startsWith(rid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkTags(LogEvent e, List<String> tags) {
        if (tags == null) {
            return true;
        }
        for (String tag: tags) {
            if (!e.containsTag(tag)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Predicate<LogEvent> getFilterForId(String id, List<String> tags) {
        return metrics.get(id).getFilter(tags);
    }

    @Override
    public Function<LogEvent, Double> getValueFunctionForMetric(String id) {
        return metrics.get(id).getValueFunction();
    }

    @Override
    public Function<LogEvent, Object> getGroupingFunctionForMetric(String id) {
        return metrics.get(id).getGroupingFunction();
    }

    @Override
    public Function<Map<String, BucketDataPoint>, BucketDataPoint> getCollectingFunction(String id, long startTime, long duration) {
        return metrics.get(id).getCollectingFunction(startTime, duration);
    }

    private class Metric {
        protected String id;

        public Metric(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public Predicate<LogEvent> getFilter(List<String> tags) {
            Predicate<LogEvent> p = e -> checkTags(e, tags);
            if (id == null) {
                return p;
            }
            return p.and(e -> e.containsTag("path:/" + e.getApplication()));
        }

        public Function<LogEvent, Double> getValueFunction() {
            return e -> 1.0;
        }

        public Function<LogEvent, Object> getGroupingFunction() {
            return null;
        }

        public Function<Map<String, BucketDataPoint>, BucketDataPoint> getCollectingFunction(long startTime, long duration) {
            return null;
        }
    }

    private class NotificationsMetric extends Metric {
        public NotificationsMetric(String id) {
            super(id);
        }

        @Override
        public Predicate<LogEvent> getFilter(List<String> tags) {
            return super.getFilter(tags)
                .and(e -> e.getNotification() != null);
        }
    }

    private class BandwidthMetric extends Metric {
        public BandwidthMetric(String id) {
            super(id);
        }

        @Override
        public Function<LogEvent, Double> getValueFunction() {
            return e -> (double) e.getTotalBytes();
        }
    }

    private class BandwidthByServiceMetric extends BandwidthMetric {
        public BandwidthByServiceMetric(String id) {
            super(id);
        }

        @Override
        public Function<LogEvent, Object> getGroupingFunction() {
            return e -> e.getPathPrefix(2);
        }
    }

    private class BandwidthByPathMetric extends BandwidthMetric {
        public BandwidthByPathMetric(String id) {
            super(id);
        }

        @Override
        public Function<LogEvent, Object> getGroupingFunction() {
            return e -> {
                String [] parsed = id.split("-");
                int depth = Integer.parseInt(parsed[parsed.length-1]);
                return e.getPathPrefix(depth);
            };
        }
    }

    private class UniqueUsersMetric extends Metric {
        public UniqueUsersMetric(String id) {
            super(id);
        }

        @Override
        public Function<LogEvent, Object> getGroupingFunction() {
            return LogEvent::getUserId;
        }

        @Override
        public Function<Map<String, BucketDataPoint>, BucketDataPoint> getCollectingFunction(long startTime, long duration) {
            return m -> {
                return new SegmentDataPoint(id, null, startTime, duration, m.keySet().size(), m.keySet().size(), 1.0, 1.0);
            };
        }
    }
}
