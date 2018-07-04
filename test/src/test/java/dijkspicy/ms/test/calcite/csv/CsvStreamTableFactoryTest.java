package dijkspicy.ms.test.calcite.csv;

import org.apache.calcite.jdbc.Driver;
import org.junit.Test;

import java.io.PrintStream;
import java.sql.*;
import java.util.Properties;
import java.util.function.Function;

/**
 * coding-demo
 *
 * @author dijkspicy
 * @date 2018/5/31
 */
public class CsvStreamTableFactoryTest {

    private Function<ResultSet, Object> function = resultSet -> {
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
    };

    @Test
    public void testCreate() throws Exception {
        Class.forName(Driver.class.getName());

        Properties info = new Properties();
        info.setProperty("schema.directory", "/Users/tangguoliang/coding/coding-demo/example/csv/src/test/resources/sales");
        String sql = "select * from DEPTS";
        try (Connection connection = DriverManager.getConnection("jdbc:calcite:schemaFactory=org.apache.calcite.adapter.csv.CsvSchemaFactory", info);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery();) {
            function.apply(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}