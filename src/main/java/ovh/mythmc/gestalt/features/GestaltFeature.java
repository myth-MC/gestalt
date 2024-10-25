package ovh.mythmc.gestalt.features;

public class GestaltFeature {

    public static Builder builder = new Builder();

    private final Class<?> clazz;

    private final FeatureConstructorParams params;

    public GestaltFeature(Class<?> clazz, FeatureConstructorParams params) {
        this.clazz = clazz;
        this.params = params;
    }

    public Class<?> getFeatureClass() { return clazz; }

    public FeatureConstructorParams getConstructorParams() { return params; }

    public static class Builder {

        private String className;

        private FeatureConstructorParams params;

        public Builder featureClass(Class<?> clazz) {
            this.className = clazz.getName();
            return this;
        }

        public Builder params(FeatureConstructorParams params) {
            this.params = params;
            return this;
        }

        public GestaltFeature build() {
            try {
                return new GestaltFeature(Class.forName(className), params);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }

    }
    
}
