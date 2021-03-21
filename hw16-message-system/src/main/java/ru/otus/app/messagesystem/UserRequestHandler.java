package ru.otus.app.messagesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.app.services.UserService;
import ru.otus.messagesystem.RequestHandler;
import ru.otus.messagesystem.message.Message;
import ru.otus.messagesystem.message.MessageBuilder;
import ru.otus.messagesystem.message.MessageHelper;
import ru.otus.utils.Contracts;

import java.util.Optional;


public class UserRequestHandler implements RequestHandler<UserRequestAndResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRequestHandler.class);

    private final UserService userService;

    public UserRequestHandler(final UserService userService) {
        Contracts.requireNonNullArgument(userService);

        this.userService = userService;
    }

    @Override
    public Optional<Message> handle(final Message msg) {
        UserRequestAndResponse request = MessageHelper.getPayload(msg);
        try {
            if (request.getType() == RequestType.CREATE) {
                final var user = request.getUserToSave();
                userService.insert(user);
                return Optional.of(MessageBuilder.buildReplyMessage(msg, request.withStatus(true)));
            } else {
                final var users = userService.findAll();
                return Optional.of(MessageBuilder.buildReplyMessage(msg, request.withFetchedUsers(users)));
            }
        } catch (final Exception ex) {
            LOGGER.error("Error while working with user service.", ex);
        }
        return Optional.of(MessageBuilder.buildReplyMessage(msg, request.withStatus(false)));
    }
}