package ovh.mythmc.gestalt.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import ovh.mythmc.gestalt.Gestalt;
import ovh.mythmc.gestalt.features.FeatureConstructorParams;

public final class AnnotationUtil {

    public static void triggerAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotation) {
        FeatureConstructorParams params = Gestalt.get().getParamsRegistry().getParameters(clazz);

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                try {
                    if (params != null) {
                        /*
                        Class<?>[] paramClasses = new Class[params.length];
                        for (int i = 0; i < params.length; i++) {
                            paramClasses[i] = params[i].getClass();
                        }
                        Arrays.stream(paramClasses).forEach(p -> System.out.println("ParamClass " + p.getName() + " for class " + clazz.getName())); */
                        method.invoke(clazz.getDeclaredConstructor(params.getParamTypes()).newInstance(params.getParams()));
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
