package ovh.mythmc.gestalt.features;

import org.jetbrains.annotations.NotNull;

public class GestaltFeature {

    private final Class<?> featureClass;
    private final FeatureConstructorParams constructorParams;

    private GestaltFeature(Class<?> featureClass, FeatureConstructorParams constructorParams) {
        this.featureClass = featureClass;
        this.constructorParams = constructorParams;
    }

    public Class<?> getFeatureClass() {
        return this.featureClass;
    }

    public FeatureConstructorParams getConstructorParams() {
        return this.constructorParams;
    }

    public static GestaltFeatureBuilder builder() {
        return new GestaltFeatureBuilder();
    }

    public static class GestaltFeatureBuilder {

        private Class<?> featureClass;
        private FeatureConstructorParams constructorParams;

        private GestaltFeatureBuilder() {
        }

        public GestaltFeatureBuilder featureClass(@NotNull Class<?> featureClass) {
            this.featureClass = featureClass;
            return this;
        }

        public GestaltFeatureBuilder constructorParams(FeatureConstructorParams constructorParams) {
            this.constructorParams = constructorParams;
            return this;
        }

        public GestaltFeature build() {
            return new GestaltFeature(featureClass, constructorParams);
        }

    }
    
}
