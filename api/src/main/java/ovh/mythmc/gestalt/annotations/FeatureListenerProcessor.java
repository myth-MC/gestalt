package ovh.mythmc.gestalt.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.RequiredArgsConstructor;
import ovh.mythmc.gestalt.Gestalt;
import ovh.mythmc.gestalt.features.FeatureEvent;

@RequiredArgsConstructor
public final class FeatureListenerProcessor {

    private final Gestalt gestalt;

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

    public ArrayList<Class<?>> getListeners(final @NotNull Class<?> clazz) {
        ArrayList<Class<?>> listeners = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            listeners.addAll(getMethodListeners(method));
        }

        return listeners;
    }

    public boolean hasListeners(final @NotNull Class<?> clazz) {
        return getListeners(clazz) != null;
    }

    public void call(final @NotNull Object instance, final @Nullable Class<?> eventClass, final @NotNull FeatureEvent event) {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(FeatureListener.class)) {
                FeatureListener listener = method.getAnnotation(FeatureListener.class);
                if (!getMethodListeners(method).contains(eventClass))
                    continue;

                boolean isPresent = !Arrays.stream(listener.events()).filter(e -> e.equals(event)).toList().isEmpty();
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
