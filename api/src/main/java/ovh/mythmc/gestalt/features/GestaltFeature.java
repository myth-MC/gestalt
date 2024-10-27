package ovh.mythmc.gestalt.features;

public class GestaltFeature {

    private final Class<?> clazz;

    private final FeatureConstructorParams params;

    public GestaltFeature(Class<?> clazz, FeatureConstructorParams params) {
        this.clazz = clazz;
        this.params = params;
    }

    public Class<?> getFeatureClass() { return clazz; }

    public FeatureConstructorParams getConstructorParams() { return params; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Class<?> clazz;

        private FeatureConstructorParams params;

        public Builder featureClass(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder params(FeatureConstructorParams params) {
            this.params = params;
            return this;
        }

        public GestaltFeature build() {
            return new GestaltFeature(clazz, params);
        }

    }
    
}
