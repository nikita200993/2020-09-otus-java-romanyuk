package ru.otus.app.messagesystem;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.app.services.UserService;
import ru.otus.messagesystem.HandlersStoreImpl;
import ru.otus.messagesystem.MessageSystemImpl;
import ru.otus.messagesystem.client.CallbackRegistryImpl;
import ru.otus.messagesystem.client.MsClientImpl;
import ru.otus.messagesystem.message.MessageType;
import ru.otus.utils.Contracts;

@Configuration
public class MessageSystemConfig {

    private static final String DB_CLIENT_NAME = "database";
    private static final String FRONT_CLIENT_NAME = "frontend";

    @Bean
    FrontendService frontendService(final UserService userService) {
        Contracts.requireNonNullArgument(userService);

        final var messageSystem = new MessageSystemImpl();
        final var callbackRegistry = new CallbackRegistryImpl();
        final var handlersStoreForDbClient = new HandlersStoreImpl();
        handlersStoreForDbClient.addHandler(MessageType.USER_DATA, new UserRequestHandler(userService));
        messageSystem.addClient(
                new MsClientImpl(
                        DB_CLIENT_NAME,
                        messageSystem,
                        handlersStoreForDbClient,
                        callbackRegistry
                )
        );
        final var handlerForFrontendClient = new HandlersStoreImpl();
        handlerForFrontendClient.addHandler(MessageType.USER_DATA, new UserResponseHandler(callbackRegistry));
        final var frontClient = new MsClientImpl(
                FRONT_CLIENT_NAME,
                messageSystem,
                handlerForFrontendClient,
                callbackRegistry
        );
        messageSystem.addClient(frontClient);
        return new FrontendServiceImpl(frontClient, DB_CLIENT_NAME);
    }
}
