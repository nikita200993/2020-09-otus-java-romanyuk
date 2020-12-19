package ru.otus.core.service;

import ru.otus.core.model.Client;

import java.util.Optional;

public interface DBServiceClient {

    void saveClient(Client client);

    Optional<Client> getClient(long id);
}
