package ovh.mythmc.gestalt.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.IGestalt;
import ovh.mythmc.gestalt.features.FeatureConstructorParams;

public final class MethodUtil {

    private static final Map<String, Object> instances = new HashMap<>();

    private MethodUtil() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void triggerAnnotatedMethod(@NotNull IGestalt gestalt, Class<?> clazz, Class<? extends Annotation> annotation) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation))
                invoke(gestalt, clazz, method);
        }
    }

    public static Object invoke(@NotNull IGestalt gestalt, @NotNull Class<?> clazz, @NotNull Method method) {
        try {
            return method.invoke(getInstance(gestalt, clazz));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object getInstance(@NotNull IGestalt gestalt, @NotNull Class<?> clazz) {
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
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

}
