package ovh.mythmc.gestalt;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.exceptions.AlreadyInitializedException;

public final class GestaltSupplier {

    private static Gestalt gestalt;

    public static void set(final @NotNull Gestalt g) {
        if (gestalt != null)
            throw new AlreadyInitializedException("Gestalt has already been initialized!");

        gestalt = g;
    }

    public static @NotNull Gestalt get() { return gestalt; }
    
}
