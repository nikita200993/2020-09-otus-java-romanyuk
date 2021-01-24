package ru.otus.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Arrays;

public final class HibernateUtils {

    private HibernateUtils() {
    }

    public static SessionFactory buildSessionFactory(String hibernateConfigFile, Class<?>... annotatedClasses) {
        Configuration configuration = new Configuration().configure(hibernateConfigFile);
        return buildSessionFactory(configuration, annotatedClasses);
    }

    public static SessionFactory buildSessionFactory(String hibernateConfigFile, final PostgreSQLContainer<?> postgreSQLContainer, Class<?>... annotatedClasses) {
        Configuration configuration = new Configuration().configure(hibernateConfigFile)
                .setProperty(
                        "hibernate.connection.url",
                        postgreSQLContainer.getJdbcUrl() + "&stringtype=unspecified")
                .setProperty("hibernate.connection.username", postgreSQLContainer.getUsername())
                .setProperty("hibernate.connection.password", postgreSQLContainer.getPassword());
        return buildSessionFactory(configuration, annotatedClasses);
    }

    public static SessionFactory buildSessionFactory(Configuration configuration, Class<?>... annotatedClasses) {
        MetadataSources metadataSources = new MetadataSources(createServiceRegistry(configuration));
        Arrays.stream(annotatedClasses).forEach(metadataSources::addAnnotatedClass);

        Metadata metadata = metadataSources.getMetadataBuilder().build();
        return metadata.getSessionFactoryBuilder().build();
    }

    private static StandardServiceRegistry createServiceRegistry(Configuration configuration) {
        return new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties()).build();
    }
}
