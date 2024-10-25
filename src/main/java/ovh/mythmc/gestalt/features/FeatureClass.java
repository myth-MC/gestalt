package ovh.mythmc.gestalt.features;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class FeatureClass {

    private final Class<?> clazz;

    private final Object[] args;

    public FeatureClass(Class<?> clazz, Object[] args) {
        this.clazz = clazz;
        this.args = args;
    }

    public Class<?> clazz() {
        return clazz;
    }

    public Object[] args() {
        return args;
    }
    
}
