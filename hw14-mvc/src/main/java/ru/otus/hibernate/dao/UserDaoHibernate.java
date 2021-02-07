package ru.otus.hibernate.dao;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import ru.otus.dao.DaoException;
import ru.otus.dao.UserDao;
import ru.otus.hibernate.sessionmanager.SessionManagerHibernate;
import ru.otus.model.User;
import ru.otus.sessionmanager.SessionManager;
import ru.otus.utils.Contracts;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoHibernate implements UserDao {

    private final SessionManagerHibernate sessionManagerHibernate;

    public UserDaoHibernate(final SessionManagerHibernate sessionManagerHibernate) {
        this.sessionManagerHibernate = Contracts.ensureNonNullArgument(sessionManagerHibernate);
    }

    @Override
    public List<User> findAll() {
        try {
            final Session session = sessionManagerHibernate.getCurrentSession().getHibernateSession();
            return session.createQuery("select u from user u", User.class)
                    .getResultList();
        } catch (final Exception exception) {
            throw new DaoException(exception);
        }
    }

    @Override
    public Optional<User> findById(long id) {
        try {
            final Session session = sessionManagerHibernate.getCurrentSession().getHibernateSession();
            return Optional.ofNullable(session.find(User.class, id));
        } catch (final Exception exception) {
            throw new DaoException(exception);
        }
    }

    @Override
    public Optional<User> findByLogin(final String login) {
        Contracts.requireNonNullArgument(login);

        try {
            final Session session = sessionManagerHibernate.getCurrentSession().getHibernateSession();
            return session.createQuery("select u from user u where login = :login", User.class)
                    .setParameter("login", login)
                    .uniqueResultOptional();
        } catch (final Exception exception) {
            throw new DaoException(exception);
        }
    }

    @Override
    public long insert(final User user) throws DaoException {
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
