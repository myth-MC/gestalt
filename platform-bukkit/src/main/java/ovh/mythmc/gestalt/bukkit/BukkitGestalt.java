package ovh.mythmc.gestalt.bukkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.IGestalt;

public class BukkitGestalt {

    private final String gestaltUrl = "https://assets.mythmc.ovh/gestalt/latest.jar";

    private final JavaPlugin initializer;

    private static Plugin plugin;

    private BukkitGestalt(@NotNull JavaPlugin initializer) {
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
            plugin = Bukkit.getPluginManager().loadPlugin(file);

            ClassLoader classLoader = plugin.getClass().getClassLoader();
            Class<?> bukkitGestaltPlugin = Class.forName("ovh.mythmc.gestalt.bukkit.BukkitGestaltPlugin", false, classLoader);
            Class<?> interfaceGestalt = Class.forName("ovh.mythmc.gestalt.IGestalt", true, classLoader);
            Method set = bukkitGestaltPlugin.getMethod("set", interfaceGestalt);
            Class<?> bukkitGestaltInstance = Class.forName("ovh.mythmc.gestalt.bukkit.BukkitGestaltInstance", true, classLoader);
            set.invoke(plugin, bukkitGestaltInstance.getConstructor().newInstance());

            Bukkit.getPluginManager().enablePlugin(plugin);
        } catch (Exception e) {
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
        ClassLoader classLoader = plugin.getClass().getClassLoader();
        Object instance = null;
        try {
            Class<?> bukkitGestaltPlugin = Class.forName("ovh.mythmc.gestalt.bukkit.BukkitGestaltPlugin", false, classLoader);
            Method get = bukkitGestaltPlugin.getMethod("get");
            instance = get.invoke(plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (IGestalt) instance;
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
