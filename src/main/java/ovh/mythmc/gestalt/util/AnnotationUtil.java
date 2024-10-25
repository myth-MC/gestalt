package ovh.mythmc.gestalt.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import ovh.mythmc.gestalt.Gestalt;

public final class AnnotationUtil {

    public static void triggerAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotation) {
        Object[] params = Gestalt.get().getParamsRegistry().getParameters(clazz);
        System.out.println("Class " + clazz.getName() + " params: " + params.toString());

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                try {
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
