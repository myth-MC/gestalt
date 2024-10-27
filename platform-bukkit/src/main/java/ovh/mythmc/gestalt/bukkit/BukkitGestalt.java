package ovh.mythmc.gestalt.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import ovh.mythmc.gestalt.GestaltImpl;

public class BukkitGestalt {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private JavaPlugin plugin;

        public Builder plugin(JavaPlugin plugin) {
            this.plugin = plugin;
            return this;
        }
 
        public GestaltImpl build() {
            return new GestaltImpl(plugin.getDescription().getVersion());
        }

    }

    /* 
    public void initialize() {
        PluginDescriptionFile descriptionFile = JavaPlugin.getPlugin(BukkitGestaltPlugin.class).getDescription();
        File library = new File(plugin.getDataFolder().getParent(), "libs" + File.separator + "gestalt-" + descriptionFile.getVersion() + ".jar");
        if (!library.exists()) {
            String url = String.format("http://github.com/myth-MC/gestalt/releases/download/%s/gestalt-platform-bukkit-%s.jar", descriptionFile.getVersion(), descriptionFile.getVersion());
            download(url, library.toPath());
        }

        try {
            Bukkit.getPluginManager().loadPlugin(library);
        } catch (UnknownDependencyException | InvalidPluginException | InvalidDescriptionException e) {
            e.printStackTrace();
        }
    }

    private void download(final @NotNull String url, final @NotNull Path path) {
        InputStream in = null;
        try {
            in = new URL(url).openStream();
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
}
