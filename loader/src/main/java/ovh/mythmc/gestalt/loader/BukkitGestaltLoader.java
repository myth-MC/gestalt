package ovh.mythmc.gestalt.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitGestaltLoader {

    private final Path dataDirectory;

    private final GestaltLoggerWrapper logger;

    private final boolean verbose;

    private final String gestaltUrl = "https://assets.mythmc.ovh/gestalt/latest.jar";

    protected BukkitGestaltLoader(Path dataDirectory, GestaltLoggerWrapper logger, boolean verbose) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.verbose = verbose;
    }

    public void initialize() {
        if (Bukkit.getPluginManager().isPluginEnabled("gestalt"))
            return;
        
        if (!Files.exists(Path.of(getGestaltPath()))) {
            setupGestaltPath();
            downloadGestalt();
        }

        File file = new File(getGestaltPath());
        try {
            Plugin plugin = Bukkit.getPluginManager().loadPlugin(file);
            Bukkit.getPluginManager().enablePlugin(plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getGestaltPath() {
        return dataDirectory + File.separator + "libs" + File.separator + "gestalt.jar";
    }

    private void setupGestaltPath() {
        try {
            Files.createDirectories(Path.of(getGestaltPath()).getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadGestalt() {
        info("Downloading Gestalt...");
        try {
            long bytes = download(gestaltUrl, getGestaltPath());
            info("Downloaded " + (bytes / 1000000) + " MBs");
        } catch (IOException e) {
            error("Couldn't fetch gestalt! (switch DNS?)");
            e.printStackTrace();
        }
    }

    private static long download(String url, String fileName) throws IOException {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            return Files.copy(in, Paths.get(fileName));
        }
    }

    private void info(String message) {
        if (verbose)
            logger.info(message);
    }

    private void error(String message) {
        logger.error(message);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private Path dataDirectory;

        private GestaltLoggerWrapper logger = new GestaltLoggerWrapper() { };

        private boolean verbose = false;

        public Builder initializer(JavaPlugin initializer) {
            this.dataDirectory = Path.of(initializer.getDataFolder().getParent());
            this.logger = GestaltLoggerWrapper.fromLogger(initializer.getLogger());
            return this;
        }

        public Builder dataDirectory(Path dataDirectory) {
            this.dataDirectory = dataDirectory;
            return this;
        }

        public Builder logger(GestaltLoggerWrapper logger) {
            this.logger = logger;
            return this;
        }

        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public BukkitGestaltLoader build() {
            return new BukkitGestaltLoader(dataDirectory, logger, verbose);
        }
    }
    
}
