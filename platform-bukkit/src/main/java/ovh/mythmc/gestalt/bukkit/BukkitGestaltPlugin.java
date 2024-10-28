package ovh.mythmc.gestalt.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.IGestalt;

public class BukkitGestaltPlugin extends JavaPlugin {

    private IGestalt gestalt;

    public void set(final @NotNull IGestalt gestalt) {
        this.gestalt = gestalt;
    }

    public IGestalt get() {
        return gestalt;
    }

}