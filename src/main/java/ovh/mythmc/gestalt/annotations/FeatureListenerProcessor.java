package ovh.mythmc.gestalt.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.Gestalt;
import ovh.mythmc.gestalt.features.FeatureEvent;

public final class FeatureListenerProcessor {

    public static ArrayList<Class<?>> getListeners(final @NotNull Class<?> clazz) {
        ArrayList<Class<?>> listeners = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(FeatureListener.class))
                listeners.add(method.getAnnotation(FeatureListener.class).feature());
        }

        return listeners;
    }

    public static boolean hasListeners(final @NotNull Class<?> ckazz) {
        return getListeners(ckazz) != null;
    }

    public static void call(final @NotNull Gestalt gestalt, final @NotNull Object instance, final @NotNull FeatureEvent event) {
        for (Method method : instance.getClass().getMethods()) {
            if (method.isAnnotationPresent(FeatureListener.class)) {
                FeatureListener listener = method.getAnnotation(FeatureListener.class);
                boolean isPresent = false;
                if (listener.group().isEmpty() || listener.identifier().isEmpty()) {
                    isPresent = !Arrays.stream(listener.events()).filter(e -> e.equals(event)).toList().isEmpty();
                } else {
                    isPresent = !gestalt.getByGroupAndIdentifier(listener.group(), listener.identifier()).isEmpty();
                }
            
                if (isPresent) {
                    try {
                        method.invoke(instance);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
}
