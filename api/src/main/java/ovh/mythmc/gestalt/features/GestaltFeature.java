package ovh.mythmc.gestalt.features;

import org.jetbrains.annotations.NotNull;

/**
 * A descriptor that bundles a {@link ovh.mythmc.gestalt.annotations.Feature}-annotated class
 * with optional custom constructor parameters used during instantiation.
 *
 * <p>Use {@link #builder()} to create instances via the fluent {@link GestaltFeatureBuilder}.
 * Pass the built instance to {@link ovh.mythmc.gestalt.Gestalt#register(GestaltFeature)} to
 * register the feature with its associated constructor parameters.
 */
public class GestaltFeature {

    private final Class<?> featureClass;
    private final FeatureConstructorParams constructorParams;

    private GestaltFeature(Class<?> featureClass, FeatureConstructorParams constructorParams) {
        this.featureClass = featureClass;
        this.constructorParams = constructorParams;
    }

    /**
     * Returns the feature class described by this descriptor.
     *
     * @return the feature class
     */
    public Class<?> getFeatureClass() {
        return this.featureClass;
    }

    /**
     * Returns the optional constructor parameters to use when instantiating the feature class,
     * or {@code null} if the feature uses a no-arg constructor.
     *
     * @return the constructor parameters, or {@code null}
     */
    public FeatureConstructorParams getConstructorParams() {
        return this.constructorParams;
    }

    /**
     * Returns a new {@link GestaltFeatureBuilder} for creating a {@link GestaltFeature}.
     *
     * @return a new builder
     */
    public static GestaltFeatureBuilder builder() {
        return new GestaltFeatureBuilder();
    }

    /**
     * Builder for {@link GestaltFeature}.
     */
    public static class GestaltFeatureBuilder {

        private Class<?> featureClass;
        private FeatureConstructorParams constructorParams;

        private GestaltFeatureBuilder() {
        }

        /**
         * Sets the feature class for this descriptor.
         *
         * @param featureClass the {@link ovh.mythmc.gestalt.annotations.Feature}-annotated class
         * @return this builder
         */
        public GestaltFeatureBuilder featureClass(@NotNull Class<?> featureClass) {
            this.featureClass = featureClass;
            return this;
        }

        /**
         * Sets the optional constructor parameters for the feature class.
         *
         * @param constructorParams the constructor parameters, or {@code null} to use the no-arg constructor
         * @return this builder
         */
        public GestaltFeatureBuilder constructorParams(FeatureConstructorParams constructorParams) {
            this.constructorParams = constructorParams;
            return this;
        }

        /**
         * Builds and returns a {@link GestaltFeature} with the configured values.
         *
         * @return a new {@link GestaltFeature} instance
         */
        public GestaltFeature build() {
            return new GestaltFeature(featureClass, constructorParams);
        }

    }
    
}
