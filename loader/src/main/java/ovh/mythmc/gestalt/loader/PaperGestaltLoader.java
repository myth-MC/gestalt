package ovh.mythmc.gestalt.loader;

import java.io.File;
import java.nio.file.Path;

import org.bukkit.Bukkit;

public class PaperGestaltLoader extends GestaltLoader {

    private final Path dataDirectory;

    private final GestaltLoggerWrapper logger;

    private final boolean verbose;

    protected PaperGestaltLoader(Path dataDirectory, GestaltLoggerWrapper logger, boolean verbose) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.verbose = verbose;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void load() {
        File file = new File(getGestaltPath());
        try {
            
            Bukkit.getPluginManager().loadPlugin(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private Path dataDirectory;

        private GestaltLoggerWrapper logger = new GestaltLoggerWrapper() { };

        private boolean verbose = false;

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
            return new PaperGestaltLoader(dataDirectory, logger, verbose);
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
