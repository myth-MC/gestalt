package ovh.mythmc.gestalt.features;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Builder
public class GestaltFeature {

    private final Class<?> featureClass;

    private final FeatureConstructorParams constructorParams;
    
}
