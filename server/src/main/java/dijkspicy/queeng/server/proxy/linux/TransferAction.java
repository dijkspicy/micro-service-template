package dijkspicy.queeng.server.proxy.linux;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

/**
 * Created by t00321127 on 2015/12/11.
 * fff
 */
public class TransferAction {
    private Map<String, LocalFile> source2file = new HashMap<>();

    private List<LocalFile> local;
    private List<Remote> upload;

    public static TransferAction fromConfig(String configPath) throws Exception {
        final String content = readConfig(configPath);
        if (configPath.endsWith("yaml") || configPath.endsWith("yml")) {
            return StringSerializer.forYaml.deserialize(content, TransferAction.class);
        }
        return StringSerializer.forShow.deserialize(content, TransferAction.class);
    }

    public void transfer() {
        for (Remote remote : this.upload) {
            String ip = remote.getIp();
            int port = remote.getPort();
            String user = remote.getUser();
            String password = remote.getPassword();

            if (!remote.isExecutable()) {
                print("\n------ " + user + "@" + ip + ":" + port + " ------ skipped");
                continue;
            }

            print("\n------ " + user + "@" + ip + ":" + port + " ------");
            SFTPProxy sftpProxy = null;
            SSHProxy sshProxy = null;
            try {
                List<Transfer> transfers = remote.getTransfer();
                List<String> commands = remote.getCommands();
                boolean doTransfer = !transfers.isEmpty();
                boolean doCommand = !commands.isEmpty();

                if (!doTransfer && !doCommand) {
                    print("No transfer or commands");
                    continue;
                }

                (sshProxy = SSHProxy.newInstance(ip, port, user, password)).connect();
                if (doTransfer) {
                    (sftpProxy = SFTPProxy.newInstance(ip, port, user, password, sshProxy)).connect();
                    for (Transfer transfer : transfers) {
                        String source = transfer.getSource();
                        if (!this.source2file.containsKey(source)) {
                            print("No such file..." + source + ", do not transfer.");
                            continue;
                        }

                        sftpProxy.transfer(this.source2file.get(source), transfer);
                    }
                }

                if (doCommand) {
                    for (String command : commands) {
                        String result = sshProxy.runCommand(command);
                        print(result);
                    }
                }
            } catch (SftpException e) {
                print("Sftp transfer failed...", e);
                e.printStackTrace();
            } catch (IOException e) {
                print("Read local file failed...", e);
                e.printStackTrace();
            } catch (JSchException e) {
                print("Sftp connect to " + remote.getIp() + " failed...", e);
                e.printStackTrace();
            } finally {
                if (sftpProxy != null) {
                    sftpProxy.disConnect();
                }

                if (sshProxy != null) {
                    sshProxy.disConnect();
                }
            }
        }
    }

    private static void print(String s) {
    }

    private static void print(String s, Exception e) {
        print(s);
    }

    private static String readConfig(String configPath) throws IOException {
        Path current = new File(configPath).getAbsoluteFile().toPath();
        print("Read config file from " + current.toString());
        return new String(Files.readAllBytes(current), StandardCharsets.UTF_8);
    }

    public List<Remote> getUpload() {
        return upload;
    }

    public void setUpload(List<Remote> upload) {
        this.upload = upload;
    }

    public List<LocalFile> getLocal() {
        return local;
    }

    public void setLocal(List<LocalFile> local) {
        this.local = local;
        for (LocalFile localFile : local) {
            this.source2file.put(localFile.getName(), localFile);
        }
    }

    public static void main(String[] args) throws Exception {
        TransferAction out = TransferAction.fromConfig("E:\\code\\dijkspicy\\SftpLord\\transfer.yaml");
        System.out.println(StringSerializer.forShow.serialize(out));
    }
}
