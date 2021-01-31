package ru.otus.hibernate.dao;

import org.hibernate.Session;
import ru.otus.dao.DaoException;
import ru.otus.dao.MyUserDao;
import ru.otus.hibernate.sessionmanager.SessionManagerHibernate;
import ru.otus.model.MyUser;
import ru.otus.sessionmanager.SessionManager;
import ru.otus.utils.Contracts;

import java.util.List;
import java.util.Optional;

public class MyUserDaoHibernate implements MyUserDao {

    private final SessionManagerHibernate sessionManagerHibernate;

    public MyUserDaoHibernate(final SessionManagerHibernate sessionManagerHibernate) {
        this.sessionManagerHibernate = Contracts.ensureNonNullArgument(sessionManagerHibernate);
    }

    @Override
    public List<MyUser> findAll() {
        try {
            final Session session = sessionManagerHibernate.getCurrentSession().getHibernateSession();
            return session.createQuery("select u from user u", MyUser.class)
                    .getResultList();
        } catch (final Exception exception) {
            throw new DaoException(exception);
        }
    }

    @Override
    public Optional<MyUser> findById(long id) {
        try {
            final Session session = sessionManagerHibernate.getCurrentSession().getHibernateSession();
            return Optional.ofNullable(session.find(MyUser.class, id));
        } catch (final Exception exception) {
            throw new DaoException(exception);
        }
    }

    @Override
    public Optional<MyUser> findByLogin(final String login) {
        Contracts.requireNonNullArgument(login);

        try {
            final Session session = sessionManagerHibernate.getCurrentSession().getHibernateSession();
            return session.createQuery("select u from user u where login = :login", MyUser.class)
                    .setParameter("login", login)
                    .uniqueResultOptional();
        } catch (final Exception exception) {
            throw new DaoException(exception);
        }
    }

    @Override
    public long insert(final MyUser user) throws DaoException {
        try {
            final Session session = sessionManagerHibernate.getCurrentSession().getHibernateSession();
            session.persist(user);
            session.flush();
            return Contracts.ensureNonNull(user.getId());
        } catch (final Exception exception) {
            throw new DaoException(exception);
        }
    }

    @Override
    public SessionManager getSessionManager() {
        return sessionManagerHibernate;
    }
}
