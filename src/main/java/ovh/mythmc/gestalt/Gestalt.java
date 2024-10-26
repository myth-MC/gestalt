package ovh.mythmc.gestalt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import ovh.mythmc.gestalt.annotations.Feature;
import ovh.mythmc.gestalt.annotations.conditions.FeatureConditionProcessor;
import ovh.mythmc.gestalt.annotations.status.FeatureDisable;
import ovh.mythmc.gestalt.annotations.status.FeatureEnable;
import ovh.mythmc.gestalt.annotations.status.FeatureInitialize;
import ovh.mythmc.gestalt.annotations.status.FeatureShutdown;
import ovh.mythmc.gestalt.exceptions.AlreadyInitializedException;
import ovh.mythmc.gestalt.exceptions.NotInitializedException;
import ovh.mythmc.gestalt.features.FeatureConstructorParamsRegistry;
import ovh.mythmc.gestalt.features.FeaturePriority;
import ovh.mythmc.gestalt.features.GestaltFeature;
import ovh.mythmc.gestalt.util.MethodUtil;

public class Gestalt {

    private final FeatureConstructorParamsRegistry paramsRegistry = new FeatureConstructorParamsRegistry();

    private final String serverVersion;

    private final boolean overrideInstance;

    private static Gestalt gestalt;

    public Gestalt(String serverVersion, boolean overrideInstance) {
        this.serverVersion = serverVersion;
        this.overrideInstance = overrideInstance;
    }

    public FeatureConstructorParamsRegistry getParamsRegistry() {
        return paramsRegistry;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public static boolean isGestaltInitialized() {
        return gestalt != null;
    }

    public static void set(final @NotNull Gestalt g) {
        if (isGestaltInitialized() && !g.overrideInstance)
            throw new AlreadyInitializedException("Gestalt is already initialized! (is Gestalt properly shaded?)");

        gestalt = g;
    }

    public static Gestalt get() {
        if (gestalt == null)
            throw new NotInitializedException();

        return gestalt;
    }

    private final Map<Class<?>, Boolean> classMap = new HashMap<>();

    public void register(final @NotNull Class<?>... classes) {
        Arrays.stream(classes).forEach(clazz -> {
            if (classMap.containsKey(clazz))
                return;

            if (!clazz.isAnnotationPresent(Feature.class))
                return;

            MethodUtil.triggerAnnotatedMethod(clazz, FeatureInitialize.class);
            classMap.put(clazz, false);
        });
    }

    public void register(final @NotNull GestaltFeature feature) {
        if (feature.getConstructorParams() != null) {
            getParamsRegistry().register(feature.getFeatureClass(), feature.getConstructorParams());
        }

        register(feature.getFeatureClass());
    }

    public void unregister(final @NotNull Class<?>... classes) {
        Arrays.stream(classes).forEach(clazz -> {
            if (!classMap.containsKey(clazz))
                return;

            MethodUtil.triggerAnnotatedMethod(clazz, FeatureShutdown.class);
            classMap.remove(clazz);
            getParamsRegistry().unregister(clazz.getName());
        });
    }

    public void unregisterAllFeatures() {
        for (int i = 0; i < classMap.keySet().size(); i++) {
            Class<?> clazz = classMap.keySet().stream().toList().get(i);
            unregister(clazz);
        }
    }

    public void enableFeature(final @NotNull Class<?> clazz) {
        if (classMap.get(clazz))
            return;

        if (FeatureConditionProcessor.canBeEnabled(clazz)) {
            classMap.put(clazz, true);
            MethodUtil.triggerAnnotatedMethod(clazz, FeatureEnable.class);
        }
    }

    public void disableFeature(final @NotNull Class<?> clazz) {
        if (classMap.get(clazz)) {
            classMap.put(clazz, false);
            MethodUtil.triggerAnnotatedMethod(clazz, FeatureDisable.class);
        }
    }

    public void enableAllFeatures() {
        getSortedByPriority().forEach(this::enableFeature);
    }

    public void enableAllFeatures(final @NotNull String group) {
        getSortedByPriority().stream().filter(clazz -> clazz.getAnnotation(Feature.class).group().equals(group)).forEach(this::enableFeature);
    }

    public void disableAllFeatures() {
        getSortedByPriority().forEach(this::disableFeature);
    }

    public void disableAllFeatures(final @NotNull String group) {
        getSortedByPriority().stream().filter(clazz -> clazz.getAnnotation(Feature.class).group().equals(group)).forEach(this::disableFeature);
    }

    public List<Class<?>> getByGroup(final @NotNull String group) {
        return classMap.keySet().stream()
            .filter(clazz -> clazz.getAnnotation(Feature.class).group().equals(group))
            .collect(Collectors.toList());
    }

    public List<Class<?>> getByIdentifier(final @NotNull String identifier) {
        return classMap.keySet().stream()
            .filter(clazz -> clazz.getAnnotation(Feature.class).identifier().equals(identifier))
            .collect(Collectors.toList());
    }

    public List<Class<?>> getByPriority(final @NotNull FeaturePriority priority) {
        return classMap.keySet().stream()
            .filter(clazz -> clazz.getAnnotation(Feature.class).priority().equals(priority))
            .collect(Collectors.toList());
    }

    public List<Class<?>> getByGroupAndIdentifier(final @NotNull String group, final @NotNull String identifier) {
        return getSortedByPriority().stream()   
            .filter(clazz -> clazz.getAnnotation(Feature.class).group().equals(group) && 
                clazz.getAnnotation(Feature.class).identifier().equals(identifier))
            .toList();
    }

    public List<Class<?>> getSortedByPriority() {
        List<Class<?>> classList = new ArrayList<>();
        getByPriority(FeaturePriority.HIGHEST).forEach(classList::add);
        getByPriority(FeaturePriority.HIGH).forEach(classList::add);
        getByPriority(FeaturePriority.NORMAL).forEach(classList::add);
        getByPriority(FeaturePriority.LOW).forEach(classList::add);
        getByPriority(FeaturePriority.LOWEST).forEach(classList::add);
        return classList;
    }

    public List<Class<?>> getEnabledClasses() {
        return classMap.entrySet().stream().filter(entry -> entry.getValue()).map(entry -> entry.getKey()).collect(Collectors.toList());
    }

    public List<Class<?>> getDisabledClasses() {
        return classMap.entrySet().stream().filter(entry -> !entry.getValue()).map(entry -> entry.getKey()).collect(Collectors.toList());
    }

    public boolean isEnabled(final @NotNull Class<?> clazz) {
        return classMap.get(clazz);
    }

}
