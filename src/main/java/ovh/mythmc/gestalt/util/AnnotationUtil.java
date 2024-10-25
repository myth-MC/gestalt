package ovh.mythmc.gestalt.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import ovh.mythmc.gestalt.Gestalt;

public final class AnnotationUtil {

    public static void triggerAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotation) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                try {
                    Object[] params = Gestalt.get().getParamsRegistry().getParameters(clazz);
                    if (params != null) {
                        Class<?>[] paramClasses = new Class[params.length];
                        for (int i = 0; i < params.length; i++) {
                            paramClasses[i] = params[i].getClass();
                        }
                        method.invoke(clazz.getDeclaredConstructor(paramClasses).newInstance(params));
                        return;
                    }

                    method.invoke(clazz.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
