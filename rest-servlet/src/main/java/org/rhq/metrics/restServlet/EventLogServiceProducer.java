package org.rhq.metrics.restServlet;

import org.rhq.metrics.core.EventLogStorage;
import org.rhq.metrics.impl.memory.MemoryEventLogStorage;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 *  @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
@ApplicationScoped
public class EventLogServiceProducer {

    private EventLogStorage eventLogStorage;

    @Produces
    public EventLogStorage getEventLogStorage() {

        if (eventLogStorage == null) {
            this.eventLogStorage = new MemoryEventLogStorage();
        }
        return eventLogStorage;
    }
}
