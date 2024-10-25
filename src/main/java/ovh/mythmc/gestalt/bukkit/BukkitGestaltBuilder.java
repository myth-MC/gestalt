package ovh.mythmc.gestalt.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import ovh.mythmc.gestalt.Gestalt;

@SuppressWarnings("unused")
public class BukkitGestaltBuilder {

    public static Builder builder() {
        return new Builder();
    }

    private static class Builder {

        private JavaPlugin plugin;

        private boolean overrideInstance = false;

        public Builder plugin(JavaPlugin plugin) {
            this.plugin = plugin;
            return this;
        }
        
        public Builder overrideInstance(boolean overrideInstance) {
            this.overrideInstance = overrideInstance;
            return this;
        }

        public Gestalt build() {
            return new Gestalt(plugin.getServer().getVersion(), overrideInstance);
        }

    }
    
}
