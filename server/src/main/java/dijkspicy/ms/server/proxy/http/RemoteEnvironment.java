package dijkspicy.ms.server.proxy.http;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Vector;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableMap;
import com.jcraft.jsch.*;

/**
 * RemoteEnvironment
 *
 * @author dijkspicy
 * @date 2018/6/28
 */
public class RemoteEnvironment {
    private String host;
    private int port = 22;
    private String user = "root";
    private String pass = "";
    private String localParentDirectory = ".env";
    private Map<String, String> remoteDirectories = ImmutableMap.<String, String>builder()
            .put("/opt/oss/SOP/etc/ssl", "ssl")
            .put("/opt/oss/SOP/etc/cipher", "cipher")
            .build();

    public Map<String, String> getRemoteDirectories() {
        return remoteDirectories;
    }

    public RemoteEnvironment setRemoteDirectories(Map<String, String> remoteDirectories) {
        this.remoteDirectories = remoteDirectories;
        return this;
    }

    public String getLocalParentDirectory() {
        return localParentDirectory;
    }

    public RemoteEnvironment setLocalParentDirectory(String localParentDirectory) {
        this.localParentDirectory = localParentDirectory;
        return this;
    }

    public String getHost() {
        return host;
    }

    public RemoteEnvironment setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public RemoteEnvironment setPort(int port) {
        this.port = port;
        return this;
    }

    public String getUser() {
        return user;
    }

    public RemoteEnvironment setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPass() {
        return pass;
    }

    public RemoteEnvironment setPass(String pass) {
        this.pass = pass;
        return this;
    }

    public Path download() throws IOException {
        Path localDirectory = Paths.get(localParentDirectory, this.host).toAbsolutePath();
        if (Files.exists(localDirectory) && Files.list(localDirectory).findFirst().isPresent()) {
            return localDirectory;
        }

        Session session = null;
        try {
            JSch jSch = new JSch();
            session = jSch.getSession(this.user, this.host, this.port);
            session.setPassword(this.pass);

            Properties sessionConfig = new Properties();
            sessionConfig.setProperty("StrictHostKeyChecking", "no");
            session.setConfig(sessionConfig);
            session.connect();

            for (Map.Entry<String, String> entry : remoteDirectories.entrySet()) {
                String remote = entry.getKey();
                String local = entry.getValue();
                this.handleConnection(session, remote, local);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to download from: " + this + ", error: " + e.getMessage(), e);
        } finally {
            Optional.ofNullable(session).ifPresent(Session::disconnect);
        }
        return localDirectory;
    }

    @Override
    public String toString() {
        return this.user + ":" + this.pass + "@" + this.host + ":" + this.port;
    }

    private void handleConnection(Session session, String remote, String local) {
        Channel channel = null;
        try {
            channel = session.openChannel("sftp");
            if (channel instanceof ChannelSftp) {
                channel.connect();
                this.download((ChannelSftp) channel, remote, Paths.get(localParentDirectory, this.host, local));
            }
        } catch (JSchException | IOException | SftpException e) {
            throw new RuntimeException("Failed to download from: " + this + ", error: " + e.getMessage(), e);
        } finally {
            Optional.ofNullable(channel).ifPresent(Channel::disconnect);
        }
    }

    @SuppressWarnings("unchecked")
    private void download(ChannelSftp channel, String remote, Path local) throws SftpException, IOException {
        Vector<ChannelSftp.LsEntry> ls = channel.ls(remote);
        for (ChannelSftp.LsEntry it : ls) {
            String filename = it.getFilename();
            if (".".equals(filename) || "..".equals(filename)) {
                continue;
            }

            Path localFile = local.resolve(filename);
            String remoteFile = remote + "/" + filename;

            if (it.getAttrs().isDir()) {
                if (Files.notExists(localFile)) {
                    Files.createDirectories(localFile);
                }
                this.download(channel, remoteFile, localFile);
            } else {
                Predicate<Path> remoteModified = path -> it.getAttrs().getMTime() > (path.toFile().lastModified() / (long) 1000);
                if (Files.notExists(localFile) || remoteModified.test(localFile)) {
                    if (Files.notExists(localFile.getParent())) {
                        Files.createDirectories(localFile.getParent());
                    }
                    channel.get(remoteFile, localFile.toAbsolutePath().toString());
                }
            }
        }
    }
}
