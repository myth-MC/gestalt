package ovh.mythmc.gestalt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ovh.mythmc.gestalt.features.FeatureEvent;

/**
 * Marks a method as a listener for one or more {@link FeatureEvent}s fired by a specific feature.
 *
 * <p>The target feature can be specified either directly via {@link #feature()} or by matching
 * a combination of {@link #group()} and {@link #identifier()}. This annotation is repeatable,
 * allowing a single method to listen for events from multiple features.
 *
 * <p>Annotated methods must be declared in classes registered via
 * {@link ovh.mythmc.gestalt.features.FeatureListenerRegistry#register(Object)}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(FeatureListener.List.class)
public @interface FeatureListener {
    
    /**
     * The specific feature class to listen to. Takes precedence over {@link #group()} and
     * {@link #identifier()} when set to a class other than the default {@link Feature}.
     *
     * @return the feature class to listen to
     */
    Class<?> feature() default Feature.class;

    /**
     * The group of the target feature. Used together with {@link #identifier()} when
     * {@link #feature()} is not explicitly set.
     *
     * @return the feature group
     */
    String group() default "";

    /**
     * The identifier of the target feature. Used together with {@link #group()} when
     * {@link #feature()} is not explicitly set.
     *
     * @return the feature identifier
     */
    String identifier() default "";

    /**
     * The lifecycle events this listener should respond to.
     *
     * @return the array of events to listen for
     */
    FeatureEvent[] events();

    /**
     * Container annotation for repeatable {@link FeatureListener} declarations on a single method.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface List {
        /** @return the array of {@link FeatureListener} annotations */
        FeatureListener[] value();
    }

}
