package ovh.mythmc.gestalt.loader;

import java.nio.file.Path;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitGestaltLoader extends GestaltLoader {

    private final Path dataDirectory;
    private final GestaltLoggerWrapper logger;

    private BukkitGestaltLoader(Path dataDirectory, GestaltLoggerWrapper logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    @Override
    public Path getDataDirectory() {
        return this.dataDirectory;
    }

    @Override
    public GestaltLoggerWrapper getLogger() {
        return this.logger;
    }

    @Override
    protected void load() {
        Plugin plugin = null;
        try {
            plugin = Bukkit.getPluginManager().loadPlugin(getJarPath().toFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (plugin != null) {
            Bukkit.getPluginManager().enablePlugin(plugin);
        }
    }

    @Override
    protected boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("gestalt");
    }

    public static BukkitGestaltLoaderBuilder builder() {
        return new BukkitGestaltLoaderBuilder();
    }

    public static class BukkitGestaltLoaderBuilder {

        private Path dataDirectory;
        private GestaltLoggerWrapper logger;

        private BukkitGestaltLoaderBuilder() {
        }

        public BukkitGestaltLoaderBuilder initializer(JavaPlugin initializer) {
            this.dataDirectory = Path.of(initializer.getDataFolder().getParent());
            this.logger = GestaltLoggerWrapper.fromLogger(initializer.getLogger(), true);
            return this;
        }

        public BukkitGestaltLoaderBuilder dataDirectory(Path dataDirectory) {
            this.dataDirectory = dataDirectory;
            return this;
        }

        public BukkitGestaltLoaderBuilder logger(GestaltLoggerWrapper logger) {
            this.logger = logger;
            return this;
        }

        public BukkitGestaltLoader build() {
            return new BukkitGestaltLoader(dataDirectory, logger);
        }

    }
    
}
