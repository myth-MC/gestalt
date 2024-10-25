package ovh.mythmc.gestalt.features;

public class FeatureConstructorParams {

    private final Object[] params;

    private final Class<?>[] paramTypes;

    public FeatureConstructorParams(Object[] params, Class<?>[] paramTypes) {
        this.params = params;
        this.paramTypes = paramTypes;
    }

    public Object[] getParams() { return params; }

    public Class<?>[] getParamTypes() { return paramTypes; }

    public static class Builder {

        private Object[] params = null;

        private Class<?>[] paramTypes = null;

        public Builder params(Object... params) {
            this.params = params;
            return this;
        }

        public Builder paramTypes(Class<?>... paramTypes) {
            this.paramTypes = paramTypes;
            return this;
        }

        public FeatureConstructorParams build() {
            return new FeatureConstructorParams(params, paramTypes);
        }

    }
    
}
