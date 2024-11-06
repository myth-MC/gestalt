package ovh.mythmc.gestalt.loader;

import java.io.File;
import java.nio.file.Path;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import ovh.mythmc.gestalt.loader.util.PaperPluginClassLoaderUtil;

public class PaperGestaltLoader extends GestaltLoader {
   
    private final Plugin initializer;

    private final Path dataDirectory;

    private final GestaltLoggerWrapper logger;

    private final boolean verbose;

    private Plugin plugin;

    private boolean isEnabled = false;

    protected PaperGestaltLoader(Plugin initializer, Path dataDirectory, GestaltLoggerWrapper logger, boolean verbose) {
        this.initializer = initializer;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.verbose = verbose;
    }

    @Override
    public void load() {
        if (!isEnabled) {
            File file = new File(getGestaltPath());
            try {
                plugin = Bukkit.getPluginManager().loadPlugin(file);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Bukkit.getPluginManager().enablePlugin(plugin);
        }
    }

    @Override
    public void enable() {
        if (!isEnabled)
            PaperPluginClassLoaderUtil.mergeClassLoaders(initializer, plugin);

        isEnabled = true;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private Plugin initializer;

        private Path dataDirectory;

        private GestaltLoggerWrapper logger = new GestaltLoggerWrapper() { };

        private boolean verbose = false;

        public Builder initializer(JavaPlugin initializer) {
            this.initializer = initializer;
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

        public PaperGestaltLoader build() {
            return new PaperGestaltLoader(initializer, dataDirectory, logger, verbose);
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
