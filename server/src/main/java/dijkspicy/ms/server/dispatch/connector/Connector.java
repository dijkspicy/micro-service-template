package dijkspicy.ms.server.dispatch.connector;

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
import java.util.Properties;

import static org.apache.calcite.jdbc.Driver.CONNECT_STRING_PREFIX;


/**
 * Connector
 *
 * @author dijkspicy
 * @date 2018/6/11
 */
public class Connector {
    private static final Logger LOGGER = LoggerFactory.getLogger(Connector.class);
    private static final Field META_FIELD;

    static {
        LOGGER.warn("Register driver: " + new Driver());
        try {
            META_FIELD = AvaticaConnection.class.getDeclaredField("meta");
            META_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ConnectorException("No 'meta' field declared in " + AvaticaConnection.class, e);
        }
    }

    private final Connection connection;
    private final SchemaPlus rootSchema;
    private final Service service;

    public Connector() {
        this(new Properties());
    }

    public Connector(Properties properties) {
        try {
            this.connection = DriverManager.getConnection(CONNECT_STRING_PREFIX, properties);
        } catch (SQLException e) {
            throw new ConnectorException("Failed to get connection of " + CONNECT_STRING_PREFIX, e);
        }

        if (this.connection instanceof AvaticaConnection && this.connection instanceof CalciteConnection) {
            Meta meta = this.createMeta((AvaticaConnection) this.connection);
            this.service = new LocalService(meta);
            this.rootSchema = ((CalciteConnection) this.connection).getRootSchema();
        } else {
            try {
                this.connection.close();
            } catch (SQLException ignored) {
            }

            throw new ConnectorException("Got invalid connection: " + this.connection.getClass());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public SchemaPlus getRootSchema() {
        return rootSchema;
    }

    public Service getService() {
        return service;
    }

    private Meta createMeta(AvaticaConnection connection) {
        try {
            return (Meta) META_FIELD.get(connection);
        } catch (IllegalAccessException e) {
            throw new ConnectorException("Failed to get meta from connection: " + connection, e);
        }
    }
}
