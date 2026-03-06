package ovh.mythmc.gestalt.loader;

import java.nio.file.Path;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import ovh.mythmc.gestalt.loader.util.PaperPluginClassLoaderUtil;

/**
 * A {@link GestaltLoader} implementation for the Paper platform.
 *
 * <p>In addition to loading and enabling the Gestalt plugin JAR, this loader merges the
 * Gestalt plugin's class loader into the initializer plugin's class loader group using
 * {@link PaperPluginClassLoaderUtil#mergeClassLoaders(Plugin, Plugin)}, allowing the
 * initializer to access Gestalt's classes directly.
 *
 * <p>Use {@link #builder()} to construct an instance:
 * <pre>{@code
 * PaperGestaltLoader loader = PaperGestaltLoader.builder()
 *     .initializer(this)
 *     .build();
 * loader.initialize();
 * }</pre>
 */
public class PaperGestaltLoader extends GestaltLoader {
   
    private final Plugin initializer;
    private final Path dataDirectory;
    private final GestaltLoggerWrapper logger;

    private PaperGestaltLoader(
        Plugin initializer,
        Path dataDirectory,
        GestaltLoggerWrapper logger
    ) {
        this.initializer = initializer;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    /**
     * Returns the plugin that initiated the Gestalt load. Used for class loader merging.
     *
     * @return the initializer plugin
     */
    public Plugin getInitializer() {
        return this.initializer;
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
            PaperPluginClassLoaderUtil.mergeClassLoaders(initializer, plugin);
        }
    }

    @Override
    protected boolean isAvailable() {
        return PaperPluginClassLoaderUtil.isAccessible(initializer, "ovh.mythmc.gestalt.Gestalt");
    }

    /**
     * Returns a new {@link PaperGestaltLoaderBuilder}.
     *
     * @return a new builder
     */
    public static PaperGestaltLoaderBuilder builder() {
        return new PaperGestaltLoaderBuilder();
    }
    
    /**
     * Builder for {@link PaperGestaltLoader}.
     */
    public static class PaperGestaltLoaderBuilder {

        private Plugin initializer;
        private Path dataDirectory;
        private GestaltLoggerWrapper logger;

        private PaperGestaltLoaderBuilder() {
        }

        /**
         * Configures the loader using the given {@link Plugin} as the initializer.
         * Sets the data directory to the parent of the plugin's data folder and
         * creates a verbose logger wrapper from the plugin's JUL logger.
         *
         * @param initializer the plugin that is loading Gestalt
         * @return this builder
         */
        public PaperGestaltLoaderBuilder initializer(Plugin initializer) {
            this.initializer = initializer;
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
        public PaperGestaltLoaderBuilder dataDirectory(Path dataDirectory) {
            this.dataDirectory = dataDirectory;
            return this;
        }

        /**
         * Overrides the logger wrapper used for status messages.
         *
         * @param logger the logger wrapper
         * @return this builder
         */
        public PaperGestaltLoaderBuilder logger(GestaltLoggerWrapper logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Builds and returns a {@link PaperGestaltLoader} with the configured values.
         *
         * @return a new {@link PaperGestaltLoader} instance
         */
        public PaperGestaltLoader build() {
            return new PaperGestaltLoader(initializer, dataDirectory, logger);
        }

    }

}
