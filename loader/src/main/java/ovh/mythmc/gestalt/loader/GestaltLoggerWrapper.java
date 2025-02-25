package ovh.mythmc.gestalt.loader;

import java.util.logging.Logger;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public abstract class GestaltLoggerWrapper {

    public boolean isVerbose() { return false; }
    
    public void info(String message) { }

    public void warn(String message) { }

    public void error(String message) { }

    public void verbose(String message) {
        if (isVerbose())
            info(message);
    }

    public static GestaltLoggerWrapper fromLogger(Logger logger, boolean verbose) {
        return new GestaltLoggerWrapper() {

            @Override
            public boolean isVerbose() {
                return verbose;
            }

            @Override
            public void info(String message) {
                logger.info(message);
            }

            @Override
            public void warn(String message) {
                logger.warning(message);
            }

            @Override
            public void error(String message) {
                logger.severe(message);
            }
            
        };
    }

    public static GestaltLoggerWrapper fromComponentLogger(ComponentLogger componentLogger, boolean verbose) {
        return new GestaltLoggerWrapper() {

            @Override
            public boolean isVerbose() {
                return verbose;
            }

            @Override
            public void info(String message) {
                componentLogger.info(message);
            }

            @Override
            public void warn(String message) {
                componentLogger.warn(message);
            }

            @Override
            public void error(String message) {
                componentLogger.error(message);
            }
            
        };
    }

}
