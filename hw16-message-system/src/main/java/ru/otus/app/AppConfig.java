package ru.otus.app;

import org.hibernate.SessionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.otus.app.hibernate.HibernateUtils;
import ru.otus.app.hibernate.dao.UserDaoHibernate;
import ru.otus.app.hibernate.sessionmanager.SessionManagerHibernate;
import ru.otus.app.model.User;
import ru.otus.app.services.UserService;
import ru.otus.app.services.UserServiceImpl;

@SpringBootApplication
public class AppConfig {

    public static void main(final String[] args) {
        new SpringApplication(AppConfig.class).run(args);
    }

    @Bean
    public SessionFactory sessionFactory() {
        return HibernateUtils.buildSessionFactory("hibernate.cfg.xml", User.class);
    }

    @Bean
    public SessionManagerHibernate sessionManagerHibernate(final SessionFactory sessionFactory) {
        return new SessionManagerHibernate(sessionFactory);
    }

    @Bean
    public UserDaoHibernate userDaoHibernate(final SessionManagerHibernate sessionManagerHibernate) {
        return new UserDaoHibernate(sessionManagerHibernate);
    }

    @Bean
    public UserService userService(final UserDaoHibernate userDaoHibernate) {
        return new UserServiceImpl(userDaoHibernate);
    }
}
