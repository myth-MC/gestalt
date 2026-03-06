package ovh.mythmc.gestalt.loader;

import java.nio.file.Path;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import ovh.mythmc.gestalt.loader.util.PaperPluginClassLoaderUtil;

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

    public static PaperGestaltLoaderBuilder builder() {
        return new PaperGestaltLoaderBuilder();
    }
    
    public static class PaperGestaltLoaderBuilder {

        private Plugin initializer;
        private Path dataDirectory;
        private GestaltLoggerWrapper logger;

        private PaperGestaltLoaderBuilder() {
        }

        public PaperGestaltLoaderBuilder initializer(Plugin initializer) {
            this.initializer = initializer;
            this.dataDirectory = Path.of(initializer.getDataFolder().getParent());
            this.logger = GestaltLoggerWrapper.fromLogger(initializer.getLogger(), true);
            return this;
        }

        public PaperGestaltLoaderBuilder dataDirectory(Path dataDirectory) {
            this.dataDirectory = dataDirectory;
            return this;
        }

        public PaperGestaltLoaderBuilder logger(GestaltLoggerWrapper logger) {
            this.logger = logger;
            return this;
        }

        public PaperGestaltLoader build() {
            return new PaperGestaltLoader(initializer, dataDirectory, logger);
        }

    }

}
