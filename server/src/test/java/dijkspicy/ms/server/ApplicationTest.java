package dijkspicy.ms.server;

import org.junit.Test;

import java.sql.*;
import java.util.StringJoiner;

/**
 * ApplicationTest
 *
 * @author dijkspicy
 * @date 2018/6/25
 */
public class ApplicationTest {

    @Test
    public void jdbc() throws ClassNotFoundException, SQLException {
        Class.forName(org.apache.calcite.avatica.remote.Driver.class.getName());

        Connection connection = DriverManager.getConnection("jdbc:avatica:remote:url=http://localhost:8443/ms/jdbc/model");

        String sql = "select * from DEPTS";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            StringJoiner sb = new StringJoiner("\t");
            for (int i = 0; i < columnCount; i++) {
                sb.add(metaData.getColumnName(i + 1));
            }
            System.out.println(sb);
            while (resultSet.next()) {
                sb = new StringJoiner("\t");
                for (int i = 1; i <= columnCount; i++) {
                    String newElement = String.valueOf(resultSet.getObject(i));
                    sb.add(newElement);
                }
                System.out.println(sb);
            }
        }
    }
}