package ovh.mythmc.gestalt.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.Gestalt;
import ovh.mythmc.gestalt.features.FeatureEvent;

public final class FeatureListenerProcessor {

    private final Gestalt gestalt;

    public FeatureListenerProcessor(@NotNull Gestalt gestalt) {
        this.gestalt = gestalt;
    }

    public ArrayList<Class<?>> getListeners(final @NotNull Class<?> clazz) {
        ArrayList<Class<?>> listeners = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(FeatureListener.class)) {
                FeatureListener listener = method.getAnnotation(FeatureListener.class);
                if (listener.feature() != Feature.class) { // We give priority to defined class before searching by group and identifier
                    listeners.add(method.getAnnotation(FeatureListener.class).feature());
                    break;
                }

                gestalt.getByGroupAndIdentifier(listener.group(), listener.identifier()).forEach(listeners::add);
            }
                
        }

        System.out.println("Listeners for class " + clazz + ":" + listeners);
        return listeners;
    }

    public boolean hasListeners(final @NotNull Class<?> clazz) {
        return getListeners(clazz) != null;
    }

    public void call(final @NotNull Object instance, final @NotNull FeatureEvent event) {
        System.out.println("Calling " + event + " on " + instance);
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
