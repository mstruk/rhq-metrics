package org.rhq.metrics.core;

import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *  @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public interface LogEventQuery<T> extends Function<Stream<LogEvent>, T> {

    public static class Pair<F, S> {
        private F first;
        private S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public F getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }
    }

    public static class Distinct<S> implements Function<LogEvent, Stream<S>> {

        private HashSet<S> uniques = new HashSet<>();
        private Function<LogEvent, S> valueGetter;

        public Distinct(Function<LogEvent, S> valueGetter) {
            this.valueGetter = valueGetter;
        }

        @Override
        public Stream<S> apply(LogEvent logEvent) {
            S value = valueGetter.apply(logEvent);
            if (uniques.contains(value)) {
                return null;
            }
            uniques.add(value);
            return Stream.of(value);
        }
    }

    public static <S> Distinct distinct(Function<LogEvent, S> valueGetter) {
        return new Distinct(valueGetter);
    }

    public static <F, S> Pair<F, S> pair(F first, S second) {
        return new Pair(first, second);
    }
}
