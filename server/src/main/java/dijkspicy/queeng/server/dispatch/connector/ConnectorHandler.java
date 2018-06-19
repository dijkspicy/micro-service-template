package dijkspicy.queeng.server.dispatch.connector;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import dijkspicy.queeng.server.dispatch.BaseHandler;
import dijkspicy.queeng.server.dispatch.HttpContext;
import dijkspicy.queeng.server.dispatch.ServiceException;
import org.apache.calcite.avatica.remote.JsonService;
import org.apache.calcite.avatica.remote.Service;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

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
        Connector connector;
        switch (this.type.toUpperCase(Locale.ENGLISH)) {
            case "AQL":
                connector = Connector.AQL;
                break;
            case "TQL":
                connector = Connector.TQL;
                break;
            default:
                throw new ServiceException("Only support AQL/TQL connector type: " + this.type);
        }

        Service service = connector.connect();
        return this.apply(service);
    }

    private String apply(Service service) throws ServiceException {
        try {
            Service.Request request = JsonService.MAPPER.readValue(this.data, Service.Request.class);
            Object response = ACCEPT_METHOD.invoke(request, service);
            return JsonService.MAPPER.writeValueAsString(response);
        } catch (IOException e) {
            throw new ServiceException("Got an illegal request message", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ServiceException("Failed to accept service: " + e.getMessage(), e);
        }
    }
}