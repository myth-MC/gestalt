package ovh.mythmc.gestalt.annotations.status;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method on a {@link ovh.mythmc.gestalt.annotations.Feature}-annotated class
 * to be invoked when the feature is unregistered via {@link ovh.mythmc.gestalt.Gestalt#unregister(Class[])}.
 *
 * <p>This is the final lifecycle callback executed and is invoked once during feature teardown.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FeatureShutdown {
}
