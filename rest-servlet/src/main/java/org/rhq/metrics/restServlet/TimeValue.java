package org.rhq.metrics.restServlet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *  @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
@XmlRootElement(name =  "time")
public class TimeValue {

    private long time;

    public TimeValue() {}

    public TimeValue(long time) {
        this.time = time;
    }

    @XmlAttribute
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
