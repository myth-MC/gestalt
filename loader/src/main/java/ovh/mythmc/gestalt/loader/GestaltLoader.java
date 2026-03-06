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

import ovh.mythmc.gestalt.loader.GestaltLoaderInitializationResponse.Warning;

/**
 * Abstract base class responsible for downloading, verifying, and loading the Gestalt JAR
 * on the target platform.
 *
 * <p>Subclasses must implement the platform-specific methods:
 * <ul>
 *   <li>{@link #getDataDirectory()} - the directory in which the {@code libs/} folder is created</li>
 *   <li>{@link #getLogger()} - the logger wrapper for status messages</li>
 *   <li>{@link #isAvailable()} - whether Gestalt is already loaded on this platform</li>
 *   <li>{@link #load()} - the platform-specific routine to load the downloaded JAR</li>
 * </ul>
 *
 * <p>The full initialization flow is driven by {@link #initialize()}, which handles directory
 * setup, version checking, downloading, and finally delegating to {@link #load()}.
 *
 * <p>Download URLs and checksum endpoints are read from a {@code gestalt.properties} resource
 * bundled in the plugin JAR.
 */
public abstract class GestaltLoader {

    /**
     * Returns the root data directory of the plugin. The Gestalt JAR will be placed at
     * {@code <dataDirectory>/libs/gestalt.jar}.
     *
     * @return the plugin data directory
     */
    protected abstract Path getDataDirectory();

    /**
     * Returns the logger wrapper used to emit status messages during loading.
     *
     * @return the logger wrapper
     */
    protected abstract GestaltLoggerWrapper getLogger();

    /**
     * Returns whether Gestalt is already available and loaded on this platform.
     *
     * <p>If {@code true}, {@link #initialize()} will return early without downloading or loading.
     *
     * @return {@code true} if Gestalt is already loaded
     */
    protected abstract boolean isAvailable();

    /**
     * Performs the platform-specific routine to load the Gestalt JAR into the server.
     * Called by {@link #initialize()} after the JAR has been verified to exist on disk.
     */
    protected abstract void load();

    /**
     * Runs the full Gestalt initialization flow:
     * <ol>
     *   <li>Checks if Gestalt is already available and returns early if so.</li>
     *   <li>Ensures the {@code libs/} directory exists.</li>
     *   <li>Compares the local JAR checksum against the remote checksum and deletes
     *       an outdated JAR if found.</li>
     *   <li>Downloads the JAR if it is not present on disk.</li>
     *   <li>Calls {@link #load()} to load the JAR into the server.</li>
     * </ol>
     *
     * @return a {@link GestaltLoaderInitializationResponse} describing the outcome of the initialization
     */
    public GestaltLoaderInitializationResponse initialize() {
        if (isAvailable()) {
            return GestaltLoaderInitializationResponse.alreadyAvailable(); // already loaded, nothing to do
        }

        if (!setupGestaltDirectory()) {
            return GestaltLoaderInitializationResponse.directoryUnavailable();
        }

        GestaltLoaderInitializationResponse response = GestaltLoaderInitializationResponse.success();

        if (!isUpToDate()) {
            if (!deleteJar()) {
                response.warnings(Warning.OUTDATED);
            }
        }

        if (!Files.exists(getJarPath())) {
            if (!downloadGestalt()) {
                getLogger().error("Gestalt could not be downloaded. Skipping load.");
                return GestaltLoaderInitializationResponse.remoteUnavailable();
            }
        }

        load();
        return response;
    }

    /**
     * Called when the loader should shut down.
     */
    public void terminate() { 
        // reserved for future usage
    }

    /**
     * Returns the path where the Gestalt JAR is stored on disk.
     * Defaults to {@code <dataDirectory>/libs/gestalt.jar}.
     *
     * @return the path to the Gestalt JAR
     */
    protected Path getJarPath() {
        return getDataDirectory().resolve("libs").resolve("gestalt.jar");
    }

    /**
     * Returns the string representation of the Gestalt JAR path.
     *
     * @return the Gestalt JAR path as a string
     * @deprecated Use {@link #getJarPath()} instead.
     */
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
