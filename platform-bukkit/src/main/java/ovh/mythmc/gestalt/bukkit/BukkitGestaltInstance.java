package ovh.mythmc.gestalt.bukkit;

import org.bukkit.Bukkit;

import ovh.mythmc.gestalt.AbstractGestalt;

public class BukkitGestaltInstance extends AbstractGestalt {

    public BukkitGestaltInstance() {
        super(Bukkit.getServer().getVersion());
    }
    
}
