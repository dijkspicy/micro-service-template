package dijkspicy.ms.server.proxy.http;

import java.util.Map;
import java.util.function.Function;

/**
 * Restful
 *
 * @author dijkspicy
 * @date 2018/6/28
 */
public interface Restful {
    default byte[] post(String uri, byte[] request, Map<String, String> headers) {
        return this.post(uri, request, headers, out -> out);
    }

    <T> T post(String uri, byte[] request, Map<String, String> headers, Function<byte[], T> callback);

    default byte[] put(String uri, byte[] request, Map<String, String> headers) {
        return this.put(uri, request, headers, out -> out);
    }

    <T> T put(String uri, byte[] request, Map<String, String> headers, Function<byte[], T> callback);

    default byte[] patch(String uri, byte[] request, Map<String, String> headers) {
        return this.patch(uri, request, headers, out -> out);
    }

    <T> T patch(String uri, byte[] request, Map<String, String> headers, Function<byte[], T> callback);

    default byte[] delete(String uri, Map<String, String> headers) {
        return this.delete(uri, headers, out -> out);
    }

    <T> T delete(String uri, Map<String, String> headers, Function<byte[], T> callback);

    default byte[] get(String uri, Map<String, String> headers) {
        return this.get(uri, headers, out -> out);
    }

    <T> T get(String uri, Map<String, String> headers, Function<byte[], T> callback);
}
