package dijkspicy.ms.server;

import org.junit.Test;

import java.sql.*;
import java.util.Properties;
import java.util.StringJoiner;

/**
 * ApplicationTest
 *
 * @author dijkspicy
 * @date 2018/6/25
 */
public class ApplicationTest {

    @Test
    public void testSend() throws ClassNotFoundException, SQLException {
        Class.forName(org.apache.calcite.avatica.remote.Driver.class.getName());

        Connection connection = DriverManager.getConnection("jdbc:avatica:remote:url=http://localhost:8443/ms/jdbc/model");

        String sql = "select * from DEPTS";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            StringJoiner joiner = new StringJoiner("\r\n");
            while (resultSet.next()) {  //while控制行数
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {//for循环控制列数
                    sb.append(resultSet.getObject(i))
                            .append("\t");
                }
                joiner.add(sb.toString());
            }

            System.out.println("---------------------------------------------");
            System.out.println(joiner);
            System.out.println("---------------------------------------------");
        }
    }

    @Test
    public void testEnv() throws ClassNotFoundException, SQLException {
        Class.forName(org.apache.calcite.avatica.remote.Driver.class.getName());

        Properties info = new Properties();
        info.setProperty("truststore", "E:\\project\\github\\queeng\\.any\\ares\\trust.jks");
        info.setProperty("truststore_password", "Changeme_123");
        String url = "https://10.180.42.209:32018/rest/odae/v1/queryengine/aql/query";
        Connection connection = DriverManager.getConnection("jdbc:avatica:remote:url=" + url, info);

        String sql = "select *";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            System.out.println(resultSet);
        }
    }
}