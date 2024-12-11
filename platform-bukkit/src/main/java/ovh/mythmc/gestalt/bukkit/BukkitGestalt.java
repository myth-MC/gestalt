package ovh.mythmc.gestalt.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import ovh.mythmc.gestalt.Gestalt;

public final class BukkitGestalt extends Gestalt {

    protected BukkitGestalt(JavaPlugin plugin) {
        super(Bukkit.getServer().getVersion(), plugin.getConfig().getBoolean("autoUpdate"));
    }
    
}
