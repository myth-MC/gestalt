package ovh.mythmc.gestalt.bukkit;

import org.bukkit.Bukkit;

import ovh.mythmc.gestalt.Gestalt;

public final class BukkitGestalt extends Gestalt {

    protected BukkitGestalt() {
        super(Bukkit.getServer().getVersion());
    }
    
}
