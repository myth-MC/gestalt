package ovh.mythmc.gestalt.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public abstract class GestaltLoader {

    protected abstract Path getDataDirectory();

    protected abstract GestaltLoggerWrapper getLogger();

    protected abstract boolean isAvailable();

    public void initialize() {
        if (!isAvailable()) {
            setupGestaltPath();
            if (!Files.exists(Path.of(getGestaltPath())))
                downloadGestalt();

            load();
        }
    }

    protected abstract void load();

    public void terminate() {
        try {
            Files.deleteIfExists(Path.of(getGestaltPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String getGestaltPath() {
        return getDataDirectory() + File.separator + "libs" + File.separator + "gestalt.jar";
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private void setupGestaltPath() {
        try {
            Files.createDirectories(Path.of(getGestaltPath()).getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadGestalt() {
        getLogger().verbose("Downloading gestalt...");
        try {
            long bytes = download(getProperties().getProperty("downloadUrl"), getGestaltPath());
            getLogger().verbose("Downloaded " + (bytes / 1000) + " KBs");
        } catch (IOException e) {
            getLogger().error("Couldn't fetch gestalt! (is server down? / switch DNS servers?)");
            e.printStackTrace();
        }
    }

    private static long download(String url, String fileName) throws IOException {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            return Files.copy(in, Paths.get(fileName));
        }
    }
    
}
