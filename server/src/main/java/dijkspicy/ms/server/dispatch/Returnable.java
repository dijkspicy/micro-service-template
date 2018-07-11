package dijkspicy.ms.server.dispatch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dijkspicy.ms.server.common.errors.InternalServerException;

import java.io.IOException;

/**
 * Returnable
 *
 * @author dijkspicy
 * @date 2018/6/18
 */
public interface Returnable {
    ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    Ret UNEXPECTED_OK = new Ret(200, 1, "OK");
    Ret OK = new Ret(200, 0, "OK");
    /**
     * client error
     */
    Ret BAD_REQUEST = new Ret(400, -1, "Bad Request");
    /**
     * server error
     */
    Ret INTERNAL_SERVER_ERROR = new Ret(500, -2, "Internal Server Error");
    Ret PROXY_ERROR = new Ret(500, 2001, "Proxy Error");
    Ret DAO_ERROR = new Ret(500, 2002, "DAO Error");
    Ret CONNECTION_ERROR = new Ret(500, 2003, "Connection Error");

    /**
     * serialize this to string, this object's field can't contains its self for cycling reference error
     *
     * @return serialized string
     */
    default String serialize() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (IOException e) {
            throw new InternalServerException("Can't serialize " + this.getClass() + " to string with mapper", e);
        }
    }

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
