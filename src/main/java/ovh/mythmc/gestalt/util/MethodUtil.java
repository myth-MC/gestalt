package ovh.mythmc.gestalt.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.Gestalt;
import ovh.mythmc.gestalt.features.FeatureConstructorParams;

public final class MethodUtil {

    public static void triggerAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotation) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation))
                invoke(clazz, method);
        }
    }

    public static Object invoke(@NotNull Class<?> clazz, @NotNull Method method) {
        FeatureConstructorParams params = Gestalt.get().getParamsRegistry().getParameters(clazz);
        try {
            if (params == null)
                return method.invoke(clazz.getDeclaredConstructor().newInstance());

            return method.invoke(clazz.getDeclaredConstructor(params.getParamTypes()).newInstance(params.getParams()));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | InstantiationException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        };

        return null;
    }

}
