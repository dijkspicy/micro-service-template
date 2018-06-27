package dijkspicy.ms.server.proxy.restful;

/**
 * EasyRestful
 *
 * @author dijkspicy
 * @date 2018/6/27
 */
public abstract class EasyRestful {

    public static EasyRestfulClientBuilder create() {
        return new EasyRestfulClientBuilder();
    }

    public static EasyRestfulSafeClientBuilder createSafe() {
        return new EasyRestfulSafeClientBuilder();
    }

    public static EasyRestfulClientBuilder createAutoSafe() {
        return new EasyRestfulSafeClientBuilder();
    }
}
