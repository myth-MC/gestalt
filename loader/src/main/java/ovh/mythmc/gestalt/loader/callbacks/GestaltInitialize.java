package ovh.mythmc.gestalt.loader.callbacks;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.callbacks.v1.annotations.Callback;
import ovh.mythmc.gestalt.callbacks.v1.annotations.CallbackFieldGetter;
import ovh.mythmc.gestalt.loader.GestaltLoader;

@Callback
public final class GestaltInitialize {

    @CallbackFieldGetter("loader")
    private final GestaltLoader loader;

    public GestaltInitialize(final @NotNull GestaltLoader loader) {
        this.loader = loader;
    }

    public GestaltLoader loader() { return this.loader; }
    
}
