package dijkspicy.ms.server.proxy.restful;

import com.jcraft.jsch.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.Vector;

/**
 * EasyRestfulSafeClientBuilder
 *
 * @author dijkspicy
 * @date 2018/6/27
 */
public class AutoSyncSSLClientBuilder extends SSLClientBuilder {
    private String envHost;
    private int envPort;
    private String envUsername;
    private String envPassword;

    public AutoSyncSSLClientBuilder setEnv(String envHost, int envPort, String envUsername, String envPassword) {
        this.envHost = envHost;
        this.envPort = envPort;
        this.envUsername = envUsername;
        this.envPassword = envPassword;
        return this;
    }

    public AutoSyncSSLClientBuilder setEnv(String envHost, String envUsername, String envPassword) {
        return this.setEnv(envHost, 22, envUsername, envPassword);
    }

    public AutoSyncSSLClientBuilder setEnv(String envHost, int envPort, String envPassword) {
        return this.setEnv(envHost, envPort, "root", envPassword);
    }

    public AutoSyncSSLClientBuilder setEnv(String envHost, String envPassword) {
        return this.setEnv(envHost, 22, "root", envPassword);
    }

    public AutoSyncSSLClientBuilder setEnv(String envHost) {
        return this.setEnv(envHost, "");
    }

    public Path download(String remote, String local) {
        if (this.envHost == null) {
            this.envHost = this.initFromURI(this.uri);
        }

        Path path = Paths.get(local, this.envHost);
        if (Files.exists(path)) {
            return path;
        }

        Session session = null;
        Channel channel = null;
        try {
            JSch jSch = new JSch();
            session = jSch.getSession(this.envUsername, this.envHost, this.envPort);
            session.setPassword(this.envPassword);

            Properties sessionConfig = new Properties();
            sessionConfig.setProperty("StrictHostKeyChecking", "no");
            session.setConfig(sessionConfig);
            session.connect();

            channel = session.openChannel("sftp");
            if (channel instanceof ChannelSftp) {
                channel.connect();
                this.download((ChannelSftp) channel, remote, local);
            }
        } catch (JSchException | SftpException e) {
            throw new RestfulException("Failed to download from: " + this.envHost + ", error: " + e.getMessage(), e);
        } finally {
            Optional.ofNullable(session).ifPresent(Session::disconnect);
            Optional.ofNullable(channel).ifPresent(Channel::disconnect);
        }
        return path;
    }

    @SuppressWarnings("unchecked")
    private void download(ChannelSftp channel, String remote, String local) throws SftpException {
        Vector<ChannelSftp.LsEntry> ls = channel.ls(remote);
        for (ChannelSftp.LsEntry it : ls) {
            String filename = it.getFilename();
            Path localFile = Paths.get(local, filename);
            String remoteFile = remote + "/" + filename;

            boolean remoteModified = it.getAttrs().getMTime() > (localFile.toFile().lastModified() / (long) 1000);
            if (!Files.exists(localFile) || remoteModified) {
                channel.get(remoteFile, localFile.toString());
            } else if (!".".equals(filename) && "..".equals(filename)) {
                this.download(channel, remoteFile, localFile.toString());
            }
        }
    }

    private String initFromURI(String uri) {
        try {
            return new URL(uri).getHost();
        } catch (MalformedURLException e) {
            throw new RestfulException("No host name to sync certificate, error: " + e.getMessage(), e);
        }
    }
}
