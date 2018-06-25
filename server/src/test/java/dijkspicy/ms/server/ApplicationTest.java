package dijkspicy.ms.server;

import java.sql.*;
import java.util.Properties;

import dijkspicy.ms.server.proxy.http.HttpInvoker;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

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

        Connection connection = DriverManager.getConnection("jdbc:avatica:remote:url=http://localhost:8443/jdbc/aql1");

        String sql = "select *";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            System.out.println(resultSet);
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

    @Test
    public void rest() {
        byte[] invoker = new HttpInvoker(HttpClients.createDefault())
                .invoke("http://localhost:8443/ms/proxy/aql2")
                .post("{}".getBytes());
        System.out.println(new String(invoker));
    }

}