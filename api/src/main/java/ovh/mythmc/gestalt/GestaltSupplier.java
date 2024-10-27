package ovh.mythmc.gestalt;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.exceptions.AlreadyInitializedException;
import ovh.mythmc.gestalt.exceptions.NotInitializedException;

public final class GestaltSupplier {

    private static Gestalt gestalt;

    private GestaltSupplier() {
    }

    public static void set(final @NotNull Gestalt g) {
        if (gestalt != null)
            throw new AlreadyInitializedException("Gestalt has already been initialized by another plugin!");

        gestalt = g;
    }

    public static Gestalt get() { 
        if (gestalt == null)
            throw new NotInitializedException();
            
        return gestalt; 
    }

    public static boolean isAvailable() { return gestalt != null; }
    
}
