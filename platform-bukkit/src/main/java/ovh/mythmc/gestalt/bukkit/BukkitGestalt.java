package ovh.mythmc.gestalt.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.IGestalt;
import ovh.mythmc.gestalt.AbstractGestalt;

public class BukkitGestalt extends AbstractGestalt {

    private final JavaPlugin initializer;

    private BukkitGestalt(@NotNull JavaPlugin initializer) {
        super(initializer.getServer().getVersion());
        this.initializer = initializer;
    }

    public void initialize() {
        RegisteredServiceProvider<IGestalt> rsp = initializer.getServer().getServicesManager().getRegistration(IGestalt.class);
        if (rsp == null) {
            Bukkit.getServicesManager().register(IGestalt.class, this, initializer, ServicePriority.Highest);    
            initializer.getLogger().info("Registered Gestalt service provider (" + IGestalt.class + ")");
        }
    }

    public static IGestalt get() {
        return Bukkit.getServer().getServicesManager().getRegistration(IGestalt.class).getProvider();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private JavaPlugin initializer;

        public Builder initializer(@NotNull JavaPlugin initializer) {
            this.initializer = initializer;
            return this;
        }
 
        public BukkitGestalt build() {
            return new BukkitGestalt(initializer);
        }

    }

}
