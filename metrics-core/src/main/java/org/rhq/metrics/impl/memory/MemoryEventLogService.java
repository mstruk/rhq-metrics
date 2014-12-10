package org.rhq.metrics.impl.memory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.rhq.metrics.core.EventLogQueryParams;
import org.rhq.metrics.core.EventLogQueryResult;
import org.rhq.metrics.core.EventLogService;
import org.rhq.metrics.core.LogEvent;
import org.rhq.metrics.core.LogEventQuery;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MemoryEventLogService implements EventLogService {

    /** Limit to prevent in-memory event log store to OOM the runtime */
    private static final int MAX_SIZE = 500000;
    private AtomicInteger size = new AtomicInteger();

    private ConcurrentLinkedDeque<LogEvent> logEvents = new ConcurrentLinkedDeque<>();
    private ConcurrentHashMap<String, Function<EventLogQueryParams, EventLogQueryResult>> metrics = new ConcurrentHashMap<>();

    @Override
    public ListenableFuture<Boolean> idExists(String id) {
        return Futures.immediateFuture(metrics.contains(id));
    }

    public void addMetric(String id, Function<EventLogQueryParams, EventLogQueryResult> metric) {
        metrics.put(id, metric);
    }

    @Override
    public void addEvent(LogEvent event) {
        logEvents.add(event);

        int length = size.incrementAndGet();
        if (length > MAX_SIZE) {
            logEvents.removeFirst();
            size.decrementAndGet();
        }
    }

    @Override
    public Collection<LogEvent> getLast(int count) {
        List<LogEvent> ret = new LinkedList<>();
        synchronized (logEvents) {
            int len = size.get();
            for (int i = 0; i < count && len > 0; i++) {
                ret.add(logEvents.getLast());
            }
        }
        return ret;
    }

    @Override
    public <T> T query(LogEventQuery<T> query) {
        return query.apply(logEvents.stream());
    }

    @Override
    public ListenableFuture<List<LogEvent>> findData(long start, long end, Predicate<LogEvent> filter) {
        Stream<LogEvent> stream = logEvents.stream().filter(e -> e.getTimestamp() >= start && e.getTimestamp() <= end);
        if (filter != null) {
            stream = stream.filter(e -> filter.test(e));
        }
        return Futures.immediateFuture(stream.collect(Collectors.toList()));
    }
}
