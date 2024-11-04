package ovh.mythmc.gestalt.loader;

import org.jetbrains.annotations.NotNull;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;

public class PaperGestaltLibraryLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        PaperGestaltLoader gestalt = PaperGestaltLoader.builder()
            .dataDirectory(classpathBuilder.getContext().getDataDirectory())
            .logger(GestaltLoggerWrapper.fromComponentLogger(classpathBuilder.getContext().getLogger()))
            .build();

        gestalt.initialize();
    }
    
}
