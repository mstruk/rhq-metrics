package org.rhq.metrics.core;

import java.util.Deque;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class EventLogQueryParams {

    private long from;
    private long to;
    private String application;
    private Deque<LogEvent> events;

    public EventLogQueryParams() {}

    public EventLogQueryParams setFrom(long millis) {
        this.from = millis;
        return this;
    }

    public long getFrom() {
        return from;
    }

    public EventLogQueryParams setTo(long millis) {
        this.to = millis;
        return this;
    }

    public long getTo() {
        return to;
    }

    public EventLogQueryParams setEvents(Deque<LogEvent> events) {
        this.events = events;
        return this;
    }

    public Deque<LogEvent> getEvents() {
        return events;
    }

    public EventLogQueryParams setApplication(String application) {
        this.application = application;
        return this;
    }

    public String getApplication() {
        return application;
    }
}
