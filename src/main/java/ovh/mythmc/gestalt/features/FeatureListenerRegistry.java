package ovh.mythmc.gestalt.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.annotations.FeatureListenerProcessor;

public final class FeatureListenerRegistry {

    // instancia que escucha, GestaltFeature que escucha (AnvilFeature.class)
    private final Map<Object, String[]> listenerRegistry = new HashMap<>();

    public void register(final @NotNull Object instance) {
        String[] classes = (String[]) FeatureListenerProcessor.getListeners(instance.getClass()).stream()
            .map(clazz -> clazz.getName())
            .toArray();

        listenerRegistry.put(instance, classes);
    }

    public void unregister(final @NotNull Object instance) {
        listenerRegistry.remove(instance);
    }

    public void unregisterAllListeners(final @NotNull String className) {
        Set.copyOf(listenerRegistry.entrySet()).stream()
            .filter(entry -> entry.getValue().equals(className))
            .forEach(entry -> listenerRegistry.remove(entry.getValue()));
    }

    public void unregisterAllListeners(final @NotNull Class<?> clazz) {
        unregister(clazz.getName());
    }

    public boolean isRegistered(final @NotNull Object instance) {
        return listenerRegistry.containsKey(instance);
    }

    // clases a las que escucha (AnvilFeature.class...)
    public String[] getListeners(final @NotNull Object instance) {
        return listenerRegistry.get(instance);
    }

    public List<Object> getInstances(final @NotNull String className) {
        List<Object> instances = new ArrayList<>();
        for (Map.Entry<Object, String[]> entry : listenerRegistry.entrySet()) {
            for (String value : entry.getValue()) {
                if (value.equals(className))
                    instances.add(entry.getKey());
            }
        }

        return instances;
    }

    public List<Object> getInstances(final @NotNull Class<?> clazz) {
        return getInstances(clazz.getName());
    }

    public boolean hasListeners(final @NotNull String className) {
        return !getInstances(className).isEmpty();
    }

    public boolean hasListeners(final @NotNull Class<?> clazz) {
        return hasListeners(clazz.getName());
    }

    // clase que llama al evento, evento
    public void call(final @NotNull Class<?> clazz, final @NotNull FeatureEvent event) {
        getInstances(clazz).forEach(instance -> FeatureListenerProcessor.call(instance, event));
    }
    
}
