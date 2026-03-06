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

/**
 * The core abstract class of the Gestalt feature management framework.
 *
 * <p>Gestalt manages the lifecycle of {@link Feature}-annotated classes, including
 * registration, initialization, enabling, disabling, and shutdown. Subclasses are
 * expected to provide platform-specific initialization and supply themselves via
 * {@link GestaltSupplier}.
 *
 * <p>The active instance can be retrieved globally via {@link #get()}.
 */
public abstract class Gestalt {

    private final String serverVersion;
    private final boolean autoUpdate; // Used by GestaltLoader
    private final FeatureConditionProcessor conditionProcessor = new FeatureConditionProcessor(this);
    private final FeatureListenerProcessor listenerProcessor = new FeatureListenerProcessor(this);
    private final FeatureConstructorParamsRegistry constructorParamsRegistry = new FeatureConstructorParamsRegistry();
    private final FeatureListenerRegistry listenerRegistry = new FeatureListenerRegistry(this);

    private final Map<Class<?>, Boolean> classMap = new HashMap<>();

    /**
     * Returns the server version string used for version-based feature conditions.
     *
     * @return the server version
     */
    public String getServerVersion() {
        return this.serverVersion;
    }

    /**
     * Returns whether Gestalt should automatically update features via the loader.
     *
     * @return {@code true} if auto-update is enabled
     */
    public boolean isAutoUpdate() {
        return this.autoUpdate;
    }

    /**
     * Returns the {@link FeatureConditionProcessor} responsible for evaluating
     * whether a feature's conditions are satisfied before enabling it.
     *
     * @return the condition processor
     */
    public FeatureConditionProcessor getConditionProcessor() {
        return this.conditionProcessor;
    }

    /**
     * Returns the {@link FeatureListenerProcessor} responsible for processing
     * and invoking methods annotated with {@link ovh.mythmc.gestalt.annotations.FeatureListener}.
     *
     * @return the listener processor
     */
    public FeatureListenerProcessor getListenerProcessor() {
        return this.listenerProcessor;
    }

    /**
     * Returns the {@link FeatureConstructorParamsRegistry} used to store
     * custom constructor parameters for feature instantiation.
     *
     * @return the constructor parameters registry
     */
    public FeatureConstructorParamsRegistry getConstructorParamsRegistry() {
        return this.constructorParamsRegistry;
    }

    /**
     * Returns the {@link FeatureListenerRegistry} used to register and dispatch
     * feature lifecycle events to listener objects.
     *
     * @return the listener registry
     */
    public FeatureListenerRegistry getListenerRegistry() {
        return this.listenerRegistry;
    }

    /**
     * Returns the globally active {@link Gestalt} instance.
     *
     * @return the active Gestalt instance
     * @see GestaltSupplier#get()
     */
    public static Gestalt get() {
        return GestaltSupplier.get();
    }

    /**
     * Constructs a new Gestalt instance with the given server version and auto-update flag.
     *
     * @param serverVersion the server version string, used for version-based conditions
     * @param autoUpdate    whether the loader should auto-update features
     */
    protected Gestalt(String serverVersion, boolean autoUpdate) {
        this.serverVersion = serverVersion;
        this.autoUpdate = autoUpdate;
    }

    /**
     * Registers one or more feature classes with Gestalt.
     *
     * <p>Each class must be annotated with {@link Feature}. Upon registration,
     * methods annotated with {@link FeatureInitialize} are invoked and a
     * {@link FeatureEvent#INITIALIZE} event is dispatched to all registered listeners.
     * Classes that are already registered are silently skipped.
     *
     * @param classes the feature classes to register
     */
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

    /**
     * Registers a feature described by a {@link GestaltFeature} wrapper.
     *
     * <p>If the wrapper provides custom {@link ovh.mythmc.gestalt.features.FeatureConstructorParams},
     * they are stored in the constructor params registry before the feature class is registered.
     *
     * @param feature the feature descriptor containing the class and optional constructor params
     */
    public void register(final @NotNull GestaltFeature feature) {
        if (feature.getConstructorParams() != null) {
            getConstructorParamsRegistry().register(feature.getFeatureClass(), feature.getConstructorParams());
        }

        register(feature.getFeatureClass());
    }

    /**
     * Unregisters one or more feature classes from Gestalt.
     *
     * <p>Methods annotated with {@link FeatureShutdown} are invoked on each class,
     * the class is removed from the internal registry, its constructor params are cleared,
     * and a {@link FeatureEvent#SHUTDOWN} event is dispatched to all listeners.
     * Classes that are not registered are silently skipped.
     *
     * @param classes the feature classes to unregister
     */
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

    /**
     * Unregisters all currently registered feature classes.
     *
     * <p>Each feature's {@link FeatureShutdown} methods are invoked and listeners
     * are notified with {@link FeatureEvent#SHUTDOWN}.
     */
    public void unregisterAllFeatures() {
        for (int i = 0; i < classMap.keySet().size(); i++) {
            Class<?> clazz = classMap.keySet().stream().toList().get(i);
            unregister(clazz);
        }
    }

    /**
     * Enables a single registered feature class.
     *
     * <p>The feature is only enabled if it is currently disabled and all of its
     * {@link FeatureConditionProcessor conditions} are satisfied. Upon enabling,
     * methods annotated with {@link FeatureEnable} are invoked and a
     * {@link FeatureEvent#ENABLE} event is dispatched to listeners.
     *
     * @param clazz the feature class to enable
     */
    public void enableFeature(final @NotNull Class<?> clazz) {
        if (classMap.get(clazz))
            return;

        if (getConditionProcessor().canBeEnabled(clazz)) {
            classMap.put(clazz, true);
            MethodUtil.triggerAnnotatedMethod(this, clazz, FeatureEnable.class);
            getListenerRegistry().call(clazz, FeatureEvent.ENABLE);
        }
    }

