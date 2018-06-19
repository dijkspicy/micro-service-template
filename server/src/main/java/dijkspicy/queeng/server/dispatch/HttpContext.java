package dijkspicy.queeng.server.dispatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HttpContext
 *
 * @author dijkspicy
 * @date 2018/6/6
 */
public final class HttpContext {
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public HttpContext(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public HttpServletRequest getHttpServletRequest() {
        return request;
    }

    public void setResponseStatus(int httpCode) {
        this.response.setStatus(httpCode);
    }
}
