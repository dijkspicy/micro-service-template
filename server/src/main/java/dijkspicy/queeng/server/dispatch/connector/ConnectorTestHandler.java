package dijkspicy.queeng.server.dispatch.connector;

import dijkspicy.queeng.server.dispatch.BaseHandler;
import dijkspicy.queeng.server.dispatch.HttpContext;
import dijkspicy.queeng.server.dispatch.ServiceException;
import dijkspicy.queeng.server.dispatch.ServiceResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * queeng
 *
 * @author dijkspicy
 * @date 2018/6/18
 */
public class ConnectorTestHandler extends BaseHandler {
    private final String type;
    private String data;

    public ConnectorTestHandler(String type) {
        this.type = type;
    }

    @Override
    protected void doPre(HttpContext context) throws ServiceException {
        try (InputStream is = context.getHttpServletRequest().getInputStream()) {
            this.data = IOUtils.toString(is, UTF_8);
        } catch (IOException e) {
            throw new ServiceException("Failed to load request body", e);
        }

        if (StringUtils.isBlank(this.type)) {
            throw new ServiceException("Connector type can't be null or empty");
        }

        if (StringUtils.isBlank(this.data)) {
            throw new ServiceException("Connector data can't be null or empty");
        }
    }

    @Override
    protected Object doMainLogic(HttpContext context) throws ServiceException {
        switch (this.type.toUpperCase(Locale.ENGLISH)) {
            case "AQL":
                break;
            case "TQL":
                break;
            default:
                throw new ServiceException("Only support AQL/TQL connector type: " + this.type);
        }
        return new ServiceResponse();
    }
}
