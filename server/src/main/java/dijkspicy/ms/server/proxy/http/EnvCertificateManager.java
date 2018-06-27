package dijkspicy.ms.server.proxy.http;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.*;

/**
 * EnvCertificateManager
 *
 * @author dijkspicy
 * @date 2018/6/27
 */
public class EnvCertificateManager {

    private final EnvInfo envInfo;

    public EnvCertificateManager(EnvInfo envInfo) {
        this.envInfo = envInfo;
    }

    public String download(String remote) {
        return this.download(remote, ".certificate/");
    }

    public String download(String remote, String local) {
        Session session = null;
        Channel channel = null;
        try {
            JSch jSch = new JSch();
            session = jSch.getSession(envInfo.getUser(), envInfo.getHost(), envInfo.getPort());
            session.setPassword(envInfo.getPassword());

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
            throw new RuntimeException("Failed to download from: " + this.envInfo + ", error: " + e.getMessage(), e);
        } finally {
            Optional.ofNullable(session).ifPresent(Session::disconnect);
            Optional.ofNullable(channel).ifPresent(Channel::disconnect);
        }
        return Paths.get(local).toAbsolutePath().toString();
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
}
