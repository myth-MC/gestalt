package ovh.mythmc.gestalt.annotations.conditions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.Gestalt;
import ovh.mythmc.gestalt.util.MethodUtil;

/**
 * Evaluates whether a {@link Feature}-annotated class satisfies all conditions
 * required to be enabled.
 *
 * <p>Conditions are checked via:
 * <ul>
 *   <li>Methods annotated with {@link FeatureConditionBoolean}, which must return {@code true}</li>
 *   <li>The {@link FeatureConditionVersion} annotation, which restricts features to specific server versions</li>
 * </ul>
 */
public final class FeatureConditionProcessor {

    private final Gestalt gestalt;

    /**
     * Constructs a new {@code FeatureConditionProcessor} for the given {@link Gestalt} instance.
     *
     * @param gestalt the active Gestalt instance
     */
    public FeatureConditionProcessor(@NotNull Gestalt gestalt) {
        this.gestalt = gestalt;
    }

    /**
     * Determines whether the given feature class satisfies all conditions required to be enabled.
     *
     * <p>Returns {@code false} if any condition check throws an exception, logging the stack trace.
     *
     * @param clazz the feature class to evaluate
     * @return {@code true} if all conditions are met, {@code false} otherwise
     */
    public boolean canBeEnabled(@NotNull Class<?> clazz) {
        try {
            return booleanCondition(clazz) && versionCondition(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean booleanCondition(@NotNull Class<?> clazz) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
        boolean b = true;
        
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(FeatureConditionBoolean.class))
                b = (boolean) MethodUtil.invoke(gestalt, clazz, method);
        }

        return b;
    }

    private boolean versionCondition(@NotNull Class<?> clazz) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
        String[] versions = null;

        if (clazz.isAnnotationPresent(FeatureConditionVersion.class))
            versions = clazz.getAnnotation(FeatureConditionVersion.class).versions();

        if (versions == null)
            return true;

        for (String version : versions) {
            if (version.equalsIgnoreCase("ALL") || gestalt.getServerVersion().startsWith(version))
                return true;
        }

        return false;
    }
    
}
