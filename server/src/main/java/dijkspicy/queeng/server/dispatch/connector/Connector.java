package dijkspicy.queeng.server.dispatch.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import dijkspicy.queeng.server.dispatch.ServiceException;
import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.remote.Driver;
import org.apache.calcite.avatica.remote.LocalService;
import org.apache.calcite.avatica.remote.Service;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Connector
 *
 * @author dijkspicy
 * @date 2018/6/11
 */
public enum Connector {
    /**
     * AQL Connector
     */
    AQL(it -> {
    }),
    /**
     * TQL Connector
     */
    TQL(it -> {
    });

    private static final Logger LOGGER = LoggerFactory.getLogger(Connector.class);
    private final Consumer<AvaticaConnection> register;
    private Service service;

    Connector(Consumer<AvaticaConnection> register) {
        this.register = register;
    }

    public final Service connect() throws ServiceException {
        return Optional.ofNullable(this.service)
                .orElse(this.service = this.connectNew());
    }

    private Service connectNew() throws ServiceException {
        String jdbcParam = "conformance=" + SqlConformanceEnum.LENIENT + ";fun=oracle";
        Properties info = new Properties();
        Connection temp;
        try {
            temp = DriverManager.getConnection("jdbc:calcite:" + jdbcParam, info);
        } catch (SQLException e) {
            throw new ServiceException("Failed to init " + this.name() + " jdbc", e);
        }

        if (!(temp instanceof AvaticaConnection) || !(temp instanceof CalciteConnection)) {
            try {
                temp.close();
            } catch (SQLException ignored) {
            }
            LOGGER.error("Need {} but found {} for {} jdbc", AvaticaConnection.class, temp.getClass(), this.name());
            throw new ServiceException("Failed to init " + this.name() + " jdbc");
        }

        this.register.accept((AvaticaConnection) temp);
        return new LocalService(new Driver().createMeta((AvaticaConnection) temp));
    }
}
