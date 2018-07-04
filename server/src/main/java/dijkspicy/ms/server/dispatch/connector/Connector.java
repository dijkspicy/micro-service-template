package dijkspicy.ms.server.dispatch.connector;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Connector
 *
 * @author dijkspicy
 * @date 2018/6/11
 */
public enum Connector {
    SOLE;

    private final Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public Connection get(String model) {
        return this.connectionMap.computeIfAbsent(model, k -> {
            String json = "server/src/main/data/" + k + ".json";
            Properties info = new Properties();
            info.setProperty("model", this.checkModel(json));
            try {
                return DriverManager.getConnection("jdbc:calcite:", info);
            } catch (SQLException e) {
                return null;
            }
        });
    }

    private String checkModel(String model) {
        return Paths.get(model).toAbsolutePath().toString();
    }
}
