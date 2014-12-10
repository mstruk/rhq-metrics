package org.rhq.metrics.core;

import org.rhq.metrics.impl.memory.MemoryEventLogService;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import static org.rhq.metrics.core.LogEventQuery.distinct;


/**
 *  @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class EventLogServiceTest {

    private final long startTime = System.currentTimeMillis();

    private final MemoryEventLogService events = new MemoryEventLogService();

    @BeforeClass
    public void initEvents() {
        events.addEvent(event(startTime, "/testApp/index.html", null, 140, 1200));
        events.addEvent(event(startTime, "/testApp/data", null, 240, 1000));
        events.addEvent(event(startTime, "/testApp/data/offers?q={type:web}", null, 240, 10000));
        events.addEvent(event(startTime + 10, "/testApp/client/liveoak.js", null, 240, 1300));
        events.addEvent(event(startTime + 20, "/testApp/js/app.js", null, 240, 1500));
        events.addEvent(event(startTime + 30, "/testApp/img/logo.png", null, 240, 50000));

        events.addEvent(event(startTime + 60, "/testApp/index.html", "john", 140, 1200));
        events.addEvent(event(startTime + 60, "/testApp/data/users/200", "john", 240, 1000));

        events.addEvent(event(startTime + 80, "/testApp/index.html", "jane", 140, 1200));
        events.addEvent(event(startTime + 80, "/testApp/data/users/34", "jane", 240, 1000));
        events.addEvent(event(startTime + 80, "/testApp/client/liveoak.js", "jane", 240, 1300));
    }

    private LogEvent event(long timestamp, String uri) {
        return event(timestamp, uri, null, 0, 0);
    }

    private LogEvent event(long timestamp, String uri, String userId) {
        return event(timestamp, uri, userId, 0, 0);
    }

    private LogEvent event(long timestamp, String uri, String userId, long requestBytes, long responseBytes) {
        LogEvent ev = new LogEvent();
        ev.setTimestamp(timestamp);
        ev.setUri(uri);
        ev.setUserId(userId);
        ev.setRequestBytes(requestBytes);
        ev.setResponseBytes(responseBytes);
        ev.setApplication("testApp");
        return ev;
    }

    @Test
    public void uniqueUsers() {
        long from = startTime;
        long to = startTime + 50;

        // count number of different users
        long count = events.query(stream -> {

            // We expect elements to naturally be ordered by timestamp, and so we could use binary search to position
            // to the point in stream where from condition matches.
            // No idea how to use API to pull that off, so we iterate over all.
            return stream.filter(e -> e.getTimestamp() >= from && e.getTimestamp() <= to)
                    .flatMap(distinct(e -> e.getUserId()))
                    .count();
        });

        System.out.println("count: " + count);

        long to2 = startTime + 100;
        count = events.query(stream -> {

            // We expect elements to naturally be ordered by timestamp, and so we could use binary search to position
            // to the point in stream where from condition matches.
            // No idea how to use API to pull that off, so we iterate over all.
            return stream.filter(e -> e.getTimestamp() >= from && e.getTimestamp() <= to2)
                    .flatMap(distinct(e -> e.getUserId()))
                    .count();
        });

        System.out.println("count: " + count);

        // now let's do it again using Collectors
        count = (long) events.query(stream -> {

            // We expect elements to naturally be ordered by timestamp, and so we could use binary search to position
            // to the point in stream where from condition matches.
            // No idea how to use API to pull that off, so we iterate over all.
            return stream.filter(e -> e.getTimestamp() >= from && e.getTimestamp() <= to2)
                    .map(e -> e.getUserId())
                    .collect(Collectors.toCollection(() -> new HashSet()))
                    .size();
        });

        System.out.println("count: " + count);
    }

    @Test
    public void requestsByUsers() {
        long from = startTime;
        long to = startTime + 100;

        // count requests for each user, and display the counts by user
        Map<String, Long> grouped = events.query(stream -> {

            return stream.filter(e -> e.getTimestamp() >= from && e.getTimestamp() <= to)
                    .map(e -> e.forProcessing())
                    .collect(Collectors.groupingBy(LogEvent::getUserId, Collectors.counting()));
        });

        System.out.println("grouped: " + grouped);
    }

    @Test
    public void topUsersByRequest() {
        long from = startTime;
        long to = startTime + 100;

        // count requests for each user, and display the counts by user
        Map<String, Long> grouped = events.query(stream -> {

            return stream.filter(e -> e.getTimestamp() >= from && e.getTimestamp() <= to)
                    .map(e -> e.forProcessing())
                    .collect(Collectors.groupingBy(LogEvent::getUserId, Collectors.counting()));
        });

        ArrayList<Map.Entry<String, Long>> sorted = new ArrayList<>(grouped.entrySet());
        Collections.sort(sorted,
                (a, b) -> a.getValue() > b.getValue() ? -1 : 1);

        System.out.println("sorted: " + sorted);
    }

    @Test
    public void topUsersByBandwidth() {
        long from = startTime;
        long to = startTime + 100;

        // sum up bandwidths for each user, and display bandwidth by user
        Map<String, Long> grouped = events.query(stream -> {

            return stream.filter(e -> e.getTimestamp() >= from && e.getTimestamp() <= to)
                    .map(e -> e.forProcessing())
                    .collect(Collectors.groupingBy(LogEvent::getUserId, Collectors.summingLong(LogEvent::getTotalBytes)));
        });

        ArrayList<Map.Entry<String, Long>> sorted = new ArrayList<>(grouped.entrySet());
        Collections.sort(sorted,
                (a, b) -> a.getValue() > b.getValue() ? -1 : 1);

        System.out.println("sorted: " + sorted);
    }

    @Test
    public void requestsByUri() {
        long from = startTime;
        long to = startTime + 100;

        Map<String, Long> grouped = events.query(stream -> {
            return stream.filter(e -> e.getTimestamp() >= from && e.getTimestamp() <= to)
                    .map(e -> e.forProcessing())
                    .collect(Collectors.groupingBy(LogEvent::getUri, Collectors.counting()));
        });

        ArrayList<Map.Entry<String, Long>> sorted = new ArrayList<>(grouped.entrySet());
        Collections.sort(sorted,
                (a, b) -> a.getValue() > b.getValue() ? -1 : 1);

        System.out.println("sorted: " + sorted);
    }

    @Test
    public void bandwidthByUri() {
        long from = startTime;
        long to = startTime + 100;

        Map<String, Long> grouped = events.query(stream -> {
            return stream.filter(e -> e.getTimestamp() >= from && e.getTimestamp() <= to)
                    .map(e -> e.forProcessing())
                    .collect(Collectors.groupingBy(LogEvent::getUri, Collectors.summingLong(LogEvent::getTotalBytes)));
        });

        ArrayList<Map.Entry<String, Long>> sorted = new ArrayList<>(grouped.entrySet());
        Collections.sort(sorted,
                (a, b) -> a.getValue() > b.getValue() ? -1 : 1);

        System.out.println("sorted: " + sorted);
    }

    @Test
    public void bandwidthByApp() {
        // normally we always set the app, and are interested in
        // stats breakdowns within an app, not across apps
        long from = startTime;
        long to = startTime + 100;

        Map<String, Long> grouped = events.query(stream -> {
            return stream.filter(e -> e.getTimestamp() >= from && e.getTimestamp() <= to)
                    .map(e -> e.forProcessing())
                    .collect(Collectors.groupingBy(LogEvent::getApplication, Collectors.summingLong(LogEvent::getTotalBytes)));
        });

        ArrayList<Map.Entry<String, Long>> sorted = new ArrayList<>(grouped.entrySet());
        Collections.sort(sorted,
                (a, b) -> a.getValue() > b.getValue() ? -1 : 1);

        System.out.println("sorted: " + sorted);
    }

    @Test
    public void bandwidthByService() {
        long from = startTime;
        long to = startTime + 100;

        Map<String, Long> grouped = events.query(stream -> {
            return stream.filter(e -> e.getTimestamp() >= from && e.getTimestamp() <= to)
                    .map(e -> e.forProcessing())
                    .collect(Collectors.groupingBy(e -> e.getPathPrefix(2), Collectors.summingLong(LogEvent::getTotalBytes)));
        });

        ArrayList<Map.Entry<String, Long>> sorted = new ArrayList<>(grouped.entrySet());
        Collections.sort(sorted,
                (a, b) -> a.getValue() > b.getValue() ? -1 : 1);

        System.out.println("sorted: " + sorted);
    }

    @Test
    public void bandwidthBySubContext() {
        long from = startTime;
        long to = startTime + 100;

        Map<String, Long> grouped = events.query(stream -> {
            return stream.filter(e -> e.getTimestamp() >= from && e.getTimestamp() <= to && e.containsTag("path=/testApp/data"))
                    .map(e -> e.forProcessing())
                    .collect(Collectors.groupingBy(e -> e.getPathPrefix(3), Collectors.summingLong(LogEvent::getTotalBytes)));
        });

        ArrayList<Map.Entry<String, Long>> sorted = new ArrayList<>(grouped.entrySet());
        Collections.sort(sorted,
                (a, b) -> a.getValue() > b.getValue() ? -1 : 1);

        System.out.println("sorted: " + sorted);
    }

    @Test
    public void requestsBySubContext() {
        long from = startTime;
        long to = startTime + 100;

        Map<String, Long> grouped = events.query(stream -> {
            return stream.filter(e -> e.getTimestamp() >= from && e.getTimestamp() <= to && e.containsTag("path=/testApp"))
                    .map(e -> e.forProcessing())
                    .collect(Collectors.groupingBy(e -> e.getPathPrefix(2), Collectors.counting()));
        });

        ArrayList<Map.Entry<String, Long>> sorted = new ArrayList<>(grouped.entrySet());
        Collections.sort(sorted,
                (a, b) -> a.getValue() > b.getValue() ? -1 : 1);

        System.out.println("sorted: " + sorted);
    }
}
