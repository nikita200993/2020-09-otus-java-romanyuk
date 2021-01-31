package ru.otus.appcontainer.implementation;

import ru.otus.appcontainer.api.AppComponent;
import ru.otus.appcontainer.api.AppComponentsContainerConfig;
import ru.otus.utils.Contracts;
import ru.otus.utils.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

class Config implements Comparable<Config> {

    private final Class<?> configClass;
    private final List<ComponentDefinition> componentDefinitions;
    private final int order;

    private Config(
            final Class<?> configClass,
            final List<ComponentDefinition> componentDefinitions,
            final int order) {
        this.configClass = Contracts.ensureNonNullArgument(configClass);
        this.componentDefinitions = Contracts.ensureNonNullArgument(componentDefinitions);
        this.order = order;
    }

    @Override
    public int compareTo(final Config o) {
        Contracts.requireNonNullArgument(o);

        return Integer.compare(order, o.order);
    }

    static Config createConfig(final Class<?> clazz) {
        Contracts.requireNonNullArgument(clazz);

        final int order = Contracts.ensureNonNull(clazz.getAnnotation(AppComponentsContainerConfig.class))
                .order();
        final List<ComponentDefinition> componentDefinitions = ReflectionUtils.getDeclaredMethods(clazz)
                .filter(method -> Modifier.isPublic(Modifier.methodModifiers()))
                .filter(method -> method.isAnnotationPresent(AppComponent.class))
                .map(Config::createConfig)
                .sorted()
                .collect(Collectors.toList());
        return new Config(clazz, List.copyOf(componentDefinitions), order);
    }

    void instantiateComponents(final ComponentCreationContext context) {
        final boolean factoryMethodNeedsReceiver = componentDefinitions.stream()
                .anyMatch(component -> !Modifier.isStatic(component.getMethod().getModifiers()));
        final Object receiver = factoryMethodNeedsReceiver
                ? ReflectionUtils.instantiateUsingNoArgConstructor(configClass) : null;
        for (final ComponentDefinition componentDefinition : componentDefinitions) {
            final Object component = componentDefinition.createComponent(receiver, context);
            context.putComponent(componentDefinition.getDeclaredType(), componentDefinition.getName(), component);
        }
    }

    private static ComponentDefinition createConfig(final Method method) {
        final Class<?> returnType = method.getReturnType();
        final AppComponent appComponent = method.getAnnotation(AppComponent.class);
        final int order = appComponent.order();
        final String name = appComponent.name();
        return new ComponentDefinition(returnType, name, method, order);
    }
}
