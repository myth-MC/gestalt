package ovh.mythmc.gestalt.annotations.conditions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method on a {@link ovh.mythmc.gestalt.annotations.Feature}-annotated class
 * as a boolean condition that must return {@code true} for the feature to be enabled.
 *
 * <p>The annotated method must be public, take no parameters, and return a {@code boolean}.
 * It will be invoked by {@link FeatureConditionProcessor} during condition evaluation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FeatureConditionBoolean {
}
