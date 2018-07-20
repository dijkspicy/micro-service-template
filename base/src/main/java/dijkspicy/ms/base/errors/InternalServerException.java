package dijkspicy.ms.base.errors;


import dijkspicy.ms.base.XXXException;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/9
 */
public class InternalServerException extends XXXException {
    private static final long serialVersionUID = -6306565730694110177L;

    public InternalServerException(Object data, String msg) {
        super(INTERNAL_SERVER_ERROR, data, msg);
    }

    public InternalServerException(Object data, String msg, Throwable e) {
        super(INTERNAL_SERVER_ERROR, data, msg, e);
    }

    public InternalServerException(String msg) {
        this(null, msg);
    }

    public InternalServerException(String msg, Throwable e) {
        this(null, msg, e);
    }
}
