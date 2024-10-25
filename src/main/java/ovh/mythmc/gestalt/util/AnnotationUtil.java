package ovh.mythmc.gestalt.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import ovh.mythmc.gestalt.Gestalt;

public final class AnnotationUtil {

    public static void triggerAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotation) {
        Object[] params = Gestalt.get().getParamsRegistry().getParameters(clazz);
        if (params != null)
            Arrays.stream(params).forEach(p -> System.out.println("Param " + p.getClass().getName() + " for class " + clazz.getName()));

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                try {
                    if (params != null) {
                        Class<?>[] paramClasses = new Class[params.length];
                        for (int i = 0; i < params.length; i++) {
                            paramClasses[i] = params[i].getClass();
                        }
                        Arrays.stream(paramClasses).forEach(p -> System.out.println("ParamClass " + p.getName() + " for class " + clazz.getName()));
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
