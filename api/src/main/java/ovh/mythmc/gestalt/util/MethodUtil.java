package ovh.mythmc.gestalt.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.Gestalt;
import ovh.mythmc.gestalt.features.FeatureConstructorParams;

/**
 * Utility class for reflectively invoking methods on feature class instances.
 *
 * <p>Manages a cache of instantiated feature objects (keyed by class name) to avoid
 * repeated instantiation. Constructor parameters, if required, are looked up via the
 * {@link ovh.mythmc.gestalt.features.FeatureConstructorParamsRegistry}.
 */
public class MethodUtil {

    private MethodUtil() {
    }

    private static final Map<String, Object> instances = new HashMap<>();

    /**
     * Invokes all methods on the given feature class that are annotated with the specified
     * lifecycle annotation.
     *
     * @param gestalt    the active Gestalt instance, used to retrieve the singleton feature instance
     * @param clazz      the feature class whose annotated methods should be invoked
     * @param annotation the lifecycle annotation to match (e.g., {@link ovh.mythmc.gestalt.annotations.status.FeatureEnable})
     */
    public static void triggerAnnotatedMethod(@NotNull Gestalt gestalt, Class<?> clazz, Class<? extends Annotation> annotation) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation))
                invoke(gestalt, clazz, method);
        }
    }

    /**
     * Reflectively invokes the given method on the cached instance of the specified feature class.
     *
     * <p>Instantiates the feature class on first call using its registered constructor parameters
     * or a no-arg constructor if none are provided.
     *
     * @param gestalt the active Gestalt instance
     * @param clazz   the feature class to get or create an instance of
     * @param method  the method to invoke
     * @return the return value of the method, or {@code null} if the invocation failed
     */
    public static Object invoke(@NotNull Gestalt gestalt, @NotNull Class<?> clazz, @NotNull Method method) {
        try {
            return method.invoke(getInstance(gestalt, clazz));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object getInstance(@NotNull Gestalt gestalt, @NotNull Class<?> clazz) {
        if (instances.containsKey(clazz.getName()))
            return instances.get(clazz.getName());
        
        FeatureConstructorParams constructorParams = gestalt.getConstructorParamsRegistry().getParameters(clazz);
        try {
            Object instance;
            if (constructorParams == null) {
                instance = clazz.getDeclaredConstructor().newInstance();
            } else {
                instance = clazz.getDeclaredConstructor(constructorParams.getParamTypes()).newInstance(constructorParams.getParams());
            }

            instances.put(clazz.getName(), instance);
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
