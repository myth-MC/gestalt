package ovh.mythmc.gestalt.features;

/**
 * Represents the lifecycle events that can be fired for a registered
 * {@link ovh.mythmc.gestalt.annotations.Feature}-annotated class.
 *
 * <p>These events are dispatched to all instances registered in the
 * {@link FeatureListenerRegistry} when a feature transitions between states.
 */
public enum FeatureEvent {

    /** Fired when a feature class is first registered with Gestalt. */
    INITIALIZE,

    /** Fired when a feature transitions from disabled to enabled. */
    ENABLE,

    /** Fired when a feature transitions from enabled to disabled. */
    DISABLE,

    /** Fired when a feature class is unregistered from Gestalt. */
    SHUTDOWN
    
}
