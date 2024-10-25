package ovh.mythmc.gestalt.annotations.conditions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.Gestalt;

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
        boolean b = false;
        
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(FeatureConditionBoolean.class)) {
                b = (boolean) method.invoke(clazz.getDeclaredConstructor().newInstance());
            } else {
                return true;
            }
        }

        return b;
    }

    private static boolean versionCondition(@NotNull Class<?> clazz) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
        Collection<String> versions = new ArrayList<>();
        
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(FeatureConditionVersion.class)) {
                Collection<?> objectList = (Collection<?>) method.invoke(clazz.getDeclaredConstructor().newInstance());
                versions = objectList.stream()
                    .filter(o -> o instanceof String)
                    .map(o -> (String) o)
                    .collect(Collectors.toList());
            }
        }

        if (versions.isEmpty())
            return true;

        for (String version : versions) {
            if (version.equalsIgnoreCase("ALL") || Gestalt.get().getServerVersion().startsWith(version))
                return true;
        }

        return false;
    }

    
}
