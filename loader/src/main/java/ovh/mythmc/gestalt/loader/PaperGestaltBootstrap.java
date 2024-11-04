package ovh.mythmc.gestalt.loader;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;

public class PaperGestaltBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        PaperGestaltLoader gestalt = PaperGestaltLoader.builder()
            .dataDirectory(context.getDataDirectory())
            .logger(GestaltLoggerWrapper.fromComponentLogger(context.getLogger()))
            .build();

        gestalt.initialize();
    }
    
}
