package dijkspicy.ms.server.proxy.http;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jcraft.jsch.*;
import org.slf4j.LoggerFactory;

/**
 * Environment
 *
 * @author dijkspicy
 * @date 2018/6/28
 */
public class Environment {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(SerializationFeature.INDENT_OUTPUT);
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Environment.class);
    private static final Map<String, EnvironmentConfig> ENVIRONMENT_MAP = new ConcurrentHashMap<>();
    private static final String LOCAL_PARENT_DIR = ".env";

    static {
        try {
            Path envFilePath = getEnvFilePath();
            Map<String, EnvironmentConfig> configMap = MAPPER.readValue(envFilePath.toFile(), new TypeReference<Map<String, EnvironmentConfig>>() {
            });
            ENVIRONMENT_MAP.putAll(configMap);
        } catch (IOException ignored) {
        }
    }

    private final EnvironmentConfig environmentConfig;
    private final Supplier<String> host;

    public Environment(@NotNull EnvironmentConfig environmentConfig) {
        this.environmentConfig = environmentConfig;
        this.host = this.environmentConfig::getHost;
    }

    public static EnvironmentConfig getEnv(String host) {
        return ENVIRONMENT_MAP.get(host);
    }

    public static void addEnv(EnvironmentConfig environment) {
        ENVIRONMENT_MAP.put(environment.getHost(), environment);
    }

    public static void addEnv(List<EnvironmentConfig> environments) {
        environments.forEach(Environment::addEnv);
    }

    public Path download() throws IOException {
        Path localDirectory = Paths.get(LOCAL_PARENT_DIR, this.host.get()).toAbsolutePath();
        if (Files.exists(localDirectory) && Files.list(localDirectory).findFirst().isPresent()) {
            return localDirectory;
        }

        try (final AutoSession autoSession = new AutoSession(this.environmentConfig)) {
            this.environmentConfig.getMapping().forEach((remote, local) -> this.handleConnection(autoSession, remote, local));
            this.writeBackEnv(this.environmentConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download from: " + this + ", error: " + e.getMessage(), e);
        }
        return localDirectory;
    }

    private static Path getEnvFilePath() {
        return Paths.get(LOCAL_PARENT_DIR, "env.json");
    }

    private void writeBackEnv(EnvironmentConfig config) {
        try {
            Path envFilePath = getEnvFilePath();
            Map<String, EnvironmentConfig> configMap = MAPPER.readValue(envFilePath.toFile(), new TypeReference<Map<String, EnvironmentConfig>>() {
            });
            if (!configMap.containsKey(config.getHost()) || !configMap.get(config.getHost()).equals(config)) {
                configMap.putAll(ENVIRONMENT_MAP);
                Files.write(envFilePath, MAPPER.writeValueAsBytes(configMap));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to write back to env.json");
        }
    }

    private void handleConnection(AutoSession autoSession, String remote, String local) {
        try (final AutoChannel autoChannel = new AutoChannel(autoSession, "sftp")) {
            Channel channel = autoChannel.channel;
            if (channel instanceof ChannelSftp) {
                channel.connect();
                this.download((ChannelSftp) channel, remote, Paths.get(LOCAL_PARENT_DIR, this.host.get(), local));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to download from: " + this + ", error: " + e.getMessage(), e);
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

    private static class AutoSession implements AutoCloseable {
        final Session session;

        AutoSession(EnvironmentConfig config) throws JSchException {
            JSch jSch = new JSch();
            this.session = jSch.getSession(config.getUser(), config.getHost(), config.getPort());
            session.setPassword(config.getPass());

            Properties sessionConfig = new Properties();
            sessionConfig.setProperty("StrictHostKeyChecking", "no");
            session.setConfig(sessionConfig);
            session.connect();
        }

        @Override
        public void close() {
            Optional.ofNullable(session).ifPresent(Session::disconnect);
        }
    }

    private static class AutoChannel implements AutoCloseable {
        final Channel channel;

        AutoChannel(AutoSession autoSession, String type) throws JSchException {
            this.channel = autoSession.session.openChannel(type);
        }

        @Override
        public void close() {
            Optional.ofNullable(channel).ifPresent(Channel::disconnect);
        }
    }
}
