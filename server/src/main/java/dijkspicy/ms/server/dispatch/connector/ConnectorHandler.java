package dijkspicy.ms.server.dispatch.connector;

import dijkspicy.ms.server.dispatch.BaseHandler;
import dijkspicy.ms.server.dispatch.HttpContext;
import dijkspicy.ms.server.dispatch.ServiceException;
import org.apache.calcite.avatica.remote.LocalJsonService;
import org.apache.calcite.avatica.remote.Service;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * ConnectorHandler
 *
 * @author dijkspicy
 * @date 2018/6/6
 */
public class ConnectorHandler extends BaseHandler<String> {
    private static final Method ACCEPT_METHOD;

    static {
        Method temp = null;
        try {
            temp = Service.Request.class.getDeclaredMethod("accept", Service.class);
            temp.setAccessible(true);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Failed to find accept method for request");
        }
        ACCEPT_METHOD = temp;
    }

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
        try {
            LocalJsonService service = new LocalJsonService(MockConnector.TQL.connector.getService());
            return service.apply(this.data);
        } catch (Exception e) {
            throw new ServiceException("Failed to accept service: " + e.getMessage(), e);
        }
    }

    private enum MockConnector {
        TQL;

        public final CalciteConnector connector;

        MockConnector() {
            Properties info = new Properties();
            info.setProperty("model", "server/src/main/data/model.json");
            connector = new CalciteConnector(info);
        }
    }
}