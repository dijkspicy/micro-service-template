package dijkspicy.queeng.server.dispatch;

/**
 * Returnable
 *
 * @author dijkspicy
 * @date 2018/6/18
 */
public interface Returnable {
    Ret UNEXPECTED_OK = new Ret(200, 1, "OK");
    Ret OK = new Ret(200, 0, "OK");
    Ret BAD_REQUEST = new Ret(400, -1, "Bad Request");
    Ret INTERNAL_SERVER_ERROR = new Ret(500, -2, "Internal Server Error");
    Ret PROXY_ERROR = new Ret(500, 2003, "Proxy Error");

    /**
     * ret code
     *
     * @return ret code
     */
    int getRetCode();

    /**
     * overview of this ret code
     *
     * @return ret info
     */
    String getRetInfo();

    final class Ret {
        final int httpCode;
        final int retCode;
        final String retInfo;

        Ret(int httpCode, int retCode, String retInfo) {
            this.httpCode = httpCode;
            this.retCode = retCode;
            this.retInfo = retInfo;
        }
    }
}
