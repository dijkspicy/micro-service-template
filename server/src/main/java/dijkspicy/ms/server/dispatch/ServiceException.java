package dijkspicy.ms.server.dispatch;

/**
 * ServiceException
 *
 * @author dijkspicy
 * @date 2018/6/18
 */
public abstract class ServiceException extends RuntimeException implements Returnable {

    private static final long serialVersionUID = -1662594400955029784L;
    private final Ret ret;

    protected ServiceException(Ret ret, String msg) {
        super(msg);
        this.ret = ret;
    }

    protected ServiceException(Ret ret, String msg, Throwable e) {
        super(msg, e);
        this.ret = ret;
    }

    public final int getHttpCode() {
        return this.ret.httpCode;
    }

    @Override
    public final int getRetCode() {
        return this.ret.retCode;
    }

    @Override
    public final String getRetInfo() {
        return "[" + this.ret.retInfo + "] " + this.getMessage();
    }
}
