package org.rhq.metrics.core;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
@XmlRootElement
public class LogEvent {

    public static final String EMPTY = "";

    private String userId;
    private String application;
    private String clientAddress;
    private String endpoint;
    private String uri;
    private long requestBytes;
    private long responseBytes;
    private int status;
    private String method;
    private String notification;
    private long timestamp = System.currentTimeMillis();
    private long duration;
    private boolean apiRequest;
    private Set<String> tags = new HashSet<>();
    private ArrayList<String> paths = new ArrayList<>();


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;

        int end = uri.length();
        int pos = uri.indexOf("?");
        if (pos != -1) {
            end = pos;
        }
        this.endpoint = uri.substring(0, end);

        String [] segments = endpoint.split("/");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String s: segments) {

            if (s.length() == 0) {
                continue;
            }
            sb.append("/").append(s);
            String path = sb.toString();
            paths.add(path);
            tags.add("path:" + path);

            if (application == null && count == 0) {
                application = s;
            }
            count++;
        }
    }

    public String getPathPrefix(int count) {
        if (count <= paths.size()) {
            return paths.get(count-1);
        }
        return EMPTY;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public boolean containsTag(String tag) {
        return tags.contains(tag);
    }

    public long getRequestBytes() {
        return requestBytes;
    }

    public void setRequestBytes(long requestBytes) {
        this.requestBytes = requestBytes;
    }

    public long getResponseBytes() {
        return responseBytes;
    }

    public void setResponseBytes(long responseBytes) {
        this.responseBytes = responseBytes;
    }

    public long getTotalBytes() {
        return requestBytes + responseBytes;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long millis) {
        this.duration = millis;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setApiRequest(boolean isApi) {
        this.apiRequest = isApi;
    }

    public boolean isApiRequest() {
        return this.apiRequest;
    }

    public void setNotification(String action) {
        this.notification = action;
    }

    public String getNotification() {
        return this.notification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogEvent logEvent = (LogEvent) o;

        if (apiRequest != logEvent.apiRequest) return false;
        if (duration != logEvent.duration) return false;
        if (requestBytes != logEvent.requestBytes) return false;
        if (responseBytes != logEvent.responseBytes) return false;
        if (status != logEvent.status) return false;
        if (timestamp != logEvent.timestamp) return false;
        if (application != null ? !application.equals(logEvent.application) : logEvent.application != null)
            return false;
        if (clientAddress != null ? !clientAddress.equals(logEvent.clientAddress) : logEvent.clientAddress != null)
            return false;
        if (endpoint != null ? !endpoint.equals(logEvent.endpoint) : logEvent.endpoint != null) return false;
        if (method != null ? !method.equals(logEvent.method) : logEvent.method != null) return false;
        if (notification != null ? !notification.equals(logEvent.notification) : logEvent.notification != null) return false;
        if (paths != null ? !paths.equals(logEvent.paths) : logEvent.paths != null) return false;
        if (tags != null ? !tags.equals(logEvent.tags) : logEvent.tags != null) return false;
        if (uri != null ? !uri.equals(logEvent.uri) : logEvent.uri != null) return false;
        if (userId != null ? !userId.equals(logEvent.userId) : logEvent.userId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (application != null ? application.hashCode() : 0);
        result = 31 * result + (clientAddress != null ? clientAddress.hashCode() : 0);
        result = 31 * result + (endpoint != null ? endpoint.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (int) (requestBytes ^ (requestBytes >>> 32));
        result = 31 * result + (int) (responseBytes ^ (responseBytes >>> 32));
        result = 31 * result + status;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (notification != null ? notification.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + (apiRequest ? 1 : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (paths != null ? paths.hashCode() : 0);
        return result;
    }

    public ProcessingLogEvent forProcessing() {
        return new ProcessingLogEvent(this);
    }

    public static class ProcessingLogEvent extends LogEvent {

        private LogEvent delegate;

        ProcessingLogEvent(LogEvent e) {
            delegate = e;
        }

        @Override
        public String getUserId() {
            return delegate.userId == null ? EMPTY : delegate.userId;
        }

        @Override
        public String getApplication() {
            return delegate.application == null ? EMPTY : delegate.application;
        }

        @Override
        public String getClientAddress() {
            return delegate.clientAddress == null ? EMPTY : delegate.clientAddress;
        }

        @Override
        public String getUri() {
            return delegate.uri == null ? EMPTY : delegate.uri;
        }

        @Override
        public long getRequestBytes() {
            return delegate.getRequestBytes();
        }

        @Override
        public long getResponseBytes() {
            return delegate.getResponseBytes();
        }

        @Override
        public long getTotalBytes() {
            return delegate.getTotalBytes();
        }

        @Override
        public Set<String> getTags() {
            return delegate.getTags();
        }

        @Override
        public long getTimestamp() {
            return delegate.getTimestamp();
        }

        @Override
        public int getStatus() {
            return delegate.getStatus();
        }

        @Override
        public String getPathPrefix(int count) {
            return delegate.getPathPrefix(count);
        }

        @Override
        public boolean containsTag(String tag) {
            return delegate.containsTag(tag);
        }

        @Override
        public String getMethod() {
            return delegate.getMethod();
        }

        @Override
        public String getNotification() {
            return delegate.notification == null ? EMPTY : delegate.notification;
        }

        @Override
        public long getDuration() {
            return delegate.getDuration();
        }

        @Override
        public boolean isApiRequest() {
            return delegate.isApiRequest();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }
    }
}
