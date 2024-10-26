package ovh.mythmc.gestalt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ovh.mythmc.gestalt.features.FeatureEvent;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(FeatureListener.List.class)
public @interface FeatureListener {
    
    Class<?> feature();

    FeatureEvent[] events();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface List {
        FeatureListener[] value();
    }

}
