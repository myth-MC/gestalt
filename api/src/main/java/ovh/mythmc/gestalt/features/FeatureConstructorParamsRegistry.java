package ovh.mythmc.gestalt.features;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FeatureConstructorParamsRegistry {

    private final Map<String, FeatureConstructorParams> paramsRegistry = new HashMap<>();

    public void register(final @NotNull String className, final @Nullable FeatureConstructorParams params) {
        paramsRegistry.put(className, params);
    }

    public void register(final @NotNull Class<?> clazz, final @Nullable FeatureConstructorParams params) {
        register(clazz.getName(), params);
    }

    public void unregister(final @NotNull String className) {
        paramsRegistry.remove(className);
    }

    public void unregister(final @NotNull Class<?> clazz) {
        unregister(clazz.getName());
    }

    public FeatureConstructorParams getParameters(final @NotNull String className) {
        return paramsRegistry.get(className);
    }

    public FeatureConstructorParams getParameters(final @NotNull Class<?> clazz) {
        return getParameters(clazz.getName());
    }
    
}
