package ru.otus;


import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.core.model.AddressDataSet;
import ru.otus.core.model.Client;
import ru.otus.core.model.PhoneDataSet;

import javax.persistence.PersistenceException;

public class TestEntities extends BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEntities.class);

    private Session session;
    private Transaction transaction;

    @BeforeEach
    void init() {
        session = getSessionFactory().openSession();
        transaction = session.beginTransaction();
    }

    @AfterEach
    void rollbackAndClose() {
        try {
            transaction.rollback();
        } catch (final PersistenceException persistenceException) {
            LOGGER.error("Unable to rollback", persistenceException);
        } finally {
            getSessionFactory().getStatistics().clear();
            session.close();
        }
    }

    @Test
    void testClientCreationCascades() {
        final var client = new Client("Petr");
        client.setAddress(new AddressDataSet("abc street"));
        client.addPhone(new PhoneDataSet("911"));
        client.addPhone(new PhoneDataSet("007"));
        session.persist(client);
        session.flush();
        final Statistics stats = getSessionFactory().getStatistics();
        Assertions.assertEquals(4, stats.getEntityInsertCount());
        Assertions.assertEquals(0, stats.getEntityUpdateCount());
        Assertions.assertEquals(0, stats.getEntityLoadCount());
    }

    @Test
    void testClientDeletionRemovesChildEntities() {
        final var client = new Client("Petr");
        client.setAddress(new AddressDataSet("abc street"));
        client.addPhone(new PhoneDataSet("911"));
        session.persist(client);
        session.flush();
        session.delete(client);
        session.flush();
        Assertions.assertEquals(0, countEntities("AddressDataSet"));
        Assertions.assertEquals(0, countEntities("PhoneDataSet"));
    }

    private long countEntities(final String entityName) {
        return session.createQuery("select count(a) from " + entityName + " a", Long.class)
                .uniqueResult();
    }
}
