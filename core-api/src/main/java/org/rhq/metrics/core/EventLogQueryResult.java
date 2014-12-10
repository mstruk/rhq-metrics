package org.rhq.metrics.core;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class EventLogQueryResult<T> {

    private T result;
    private Type type;

    public EventLogQueryResult(T result, Type type) {
        this.result = result;
        this.type = type;
    }

    public T getResult() {
        return result;
    }

    public static enum Type {
        COUNT,
        SEGMENTS_COUNT,
        TOTAL,
        SEGMENTS_TOTAL
    }
}
