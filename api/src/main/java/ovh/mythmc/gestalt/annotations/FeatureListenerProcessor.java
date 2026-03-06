package ovh.mythmc.gestalt.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ovh.mythmc.gestalt.Gestalt;
import ovh.mythmc.gestalt.features.FeatureEvent;

/**
 * Processes {@link FeatureListener} annotations and dispatches feature lifecycle events
 * to the appropriate listener methods.
 *
 * <p>This processor is responsible for resolving which feature classes a method listens to
 * and for invoking listener methods when a {@link FeatureEvent} is fired.
 */
public final class FeatureListenerProcessor {

    private final Gestalt gestalt;

    /**
     * Constructs a new {@code FeatureListenerProcessor} for the given {@link Gestalt} instance.
     *
     * @param gestalt the active Gestalt instance
     */
    public FeatureListenerProcessor(@NotNull Gestalt gestalt) {
        this.gestalt = gestalt;
    }

    /**
     * Returns the list of feature classes that the given method listens to,
     * based on its {@link FeatureListener} annotation.
     *
     * <p>If a specific {@link FeatureListener#feature()} class is defined (other than the
     * default {@link Feature}), it takes priority. Otherwise, features are resolved by
     * {@link FeatureListener#group()} and {@link FeatureListener#identifier()}.
     *
     * @param method the method to inspect
     * @return a list of feature classes the method listens to
     */
    public ArrayList<Class<?>> getMethodListeners(final @NotNull Method method) {
        ArrayList<Class<?>> methodListeners = new ArrayList<>();
        if (method.isAnnotationPresent(FeatureListener.class)) {
            FeatureListener listener = method.getAnnotation(FeatureListener.class);
            if (listener.feature() != Feature.class) { // We give priority to defined class before searching by group and identifier
                methodListeners.add(listener.feature());
            }

            gestalt.getByGroupAndIdentifier(listener.group(), listener.identifier()).forEach(methodListeners::add);
        }

        return methodListeners;
    }

    /**
     * Returns all feature classes that any method in the given class listens to.
     *
     * @param clazz the class to inspect for {@link FeatureListener}-annotated methods
     * @return a list of all feature classes listened to by methods in the class
     */
    public ArrayList<Class<?>> getListeners(final @NotNull Class<?> clazz) {
        ArrayList<Class<?>> listeners = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            listeners.addAll(getMethodListeners(method));
        }

        return listeners;
    }

    /**
     * Returns whether the given class has any {@link FeatureListener}-annotated methods.
     *
     * @param clazz the class to check
     * @return {@code true} if the class has at least one listener method
     */
    public boolean hasListeners(final @NotNull Class<?> clazz) {
        return getListeners(clazz) != null;
    }

    /**
     * Invokes all {@link FeatureListener}-annotated methods on the given instance that
     * are listening to the specified feature class and event.
     *
     * @param instance   the listener object to call methods on
     * @param eventClass the feature class that triggered the event, or {@code null}
     * @param event      the lifecycle event that was fired
     */
    public void call(final @NotNull Object instance, final @Nullable Class<?> eventClass, final @NotNull FeatureEvent event) {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(FeatureListener.class)) {
                final FeatureListener listener = method.getAnnotation(FeatureListener.class);
                if (!getMethodListeners(method).contains(eventClass))
                    continue;

                final boolean isPresent = Arrays.asList(listener.events()).contains(event);
                //boolean isPresent = !Arrays.stream(listener.events()).filter(e -> e.equals(event)).toList().isEmpty();
                if (isPresent) {
                    try {
                        method.invoke(instance);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        e.getCause().printStackTrace(System.err);
                    }
                }
            }
        }
    }
    
}
