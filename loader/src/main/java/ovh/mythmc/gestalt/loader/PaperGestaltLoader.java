package ovh.mythmc.gestalt.loader;

import java.io.File;
import java.nio.file.Path;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ovh.mythmc.gestalt.loader.util.PaperPluginClassLoaderUtil;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class PaperGestaltLoader extends GestaltLoader {
   
    private final Plugin initializer;

    private final Path dataDirectory;

    private final GestaltLoggerWrapper logger;

    @Override
    protected void load() {
        File file = new File(getGestaltPath());
        Plugin plugin = null;
        try {
            plugin = Bukkit.getPluginManager().loadPlugin(file);
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
    
    public static class PaperGestaltLoaderBuilder {

        public PaperGestaltLoaderBuilder initializer(Plugin initializer) {
            this.initializer = initializer;
            this.dataDirectory = Path.of(initializer.getDataFolder().getParent());
            this.logger = GestaltLoggerWrapper.fromLogger(initializer.getLogger(), true);
            return this;
        }

    }

}
