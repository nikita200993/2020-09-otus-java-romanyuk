package ru.otus.appcontainer.implementation;

import ru.otus.utils.Contracts;
import ru.otus.utils.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class ComponentDefinition implements Comparable<ComponentDefinition> {

    private final Class<?> declaredType;
    private final String name;
    private final Method method;
    private final List<Class<?>> dependencies;
    private final int order;

    ComponentDefinition(
            final Class<?> declaredType,
            final String name,
            final Method method,
            final int order) {
        this.declaredType = Contracts.ensureNonNullArgument(declaredType);
        this.name = name;
        this.method = Contracts.ensureNonNullArgument(method);
        this.dependencies = Arrays.asList(method.getParameterTypes());
        this.order = order;
    }

    Object createComponent(@Nullable final Object factory, final ComponentCreationContext context) {
        Contracts.requireNonNullArgument(context);

        final List<Object> argumentsOfFactoryMethod = new ArrayList<>();
        for (final var dependencyType : dependencies) {
            final Object dependency = Contracts.ensureNonNull(
                    context.getByType(dependencyType),
                    String.format(
                            "Component named %s of type %s needs component of type %s.",
                            name,
                            declaredType,
                            dependencyType
                    )
            );
            argumentsOfFactoryMethod.add(dependency);
        }
        return ReflectionUtils.invokeMethod(method, factory, argumentsOfFactoryMethod.toArray());
    }

    String getName() {
        return name;
    }

    Method getMethod() {
        return method;
    }

    Class<?> getDeclaredType() {
        return declaredType;
    }

    @Override
    public int compareTo(final ComponentDefinition other) {
        Contracts.requireNonNullArgument(other);

        return Integer.compare(order, other.order);
    }
}
