package ru.otus.hibernate.dao;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.otus.hibernate.BaseTest;
import ru.otus.hibernate.sessionmanager.SessionManagerHibernate;
import ru.otus.model.MyUser;
import ru.otus.services.MyUserService;
import ru.otus.services.MyUserServiceImpl;

public class HibernateDaoTest extends BaseTest {

    private static final MyUserService myUserService = new MyUserServiceImpl(
            new MyUserDaoHibernate(
                    new SessionManagerHibernate(getSessionFactory())
            )
    );

    @Test
    void testInsert() {
        final var user = getUserOne();
        myUserService.insert(user);
        Assertions.assertThat(myUserService.findById(user.getId()))
                .isPresent()
                .get()
                .isEqualTo(user);
    }

    @Test
    void testFindByLogin() {
        final var user = getUserTwo();
        myUserService.insert(user);
        Assertions.assertThat(myUserService.findByLogin(user.getLogin()))
                .isPresent()
                .get()
                .isEqualTo(user);
    }

    private static MyUser getUserOne() {
        return new MyUser("ab", "ab", MyUser.Role.USER);
    }

    private static MyUser getUserTwo() {
        return new MyUser("abc", "abc", MyUser.Role.USER);
    }
}
