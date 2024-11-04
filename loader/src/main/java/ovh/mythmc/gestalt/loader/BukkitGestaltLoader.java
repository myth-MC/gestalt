package ovh.mythmc.gestalt.loader;

import java.io.File;
import java.nio.file.Path;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitGestaltLoader extends GestaltLoader {

    private final Path dataDirectory;

    private final GestaltLoggerWrapper logger;

    private final boolean verbose;

    private Plugin plugin;

    protected BukkitGestaltLoader(Path dataDirectory, GestaltLoggerWrapper logger, boolean verbose) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.verbose = verbose;
    }

    @Override
    public void initialize() {
        if (Bukkit.getPluginManager().isPluginEnabled("gestalt"))
            return;
        
        super.initialize();
    }

    @Override
    public void load() {
        File file = new File(getGestaltPath());
        try {
            plugin = Bukkit.getPluginManager().loadPlugin(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().enablePlugin(plugin);
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

    @Override
    public Path getDataDirectory() {
        return dataDirectory;
    }

    @Override
    public GestaltLoggerWrapper getLogger() {
        return logger;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }
    
}
