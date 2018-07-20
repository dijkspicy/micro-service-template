package dijkspicy.ms.server.dispatch.connector;

import dijkspicy.ms.base.errors.InternalServerException;
import dijkspicy.ms.server.dispatch.BaseHandler;
import dijkspicy.ms.server.dispatch.ServiceException;
import dijkspicy.ms.server.dispatch.ServiceResponse;
import org.apache.calcite.avatica.remote.JsonService;
import org.apache.calcite.avatica.remote.LocalJsonService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * ConnectorHandler
 *
 * @author dijkspicy
 * @date 2018/6/6
 */
public class ConnectorHandler extends BaseHandler {
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
    protected void doPre(HttpServletRequest httpServletRequest) throws ServiceException {
        try (InputStream is = httpServletRequest.getInputStream()) {
            this.data = IOUtils.toString(is, UTF_8);
        } catch (IOException e) {
            throw new InternalServerException("Failed to load request body", e);
        }

        if (StringUtils.isBlank(this.type)) {
            throw new InternalServerException("Connector type can't be null or empty");
        }

        if (StringUtils.isBlank(this.data)) {
            throw new InternalServerException("Connector data can't be null or empty");
        }
    }

    @Override
    protected ServiceResponse doMainLogic(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        try {
            JsonService service = new LocalJsonService(MockConnector.TQL.connector.getService());
            return new ServiceResponse(service.apply(this.data));
        } catch (Exception e) {
            throw new InternalServerException("Failed to accept service: " + e.getMessage(), e);
        }
    }

    private enum MockConnector {
        TQL;

        public final Connector connector;

        MockConnector() {
            Properties info = new Properties();
            info.setProperty("model", "server/src/main/data/model.json");
            connector = new Connector(info);
        }
    }
}