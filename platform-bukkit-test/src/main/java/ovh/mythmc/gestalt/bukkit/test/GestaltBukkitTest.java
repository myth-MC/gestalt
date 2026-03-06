package ovh.mythmc.gestalt.bukkit.test;

import org.bukkit.plugin.java.JavaPlugin;

import ovh.mythmc.gestalt.loader.BukkitGestaltLoader;
import ovh.mythmc.gestalt.loader.GestaltLoggerWrapper;

public class GestaltBukkitTest extends JavaPlugin {

    private BukkitGestaltLoader loader;

    @Override
    public void onEnable() {
        loader = BukkitGestaltLoader.builder()
            .initializer(this)
            .logger(GestaltLoggerWrapper.fromLogger(getLogger(), true))
            .build();

        loader.initialize();
    }

    @Override
    public void onDisable() {
        loader.terminate();
    }
    
}
