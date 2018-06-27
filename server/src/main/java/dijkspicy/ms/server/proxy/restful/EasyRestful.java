package dijkspicy.ms.server.proxy.restful;

/**
 * EasyRestful
 *
 * @author dijkspicy
 * @date 2018/6/27
 */
public abstract class EasyRestful {

    public static SimpleClientBuilder create() {
        return new SimpleClientBuilder();
    }

    public static SSLClientBuilder createSafe() {
        return new SSLClientBuilder();
    }

    public static AutoSyncSSLClientBuilder createAutoSafe() {
        return new AutoSyncSSLClientBuilder();
    }
}