    /**
     * Disables a single registered feature class.
     *
     * <p>The feature is only disabled if it is currently enabled. Methods annotated
     * with {@link FeatureDisable} are invoked and a {@link FeatureEvent#DISABLE}
     * event is dispatched to listeners.
     *
     * @param clazz the feature class to disable
     */
    public void disableFeature(final @NotNull Class<?> clazz) {
        if (classMap.get(clazz)) {
            classMap.put(clazz, false);
            MethodUtil.triggerAnnotatedMethod(this, clazz, FeatureDisable.class);
            getListenerRegistry().call(clazz, FeatureEvent.DISABLE);
        }
    }

    /**
     * Enables all registered features, respecting their {@link FeaturePriority} order.
     */
    public void enableAllFeatures() {
        getSortedByPriority().forEach(this::enableFeature);
    }

    /**
     * Enables all registered features belonging to the specified group,
     * respecting their {@link FeaturePriority} order.
     *
     * @param group the feature group name to enable
     */
    public void enableAllFeatures(final @NotNull String group) {
        getSortedByPriority().stream().filter(clazz -> clazz.getAnnotation(Feature.class).group().equals(group)).forEach(this::enableFeature);
    }

    /**
     * Disables all registered features, respecting their {@link FeaturePriority} order.
     */
    public void disableAllFeatures() {
        getSortedByPriority().forEach(this::disableFeature);
    }

    /**
     * Disables all registered features belonging to the specified group,
     * respecting their {@link FeaturePriority} order.
     *
     * @param group the feature group name to disable
     */
    public void disableAllFeatures(final @NotNull String group) {
        getSortedByPriority().stream().filter(clazz -> clazz.getAnnotation(Feature.class).group().equals(group)).forEach(this::disableFeature);
    }

    /**
     * Returns all registered feature classes belonging to the given group (case-insensitive).
     *
     * @param group the group name to filter by
     * @return a list of matching feature classes
     */
    public List<Class<?>> getByGroup(final @NotNull String group) {
        return classMap.keySet().stream()
            .filter(clazz -> clazz.getAnnotation(Feature.class).group().equalsIgnoreCase(group))
            .collect(Collectors.toList());
    }

    /**
     * Returns all registered feature classes with the given identifier (case-insensitive).
     *
     * @param identifier the identifier to filter by
     * @return a list of matching feature classes
     */
    public List<Class<?>> getByIdentifier(final @NotNull String identifier) {
        return classMap.keySet().stream()
            .filter(clazz -> clazz.getAnnotation(Feature.class).identifier().equalsIgnoreCase(identifier))
            .collect(Collectors.toList());
    }

    /**
     * Returns all registered feature classes with the given {@link FeaturePriority}.
     *
     * @param priority the priority to filter by
     * @return a list of matching feature classes
     */
    public List<Class<?>> getByPriority(final @NotNull FeaturePriority priority) {
        return classMap.keySet().stream()
            .filter(clazz -> clazz.getAnnotation(Feature.class).priority().equals(priority))
            .collect(Collectors.toList());
    }

    /**
     * Returns all registered feature classes matching both the given group and identifier
     * (case-insensitive), sorted by {@link FeaturePriority}.
     *
     * @param group      the group name to filter by
     * @param identifier the identifier to filter by
     * @return a sorted list of matching feature classes
     */
    public List<Class<?>> getByGroupAndIdentifier(final @NotNull String group, final @NotNull String identifier) {
        return getSortedByPriority().stream()   
            .filter(clazz -> clazz.getAnnotation(Feature.class).group().equalsIgnoreCase(group) && 
                clazz.getAnnotation(Feature.class).identifier().equalsIgnoreCase(identifier))
            .collect(Collectors.toList());
    }

    /**
     * Returns all registered feature classes sorted by their {@link FeaturePriority},
     * from {@link FeaturePriority#HIGHEST} to {@link FeaturePriority#LOWEST}.
     *
     * @return a priority-ordered list of all registered feature classes
     */
    public List<Class<?>> getSortedByPriority() {
        List<Class<?>> classList = new ArrayList<>();
        getByPriority(FeaturePriority.HIGHEST).forEach(classList::add);
        getByPriority(FeaturePriority.HIGH).forEach(classList::add);
        getByPriority(FeaturePriority.NORMAL).forEach(classList::add);
        getByPriority(FeaturePriority.LOW).forEach(classList::add);
        getByPriority(FeaturePriority.LOWEST).forEach(classList::add);
        return classList;
    }

    /**
     * Returns all currently enabled feature classes.
     *
     * @return a list of enabled feature classes
     */
    public List<Class<?>> getEnabledClasses() {
        return classMap.entrySet().stream().filter(entry -> entry.getValue()).map(entry -> entry.getKey()).collect(Collectors.toList());
    }

    /**
     * Returns all currently disabled (but registered) feature classes.
     *
     * @return a list of disabled feature classes
     */
    public List<Class<?>> getDisabledClasses() {
        return classMap.entrySet().stream().filter(entry -> !entry.getValue()).map(entry -> entry.getKey()).collect(Collectors.toList());
    }

    /**
     * Returns whether the given feature class is currently enabled.
     *
     * @param clazz the feature class to check
     * @return {@code true} if the feature is enabled, {@code false} otherwise
     */
    public boolean isEnabled(final @NotNull Class<?> clazz) {
        return classMap.get(clazz);
    }

}
