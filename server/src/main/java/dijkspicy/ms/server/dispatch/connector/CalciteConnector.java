package dijkspicy.ms.server.dispatch.connector;

import dijkspicy.ms.server.dispatch.ServiceException;
import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.remote.LocalService;
import org.apache.calcite.avatica.remote.Service;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.Driver;
import org.apache.calcite.schema.SchemaPlus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

import static org.apache.calcite.jdbc.Driver.CONNECT_STRING_PREFIX;


/**
 * Connector
 *
 * @author dijkspicy
 * @date 2018/6/11
 */
public class CalciteConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalciteConnector.class);
    private static Field metaField;

    static {
        LOGGER.warn("Register driver: " + new Driver());
    }

    private final Connection connection;
    private final Meta meta;
    private final SchemaPlus rootSchema;
    private final Service service;

    public CalciteConnector() {
        this(new Properties());
    }

    public CalciteConnector(Properties properties) {
        try {
            this.connection = DriverManager.getConnection(CONNECT_STRING_PREFIX, properties);
        } catch (SQLException e) {
            throw new ServiceException("Failed to get connection of " + CONNECT_STRING_PREFIX, e);
        }

        if (this.connection instanceof AvaticaConnection && this.connection instanceof CalciteConnection) {
            this.meta = this.createMeta((AvaticaConnection) this.connection);
            this.service = new LocalService(this.meta);
            this.rootSchema = ((CalciteConnection) this.connection).getRootSchema();
        } else {
            try {
                this.connection.close();
            } catch (SQLException ignored) {
            }

            throw new ServiceException("Got invalid connection: " + this.connection.getClass());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public Meta getMeta() {
        return meta;
    }

    public SchemaPlus getRootSchema() {
        return rootSchema;
    }

    public Service getService() {
        return service;
    }

    private Meta createMeta(AvaticaConnection connection) {
        synchronized (CalciteConnector.this) {
            Optional.ofNullable(metaField)
                    .orElseGet(() -> {
                        try {
                            metaField = AvaticaConnection.class.getDeclaredField("meta");
                            metaField.setAccessible(true);
                            return metaField;
                        } catch (NoSuchFieldException e) {
                            throw new ServiceException("No 'meta' field declared in " + AvaticaConnection.class, e);
                        }
                    });
        }
        try {
            return (Meta) metaField.get(connection);
        } catch (IllegalAccessException e) {
            throw new ServiceException("Failed to get meta from connection: " + connection, e);
        }
    }
}
