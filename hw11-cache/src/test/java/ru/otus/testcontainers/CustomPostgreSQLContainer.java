package ru.otus.testcontainers;

import org.testcontainers.containers.PostgreSQLContainer;

public class CustomPostgreSQLContainer extends PostgreSQLContainer<CustomPostgreSQLContainer> {

    private static final String IMAGE_VERSION = "postgres:12";

    private static CustomPostgreSQLContainer container;

    public CustomPostgreSQLContainer() {
        super(IMAGE_VERSION);
    }

    public static CustomPostgreSQLContainer getInstance() {
        if (container == null) {
            container = new CustomPostgreSQLContainer();
        }
        return container;
    }
}
