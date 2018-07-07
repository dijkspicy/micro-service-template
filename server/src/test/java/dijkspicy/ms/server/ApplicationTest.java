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

        String sql = "select count(*) from SDEPTS";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {  //while控制行数
                StringJoiner sb = new StringJoiner("\t");
                for (int i = 1; i <= columnCount; i++) {//for循环控制列数
                    sb.add(String.valueOf(resultSet.getObject(i)));
                }
                System.out.println(sb);
            }
        }
    }
}