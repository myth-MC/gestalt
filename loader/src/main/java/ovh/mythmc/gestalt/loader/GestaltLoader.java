package ovh.mythmc.gestalt.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class GestaltLoader {

    private final String gestaltUrl = "https://assets.mythmc.ovh/gestalt/latest.jar";

    protected abstract Path getDataDirectory();

    protected abstract GestaltLoggerWrapper getLogger();

    public abstract boolean isAvailable();

    public void initialize() {
        if (!isAvailable()) {
            if (!Files.exists(Path.of(getGestaltPath()))) {
                setupGestaltPath();
                downloadGestalt();
            }
    
            load();
        }
    }

    public abstract void load();

    protected String getGestaltPath() {
        return getDataDirectory() + File.separator + "libs" + File.separator + "gestalt.jar";
    }

    private void setupGestaltPath() {
        try {
            Files.createDirectories(Path.of(getGestaltPath()).getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadGestalt() {
        getLogger().verbose("Downloading Gestalt...");
        try {
            long bytes = download(gestaltUrl, getGestaltPath());
            getLogger().verbose("Downloaded " + bytes + " MBs");
        } catch (IOException e) {
            getLogger().error("Couldn't fetch gestalt! (server down / switch DNS servers?)");
            e.printStackTrace();
        }
    }

    private static long download(String url, String fileName) throws IOException {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            return Files.copy(in, Paths.get(fileName));
        }
    }
    
}
