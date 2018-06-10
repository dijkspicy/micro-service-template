package dijkspicy.queeng.server.dispatch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * queeng
 *
 * @author dijkspicy
 * @date 2018/6/1
 */
public class ServiceException extends Exception {
    private static final long serialVersionUID = 2600476263853583342L;
    private final String id;
    private final int httpCode;

    public ServiceException(String msg) {
        this(msg, (Throwable) null);
    }

    public ServiceException(String msg, Throwable throwable) {
        this("SystemError", msg, throwable);
    }

    public ServiceException(String id, String msg) {
        this(id, 500, msg);
    }

    public ServiceException(String id, String msg, Throwable throwable) {
        this(id, 500, msg, throwable);
    }

    protected ServiceException(String id, int httpCode, String msg) {
        this(id, httpCode, msg, null);
    }

    protected ServiceException(String id, int httpCode, String msg, Throwable throwable) {
        super(msg, throwable);
        this.id = id;
        this.httpCode = httpCode;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonIgnore
    public int getHttpCode() {
        return httpCode;
    }

    @JsonProperty("message")
    @Override
    public String getMessage() {
        return this.id + (super.getMessage() == null ? "" : ": " + super.getMessage());
    }
}
