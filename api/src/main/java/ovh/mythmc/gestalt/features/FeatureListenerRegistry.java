package ovh.mythmc.gestalt.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Experimental;

import ovh.mythmc.gestalt.Gestalt;

/**
 * Manages the registry of listener objects that receive {@link FeatureEvent} notifications.
 *
 * <p>Objects are registered via {@link #register(Object)} and are notified of feature lifecycle
 * events by {@link #call(Class, FeatureEvent)}. The registry uses {@link ovh.mythmc.gestalt.annotations.FeatureListenerProcessor}
 * to resolve which methods on each listener object should be invoked for a given event.
 */
public final class FeatureListenerRegistry {

    private final Gestalt gestalt;

    private final List<Object> listenerRegistry = new ArrayList<>();

    /**
     * Constructs a new {@code FeatureListenerRegistry} for the given {@link Gestalt} instance.
     *
     * @param gestalt the active Gestalt instance
     */
    public FeatureListenerRegistry(@NotNull Gestalt gestalt) {
        this.gestalt = gestalt;
    }

    /**
     * Registers a listener object to receive feature lifecycle events.
     *
     * @param instance the listener object to register
     */
    public void register(final @NotNull Object instance) {
        listenerRegistry.add(instance);
    }

    /**
     * Registers a listener object and optionally immediately dispatches {@link FeatureEvent#ENABLE}
     * for all currently enabled features.
     *
     * @param instance                the listener object to register
     * @param callEventWhenRegistered if {@code true}, fires {@link FeatureEvent#ENABLE} for every
     *                                currently enabled feature immediately upon registration
     */
    @Experimental
    public void register(final @NotNull Object instance, boolean callEventWhenRegistered) {
        register(instance);
        if (callEventWhenRegistered) {
            gestalt.getEnabledClasses().forEach(clazz -> gestalt.getListenerProcessor().call(instance, clazz, FeatureEvent.ENABLE));
        }
    }

    /**
     * Unregisters a listener object so it no longer receives feature lifecycle events.
     *
     * @param instance the listener object to unregister
     */
    public void unregister(final @NotNull Object instance) {
        listenerRegistry.remove(instance);
    }

    /**
     * Unregisters all listener objects associated with the specified class name.
     *
     * @param className the fully qualified name of the listener class to remove
     */
    public void unregisterAllListeners(final @NotNull String className) {
        getInstances(className).forEach(listenerRegistry::remove);
    }

    /**
     * Unregisters all listener objects that are instances of the specified class.
     *
     * @param clazz the listener class whose instances should be removed
     */
    public void unregisterAllListeners(final @NotNull Class<?> clazz) {
        unregisterAllListeners(clazz.getName());
    }

    /**
     * Returns whether the given object is currently registered as a listener.
     *
     * @param instance the object to check
     * @return {@code true} if the object is registered
     */
    public boolean isRegistered(final @NotNull Object instance) {
        return listenerRegistry.contains(instance);
    }

    /**
     * Returns all registered listener objects that listen to the feature identified by
     * the given class name.
     *
     * @param className the fully qualified name of the feature class
     * @return a set of listener instances that listen to the specified feature
     */
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

    /**
     * Returns all registered listener objects that listen to the given feature class.
     *
     * @param clazz the feature class
     * @return a set of listener instances that listen to the specified feature
     */
    public Set<Object> getInstances(final @NotNull Class<?> clazz) {
        return getInstances(clazz.getName());
    }

    /**
     * Returns whether any listener is registered for the feature identified by the given class name.
     *
     * @param className the fully qualified name of the feature class
     * @return {@code true} if at least one listener is registered for the feature
     */
    public boolean hasListeners(final @NotNull String className) {
        return !getInstances(className).isEmpty();
    }

    /**
     * Returns whether any listener is registered for the given feature class.
     *
     * @param clazz the feature class
     * @return {@code true} if at least one listener is registered for the feature
     */
    public boolean hasListeners(final @NotNull Class<?> clazz) {
        return hasListeners(clazz.getName());
    }

    /**
     * Dispatches the given {@link FeatureEvent} to all listener objects registered for the
     * specified feature class.
     *
     * @param clazz the feature class that triggered the event
     * @param event the lifecycle event to dispatch
     */
    public void call(final @NotNull Class<?> clazz, final @NotNull FeatureEvent event) {
        getInstances(clazz).forEach(instance -> gestalt.getListenerProcessor().call(instance, clazz, event));
    }
    
}
