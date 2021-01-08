package ru.otus;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.otus.core.model.AddressDataSet;
import ru.otus.core.model.Client;
import ru.otus.core.model.PhoneDataSet;
import ru.otus.hibernate.HibernateUtils;
import ru.otus.testcontainers.CustomPostgreSQLContainer;

public class BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);
    private static final String HIBERNATE_CONFIG = "hibernate.cfg_test.xml";

    private static PostgreSQLContainer<?> postgreSQLContainer;
    private static SessionFactory sessionFactory;

    @BeforeAll
    public static void runContainerAndSetupSessionFactory() {
        LOGGER.info("Starting postgre container");
        postgreSQLContainer = new CustomPostgreSQLContainer();
        postgreSQLContainer.start();
        sessionFactory = HibernateUtils.buildSessionFactory(
                getConfiguration(HIBERNATE_CONFIG, postgreSQLContainer),
                Client.class,
                PhoneDataSet.class,
                AddressDataSet.class);
    }

    @AfterAll
    public static void closeContainerAndFactory() {
        LOGGER.info("Stopping postgre container");
        sessionFactory.close();
        postgreSQLContainer.close();
    }

    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private static Configuration getConfiguration(
            final String configResourceName,
            final PostgreSQLContainer<?> postgreSQLContainer) {
        return new Configuration().configure(configResourceName)
                .setProperty(
                        "hibernate.connection.url",
                        postgreSQLContainer.getJdbcUrl() + "&stringtype=unspecified")
                .setProperty("hibernate.connection.username", postgreSQLContainer.getUsername())
                .setProperty("hibernate.connection.password", postgreSQLContainer.getPassword());
    }
}
