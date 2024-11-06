package ovh.mythmc.gestalt.features;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.Gestalt;

public final class FeatureListenerRegistry {

    private final Gestalt gestalt;

    // Instance that is listening for events, Class name of event that instance listens to
    // Instance that is listening for events, group:identifier (for example [ social:reactions, social:test, ... ])
    //private final Map<Object, String[]> listenerRegistry = new HashMap<>();
    private final List<Object> listenerRegistry = new ArrayList<>();

    public FeatureListenerRegistry(@NotNull Gestalt gestalt) {
        this.gestalt = gestalt;
    }

    public void register(final @NotNull Object instance) {
        /*
        System.out.println("Registering listeners for " + instance);
        String[] classes = gestalt.getListenerProcessor().getListeners(instance.getClass()).stream()
            .map(clazz -> clazz.getName())
            .toArray(String[]::new);

        listenerRegistry.put(instance, classes);
        */
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

    /*
    public void unregisterAllListeners(final @NotNull String className) {
        Set.copyOf(listenerRegistry.entrySet()).stream()
            .filter(entry -> entry.getValue().equals(className))
            .forEach(entry -> listenerRegistry.remove(entry.getValue()));
    } */

    /*
    public void unregisterAllListeners(final @NotNull Class<?> clazz) {
        unregister(clazz.getName());
    }
    */

    public boolean isRegistered(final @NotNull Object instance) {
        return listenerRegistry.contains(instance);
        //return listenerRegistry.containsKey(instance);
    }

    public List<Object> getInstances(final @NotNull String className) {
        List<Object> instances = new ArrayList<>();
        /*
        for (Map.Entry<Object, String[]> entry : listenerRegistry.entrySet()) {
            for (String value : entry.getValue()) {
                if (value.equals(className))
                    instances.add(entry.getKey());
            }
        }
        */

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
