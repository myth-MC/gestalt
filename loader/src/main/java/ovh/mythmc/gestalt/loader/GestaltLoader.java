package ovh.mythmc.gestalt.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import ovh.mythmc.gestalt.Gestalt;
import ovh.mythmc.gestalt.loader.callbacks.GestaltInitialize;
import ovh.mythmc.gestalt.loader.callbacks.GestaltInitializeCallback;

public abstract class GestaltLoader {

    protected abstract Path getDataDirectory();

    protected abstract GestaltLoggerWrapper getLogger();

    protected abstract boolean isAvailable();

    public void initialize() {
        if (isAvailable())
            return;

        setupGestaltPath();

        boolean load = true;
        if (!Files.exists(Path.of(getGestaltPath())))
            load = downloadGestalt();

        if (load)
            load();

        GestaltInitializeCallback.INSTANCE.handle(new GestaltInitialize(this));
    }

    protected abstract void load();

    public void terminate() {
        if (!Gestalt.get().isAutoUpdate())
            return;

        try {
            // Get class loader and close with reflection
            URLClassLoader loader = (URLClassLoader) Gestalt.get().getClass().getClassLoader();
            loader.close();

            // Delete file
            Files.deleteIfExists(Path.of(getGestaltPath()));
        } catch (Throwable t) {
            getLogger().error("Error while terminating the instance!");
            t.printStackTrace(System.err);
        }
    }

    protected String getGestaltPath() {
        return getDataDirectory() + File.separator + "libs" + File.separator + "gestalt.jar";
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("gestalt.properties"));
        } catch (IOException e) {
            getLogger().error("Gestalt properties file not accessible! (shaded?)");
            e.printStackTrace(System.err);
        }
        return properties;
    }

    private void setupGestaltPath() {
        try {
            Files.createDirectories(Path.of(getGestaltPath()).getParent());
        } catch (IOException e) {
            getLogger().error("Gestalt path cannot be created! (permission issue?)");
            e.printStackTrace(System.err);
        }
    }

    private boolean downloadGestalt() {
        boolean downloaded = false;

        getLogger().verbose("Downloading gestalt (event-based library for managing features)...");
        for (int i = 1; i <= 10; i++) { // Up to 10 download links
            String key = "server." + i;
            String url = getProperties().getProperty(key);

            if (url != null) {
                try {
                    long bytes = download(url, getGestaltPath());
                    getLogger().verbose("Downloaded " + (bytes / 1000) + " KBs from server " + i);
                    downloaded = true;
                    break;
                } catch (IOException e) {
                    getLogger().error("Couldn't fetch gestalt! (server down? / switch DNS servers?)");
                    getLogger().info("Retrying with server " + (i+1) + "...");
                }
            }
        }

        return downloaded;
    }

    private static long download(String url, String fileName) throws IOException {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            return Files.copy(in, Paths.get(fileName));
        }
    }
    
}
