package dijkspicy.ms.test.calcite.csv;

import java.io.PrintStream;
import java.sql.*;
import java.util.Properties;
import java.util.function.Function;

/**
 * coding-demo
 *
 * @author dijkspicy
 * @date 2018/5/20
 */
public class Connector {
    private final String model;
    private Connection connection;

    public Connector(String model) {
        this.model = model;
    }

    public static void main(String[] args) throws SQLException {
        Connector connector = new Connector("/Users/tangguoliang/coding/coding-demo/example/csv/src/test/resources/model.json");
        connector.execute("select * from DEPTS", resultSet -> {
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
            return null;
        });
    }

    public boolean connected() {
        if (connection != null) {
            return true;
        }
        try {
            Properties info = new Properties();
            info.setProperty("model", this.checkModel(model));
            connection = DriverManager.getConnection("jdbc:calcite:", info);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public <T> T execute(String sql, Function<ResultSet, T> function) {
        if (this.connected()) {
            try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
                ResultSet resultSet = statement.executeQuery();
                return function.apply(resultSet);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String checkModel(String model) {
        return model;
    }
}
