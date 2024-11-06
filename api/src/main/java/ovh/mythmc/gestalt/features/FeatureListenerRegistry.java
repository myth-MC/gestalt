package ovh.mythmc.gestalt.features;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import lombok.RequiredArgsConstructor;
import ovh.mythmc.gestalt.Gestalt;

@RequiredArgsConstructor
public final class FeatureListenerRegistry {

    private final Gestalt gestalt;

    private final List<Object> listenerRegistry = new ArrayList<>();

    public void register(final @NotNull Object instance) {
        listenerRegistry.add(instance);
    }

    public void unregister(final @NotNull Object instance) {
        listenerRegistry.remove(instance);
    }

    public void unregisterAllListeners(final @NotNull String className) {
        getInstances(className).forEach(listenerRegistry::remove);
    }

    public void unregisterAllListeners(final @NotNull Class<?> clazz) {
        unregisterAllListeners(clazz.getName());
    }

    public boolean isRegistered(final @NotNull Object instance) {
        return listenerRegistry.contains(instance);
    }

    public List<Object> getInstances(final @NotNull String className) {
        List<Object> instances = new ArrayList<>();

        listenerRegistry.forEach(instance -> {
            gestalt.getListenerProcessor().getListeners(instance.getClass()).forEach(clazz -> {
                if (clazz.getName().equals(className))
                    instances.add(instance);
            });
        });

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

    public void call(final @NotNull Class<?> clazz, final @NotNull FeatureEvent event) {
        getInstances(clazz).forEach(instance -> gestalt.getListenerProcessor().call(instance, event));
    }
    
}
