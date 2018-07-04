package dijkspicy.ms.server.dispatch.connector;

import dijkspicy.ms.server.dispatch.BaseHandler;
import dijkspicy.ms.server.dispatch.HttpContext;
import dijkspicy.ms.server.dispatch.ServiceException;
import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.remote.JsonService;
import org.apache.calcite.avatica.remote.LocalService;
import org.apache.calcite.avatica.remote.Service;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

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
        AvaticaConnection connection = (AvaticaConnection) Connector.SOLE.get(this.type);

        try (PreparedStatement statement = connection.prepareStatement("select * from DEPTS")) {
            ResultSet resultSet = statement.executeQuery();
            try {
                final ResultSetMetaData metaData = resultSet.getMetaData();
                final int columnCount = metaData.getColumnCount();
                PrintStream out = System.out;
                while (resultSet.next()) {
                    for (int i = 1; ; i++) {
                        out.print(resultSet.getString(i));
                        if (i < columnCount) {
                            out.print(", ");
                        } else {
                            out.println();
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            Field field = AvaticaConnection.class.getDeclaredField("meta");
            field.setAccessible(true);
            Meta meta = (Meta) field.get(connection);
            Service service = new LocalService(meta);
            Service.Request request = JsonService.MAPPER.readValue(this.data, Service.Request.class);
            Object response = ACCEPT_METHOD.invoke(request, service);
            return JsonService.MAPPER.writeValueAsString(response);
        } catch (IOException e) {
            throw new ServiceException("Got an illegal request message", e);
        } catch (Exception e) {
            throw new ServiceException("Failed to accept service: " + e.getMessage(), e);
        }
    }
}