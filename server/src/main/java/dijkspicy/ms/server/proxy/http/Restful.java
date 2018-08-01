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
        return this.post(uri, request, headers, out -> out.response);
    }

    <T> T post(String uri, byte[] request, Map<String, String> headers, Function<RestfulClientCallback, T> callback);

    default byte[] put(String uri, byte[] request, Map<String, String> headers) {
        return this.put(uri, request, headers, out -> out.response);
    }

    <T> T put(String uri, byte[] request, Map<String, String> headers, Function<RestfulClientCallback, T> callback);

    default byte[] patch(String uri, byte[] request, Map<String, String> headers) {
        return this.patch(uri, request, headers, out -> out.response);
    }

    <T> T patch(String uri, byte[] request, Map<String, String> headers, Function<RestfulClientCallback, T> callback);

    default byte[] delete(String uri, Map<String, String> headers) {
        return this.delete(uri, headers, out -> out.response);
    }

    <T> T delete(String uri, Map<String, String> headers, Function<RestfulClientCallback, T> callback);

    default byte[] get(String uri, Map<String, String> headers) {
        return this.get(uri, headers, out -> out.response);
    }

    <T> T get(String uri, Map<String, String> headers, Function<RestfulClientCallback, T> callback);

    class RestfulClientCallback {
        public final String method;
        public final String url;
        public final Map<String, String> headers;
        public final byte[] response;

        protected RestfulClientCallback(String method, String url, Map<String, String> headers, byte[] response) {
            this.method = method;
            this.url = url;
            this.headers = headers;
            this.response = response;
        }
    }
}
