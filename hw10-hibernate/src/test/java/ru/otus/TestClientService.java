package ru.otus;

import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.otus.core.model.AddressDataSet;
import ru.otus.core.model.Client;
import ru.otus.core.model.PhoneDataSet;
import ru.otus.core.service.DBServiceClient;
import ru.otus.core.service.DbServiceClientImpl;
import ru.otus.hibernate.dao.ClientDaoHibernate;
import ru.otus.hibernate.sessionmanager.SessionManagerHibernate;

public class TestClientService extends BaseTest {

    private final DBServiceClient clientService = new DbServiceClientImpl(
            new ClientDaoHibernate(new SessionManagerHibernate(getSessionFactory()))
    );

    @Test
    void testSavingNewClient() {
        final var client = createClient();
        clientService.saveClient(client);
        final var loadedClientOptional = clientService.getClient(client.getId());
        Assertions.assertTrue(loadedClientOptional.isPresent());
        Assertions.assertEquals(client, loadedClientOptional.get());
        final Statistics stats = getSessionFactory().getStatistics();
        Assertions.assertEquals(0, stats.getEntityUpdateCount());
    }

    private static Client createClient() {
        final var client = new Client("Peter");
        client.setAddress(new AddressDataSet("abc street"));
        client.addPhone(new PhoneDataSet("911"));
        return client;
    }
}
