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
            if (method.isAnnotationPresent(FeatureListener.class)) {
                FeatureListener listener = method.getAnnotation(FeatureListener.class);
                if (listener.feature() != Feature.class) { // We give priority to defined class instead of group and identifier
                    listeners.add(method.getAnnotation(FeatureListener.class).feature());
                    break;
                }

                Gestalt.get().getByGroupAndIdentifier(listener.group(), listener.identifier()).forEach(listeners::add);
            }
                
        }

        return listeners;
    }

    public static boolean hasListeners(final @NotNull Class<?> ckazz) {
        return getListeners(ckazz) != null;
    }

    public static void call(final @NotNull Object instance, final @NotNull FeatureEvent event) {
        for (Method method : instance.getClass().getMethods()) {
            if (method.isAnnotationPresent(FeatureListener.class)) {
                FeatureListener listener = method.getAnnotation(FeatureListener.class);
                boolean isPresent = !Arrays.stream(listener.events()).filter(e -> e.equals(event)).toList().isEmpty();
            
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
