package ovh.mythmc.gestalt.exceptions;

/**
 * Thrown when an attempt is made to initialize a Gestalt instance that has already been set.
 *
 * @see ovh.mythmc.gestalt.GestaltSupplier#set(ovh.mythmc.gestalt.Gestalt)
 */
public final class AlreadyInitializedException extends RuntimeException {

    /**
     * Constructs a new {@code AlreadyInitializedException} with the given message.
     *
     * @param exception the detail message
     */
    public AlreadyInitializedException(String exception) {
        super(exception);
    }

}
