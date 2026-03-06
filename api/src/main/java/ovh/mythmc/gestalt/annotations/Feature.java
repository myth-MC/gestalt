package ovh.mythmc.gestalt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ovh.mythmc.gestalt.features.FeaturePriority;

/**
 * Marks a class as a Gestalt-managed feature.
 *
 * <p>Classes annotated with {@code @Feature} can be registered with {@link ovh.mythmc.gestalt.Gestalt}
 * and participate in the full feature lifecycle: initialization, enabling, disabling, and shutdown.
 *
 * <p>This annotation is inherited, meaning subclasses of a {@code @Feature}-annotated class
 * are also considered features.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Feature {

    /**
     * The group this feature belongs to. Used to batch-enable or batch-disable related features.
     *
     * @return the group name
     */
    String group();

    /**
     * A unique identifier for this feature within its group.
     *
     * @return the feature identifier
     */
    String identifier();

    /**
     * The priority that determines the order in which this feature is enabled or disabled.
     * Defaults to {@link FeaturePriority#NORMAL}.
     *
     * @return the feature priority
     */
    FeaturePriority priority() default FeaturePriority.NORMAL;
    
}
