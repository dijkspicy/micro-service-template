package dijkspicy.ms.server.proxy.http;

import java.util.Objects;

/**
 * EnvironmentConfig
 *
 * @author dijkspicy
 * @date 2018/6/29
 */
public class EnvironmentConfig {
    private String host;
    private int port = 22;
    private String user = "root";
    private String pass = "";
    private String tenant = "SOP";

    public String getHost() {
        return host;
    }

    public EnvironmentConfig setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public EnvironmentConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public String getUser() {
        return user;
    }

    public EnvironmentConfig setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPass() {
        return pass;
    }

    public EnvironmentConfig setPass(String pass) {
        this.pass = pass;
        return this;
    }

    public String getTenant() {
        return tenant;
    }

    public EnvironmentConfig setTenant(String tenant) {
        this.tenant = tenant;
        return this;
    }

    @Override
    public int hashCode() {

        return Objects.hash(host, port, user, pass, tenant);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EnvironmentConfig that = (EnvironmentConfig) o;
        return port == that.port &&
                Objects.equals(host, that.host) &&
                Objects.equals(user, that.user) &&
                Objects.equals(pass, that.pass) &&
                Objects.equals(tenant, that.tenant);
    }

    @Override
    public String toString() {
        return this.user + ":" + this.pass + "@" + this.host + ":" + this.port + "/opt/oss/" + this.tenant;
    }
}
