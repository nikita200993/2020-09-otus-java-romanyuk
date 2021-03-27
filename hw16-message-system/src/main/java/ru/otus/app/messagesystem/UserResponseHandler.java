package ru.otus.app.messagesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.messagesystem.RequestHandler;
import ru.otus.messagesystem.client.CallbackRegistry;
import ru.otus.messagesystem.client.MessageCallback;
import ru.otus.messagesystem.client.ResultDataType;
import ru.otus.messagesystem.message.Message;
import ru.otus.messagesystem.message.MessageHelper;
import ru.otus.utils.Contracts;

import java.util.Optional;

public class UserResponseHandler implements RequestHandler<UserRequestAndResponse> {
    private static final Logger logger = LoggerFactory.getLogger(UserResponseHandler.class);

    private final CallbackRegistry callbackRegistry;

    public UserResponseHandler(final CallbackRegistry callbackRegistry) {
        Contracts.requireNonNullArgument(callbackRegistry);

        this.callbackRegistry = callbackRegistry;
    }

    @Override
    public Optional<Message> handle(final Message msg) {
        try {
            MessageCallback<? extends ResultDataType> callback = callbackRegistry.getAndRemove(msg.getCallbackId());
            if (callback != null) {
                callback.accept(MessageHelper.getPayload(msg));
            } else {
                logger.error("callback for Id:{} not found", msg.getCallbackId());
            }
        } catch (Exception ex) {
            logger.error("msg:{}", msg, ex);
        }
        return Optional.empty();
    }
}