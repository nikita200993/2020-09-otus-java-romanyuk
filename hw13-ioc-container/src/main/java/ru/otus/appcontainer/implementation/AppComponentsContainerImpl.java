package ru.otus.appcontainer.implementation;

import ru.otus.appcontainer.api.AppComponentsContainer;
import ru.otus.utils.Contracts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AppComponentsContainerImpl implements AppComponentsContainer {

    private final Map<String, Object> appComponentsByName;
    private final Map<Class<?>, Object> appComponentsByDeclaredType;
    private final Map<Class<?>, Object> appComponentsByExactType;

    private AppComponentsContainerImpl(
            final Map<Class<?>, Object> appComponentsByDeclaredType,
            final Map<String, Object> appComponentsByName) {
        this.appComponentsByName = appComponentsByName;
        this.appComponentsByDeclaredType = appComponentsByDeclaredType;
        this.appComponentsByExactType = appComponentsByDeclaredType.entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                entry -> entry.getValue().getClass(),
                                Map.Entry::getValue
                        )
                );
    }

    public static AppComponentsContainerImpl create(final Class<?> configClass, final Class<?>... otherConfigClasses) {
        Contracts.requireNonNullArgument(configClass);
        Contracts.requireNonNullArgument(otherConfigClasses);

        final List<Class<?>> configClasses = new ArrayList<>();
        configClasses.add(configClass);
        Arrays.stream(otherConfigClasses)
                .map(Contracts::ensureNonNull)
                .forEach(configClasses::add);
        return createContainer(configClasses);
    }

    public static AppComponentsContainerImpl createContainer(final List<Class<?>> configClasses) {
        Contracts.requireNonNullArgument(configClasses);

        final ComponentCreationContext context = new ComponentCreationContext();
        configClasses.stream()
                .map(Contracts::ensureNonNull)
                .map(Config::createConfig)
                .sorted()
                .forEachOrdered(config -> config.instantiateComponents(context));
        return fromContext(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C> C getAppComponent(final Class<C> componentClass) {
        Contracts.requireNonNullArgument(componentClass);

        final C matchByDeclaredType = (C) appComponentsByDeclaredType.get(componentClass);
        if (matchByDeclaredType != null) {
            return matchByDeclaredType;
        }
        return (C) appComponentsByExactType.get(componentClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C> C getAppComponent(final String componentName) {
        Contracts.requireNonNullArgument(componentName);

        return (C) appComponentsByName.get(componentName);
    }

    private static AppComponentsContainerImpl fromContext(final ComponentCreationContext context) {
        return new AppComponentsContainerImpl(
                context.getComponentsByType(),
                context.getComponentsByName()
        );
    }
}
