package ovh.mythmc.gestalt.features;

/**
 * Defines the execution priority of a {@link ovh.mythmc.gestalt.annotations.Feature}-annotated class.
 *
 * <p>Priority determines the order in which features are enabled and disabled when
 * {@link ovh.mythmc.gestalt.Gestalt#enableAllFeatures()} or
 * {@link ovh.mythmc.gestalt.Gestalt#disableAllFeatures()} is called.
 * Features with {@link #HIGHEST} priority are processed first.
 */
public enum FeaturePriority {
    /** Highest priority; processed before all others. */
    HIGHEST,
    /** High priority; processed second. */
    HIGH,
    /** Default priority. */
    NORMAL,
    /** Low priority; processed fourth. */
    LOW,
    /** Lowest priority; processed last. */
    LOWEST
}
