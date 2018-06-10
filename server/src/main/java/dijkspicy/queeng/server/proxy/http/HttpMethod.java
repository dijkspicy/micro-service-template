package dijkspicy.queeng.server.proxy.http;

import org.apache.http.client.methods.*;

/**
 * queeng
 *
 * @author dijkspicy
 * @date 2018/5/28
 */
public enum HttpMethod {
    POST(HttpPost.METHOD_NAME),
    PUT(HttpPut.METHOD_NAME),
    DELETE(HttpDelete.METHOD_NAME),
    GET(HttpGet.METHOD_NAME),
    PATCH(HttpPatch.METHOD_NAME);

    private final String methodName;

    HttpMethod(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }
}
