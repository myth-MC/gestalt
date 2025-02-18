package ovh.mythmc.gestalt.loader.callbacks;

import ovh.mythmc.callbacks.annotations.v1.Callback;
import ovh.mythmc.gestalt.loader.GestaltLoader;

@Callback
public final record GestaltInitialize(GestaltLoader loader) { 

}
