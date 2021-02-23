package ru.otus.hibernate.sessionmanager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;
import ru.otus.sessionmanager.SessionManager;
import ru.otus.sessionmanager.SessionManagerException;
import ru.otus.utils.Contracts;

@Component
public class SessionManagerHibernate implements SessionManager {

    private final SessionFactory sessionFactory;
    private final ThreadLocal<DatabaseSessionHibernate> databaseSession;

    public SessionManagerHibernate(final SessionFactory sessionFactory) {
        Contracts.requireNonNullArgument(sessionFactory);

        this.sessionFactory = sessionFactory;
        this.databaseSession = new ThreadLocal<>();
    }

    @Override
    public void beginSession() {
        try {
            databaseSession.set(new DatabaseSessionHibernate(sessionFactory.openSession()));
        } catch (Exception e) {
            throw new SessionManagerException(e);
        }
    }

    @Override
    public void commitSession() {
        checkSessionAndTransaction();
        try {
            databaseSession.get()
                    .getTransaction()
                    .commit();
            databaseSession.get()
                    .getHibernateSession()
                    .close();
        } catch (Exception e) {
            throw new SessionManagerException(e);
        }
    }

    @Override
    public void rollbackSession() {
        checkSessionAndTransaction();
        try {
            databaseSession.get()
                    .getTransaction()
                    .rollback();
            databaseSession.get()
                    .getHibernateSession()
                    .close();
        } catch (Exception e) {
            throw new SessionManagerException(e);
        }
    }

    @Override
    public void close() {
        if (databaseSession.get() == null) {
            return;
        }
        Session session = databaseSession.get()
                .getHibernateSession();
        if (session == null || !session.isConnected()) {
            return;
        }

        Transaction transaction = databaseSession.get()
                .getTransaction();
        if (transaction == null || !transaction.isActive()) {
            return;
        }

        try {
            databaseSession.get()
                    .close();
            databaseSession.set(null);
        } catch (Exception e) {
            throw new SessionManagerException(e);
        }
    }

    @Override
    public DatabaseSessionHibernate getCurrentSession() {
        checkSessionAndTransaction();
        return databaseSession.get();
    }

    private void checkSessionAndTransaction() {
        if (databaseSession.get() == null) {
            throw new SessionManagerException("DatabaseSession not opened ");
        }
        Session session = databaseSession.get()
                .getHibernateSession();
        if (session == null || !session.isConnected()) {
            throw new SessionManagerException("Session not opened ");
        }
        Transaction transaction = databaseSession.get()
                .getTransaction();
        if (transaction == null || !transaction.isActive()) {
            throw new SessionManagerException("Transaction not opened ");
        }
    }
}
