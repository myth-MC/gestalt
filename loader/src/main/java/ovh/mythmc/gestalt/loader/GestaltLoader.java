package ovh.mythmc.gestalt.loader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

import ovh.mythmc.gestalt.loader.GestaltLoaderResponse.Warning;

public abstract class GestaltLoader {

    protected abstract Path getDataDirectory();

    protected abstract GestaltLoggerWrapper getLogger();

    protected abstract boolean isAvailable();

    protected abstract void load();

    public GestaltLoaderResponse initialize() {
        if (isAvailable()) {
            return GestaltLoaderResponse.alreadyAvailable(); // already loaded, nothing to do
        }

        if (!setupGestaltDirectory()) {
            return GestaltLoaderResponse.directoryUnavailable();
        }

        GestaltLoaderResponse response = GestaltLoaderResponse.success();

        if (!isUpToDate()) {
            if (!deleteJar()) {
                response.warnings(Warning.OUTDATED);
            }
        }

        if (!Files.exists(getJarPath())) {
            if (!downloadGestalt()) {
                getLogger().error("Gestalt could not be downloaded. Skipping load.");
                return GestaltLoaderResponse.remoteUnavailable();
            }
        }

        load();
        return response;
    }

    public void terminate() { 
        // reserved for future usage
    }

    protected Path getJarPath() {
        return getDataDirectory().resolve("libs").resolve("gestalt.jar");
    }

    /** @deprecated Use {@link #getJarPath()} instead. */
    @Deprecated
    protected String getGestaltPath() {
        return getJarPath().toString();
    }

    private boolean setupGestaltDirectory() {
        try {
            Files.createDirectories(getJarPath().getParent());
            return true;
        } catch (IOException e) {
            getLogger().error("Could not create Gestalt directory (permission issue?)");
            e.printStackTrace(System.err);
        }
        
        return false;
    }

    private boolean deleteJar() {
        try {
            Files.deleteIfExists(getJarPath());
            return true;
        } catch (IOException e) {
            getLogger().error("Could not delete outdated Gestalt JAR:");
            e.printStackTrace(System.err);
        }

        return false;
    }

    private boolean isUpToDate() {
        final String localChecksum = computeJarChecksum();
        final String remoteChecksum = fetchRemoteChecksum();

        getLogger().verbose("Local JAR checksum:  " + localChecksum);
        getLogger().verbose("Remote JAR checksum: " + remoteChecksum);

        return Objects.equals(localChecksum, remoteChecksum);
    }

    private String fetchRemoteChecksum() {
        final Properties properties = loadProperties();

        for (int i = 1; i <= 10; i++) {
            final String url = properties.getProperty("checksum." + i);
            if (url == null) continue;

            try {
                final URLConnection connection = URI.create(url).toURL().openConnection();
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    return scanner.next();
                }
            } catch (IOException e) {
                getLogger().error("Could not fetch remote checksum from server " + i
                        + " (server down? try switching DNS servers)");
                if (i < 10) {
                    getLogger().info("Retrying with server " + (i + 1) + "...");
                }
            }
        }

        // Assume up-to-date if all remote checks fail
        return computeJarChecksum();
    }

    private boolean downloadGestalt() {
        getLogger().verbose("Downloading gestalt...");
        final Properties properties = loadProperties();

        for (int i = 1; i <= 10; i++) {
            final String url = properties.getProperty("server." + i);
            if (url == null) continue;

            try {
                final long bytes = downloadFile(url, getJarPath());
                getLogger().verbose("Downloaded " + (bytes / 1_000) + " KBs from server " + i);
                return true;
            } catch (IOException e) {
                getLogger().error("Could not download from server " + i
                        + " (server down? try switching DNS servers)");
                if (i < 10) {
                    getLogger().info("Retrying with server " + (i + 1) + "...");
                }
            }
        }

        return false;
    }

    private static long downloadFile(String url, Path destination) throws IOException {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            return Files.copy(in, destination);
        }
    }

    private String computeJarChecksum() {
        if (!Files.exists(getJarPath())) {
            return "";
        }

        try (FileInputStream fis = new FileInputStream(getJarPath().toFile());
             DigestInputStream dis = new DigestInputStream(fis, MessageDigest.getInstance("MD5"))) {

            dis.transferTo(OutputStream.nullOutputStream());
            return HexFormat.of().formatHex(dis.getMessageDigest().digest());
        } catch (Exception e) {
            getLogger().error("Could not compute JAR checksum:");
            e.printStackTrace(System.err);
            return "";
        }
    }

    private Properties loadProperties() {
        final Properties properties = new Properties();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("gestalt.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            getLogger().error("Could not read gestalt.properties (was it shaded?)");
            e.printStackTrace(System.err);
        }
        return properties;
    }

}
