package ru.otus.cachehw.service;

import ru.otus.cachehw.HwCache;
import ru.otus.cachehw.MyCache;
import ru.otus.core.model.Client;
import ru.otus.core.service.DBServiceClient;
import ru.otus.utils.Contracts;

import java.util.Optional;

public class CachingClientService implements DBServiceClient {

    private final HwCache<Long, Client> hwCache = new MyCache<>();

    private final DBServiceClient serviceClient;

    public CachingClientService(final DBServiceClient serviceClient) {
        this.serviceClient = Contracts.ensureNonNullArgument(serviceClient);
    }

    @Override
    public long saveClient(final Client client) {
        Contracts.requireNonNullArgument(client);

        if (client.getId() != null) {
            hwCache.remove(client.getId());
        }
        return serviceClient.saveClient(client);
    }

    @Override
    public Optional<Client> getClient(long id) {
        final var cachedResult = hwCache.get(id);
        if (cachedResult != null) {
            return Optional.of(cachedResult);
        }
        return serviceClient.getClient(id);
    }
}
