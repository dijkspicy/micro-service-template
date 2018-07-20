package dijkspicy.ms.server.dispatch;

import dijkspicy.ms.base.Returnable;

/**
 * ServiceResponse
 *
 * @author dijkspicy
 * @date 2018/6/18
 */
public class ServiceResponse implements Returnable {

    private Object data;
    private int status = OK.status;
    private String message = OK.message;

    public ServiceResponse() {
    }

    public ServiceResponse(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return data;
    }

    public ServiceResponse setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public ServiceResponse setStatus(int status) {
        this.status = status;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ServiceResponse setMessage(String message) {
        this.message = message;
        return this;
    }
}
