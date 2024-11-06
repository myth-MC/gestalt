package ovh.mythmc.gestalt.loader.util;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import io.papermc.paper.plugin.provider.classloader.PaperClassLoaderStorage;

public final class PaperPluginClassLoaderUtil {

    public static ConfiguredPluginClassLoader getPluginClassLoaderGroup(@NotNull Plugin plugin) {
        ConfiguredPluginClassLoader configuredPluginClassLoader = null;
        PaperClassLoaderStorage instance = PaperClassLoaderStorage.instance();

        try {
            // Get plugin classloader storage
            Class<?> pluginClassLoaderStorage = Class.forName("io.papermc.paper.plugin.entrypoint.classloader.group.PaperPluginClassLoaderStorage");
            // Get global plugin class loader group
            Field globalGroupField = getField(pluginClassLoaderStorage, "globalGroup");
            globalGroupField.setAccessible(true);
            Object globalGroup = globalGroupField.get(instance);
            // Get classloaders inside global plugin class loader group
            Field classloadersField = getField(globalGroup.getClass(), "classloaders");
            classloadersField.setAccessible(true);
            List<?> classloaders = (List<?>) classloadersField.get(PaperClassLoaderStorage.instance());

            // Search for this plugin on global group's classloaders
            for (Object o : classloaders) {
                if (o instanceof ConfiguredPluginClassLoader classLoader) {
                    if (classLoader.getConfiguration().getName().equals(plugin.getDescription().getName()))
                        configuredPluginClassLoader = classLoader;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace(); // Todo: more robust logging
        }

        return configuredPluginClassLoader;
    }

    private static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            } else {
                return getField(superClass, fieldName);
            }
        }
    }

    public static void mergeClassLoaders(@NotNull Plugin pluginToMerge, @NotNull Plugin destination) {
        getPluginClassLoaderGroup(destination).getGroup().add(getPluginClassLoaderGroup(pluginToMerge));
    }
    
}
