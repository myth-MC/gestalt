package ovh.mythmc.gestalt.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public final class AnnotationUtil {

    public static void triggerAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotation) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                System.out.println("funciona");
                try {
                    method.invoke(clazz.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /*
    public static Class<?> findAnnotationClass(IFeature instance, Class<? extends Annotation> annotation) {
        Class<?> annotatedClass = null;

        Class<?> cl = instance.getClass();
        while (cl != null) {
            if (!cl.isAnnotationPresent(annotation)) {
                cl = cl.getSuperclass();
                continue;
            }

            annotatedClass = cl;
        }

        return annotatedClass;
    }
         */
    
}
