package ru.otus.app;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.app.hibernate.HibernateUtils;
import ru.otus.app.hibernate.dao.UserDaoHibernate;
import ru.otus.app.hibernate.sessionmanager.SessionManagerHibernate;
import ru.otus.app.model.User;
import ru.otus.app.services.UserService;
import ru.otus.app.services.UserServiceImpl;

@Configuration
public class AppConfig {

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
