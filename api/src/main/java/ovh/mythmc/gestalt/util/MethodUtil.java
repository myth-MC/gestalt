package ovh.mythmc.gestalt.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import lombok.experimental.UtilityClass;
import ovh.mythmc.gestalt.Gestalt;
import ovh.mythmc.gestalt.features.FeatureConstructorParams;

@UtilityClass
public class MethodUtil {

    private final Map<String, Object> instances = new HashMap<>();

    public void triggerAnnotatedMethod(@NotNull Gestalt gestalt, Class<?> clazz, Class<? extends Annotation> annotation) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation))
                invoke(gestalt, clazz, method);
        }
    }

    public Object invoke(@NotNull Gestalt gestalt, @NotNull Class<?> clazz, @NotNull Method method) {
        try {
            return method.invoke(getInstance(gestalt, clazz));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Object getInstance(@NotNull Gestalt gestalt, @NotNull Class<?> clazz) {
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
