package dijkspicy.ms.server.proxy.http;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
    private List<String> remoteDirectories = Arrays.asList("/opt/oss/SOP/etc/ssl", "/opt/oss/SOP/etc/cipher");
    private String localParentDirectory = ".env";

    public List<String> getRemoteDirectories() {
        return remoteDirectories;
    }

    public RemoteEnvironment setRemoteDirectories(List<String> remoteDirectories) {
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

        remoteDirectories.forEach(remote -> {
            Session session = null;
            Channel channel = null;
            try {
                JSch jSch = new JSch();
                session = jSch.getSession(this.user, this.host, this.port);
                session.setPassword(this.pass);

                Properties sessionConfig = new Properties();
                sessionConfig.setProperty("StrictHostKeyChecking", "no");
                session.setConfig(sessionConfig);
                session.connect();

                channel = session.openChannel("sftp");
                if (channel instanceof ChannelSftp) {
                    channel.connect();
                    this.download((ChannelSftp) channel, remote, localDirectory);
                }
            } catch (JSchException | SftpException e) {
                throw new RuntimeException("Failed to download from: " + this + ", error: " + e.getMessage(), e);
            } finally {
                Optional.ofNullable(session).ifPresent(Session::disconnect);
                Optional.ofNullable(channel).ifPresent(Channel::disconnect);
            }
        });
        return localDirectory;
    }

    @Override
    public String toString() {
        return this.user + ":" + this.pass + "@" + this.host + ":" + this.port;
    }

    @SuppressWarnings("unchecked")
    private void download(ChannelSftp channel, String remote, Path local) throws SftpException {
        Vector<ChannelSftp.LsEntry> ls = channel.ls(remote);
        for (ChannelSftp.LsEntry it : ls) {
            String filename = it.getFilename();
            Path localFile = local.resolve(filename);
            String remoteFile = remote + "/" + filename;

            boolean remoteModified = it.getAttrs().getMTime() > (localFile.toFile().lastModified() / (long) 1000);
            if (!Files.exists(localFile) || remoteModified) {
                channel.get(remoteFile, localFile.toString());
            } else if (!".".equals(filename) && "..".equals(filename)) {
                this.download(channel, remoteFile, localFile);
            }
        }
    }
}
