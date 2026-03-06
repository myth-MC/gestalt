package ovh.mythmc.gestalt.loader;

import java.nio.file.Path;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A {@link GestaltLoader} implementation for the Bukkit/Spigot platform.
 *
 * <p>Loads the Gestalt JAR via {@code Bukkit.getPluginManager().loadPlugin()} and reports
 * availability via {@code Bukkit.getPluginManager().isPluginEnabled("gestalt")}.
 *
 * <p>Use {@link #builder()} to construct an instance:
 * <pre>{@code
 * BukkitGestaltLoader loader = BukkitGestaltLoader.builder()
 *     .initializer(this)
 *     .build();
 * loader.initialize();
 * }</pre>
 */
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

    /**
     * Returns a new {@link BukkitGestaltLoaderBuilder}.
     *
     * @return a new builder
     */
    public static BukkitGestaltLoaderBuilder builder() {
        return new BukkitGestaltLoaderBuilder();
    }

    /**
     * Builder for {@link BukkitGestaltLoader}.
     */
    public static class BukkitGestaltLoaderBuilder {

        private Path dataDirectory;
        private GestaltLoggerWrapper logger;

        private BukkitGestaltLoaderBuilder() {
        }

        /**
         * Configures the loader using the given {@link JavaPlugin} as the initializer.
         * Sets the data directory to the parent of the plugin's data folder and
         * creates a verbose logger wrapper from the plugin's JUL logger.
         *
         * @param initializer the plugin that is loading Gestalt
         * @return this builder
         */
        public BukkitGestaltLoaderBuilder initializer(JavaPlugin initializer) {
            this.dataDirectory = Path.of(initializer.getDataFolder().getParent());
            this.logger = GestaltLoggerWrapper.fromLogger(initializer.getLogger(), true);
            return this;
        }

        /**
         * Overrides the data directory used to store the Gestalt JAR.
         *
         * @param dataDirectory the directory in which {@code libs/gestalt.jar} is placed
         * @return this builder
         */
        public BukkitGestaltLoaderBuilder dataDirectory(Path dataDirectory) {
            this.dataDirectory = dataDirectory;
            return this;
        }

        /**
         * Overrides the logger wrapper used for status messages.
         *
         * @param logger the logger wrapper
         * @return this builder
         */
        public BukkitGestaltLoaderBuilder logger(GestaltLoggerWrapper logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Builds and returns a {@link BukkitGestaltLoader} with the configured values.
         *
         * @return a new {@link BukkitGestaltLoader} instance
         */
        public BukkitGestaltLoader build() {
            return new BukkitGestaltLoader(dataDirectory, logger);
        }

    }
    
}
