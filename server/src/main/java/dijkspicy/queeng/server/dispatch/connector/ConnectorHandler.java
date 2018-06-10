package dijkspicy.queeng.server.dispatch.connector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dijkspicy.queeng.server.dispatch.BaseHandler;
import dijkspicy.queeng.server.dispatch.HttpContext;
import dijkspicy.queeng.server.dispatch.ServiceException;
import org.apache.calcite.avatica.metrics.noop.NoopMetricsSystem;
import org.apache.calcite.avatica.remote.Handler;
import org.apache.calcite.avatica.remote.JsonHandler;
import org.apache.calcite.avatica.remote.Service;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * queeng
 *
 * @author dijkspicy
 * @date 2018/6/3
 */
public class ConnectorHandler extends BaseHandler<String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final String type;
    private String data;

    public ConnectorHandler(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "JDBC Connector of " + this.type + " with " + this.data;
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
    protected String doMainLogic(HttpContext context) throws ServiceException {
        Service service;
        switch (this.type.toUpperCase(Locale.ENGLISH)) {
            case "AQL":
                service = new ConnectorAQLService();
                break;
            case "TQL":
            default:
                throw new ServiceException("Only support AQL/TQL connector type: " + this.type);
        }

        JsonHandler jsonHandler = new JsonHandler(service, NoopMetricsSystem.getInstance());
        Handler.HandlerResponse<String> response = jsonHandler.apply(this.data);
        try {
            return MAPPER.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new ServiceException("Failed to convert response to json string", e);
        }
    }
}
