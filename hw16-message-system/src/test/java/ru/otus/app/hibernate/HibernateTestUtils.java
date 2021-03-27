package ru.otus.app.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;

public class HibernateTestUtils {

    private HibernateTestUtils() {
        throw new IllegalAccessError();
    }

    public static SessionFactory buildSessionFactory(
            final String hibernateConfigFile,
            final PostgreSQLContainer<?> postgreSQLContainer,
            final Class<?>... annotatedClasses) {
        Configuration configuration = new Configuration().configure(hibernateConfigFile)
                .setProperty(
                        "hibernate.connection.url",
                        postgreSQLContainer.getJdbcUrl() + "&stringtype=unspecified")
                .setProperty("hibernate.connection.username", postgreSQLContainer.getUsername())
                .setProperty("hibernate.connection.password", postgreSQLContainer.getPassword());
        return HibernateUtils.buildSessionFactory(configuration, annotatedClasses);
    }
}
