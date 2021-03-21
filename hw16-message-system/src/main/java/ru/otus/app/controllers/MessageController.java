package ru.otus.app.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import ru.otus.app.messagesystem.FrontendService;
import ru.otus.app.messagesystem.UserRequestAndResponse;

import java.util.Collections;

@Controller
public class MessageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final FrontendService frontendService;

    public MessageController(
            final SimpMessagingTemplate simpMessagingTemplate,
            final FrontendService frontendService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.frontendService = frontendService;
    }

    @MessageMapping("/create")
    public void createUser(final UserDto userDto, @Header("simpSessionId") String sessionId) {
        frontendService.serveRequest(
                UserRequestAndResponse.newCreationRequest(userDto),
                response -> simpMessagingTemplate.convertAndSendToUser(
                        sessionId,
                        "/queue/reply/create",
                        response.isCompletedSuccessfully() ? CreationResponse.OK : CreationResponse.FAIL,
                        createHeaders(sessionId)
                )
        );
    }

    @MessageMapping("/users")
    public void users(@Header("simpSessionId") String sessionId) {
        frontendService.serveRequest(
                UserRequestAndResponse.newGetALlUsersRequest(),
                response -> simpMessagingTemplate.convertAndSendToUser(
                        sessionId,
                        "/queue/reply/users",
                        response.isCompletedSuccessfully() ? response.getUsers() : Collections.emptyList(),
                        createHeaders(sessionId)
                )
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
