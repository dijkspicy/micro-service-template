package dijkspicy.ms.server.proxy.linux;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcraft.jsch.*;

/**
 * Created by t00321127 on 2015/12/11.
 * ftp
 */
public class SFTPProxy {
    private final String password;
    private final String user;
    private final int port;
    private final String ip;
    private final SSHProxy sshProxy;
    private final Pattern NUMBER_PATTERN = Pattern.compile("\\d*");

    private ChannelSftp sftp = null;
    private boolean connected;
    private String ipMsg;

    private SFTPProxy(String ip, int port, String user, String password, SSHProxy sshProxy) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
        this.sshProxy = sshProxy;

        this.ipMsg = "sftp> " + user + "@" + ip + ":" + port;
    }

    public static SFTPProxy newInstance(String ip, int port, String user, String password, SSHProxy sshProxy) {
        return new SFTPProxy(ip, port, user, password, sshProxy);
    }

    public void connect() throws JSchException {
        if (this.connected) {
            print("Sftp is connected...");
            return;
        }

        Session sshSession = new JSch().getSession(user, ip, port);
        sshSession.setPassword(password);
        sshSession.setConfig(new Properties() {
            private static final long serialVersionUID = 1819789025322145901L;

            {
                put("StrictHostKeyChecking", "no");
            }
        });
        sshSession.connect();
        print("Session connected...");

        this.sftp = (ChannelSftp) sshSession.openChannel("sftp");
        this.sftp.connect();
        print("Sftp channel opened...");

        this.connected = true;
    }

    public void disConnect() {
        if (this.connected) {
            if (this.sftp != null) {
                if (this.sftp.isConnected()) {
                    this.sftp.disconnect();
                    print("Sftp disconnected...");
                } else if (this.sftp.isClosed()) {
                    print("Sftp is closed already...");
                }
            }
        }
        this.connected = false;
    }

    public void transfer(LocalFile local, Transfer remote) throws IOException, SftpException {
        remote.setUid(this.idCommandResult("/usr/bin/id -u ossuser"));
        remote.setGid(this.idCommandResult("/usr/bin/id -g ossuser"));
        Path localPath = Paths.get(local.getPath());

        if (Files.isDirectory(localPath)) {
            transferDirectory(localPath, localPath, remote);
        } else {
            transferOneFile(localPath, remote);
        }
    }

    private static String getNewDestination(Path originalPath, Path currentPath, String oldDestination) {
        Stack<String> subs = new Stack<>();
        while (!currentPath.equals(originalPath)) {
            String cu = currentPath.getFileName().toString();
            subs.push(cu);

            currentPath = currentPath.getParent();
        }

        String[] paths = new String[subs.size()];
        int index = 0;
        while (!subs.isEmpty()) {
            paths[index] = subs.pop();
            index++;
        }

        return Paths.get(oldDestination, paths).normalize().toString();
    }

    private void print(String msg) {
    }

    private int idCommandResult(String command) {
        String out = null;
        try {
            out = sshProxy.runCommand(command);
            Matcher matcher = NUMBER_PATTERN.matcher(out);
            if (matcher.find()) {
                out = matcher.group();
            }
            return Integer.valueOf(out);
        } catch (IOException e) {
            print(command + " run failed: " + e.getMessage());
        } catch (NumberFormatException e) {
            print(command + " output: " + out + ": " + e.getMessage());
        }
        return 0;
    }

    private void transferDirectory(Path localPath, Path currentPath, Transfer remote) throws IOException, SftpException {
        DirectoryStream<Path> files = null;
        try {
            files = Files.newDirectoryStream(currentPath);
            for (Path file : files) {
                if (Files.isDirectory(file)) {
                    transferDirectory(localPath, file, remote);
                } else {
                    String oldDes = remote.getDestination();
                    String currentDes = getNewDestination(localPath, file.getParent(), oldDes);
                    remote.setDestination(currentDes);
                    transferOneFile(file, remote);

                    remote.setDestination(oldDes);
                }
            }
        } finally {
            if (files != null) {
                files.close();
            }
        }
    }

    private void createDir(Path destination, int chown, int chgrp) throws SftpException {
        if (!this.isExists(destination)) {
            Path parent = destination.getParent();
            this.createDir(parent, chown, chgrp);

            String des = destination.toString().replace("\\", "/");
            this.sftp.mkdir(des);
            this.sftp.chown(chown, des);
            this.sftp.chgrp(chgrp, des);
            print("Create remote dir: " + des);
        }
    }

    private boolean isExists(Path destination) {
        try {
            this.sftp.cd(destination.toString().replace("\\", "/"));
            return true;
        } catch (SftpException e) {
            return false;
        }
    }

    private void transferOneFile(Path localPath, Transfer remote) throws SftpException, IOException {
        int chmod = remote.getPer();
        int chown = remote.getUid();
        int chgrp = remote.getGid();

        Path fileName = localPath.getFileName();
        String extension = this.getExtension(fileName.toString());
        if (remote.getDestination().endsWith("." + extension)) {
            String dir = new File(remote.getDestination()).getParent();
            this.createDir(Paths.get(dir), chown, chgrp);

            if (this.sftp.ls(remote.getDestination()).isEmpty()) {
                if (remote.getDestination().contains("*")) {
                    throw new IOException("no such file matches " + remote.getDestination());
                }
            }
        } else {
            this.createDir(Paths.get(remote.getDestination()), chown, chgrp);
            remote.setDestination(remote.getDestination() + "/" + fileName);
        }

        String remoteFile = remote.getDestination().replace("\\", "/");
        this.sftp.put(Files.newInputStream(localPath), remoteFile);
        print("Send " + localPath + " to " + remoteFile);

        this.sftp.chmod(chmod, remoteFile);
        this.sftp.chown(chown, remoteFile);
        this.sftp.chgrp(chgrp, remoteFile);
    }

    private String getExtension(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (i > p) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }
}
