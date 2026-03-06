package ovh.mythmc.gestalt.loader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GestaltLoaderResponse {

    private final Type type;
    private final Set<Warning> warnings = new HashSet<>();

    static GestaltLoaderResponse success() {
        return new GestaltLoaderResponse(Type.SUCCESS);
    }

    static GestaltLoaderResponse alreadyAvailable() {
        return new GestaltLoaderResponse(Type.ALREADY_AVAILABLE);
    }

    static GestaltLoaderResponse remoteUnavailable() {
        return new GestaltLoaderResponse(Type.REMOTE_UNAVAILABLE);
    }

    static GestaltLoaderResponse directoryUnavailable() {
        return new GestaltLoaderResponse(Type.DIRECTORY_UNAVAILABLE);
    }

    private GestaltLoaderResponse(Type type) {
        this.type = type;
    }

    public Type type() {
        return this.type;
    }

    public Set<Warning> warnings() {
        return this.warnings;
    }

    protected GestaltLoaderResponse warnings(Warning... warnings) {
        this.warnings.addAll(Arrays.asList(warnings));
        return this;
    }

    // Helper methods
    public boolean isSuccess() {
        return this.type == Type.SUCCESS
            || this.type == Type.ALREADY_AVAILABLE;
    }
    
    public static enum Type {
        SUCCESS,
        ALREADY_AVAILABLE,
        REMOTE_UNAVAILABLE,
        DIRECTORY_UNAVAILABLE
    }

    public static enum Warning {
        UNKNOWN_REMOTE_CHECKSUM,
        OUTDATED
    }

}
