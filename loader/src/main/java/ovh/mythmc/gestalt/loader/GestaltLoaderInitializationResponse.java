package ovh.mythmc.gestalt.loader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the outcome of a {@link GestaltLoader#initialize()} call.
 *
 * <p>Contains a {@link Type} indicating the overall result and an optional set of
 * {@link Warning}s for non-fatal issues encountered during initialization (e.g., an
 * outdated JAR that could not be deleted).
 */
public class GestaltLoaderInitializationResponse {

    private final Type type;
    private final Set<Warning> warnings = new HashSet<>();

    static GestaltLoaderInitializationResponse success() {
        return new GestaltLoaderInitializationResponse(Type.SUCCESS);
    }

    static GestaltLoaderInitializationResponse alreadyAvailable() {
        return new GestaltLoaderInitializationResponse(Type.ALREADY_AVAILABLE);
    }

    static GestaltLoaderInitializationResponse remoteUnavailable() {
        return new GestaltLoaderInitializationResponse(Type.REMOTE_UNAVAILABLE);
    }

    static GestaltLoaderInitializationResponse directoryUnavailable() {
        return new GestaltLoaderInitializationResponse(Type.DIRECTORY_UNAVAILABLE);
    }

    private GestaltLoaderInitializationResponse(Type type) {
        this.type = type;
    }

    /**
     * Returns the result type of this response.
     *
     * @return the response type
     */
    public Type type() {
        return this.type;
    }

    /**
     * Returns the set of non-fatal warnings that occurred during initialization.
     *
     * @return the set of warnings, possibly empty
     */
    public Set<Warning> warnings() {
        return this.warnings;
    }

    protected GestaltLoaderInitializationResponse warnings(Warning... warnings) {
        this.warnings.addAll(Arrays.asList(warnings));
        return this;
    }

    /**
     * Returns {@code true} if initialization ended in a usable state, i.e., the type is
     * {@link Type#SUCCESS} or {@link Type#ALREADY_AVAILABLE}.
     *
     * @return {@code true} if Gestalt was successfully loaded or was already available
     */
    public boolean isSuccess() {
        return this.type == Type.SUCCESS
            || this.type == Type.ALREADY_AVAILABLE;
    }

    /**
     * Enumerates the possible outcomes of a {@link GestaltLoader#initialize()} call.
     */
    public static enum Type {
        /** Gestalt was downloaded (if needed) and loaded successfully. */
        SUCCESS,
        /** Gestalt was already loaded on this platform; no action was taken. */
        ALREADY_AVAILABLE,
        /** The remote download servers were unreachable and the JAR was not present locally. */
        REMOTE_UNAVAILABLE,
        /** The {@code libs/} directory could not be created due to a filesystem error. */
        DIRECTORY_UNAVAILABLE
    }

    /**
     * Non-fatal warnings that may be attached to a {@link GestaltLoaderInitializationResponse}.
     */
    public static enum Warning {
        /** The remote checksum could not be validated and the local JAR version is uncertain. */
        UNKNOWN_REMOTE_CHECKSUM,
        /** The local Gestalt JAR was outdated but could not be deleted and replaced. */
        OUTDATED
    }

}
