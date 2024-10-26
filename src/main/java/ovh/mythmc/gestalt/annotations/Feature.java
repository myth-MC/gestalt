package ovh.mythmc.gestalt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ovh.mythmc.gestalt.features.FeaturePriority;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Feature {

    String group();

    String identifier();

    FeaturePriority priority() default FeaturePriority.NORMAL;
    
}
