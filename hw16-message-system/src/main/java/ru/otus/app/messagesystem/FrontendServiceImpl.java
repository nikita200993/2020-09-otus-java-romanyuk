package ru.otus.app.messagesystem;

import ru.otus.messagesystem.client.MessageCallback;
import ru.otus.messagesystem.client.MsClient;
import ru.otus.messagesystem.message.Message;
import ru.otus.messagesystem.message.MessageType;
import ru.otus.utils.Contracts;

public class FrontendServiceImpl implements FrontendService {

    private final MsClient msClient;
    private final String databaseServiceClientName;

    public FrontendServiceImpl(final MsClient msClient, final String databaseServiceClientName) {
        Contracts.requireNonNullArgument(msClient);
        Contracts.requireNonNullArgument(databaseServiceClientName);

        this.msClient = msClient;
        this.databaseServiceClientName = databaseServiceClientName;
    }

    @Override
    public void serveRequest(
            final UserRequestAndResponse request,
            final MessageCallback<UserRequestAndResponse> onComplete) {
        Contracts.requireNonNullArgument(request);
        Contracts.requireNonNullArgument(onComplete);

        final Message message = msClient.produceMessage(
                databaseServiceClientName,
                request,
                MessageType.USER_DATA,
                onComplete
        );
        msClient.sendMessage(message);
    }
}
