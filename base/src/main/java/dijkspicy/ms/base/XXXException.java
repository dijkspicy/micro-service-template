package dijkspicy.ms.base;

/**
 * ServiceException
 *
 * @author dijkspicy
 * @date 2018/6/18
 */
public abstract class XXXException extends RuntimeException implements Returnable {

    private static final long serialVersionUID = -1662594400955029784L;
    private final Ret ret;
    private final Object data;

    protected XXXException(Ret ret, String msg) {
        this(ret, null, msg);
    }

    protected XXXException(Ret ret, String msg, Throwable e) {
        this(ret, null, msg, e);
    }

    protected XXXException(Ret ret, Object data, String msg) {
        super(msg);
        this.ret = ret;
        this.data = data;
    }

    protected XXXException(Ret ret, Object data, String msg, Throwable e) {
        super(msg, e);
        this.ret = ret;
        this.data = data;
    }

    public final int getHttpCode() {
        return this.ret.httpCode;
    }

    @Override
    public Object getData() {
        return this.data;
    }

    @Override
    public final int getStatus() {
        return this.ret.status;
    }

    @Override
    public final String getMessage() {
        return "[" + this.ret.message + "] " + super.getMessage();
    }
}
