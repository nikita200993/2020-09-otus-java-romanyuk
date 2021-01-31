package ru.otus.appcontainer.implementation;

import ru.otus.utils.Contracts;

import java.util.HashMap;
import java.util.Map;

class ComponentCreationContext {

    private final Map<String, Object> componentsByName;
    private final Map<Class<?>, Object> componentsByType;

    ComponentCreationContext() {
        componentsByType = new HashMap<>();
        componentsByName = new HashMap<>();
    }

    void putComponent(final Class<?> clazz, final String name, final Object component) {
        Contracts.requireNonNullArgument(clazz);
        Contracts.requireNonNullArgument(name);
        Contracts.requireNonNullArgument(component);

        componentsByType.put(clazz, component);
        Contracts.forbidThat(
                componentsByName.containsKey(name),
                "Duplicated name '" + name + "'."
        );
        componentsByName.put(name, component);
    }

    Object getByType(final Class<?> clazz) {
        Contracts.requireNonNullArgument(clazz);

        return componentsByType.get(clazz);
    }

    Map<String, Object> getComponentsByName() {
        return componentsByName;
    }

    Map<Class<?>, Object> getComponentsByType() {
        return componentsByType;
    }
}
