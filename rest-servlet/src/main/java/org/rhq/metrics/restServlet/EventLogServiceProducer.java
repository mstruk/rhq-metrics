package org.rhq.metrics.restServlet;

import org.rhq.metrics.core.EventLogQueryResult;
import org.rhq.metrics.core.EventLogService;
import org.rhq.metrics.core.LogEvent;
import org.rhq.metrics.impl.memory.MemoryEventLogService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
@ApplicationScoped
public class EventLogServiceProducer {

    private EventLogService eventLogService;

    @Produces
    public EventLogService getEventLogService() {

        if (eventLogService == null) {

            MemoryEventLogService service = new MemoryEventLogService();

            service.addMetric("top-services-by-request", params -> {
                Map<String, Long> grouped = params.getEvents().stream()
                        .filter(e -> e.getTimestamp() >= params.getFrom() && e.getTimestamp() <= params.getTo()
                                && e.containsTag("path:/" + e.getApplication()))
                        .map(e -> e.forProcessing())
                        .collect(Collectors.groupingBy(e -> e.getPathPrefix(2), Collectors.counting()));

                ArrayList<Map.Entry<String, Long>> sorted = new ArrayList<>(grouped.entrySet());
                Collections.sort(sorted,
                        (a, b) -> a.getValue() > b.getValue() ? -1 : 1);

                return new EventLogQueryResult<List<Map.Entry<String, Long>>>(sorted, EventLogQueryResult.Type.SEGMENTS_COUNT);
            });

            service.addMetric("active-users-count", params -> {
                long result = params.getEvents().stream()
                        .filter(e -> e.getTimestamp() >= params.getFrom() && e.getTimestamp() <= params.getTo()
                                && e.containsTag("path:/" + e.getApplication()))
                        .map(e -> e.getUserId())
                        .collect(Collectors.toCollection(() -> new HashSet()))
                        .size();

                return new EventLogQueryResult<Long>(result, EventLogQueryResult.Type.COUNT);
            });

            service.addMetric("notifications", params -> {
                long result = params.getEvents().stream()
                        .filter(e -> e.getTimestamp() >= params.getFrom() && e.getTimestamp() <= params.getTo()
                                && "/subscriptions".equals(e.getPathPrefix(1)) && e.containsTag("path:/" + e.getApplication()))
                        .map(e -> e.getUserId())
                        .collect(Collectors.toCollection(() -> new HashSet()))
                        .size();

                return new EventLogQueryResult<Long>(result, EventLogQueryResult.Type.COUNT);
            });

            service.addMetric("total-bandwidth", params -> {
                long result = params.getEvents().stream()
                        .filter(e -> e.getTimestamp() >= params.getFrom() && e.getTimestamp() <= params.getTo()
                                && params.getApplication().equals(e.getApplication()))
                        .map(e -> e.forProcessing())
                        .collect(Collectors.summingLong(LogEvent::getTotalBytes));

                return new EventLogQueryResult<Long>(result, EventLogQueryResult.Type.TOTAL);
            });

            this.eventLogService = service;
        }
        return eventLogService;
    }
}
