package ovh.mythmc.gestalt.annotations.status;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method on a {@link ovh.mythmc.gestalt.annotations.Feature}-annotated class
 * to be invoked when the feature is disabled via {@link ovh.mythmc.gestalt.Gestalt#disableFeature(Class)}.
 *
 * <p>This callback is fired each time the feature transitions from an enabled to a disabled state.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FeatureDisable {
}
