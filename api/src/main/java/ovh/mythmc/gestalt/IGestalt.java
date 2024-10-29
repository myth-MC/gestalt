package ovh.mythmc.gestalt;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.annotations.FeatureListenerProcessor;
import ovh.mythmc.gestalt.annotations.conditions.FeatureConditionProcessor;
import ovh.mythmc.gestalt.features.FeatureConstructorParamsRegistry;
import ovh.mythmc.gestalt.features.FeatureListenerRegistry;
import ovh.mythmc.gestalt.features.FeaturePriority;
import ovh.mythmc.gestalt.features.GestaltFeature;

import java.util.List;

public interface IGestalt {

    FeatureConditionProcessor getConditionProcessor();

    FeatureListenerProcessor getListenerProcessor();

    FeatureConstructorParamsRegistry getConstructorParamsRegistry();

    FeatureListenerRegistry getListenerRegistry();

    // @NotNull default FeatureListenerRegistry getListenerRegistry() { return new FeatureListenerRegistry(this); }

    String getServerVersion();

    void register(final @NotNull Class<?>... classes);

    void register(final @NotNull GestaltFeature feature);

    void unregister(final @NotNull Class<?>... classes);

    void unregisterAllFeatures();

    void enableFeature(final @NotNull Class<?> clazz);

    void disableFeature(final @NotNull Class<?> clazz);

    void enableAllFeatures();

    void enableAllFeatures(final @NotNull String group);

    void disableAllFeatures();

    void disableAllFeatures(final @NotNull String group);

    List<Class<?>> getByGroup(final @NotNull String group);

    List<Class<?>> getByIdentifier(final @NotNull String identifier);

    List<Class<?>> getByPriority(final @NotNull FeaturePriority priority);

    List<Class<?>> getByGroupAndIdentifier(final @NotNull String group, final @NotNull String identifier);

    List<Class<?>> getSortedByPriority();

    List<Class<?>> getEnabledClasses();

    List<Class<?>> getDisabledClasses();

    boolean isEnabled(final @NotNull Class<?> clazz);

}