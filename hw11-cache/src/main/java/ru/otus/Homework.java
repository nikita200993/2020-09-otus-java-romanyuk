package ru.otus;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.otus.cachehw.service.CachingClientService;
import ru.otus.core.dao.ClientDao;
import ru.otus.core.model.AddressDataSet;
import ru.otus.core.model.Client;
import ru.otus.core.model.PhoneDataSet;
import ru.otus.core.service.DbServiceClientImpl;
import ru.otus.hibernate.HibernateUtils;
import ru.otus.hibernate.dao.ClientDaoHibernate;
import ru.otus.hibernate.sessionmanager.SessionManagerHibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Homework {

    private static final Logger LOGGER = LoggerFactory.getLogger(Homework.class);
    private static final String HIBERNATE_CONFIG = "hibernate.cfg.xml";

    public static void main(String[] args) throws InterruptedException {
        try (PostgreSQLContainer<?> container = getRunningContainer()) {
            try (SessionFactory sessionFactory = getSessionFactory(
                    container.getJdbcUrl() + "&stringtype=unspecified",
                    container.getUsername(),
                    container.getPassword())) {
                final ClientDao clientDao = new ClientDaoHibernate(new SessionManagerHibernate(sessionFactory));
                final CachingClientService cachingClientService = new CachingClientService(
                        new DbServiceClientImpl(clientDao));
                List<Long> ids = getClients().stream()
                        .map(cachingClientService::saveClient)
                        .collect(Collectors.toList());
                long startTime = System.currentTimeMillis();
                List<Client> loadedClients = ids.stream()
                        .map(cachingClientService::getClient)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList());
                LOGGER.info("Execution time without cache {}ms", System.currentTimeMillis() - startTime);
                startTime = System.currentTimeMillis();
                List<Client> cachedClients = ids.stream()
                        .map(cachingClientService::getClient)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList());
                LOGGER.info("Execution time with cache {}ms", System.currentTimeMillis() - startTime);
                assert cachedClients.size() == loadedClients.size();
                ids = null;
                cachedClients = null;
                loadedClients = null;
                LOGGER.info("Cache size before gc {}", cachingClientService.cacheSize());
                System.gc();
                Thread.sleep(1000);
                LOGGER.info("Cache size after gc {}", cachingClientService.cacheSize());
                // size doesn't change because weak hash map has only "weak" keys, not values, and client class refers to the same long
            }
        }
    }

    private static List<Client> getClients() {
        final List<Client> result = new ArrayList<>();
        result.add(new Client("Petr"));
        result.add(new Client("John"));
        result.add(new Client("Kate"));
        result.add(new Client("Kek"));
        result.add(new Client("Lol"));
        return result;
    }

    private static PostgreSQLContainer<?> getRunningContainer() {
        final var container = new PostgreSQLContainer<>("postgres:12");
        container.start();
        return container;
    }

    private static SessionFactory getSessionFactory(final String url, final String username, final String password) {
        final var config = new Configuration().configure(HIBERNATE_CONFIG)
                .setProperty("hibernate.connection.url", url)
                .setProperty("hibernate.connection.username", username)
                .setProperty("hibernate.connection.password", password);
        return HibernateUtils.buildSessionFactory(config, Client.class, AddressDataSet.class, PhoneDataSet.class);
    }

    private static Configuration getConfiguration(
            final String configResourceName,
            final PostgreSQLContainer<?> postgreSQLContainer) {
        return new Configuration().configure(configResourceName)
                .setProperty(
                        "hibernate.connection.url",
                        postgreSQLContainer.getJdbcUrl() + "&stringtype=unspecified")
                .setProperty("hibernate.connection.username", postgreSQLContainer.getUsername())
                .setProperty("hibernate.connection.password", postgreSQLContainer.getPassword());
    }

}
