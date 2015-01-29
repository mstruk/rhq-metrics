package org.rhq.metrics.core;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * EventLogService represents a log of all events that can be queried using different matching, and grouping criteria
 * to get a more complex drill down / cross sections of data, and perform aggregates on them.
 *
 * This can be used for statistics like: top visited URLs, pie chart data to segment counts / sums by sub criteria,
 * counting distinct occurrences of some values, ...
 *
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public interface EventLogStorage {

    /**
     * Return true if metric with specified id exists
     *
     * @param id
     * @return
     */
    ListenableFuture<Boolean> idExists(String id);

    /**
     *  Add logging event to log service
     */
    void addEvent(LogEvent event);


    /**
     * Get the last {count} events.
     *
     * @param count Number of events
     * @return
     */
    Collection<LogEvent> getLast(int count);

    /**
     * Perform query on event log data
     *
     * @param query A function to apply on the stream of logging events
     * @return a collection or a map with query results
     */
    <T> T query(LogEventQuery<T> query) ;

    /**
     * A fast filtering function that returns a list of events with timestamps between a specified start and end time,
     * and additionally filtered using a passed-in filter object.
     *
     * @param start start timestamp - only logging events with timestamp greater or equal to that will be included
     * @param end end timestamp - only logging events with smaller timestamp will be included
     * @param filter filter object to be used in order to additionally filter logging events
     * @return list of logging events matching criteria
     */
    ListenableFuture<List<LogEvent>> findData(long start, long end, Predicate<LogEvent> filter);
}
