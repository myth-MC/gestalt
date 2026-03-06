package ovh.mythmc.gestalt.features;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores custom constructor parameters for {@link ovh.mythmc.gestalt.annotations.Feature}-annotated classes
 * that cannot be instantiated with a no-arg constructor.
 *
 * <p>When a feature requires constructor arguments, the corresponding {@link FeatureConstructorParams}
 * should be registered here before the feature class is registered with
 * {@link ovh.mythmc.gestalt.Gestalt}. The parameters are keyed by fully qualified class name.
 */
public final class FeatureConstructorParamsRegistry {

    private final Map<String, FeatureConstructorParams> paramsRegistry = new HashMap<>();

    /**
     * Associates the given constructor parameters with the specified class name.
     *
     * @param className the fully qualified name of the feature class
     * @param params    the constructor parameters to store, or {@code null} to clear them
     */
    public void register(final @NotNull String className, final @Nullable FeatureConstructorParams params) {
        paramsRegistry.put(className, params);
    }

    /**
     * Associates the given constructor parameters with the specified class.
     *
     * @param clazz  the feature class
     * @param params the constructor parameters to store, or {@code null} to clear them
     */
    public void register(final @NotNull Class<?> clazz, final @Nullable FeatureConstructorParams params) {
        register(clazz.getName(), params);
    }

    /**
     * Removes the constructor parameters associated with the specified class name.
     *
     * @param className the fully qualified name of the feature class
     */
    public void unregister(final @NotNull String className) {
        paramsRegistry.remove(className);
    }

    /**
     * Removes the constructor parameters associated with the specified class.
     *
     * @param clazz the feature class
     */
    public void unregister(final @NotNull Class<?> clazz) {
        unregister(clazz.getName());
    }

    /**
     * Returns the constructor parameters registered for the specified class name,
     * or {@code null} if none have been registered.
     *
     * @param className the fully qualified name of the feature class
     * @return the registered constructor parameters, or {@code null}
     */
    public FeatureConstructorParams getParameters(final @NotNull String className) {
        return paramsRegistry.get(className);
    }

    /**
     * Returns the constructor parameters registered for the specified class,
     * or {@code null} if none have been registered.
     *
     * @param clazz the feature class
     * @return the registered constructor parameters, or {@code null}
     */
    public FeatureConstructorParams getParameters(final @NotNull Class<?> clazz) {
        return getParameters(clazz.getName());
    }
    
}
