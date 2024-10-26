package ovh.mythmc.gestalt.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.Gestalt;
import ovh.mythmc.gestalt.features.FeatureConstructorParams;

public final class MethodUtil {

    private static final Map<String, Object> instances = new HashMap<>();

    public static void triggerAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotation) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation))
                invoke(clazz, method);
        }
    }

    public static Object invoke(@NotNull Class<?> clazz, @NotNull Method method) {
        try {
            return method.invoke(getInstance(clazz));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object getInstance(@NotNull Class<?> clazz) {
        if (instances.containsKey(clazz.getName()))
            return instances.get(clazz.getName());
        
        FeatureConstructorParams constructorParams = Gestalt.get().getParamsRegistry().getParameters(clazz);
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
