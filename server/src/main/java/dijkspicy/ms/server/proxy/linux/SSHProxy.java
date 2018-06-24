package dijkspicy.ms.server.proxy.linux;


import java.io.IOException;
import java.io.InputStream;

/**
 * Created by t00321127 on 2015/12/11.
 * adf
 */
public class SSHProxy {
    private String ip;
    private String user;
    private String password;
    private int port;

    private String ipMsg;

    private SSHProxy() {

    }

    public static SSHProxy newInstance(String ip, int port, String user, String password) {
        SSHProxy ins = new SSHProxy();
        ins.ip = ip;
        ins.port = port;
        ins.user = user;
        ins.password = password;
        ins.ipMsg = "ssh > " + user + "@" + ip + ":" + port;
        return ins;
    }

    public void connect() throws IOException {
    }

    public void disConnect() {
    }

    public String runCommand(String command) throws IOException {
        return command;
    }

    private void print(String msg) {
    }

    /**
     * 解析脚本执行返回的结果集
     *
     * @param in 输入流对象
     * @return 以纯文本的格式返回
     */
    private String processStdout(InputStream in) {
        return null;
    }
}
