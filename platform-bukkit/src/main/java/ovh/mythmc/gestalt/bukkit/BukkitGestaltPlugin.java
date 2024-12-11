package ovh.mythmc.gestalt.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import ovh.mythmc.gestalt.GestaltSupplier;

public class BukkitGestaltPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        GestaltSupplier.set(new BukkitGestalt(this));
    }
    
}
