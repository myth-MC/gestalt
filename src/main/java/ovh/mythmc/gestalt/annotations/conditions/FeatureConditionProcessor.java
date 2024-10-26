package ovh.mythmc.gestalt.annotations.conditions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.Gestalt;
import ovh.mythmc.gestalt.util.MethodUtil;

public final class FeatureConditionProcessor {

    public static boolean canBeEnabled(@NotNull Class<?> clazz) {
        try {
            return booleanCondition(clazz) && versionCondition(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean booleanCondition(@NotNull Class<?> clazz) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
        boolean b = true;
        
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(FeatureConditionBoolean.class))
                b = (boolean) MethodUtil.invoke(clazz, method);
        }

        return b;
    }

    private static boolean versionCondition(@NotNull Class<?> clazz) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
        String[] versions = null;

        if (clazz.isAnnotationPresent(FeatureConditionVersion.class))
            versions = clazz.getAnnotation(FeatureConditionVersion.class).versions();

        if (versions == null)
            return true;

        for (String version : versions) {
            if (version.equalsIgnoreCase("ALL") || Gestalt.get().getServerVersion().startsWith(version))
                return true;
        }

        return false;
    }
    
}
