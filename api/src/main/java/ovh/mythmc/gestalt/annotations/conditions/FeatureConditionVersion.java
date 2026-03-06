package ovh.mythmc.gestalt.annotations.conditions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restricts a {@link ovh.mythmc.gestalt.annotations.Feature}-annotated class to specific
 * server versions.
 *
 * <p>If this annotation is present, the feature will only be enabled when the current
 * server version (as reported by {@link ovh.mythmc.gestalt.Gestalt#getServerVersion()})
 * starts with one of the specified version prefixes, or if one of the values is {@code "ALL"}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FeatureConditionVersion {

    /**
     * The server version prefixes that allow this feature to be enabled.
     * Use {@code "ALL"} to allow any server version.
     *
     * @return the allowed version prefixes
     */
    String[] versions();

}
