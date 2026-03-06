package ovh.mythmc.gestalt.annotations.status;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method on a {@link ovh.mythmc.gestalt.annotations.Feature}-annotated class
 * to be invoked when the feature is first registered with {@link ovh.mythmc.gestalt.Gestalt}.
 *
 * <p>This is the first lifecycle callback executed. It is called once, before the feature
 * is enabled for the first time.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FeatureInitialize {
}
