package ovh.mythmc.gestalt.loader;

import java.io.File;
import java.nio.file.Path;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class BukkitGestaltLoader extends GestaltLoader {

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

        if (plugin != null)
            Bukkit.getPluginManager().enablePlugin(plugin);
    }

    @Override
    protected boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("gestalt");
    }

    public static class BukkitGestaltLoaderBuilder {

        public BukkitGestaltLoaderBuilder initializer(JavaPlugin initializer) {
            this.dataDirectory = Path.of(initializer.getDataFolder().getParent());
            this.logger = GestaltLoggerWrapper.fromLogger(initializer.getLogger(), true);
            return this;
        }
    }
    
}
