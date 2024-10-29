package ovh.mythmc.gestalt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.annotations.Feature;
import ovh.mythmc.gestalt.annotations.FeatureListenerProcessor;
import ovh.mythmc.gestalt.annotations.conditions.FeatureConditionProcessor;
import ovh.mythmc.gestalt.annotations.status.FeatureDisable;
import ovh.mythmc.gestalt.annotations.status.FeatureEnable;
import ovh.mythmc.gestalt.annotations.status.FeatureInitialize;
import ovh.mythmc.gestalt.annotations.status.FeatureShutdown;
import ovh.mythmc.gestalt.features.FeatureConstructorParamsRegistry;
import ovh.mythmc.gestalt.features.FeatureEvent;
import ovh.mythmc.gestalt.features.FeatureListenerRegistry;
import ovh.mythmc.gestalt.features.FeaturePriority;
import ovh.mythmc.gestalt.features.GestaltFeature;
import ovh.mythmc.gestalt.util.MethodUtil;

public abstract class AbstractGestalt implements IGestalt {

    private final String serverVersion;

    private final FeatureConditionProcessor conditionProcessor = new FeatureConditionProcessor(this);

    private final FeatureListenerProcessor listenerProcessor = new FeatureListenerProcessor(this);

    private final FeatureConstructorParamsRegistry constructorParamsRegistry = new FeatureConstructorParamsRegistry();

    private final FeatureListenerRegistry listenerRegistry = new FeatureListenerRegistry(this);

    protected AbstractGestalt(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    @Override
    public FeatureConditionProcessor getConditionProcessor() {
        return conditionProcessor;
    }

    @Override
    public FeatureListenerProcessor getListenerProcessor() {
        return listenerProcessor;
    }

    @Override
    public FeatureConstructorParamsRegistry getConstructorParamsRegistry() {
        return constructorParamsRegistry;
    }

    @Override
    public FeatureListenerRegistry getListenerRegistry() {
        return listenerRegistry;
    }

    @Override
    public String getServerVersion() {
        return serverVersion;
    }

    private final Map<Class<?>, Boolean> classMap = new HashMap<>();

    @Override
    public void register(final @NotNull Class<?>... classes) {
        Arrays.stream(classes).forEach(clazz -> {
            if (classMap.containsKey(clazz))
                return;

            if (!clazz.isAnnotationPresent(Feature.class))
                return;

            MethodUtil.triggerAnnotatedMethod(this, clazz, FeatureInitialize.class);
            classMap.put(clazz, false);

            getListenerRegistry().call(clazz, FeatureEvent.INITIALIZE);
        });
    }

    @Override
    public void register(final @NotNull GestaltFeature feature) {
        if (feature.getConstructorParams() != null) {
            getConstructorParamsRegistry().register(feature.getFeatureClass(), feature.getConstructorParams());
        }

        register(feature.getFeatureClass());
    }

    @Override
    public void unregister(final @NotNull Class<?>... classes) {
        Arrays.stream(classes).forEach(clazz -> {
            if (!classMap.containsKey(clazz))
                return;

            MethodUtil.triggerAnnotatedMethod(this, clazz, FeatureShutdown.class);
            classMap.remove(clazz);
            getConstructorParamsRegistry().unregister(clazz.getName());
            getListenerRegistry().call(clazz, FeatureEvent.SHUTDOWN);
        });
    }

    @Override
    public void unregisterAllFeatures() {
        for (int i = 0; i < classMap.keySet().size(); i++) {
            Class<?> clazz = classMap.keySet().stream().toList().get(i);
            unregister(clazz);
        }
    }

    @Override
    public void enableFeature(final @NotNull Class<?> clazz) {
        if (classMap.get(clazz))
            return;

        if (getConditionProcessor().canBeEnabled(clazz)) {
            classMap.put(clazz, true);
            MethodUtil.triggerAnnotatedMethod(this, clazz, FeatureEnable.class);
            getListenerRegistry().call(clazz, FeatureEvent.ENABLE);
        }
    }

    @Override
    public void disableFeature(final @NotNull Class<?> clazz) {
        if (classMap.get(clazz)) {
            classMap.put(clazz, false);
            MethodUtil.triggerAnnotatedMethod(this, clazz, FeatureDisable.class);
            getListenerRegistry().call(clazz, FeatureEvent.DISABLE);
        }
    }

    @Override
    public void enableAllFeatures() {
        getSortedByPriority().forEach(this::enableFeature);
    }

    @Override
    public void enableAllFeatures(final @NotNull String group) {
        getSortedByPriority().stream().filter(clazz -> clazz.getAnnotation(Feature.class).group().equals(group)).forEach(this::enableFeature);
    }

    @Override
    public void disableAllFeatures() {
        getSortedByPriority().forEach(this::disableFeature);
    }

    @Override
    public void disableAllFeatures(final @NotNull String group) {
        getSortedByPriority().stream().filter(clazz -> clazz.getAnnotation(Feature.class).group().equals(group)).forEach(this::disableFeature);
    }

    @Override
    public List<Class<?>> getByGroup(final @NotNull String group) {
        return classMap.keySet().stream()
            .filter(clazz -> clazz.getAnnotation(Feature.class).group().equalsIgnoreCase(group))
            .collect(Collectors.toList());
    }

    @Override
    public List<Class<?>> getByIdentifier(final @NotNull String identifier) {
        return classMap.keySet().stream()
            .filter(clazz -> clazz.getAnnotation(Feature.class).identifier().equalsIgnoreCase(identifier))
            .collect(Collectors.toList());
    }

    @Override
    public List<Class<?>> getByPriority(final @NotNull FeaturePriority priority) {
        return classMap.keySet().stream()
            .filter(clazz -> clazz.getAnnotation(Feature.class).priority().equals(priority))
            .collect(Collectors.toList());
    }

    @Override
    public List<Class<?>> getByGroupAndIdentifier(final @NotNull String group, final @NotNull String identifier) {
        return getSortedByPriority().stream()   
            .filter(clazz -> clazz.getAnnotation(Feature.class).group().equalsIgnoreCase(group) && 
                clazz.getAnnotation(Feature.class).identifier().equalsIgnoreCase(identifier))
            .collect(Collectors.toList());
    }

    @Override
    public List<Class<?>> getSortedByPriority() {
        List<Class<?>> classList = new ArrayList<>();
        getByPriority(FeaturePriority.HIGHEST).forEach(classList::add);
        getByPriority(FeaturePriority.HIGH).forEach(classList::add);
        getByPriority(FeaturePriority.NORMAL).forEach(classList::add);
        getByPriority(FeaturePriority.LOW).forEach(classList::add);
        getByPriority(FeaturePriority.LOWEST).forEach(classList::add);
        return classList;
    }

    @Override
    public List<Class<?>> getEnabledClasses() {
        return classMap.entrySet().stream().filter(entry -> entry.getValue()).map(entry -> entry.getKey()).collect(Collectors.toList());
    }

    @Override
    public List<Class<?>> getDisabledClasses() {
        return classMap.entrySet().stream().filter(entry -> !entry.getValue()).map(entry -> entry.getKey()).collect(Collectors.toList());
    }

    @Override
    public boolean isEnabled(final @NotNull Class<?> clazz) {
        return classMap.get(clazz);
    }

}