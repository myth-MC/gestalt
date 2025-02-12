package ovh.mythmc.gestalt.loader.callbacks;

import ovh.mythmc.gestalt.callbacks.v1.annotations.Callback;
import ovh.mythmc.gestalt.loader.GestaltLoader;

@Callback
public final record GestaltShutdown(GestaltLoader loader) { 

}
