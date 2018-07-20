package dijkspicy.ms.base.errors;


import dijkspicy.ms.base.XXXException;

/**
 * SystemServerException
 *
 * @author dijkspicy
 * @date 2018/7/17
 */
public class SystemServerException extends XXXException {
    private static final long serialVersionUID = 345649981650571902L;

    public SystemServerException(String msg) {
        this(null, msg);
    }

    public SystemServerException(String msg, Throwable e) {
        this(null, msg, e);
    }

    public SystemServerException(Object data, String msg) {
        super(SYSTEM_SERVER_ERROR, data, msg);
    }

    public SystemServerException(Object data, String msg, Throwable e) {
        super(SYSTEM_SERVER_ERROR, data, msg, e);
    }
}
