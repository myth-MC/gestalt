package ovh.mythmc.gestalt.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import ovh.mythmc.gestalt.Gestalt;

public class BukkitGestaltBuilder {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private JavaPlugin plugin;

        public Builder plugin(JavaPlugin plugin) {
            this.plugin = plugin;
            return this;
        }
 
        public Gestalt build() {
            return new Gestalt(plugin.getServer().getVersion());
        }

    }
    
}
