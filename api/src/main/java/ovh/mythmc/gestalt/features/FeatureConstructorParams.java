package ovh.mythmc.gestalt.features;

public class FeatureConstructorParams {

    private final Object[] params;

    private final Class<?>[] paramTypes;

    private FeatureConstructorParams(Object[] params, Class<?>[] paramTypes) {
        this.params = params;
        this.paramTypes = paramTypes;
    }

    public Object[] getParams() {
        return this.params;
    }

    public Class<?>[] getParamTypes() {
        return this.paramTypes;
    }

    public static FeatureConstructorParamsBuilder builder() {
        return new FeatureConstructorParamsBuilder();
    }

    public static class FeatureConstructorParamsBuilder {

        private Object[] params;
        private Class<?>[] paramTypes;

        private FeatureConstructorParamsBuilder() {
        }

        public FeatureConstructorParamsBuilder params(Object... params) {
            this.params = params;
            return this;
        }

        public FeatureConstructorParamsBuilder types(Class<?>... paramTypes) {
            this.paramTypes = paramTypes;
            return this;
        }

        public FeatureConstructorParams build() {
            return new FeatureConstructorParams(params, paramTypes);
        }
        
    }
    
}
