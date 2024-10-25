package ovh.mythmc.gestalt.features;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public final class FeatureParamsRegistry {

    private final Map<String, Object[]> paramsRegistry = new HashMap<>();

    public void register(final @NotNull String className, final @Nullable Object... params) {
        paramsRegistry.put(className, params);
    }

    public void register(final @NotNull Class<?> clazz, final @Nullable Object... params) {
        register(clazz.getName(), params);
    }

    public void unregister(final @NotNull String className) {
        paramsRegistry.remove(className);
    }

    public void unregister(final @NotNull Class<?> clazz) {
        unregister(clazz.getName());
    }

    public Object[] getParameters(final @NotNull String className) {
        if (!paramsRegistry.containsKey(className))
            return null;
            
        System.out.println("Getting params for class " + className + " -> " + paramsRegistry.get(className));
        return paramsRegistry.get(className);
    }

    public Object[] getParameters(final @NotNull Class<?> clazz) {
        return getParameters(clazz.getName());
    }
    
}
