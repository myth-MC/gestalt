package ovh.mythmc.gestalt.loader;

import java.util.logging.Logger;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

/**
 * An abstract logger wrapper that adapts various logging backends for use by {@link GestaltLoader}.
 *
 * <p>Provides structured logging methods ({@link #info}, {@link #warn}, {@link #error})
 * and an opt-in {@link #verbose} channel that only emits when {@link #isVerbose()} returns
 * {@code true}.
 *
 * <p>Use the static factory methods to create instances backed by a {@link java.util.logging.Logger}
 * or a {@link net.kyori.adventure.text.logger.slf4j.ComponentLogger}.
 */
public abstract class GestaltLoggerWrapper {

    /**
     * Returns whether verbose (debug-level) logging is enabled.
     * Defaults to {@code false}.
     *
     * @return {@code true} if verbose logging is active
     */
    public boolean isVerbose() { return false; }
    
    /**
     * Logs an informational message.
     *
     * @param message the message to log
     */
    public void info(String message) { }

    /**
     * Logs a warning message.
     *
     * @param message the message to log
     */
    public void warn(String message) { }

    /**
     * Logs an error message.
     *
     * @param message the message to log
     */
    public void error(String message) { }

    /**
     * Logs a verbose (debug) message. Only emits output when {@link #isVerbose()} returns
     * {@code true}, delegating to {@link #info(String)}.
     *
     * @param message the message to log
     */
    public void verbose(String message) {
        if (isVerbose())
            info(message);
    }

    /**
     * Creates a {@link GestaltLoggerWrapper} backed by a {@link java.util.logging.Logger}.
     * All messages are prefixed with {@code [gestalt]}.
     *
     * @param logger  the JUL logger to delegate to
     * @param verbose whether verbose logging should be enabled
     * @return a new {@link GestaltLoggerWrapper} instance
     */
    public static GestaltLoggerWrapper fromLogger(Logger logger, boolean verbose) {
        return new GestaltLoggerWrapper() {

            @Override
            public boolean isVerbose() {
                return verbose;
            }

            @Override
            public void info(String message) {
                logger.info("[gestalt] " + message);
            }

            @Override
            public void warn(String message) {
                logger.warning("[gestalt] " + message);
            }

            @Override
            public void error(String message) {
                logger.severe("[gestalt] " + message);
            }
            
        };
    }

    /**
     * Creates a {@link GestaltLoggerWrapper} backed by a
     * {@link net.kyori.adventure.text.logger.slf4j.ComponentLogger}.
     * All messages are prefixed with {@code [gestalt]}.
     *
     * @param componentLogger the Adventure component logger to delegate to
     * @param verbose         whether verbose logging should be enabled
     * @return a new {@link GestaltLoggerWrapper} instance
     */
    public static GestaltLoggerWrapper fromComponentLogger(ComponentLogger componentLogger, boolean verbose) {
        return new GestaltLoggerWrapper() {

            @Override
            public boolean isVerbose() {
                return verbose;
            }

            @Override
            public void info(String message) {
                componentLogger.info("[gestalt] " + message);
            }

            @Override
            public void warn(String message) {
                componentLogger.warn("[gestalt] " + message);
            }

            @Override
            public void error(String message) {
                componentLogger.error("[gestalt] " + message);
            }
            
        };
    }

}
