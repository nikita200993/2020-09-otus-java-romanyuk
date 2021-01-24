package ru.otus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hibernate.SessionFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.otus.hibernate.HibernateUtils;
import ru.otus.hibernate.dao.MyUserDaoHibernate;
import ru.otus.hibernate.sessionmanager.SessionManagerHibernate;
import ru.otus.model.MyUser;
import ru.otus.server.MyUsersWebServer;
import ru.otus.services.MyUserService;
import ru.otus.services.MyUserServiceImpl;
import ru.otus.services.TemplateProcessor;
import ru.otus.services.TemplateProcessorImpl;

import java.io.IOException;

public class ServerRunner {

    private static final int WEB_SERVER_PORT = 8080;
    private static final String TEMPLATES_DIR = "/templates/";
    private static final String HIBERNATE_CONFIG = "hibernate.cfg.xml";

    private final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");

    public static void main(final String[] args) {
        new ServerRunner().run();
    }

    private void run() {
        postgreSQLContainer.start();
        final var userService = createUserService();
        userService.insert(new MyUser("admin", "admin", MyUser.Role.ADMIN));
        final var server = new MyUsersWebServer(
                WEB_SERVER_PORT,
                userService,
                createGson(),
                createTemplateProcessor()
        );
        try {
            server.start();
            server.join();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private MyUserService createUserService() {
        return new MyUserServiceImpl(
                new MyUserDaoHibernate(
                        new SessionManagerHibernate(
                                buildHibernateSessionFactory()
                        )
                )
        );
    }

    private SessionFactory buildHibernateSessionFactory() {
        return HibernateUtils.buildSessionFactory(HIBERNATE_CONFIG, postgreSQLContainer, MyUser.class);
    }

    private Gson createGson() {
        return new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .create();
    }

    private TemplateProcessor createTemplateProcessor() {
        try {
            return new TemplateProcessorImpl(TEMPLATES_DIR);
        } catch (final IOException ioException) {
            throw new RuntimeException("unable to create template processor", ioException);
        }
    }
}
