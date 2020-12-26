package ru.otus;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.core.dao.ClientDao;
import ru.otus.core.model.Account;
import ru.otus.core.model.Client;
import ru.otus.core.service.DBServiceAccount;
import ru.otus.core.service.DBServiceAccountImpl;
import ru.otus.core.service.DbServiceClientImpl;
import ru.otus.demo.DataSourceDemo;
import ru.otus.jdbc.DbExecutorImpl;
import ru.otus.jdbc.dao.JdbcMapperAccountDao;
import ru.otus.jdbc.dao.JdbcMapperClientDao;
import ru.otus.jdbc.mapper.JdbcMapper;
import ru.otus.jdbc.mapper.JdbcMapperImpl;
import ru.otus.jdbc.mapper.SQLEntity;
import ru.otus.jdbc.mapper.SqlEntityFactory;
import ru.otus.jdbc.sessionmanager.SessionManagerJdbc;

import javax.sql.DataSource;


public class HomeWork {
    private static final Logger logger = LoggerFactory.getLogger(HomeWork.class);

    public static void main(String[] args) {
// Общая часть
        var dataSource = new DataSourceDemo();
        flywayMigrations(dataSource);
        final var sessionManager = new SessionManagerJdbc(dataSource);
        final SqlEntityFactory factory = new SqlEntityFactory();

// Работа с пользователем
        final SQLEntity<Client> clientSql = factory.from(Client.class);
        final JdbcMapper<Client> jdbcMapper = new JdbcMapperImpl<>(clientSql, new DbExecutorImpl());
        final ClientDao clientDao = new JdbcMapperClientDao(sessionManager, jdbcMapper);
        final DbServiceClientImpl dbServiceClient = new DbServiceClientImpl(clientDao);
        final Client client1 = new Client(1, "John", 30);
        final Client client2 = new Client(2, "Ivan", 23);
        dbServiceClient.saveClient(client1);
        dbServiceClient.saveClient(client2);
        logger.info("Expected: {}, actual {}", client1, dbServiceClient.getClient(1).get());
        logger.info("Expected: {}, actual {}", client2, dbServiceClient.getClient(2).get());
        // check insertOrUpdate
        dbServiceClient.saveClient(new Client(1, "Petr", 30));
        logger.info("Should be changed to 'Petr': '{}'", dbServiceClient.getClient(1).get().getName());
        // Работа со счетом
        final JdbcMapper<Account> accountJdbcMapper = new JdbcMapperImpl<>(
                factory.from(Account.class),
                new DbExecutorImpl());
        final DBServiceAccount dbServiceAccount = new DBServiceAccountImpl(
                new JdbcMapperAccountDao(accountJdbcMapper, sessionManager));
        final Account account1 = new Account("A", "a", 22.1f);
        final Account account2 = new Account("B", "b", 101.10f);
        dbServiceAccount.saveAccount(account1);
        dbServiceAccount.saveAccount(account2);
        logger.info("Expected {}, actual {}", account1, dbServiceAccount.findById("A").get());
        logger.info("Expected {}, actual {}", account2, dbServiceAccount.findById("B").get());
        // check insertOrUpdate
        dbServiceAccount.saveAccount(new Account("A", "b", 30.5f));
        logger.info("Should be changed to  'b', actual '{}'", dbServiceAccount.findById("A").get().getType());
    }

    private static void flywayMigrations(DataSource dataSource) {
        logger.info("db migration started...");
        var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:/db/migration")
                .load();
        flyway.migrate();
        logger.info("db migration finished.");
        logger.info("***");
    }
}
