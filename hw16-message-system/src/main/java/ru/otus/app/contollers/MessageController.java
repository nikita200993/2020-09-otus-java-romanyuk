package ru.otus.app.contollers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import ru.otus.app.dto.UserDto;
import ru.otus.app.services.UserService;

import java.util.Collections;
import java.util.List;

@Controller
public class MessageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;

    public MessageController(
            final SimpMessagingTemplate simpMessagingTemplate,
            final UserService userService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userService = userService;
    }

    @MessageMapping("/create")
    public void createUser(final UserDto userDto, @Header("simpSessionId") String sessionId) {
        LOGGER.info("Session = {}, message = {}", sessionId, userDto);
        CreationResponse response;
        try {
            userService.insert(userDto);
            response = CreationResponse.OK;
        } catch (final Exception ex) {
            LOGGER.error("Failed to create user {}", userDto, ex);
            response = CreationResponse.FAIL;
        }
        simpMessagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/reply/create",
                response,
                createHeaders(sessionId)
        );
    }

    @MessageMapping("/users")
    public void users(@Header("simpSessionId") String sessionId) {
        LOGGER.info("Session = {}", sessionId);
        List<UserDto> result;
        try {
            result = userService.findAll();
        } catch (final Exception ex) {
            result = Collections.emptyList();
            LOGGER.error("Failed to retrieve users", ex);
        }
        simpMessagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/reply/users",
                result,
                createHeaders(sessionId)
        );
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        return headerAccessor.getMessageHeaders();
    }

    private static class CreationResponse {

        static final CreationResponse OK = new CreationResponse("ok");
        static final CreationResponse FAIL = new CreationResponse("fail");

        @JsonProperty
        private final String status;

        public CreationResponse(final String status) {
            this.status = status;
        }
    }
}
