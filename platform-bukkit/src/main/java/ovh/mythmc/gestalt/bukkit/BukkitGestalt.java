package ovh.mythmc.gestalt.bukkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.IGestalt;
import ovh.mythmc.gestalt.AbstractGestalt;

public class BukkitGestalt extends AbstractGestalt {

    private final String gestaltUrl = "https://assets.mythmc.ovh/gestalt/latest.jar";

    private final JavaPlugin initializer;

    private BukkitGestalt(@NotNull JavaPlugin initializer) {
        super(initializer.getServer().getVersion());
        this.initializer = initializer;
    }

    public void initialize() {
        if (Bukkit.getPluginManager().isPluginEnabled("gestalt"))
            return;

        setupGestaltPath();
        if (!Files.exists(Path.of(getGestaltPath())))
            downloadGestalt();

        File file = new File(getGestaltPath());
        try {
            BukkitGestaltPlugin plugin = (BukkitGestaltPlugin) Bukkit.getPluginManager().loadPlugin(file);
            Bukkit.getPluginManager().enablePlugin(plugin);
            plugin.set(this);
        } catch (UnknownDependencyException | InvalidPluginException | InvalidDescriptionException e) {
            e.printStackTrace();
        }
    }

    private void setupGestaltPath() {
        try {
            Files.createDirectories(Path.of(getGestaltPath()).getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getGestaltPath() {
        return initializer.getDataFolder() + File.separator + "libs" + File.separator + "gestalt.jar";
    }

    private void downloadGestalt() {
        initializer.getLogger().info("Downloading Gestalt...");
        try {
            long bytes = download(gestaltUrl, getGestaltPath());
            initializer.getLogger().info("Downloaded " + (bytes / 1000000) + " MBs!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long download(String url, String fileName) throws IOException {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            return Files.copy(in, Paths.get(fileName));
    }
}

    public static IGestalt get() {
        BukkitGestaltPlugin gestalt = ((BukkitGestaltPlugin) Bukkit.getPluginManager().getPlugin("gestalt"));
        return gestalt.get();
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
