package ovh.mythmc.gestalt.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class GestaltLoader {

    protected final String gestaltUrl = "https://assets.mythmc.ovh/gestalt/latest.jar";

    public abstract Path getDataDirectory();

    public abstract GestaltLoggerWrapper getLogger();

    public abstract boolean isVerbose();

    public void initialize() {
        if (!Files.exists(Path.of(getGestaltPath()))) {
            setupGestaltPath();
            downloadGestalt();
        }

        load();
        enable();
    }

    public void load() { }

    public void enable() { }

    protected String getGestaltPath() {
        return getDataDirectory() + File.separator + "libs" + File.separator + "gestalt.jar";
    }

    protected void setupGestaltPath() {
        try {
            Files.createDirectories(Path.of(getGestaltPath()).getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void downloadGestalt() {
        getLogger().info("Downloading Gestalt...");
        try {
            long bytes = download(gestaltUrl, getGestaltPath());
            getLogger().info("Downloaded " + (bytes / 1000000) + " MBs");
        } catch (IOException e) {
            getLogger().error("Couldn't fetch gestalt! (switch DNS servers?)");
            e.printStackTrace();
        }
    }

    private static long download(String url, String fileName) throws IOException {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            return Files.copy(in, Paths.get(fileName));
        }
    }
    
}
