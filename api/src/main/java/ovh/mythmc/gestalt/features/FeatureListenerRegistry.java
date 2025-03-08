package ovh.mythmc.gestalt.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Experimental;

import lombok.RequiredArgsConstructor;
import ovh.mythmc.gestalt.Gestalt;

@RequiredArgsConstructor
public final class FeatureListenerRegistry {

    private final Gestalt gestalt;

    private final List<Object> listenerRegistry = new ArrayList<>();

    public void register(final @NotNull Object instance) {
        listenerRegistry.add(instance);
    }

    @Experimental
    public void register(final @NotNull Object instance, boolean callEventWhenRegistered) {
        register(instance);
        if (callEventWhenRegistered) {
            gestalt.getEnabledClasses().forEach(clazz -> gestalt.getListenerProcessor().call(instance, clazz, FeatureEvent.ENABLE));
        }
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

    public Set<Object> getInstances(final @NotNull String className) {
        Set<Object> instances = new HashSet<>();

        listenerRegistry.forEach(instance -> {
            if (instances.contains(instance))
                return;

            gestalt.getListenerProcessor().getListeners(instance.getClass()).forEach(clazz -> {
                if (clazz.getName().equals(className))
                    instances.add(instance);
            });
        });

        return instances;
    }

    public Set<Object> getInstances(final @NotNull Class<?> clazz) {
        return getInstances(clazz.getName());
    }

    public boolean hasListeners(final @NotNull String className) {
        return !getInstances(className).isEmpty();
    }

    public boolean hasListeners(final @NotNull Class<?> clazz) {
        return hasListeners(clazz.getName());
    }

    public void call(final @NotNull Class<?> clazz, final @NotNull FeatureEvent event) {
        getInstances(clazz).forEach(instance -> gestalt.getListenerProcessor().call(instance, clazz, event));
    }
    
}
