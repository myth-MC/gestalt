package ovh.mythmc.gestalt;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.exceptions.AlreadyInitializedException;

/**
 * Static holder for the active {@link Gestalt} instance.
 *
 * <p>The instance can be set exactly once via {@link #set(Gestalt)}; subsequent
 * calls will throw an {@link AlreadyInitializedException}. The stored instance
 * is retrieved via {@link #get()}.
 */
public final class GestaltSupplier {

    private static Gestalt gestalt;

    /**
     * Sets the active {@link Gestalt} instance.
     *
     * @param g the Gestalt instance to register
     * @throws AlreadyInitializedException if a Gestalt instance has already been set
     */
    public static void set(final @NotNull Gestalt g) {
        if (gestalt != null)
            throw new AlreadyInitializedException("Gestalt has already been initialized!");

        gestalt = g;
    }

    /**
     * Returns the active {@link Gestalt} instance.
     *
     * @return the active Gestalt instance
     */
    public static @NotNull Gestalt get() { 
        return gestalt; 
    }
    
}
