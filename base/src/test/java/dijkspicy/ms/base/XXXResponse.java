package dijkspicy.ms.base;

/**
 * XXXResponse
 *
 * @author dijkspicy
 * @date 2018/7/17
 */
public class XXXResponse implements Returnable {
    private int status = OK.status;
    private String message = OK.message;
    private Object data = null;

    @Override
    public int getStatus() {
        return status;
    }

    public XXXResponse setStatus(int status) {
        this.status = status;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public XXXResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public Object getData() {
        return data;
    }

    public XXXResponse setData(Object data) {
        this.data = data;
        return this;
    }
}
