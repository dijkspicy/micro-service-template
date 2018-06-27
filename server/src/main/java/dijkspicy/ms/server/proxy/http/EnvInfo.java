package dijkspicy.ms.server.proxy.http;

/**
 * EnvInfo
 *
 * @author dijkspicy
 * @date 2018/6/27
 */
public class EnvInfo {
    private String host;
    private int port;
    private String user;
    private String password;

    public String getHost() {
        return host;
    }

    public EnvInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public EnvInfo setPort(int port) {
        this.port = port;
        return this;
    }

    public String getUser() {
        return user;
    }

    public EnvInfo setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public EnvInfo setPassword(String password) {
        this.password = password;
        return this;
    }
}
