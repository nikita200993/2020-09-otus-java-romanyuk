package ru.otus.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.otus.model.MyUser;

public class BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);
    private static final String HIBERNATE_CONFIG = "hibernate-test.cfg.xml";

    private static PostgreSQLContainer<?> postgreSQLContainer;
    private static SessionFactory sessionFactory;

    @BeforeAll
    public static void runContainerAndSetupSessionFactory() {
        LOGGER.info("Starting postgre container");
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");
        postgreSQLContainer.start();
        sessionFactory = HibernateUtils.buildSessionFactory(HIBERNATE_CONFIG, postgreSQLContainer, MyUser.class);
    }

    @AfterAll
    public static void closeContainerAndFactory() {
        LOGGER.info("Stopping postgre container");
        sessionFactory.close();
        postgreSQLContainer.close();
    }

    protected static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
