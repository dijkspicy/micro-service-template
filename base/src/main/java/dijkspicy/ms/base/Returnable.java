package dijkspicy.ms.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Returnable
 *
 * @author dijkspicy
 * @date 2018/6/18
 */
public interface Returnable {
    ObjectMapper RET_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    Ret UNEXPECTED_OK = new Ret(200, 1, "OK");
    Ret OK = new Ret(200, 0, "OK");
    Ret BAD_REQUEST = new Ret(400, -1, "Bad Request");
    Ret INTERNAL_SERVER_ERROR = new Ret(500, -2, "Internal Server Error");
    Ret SYSTEM_SERVER_ERROR = new Ret(500, -3, "System Server Error");

    /**
     * 返回报文
     *
     * @return 数据
     */
    Object getData();

    /**
     * ret code
     *
     * @return ret code
     */
    int getStatus();

    /**
     * overview of this ret code
     *
     * @return ret info
     */
    String getMessage();

    final class Ret {
        public final int httpCode;
        public final int status;
        public final String message;

        public Ret(int httpCode, int status, String message) {
            this.httpCode = httpCode;
            this.status = status;
            this.message = message;
        }
    }
}
