package dijkspicy.ms.server.proxy.restful;

/**
 * RestfulException
 *
 * @author dijkspicy
 * @date 2018/6/27
 */
public class RestfulException extends RuntimeException {
    public RestfulException(String msg) {
        super(msg);
    }

    public RestfulException(String msg, Throwable e) {
        super(msg, e);
    }
}
