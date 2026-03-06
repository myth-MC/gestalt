package ovh.mythmc.gestalt.features;

/**
 * Holds the constructor parameter values and their types for a feature class that cannot
 * be instantiated with a no-arg constructor.
 *
 * <p>Use {@link #builder()} to create instances via the fluent {@link FeatureConstructorParamsBuilder}.
 */
public class FeatureConstructorParams {

    private final Object[] params;

    private final Class<?>[] paramTypes;

    private FeatureConstructorParams(Object[] params, Class<?>[] paramTypes) {
        this.params = params;
        this.paramTypes = paramTypes;
    }

    /**
     * Returns the constructor argument values.
     *
     * @return the parameter values array
     */
    public Object[] getParams() {
        return this.params;
    }

    /**
     * Returns the types of the constructor parameters, used to resolve the correct constructor
     * via reflection.
     *
     * @return the parameter type array
     */
    public Class<?>[] getParamTypes() {
        return this.paramTypes;
    }

    /**
     * Returns a new {@link FeatureConstructorParamsBuilder} for constructing a
     * {@link FeatureConstructorParams} instance.
     *
     * @return a new builder
     */
    public static FeatureConstructorParamsBuilder builder() {
        return new FeatureConstructorParamsBuilder();
    }

    /**
     * Builder for {@link FeatureConstructorParams}.
     */
    public static class FeatureConstructorParamsBuilder {

        private Object[] params;
        private Class<?>[] paramTypes;

        private FeatureConstructorParamsBuilder() {
        }

        /**
         * Sets the constructor argument values.
         *
         * @param params the parameter values
         * @return this builder
         */
        public FeatureConstructorParamsBuilder params(Object... params) {
            this.params = params;
            return this;
        }

        /**
         * Sets the constructor parameter types.
         *
         * @param paramTypes the parameter types, used to resolve the constructor via reflection
         * @return this builder
         */
        public FeatureConstructorParamsBuilder types(Class<?>... paramTypes) {
            this.paramTypes = paramTypes;
            return this;
        }

        /**
         * Builds and returns a {@link FeatureConstructorParams} with the configured values.
         *
         * @return a new {@link FeatureConstructorParams} instance
         */
        public FeatureConstructorParams build() {
            return new FeatureConstructorParams(params, paramTypes);
        }
        
    }
    
}
