package org.rhq.metrics.restServlet;

import org.rhq.metrics.core.LogEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public interface EventLogService {

    boolean checkMetricId(String id);

    boolean checkTags(LogEvent e, List<String> tags);

    Predicate<LogEvent> getFilterForId(String id, List<String> tags);

    Function<LogEvent, Double> getValueFunctionForMetric(String id);

    Function<LogEvent, Object> getGroupingFunctionForMetric(String id);

    Function<Map<String, BucketDataPoint>, BucketDataPoint> getCollectingFunction(String id, long startTime, long duration);
}
