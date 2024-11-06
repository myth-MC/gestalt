package ovh.mythmc.gestalt.features;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class FeatureConstructorParams {

    private final Object[] params;

    private final Class<?>[] paramTypes;

    public static class FeatureConstructorParamsBuilder {

        public FeatureConstructorParamsBuilder params(Object... params) {
            this.params = params;
            return this;
        }

        public FeatureConstructorParamsBuilder types(Class<?>... paramTypes) {
            this.paramTypes = paramTypes;
            return this;
        }
        
    }
    
}
