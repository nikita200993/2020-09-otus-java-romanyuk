package ru.otus.app;


import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.otus.app.hibernate.HibernateTestUtils;
import ru.otus.app.model.User;

@Import(WebConfig.class)
@Configuration
public class TestConfig {

    private static final String HIBERNATE_CONFIG = "hibernate-test.cfg.xml";

    @Bean
    PostgreSQLContainer<?> postgreSQLContainer() {
        final var postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");
        postgreSQLContainer.start();
        return postgreSQLContainer;
    }

    @Bean
    SessionFactory sessionFactory(final PostgreSQLContainer<?> runningContainer) {
        return HibernateTestUtils.buildSessionFactory(HIBERNATE_CONFIG, runningContainer, User.class);
    }
}
