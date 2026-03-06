package ovh.mythmc.gestalt.loader.util;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import io.papermc.paper.plugin.provider.classloader.PaperClassLoaderStorage;

/**
 * Utility class for manipulating Paper plugin class loaders via reflection.
 *
 * <p>Paper uses an isolated class loader system for plugins. This utility provides methods
 * to merge two plugins' class loaders so that one plugin can access the other's classes,
 * and to check whether a class is accessible from a given plugin's class loader.
 *
 * <p>These operations rely on internal Paper API fields accessed through reflection and
 * may break between Paper versions.
 */
public class PaperPluginClassLoaderUtil {

    private PaperPluginClassLoaderUtil() {
    }

    private static ConfiguredPluginClassLoader getPluginClassLoaderGroup(@NotNull Plugin plugin) {
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
            List<?> classloaders = (List<?>) classloadersField.get(globalGroup);

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

    /**
     * Merges the class loader of {@code pluginToMerge} into the class loader group of
     * {@code destination}, allowing {@code destination} to load classes from {@code pluginToMerge}.
     *
     * @param pluginToMerge the plugin whose class loader should be made accessible
     * @param destination   the plugin that should gain access to {@code pluginToMerge}'s classes
     */
    public static void mergeClassLoaders(@NotNull Plugin pluginToMerge, @NotNull Plugin destination) {
        getPluginClassLoaderGroup(destination).getGroup().add(getPluginClassLoaderGroup(pluginToMerge));
    }

    /**
     * Returns whether the given class name is loadable from the given plugin's class loader.
     *
     * @param plugin    the plugin whose class loader to query
     * @param className the fully qualified name of the class to look up
     * @return {@code true} if the class is accessible, {@code false} otherwise
     */
    public static boolean isAccessible(@NotNull Plugin plugin, @NotNull String className) {
        try {
            getPluginClassLoaderGroup(plugin).loadClass(className, false, true, true);
            return true;
        } catch (ClassNotFoundException ignored) { }
        
        return false;
    }
    
}
