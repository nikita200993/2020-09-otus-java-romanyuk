package ru.otus.app.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.app.dto.UserDto;
import ru.otus.app.services.UserService;
import ru.otus.utils.Contracts;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    public static ResponseEntity<Map<String, String>> CREATION_SUCCESS_RESPONSE = new ResponseEntity<>(
            Map.of("status", "ok"), HttpStatus.OK);
    public static ResponseEntity<Map<String, String>> CREATION_FAILURE_RESPONSE = new ResponseEntity<>(
            Map.of("status", "error"), HttpStatus.INTERNAL_SERVER_ERROR);
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);


    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = Contracts.ensureNonNullArgument(userService);
    }

    @GetMapping({"/api/user"})
    public ResponseEntity<List<UserDto>> getUsers() {
        try {
            return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
        } catch (final Exception exception) {
            LOGGER.error("Error fetching all users", exception);
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping({"/api/user"})
    public ResponseEntity<Map<String, String>> createUser(@RequestBody final UserDto user) {
        Contracts.requireNonNullArgument(user);
        try {
            userService.insert(user);
            return CREATION_SUCCESS_RESPONSE;
        } catch (final Exception exception) {
            LOGGER.error("Error saving {}", user, exception);
            return CREATION_FAILURE_RESPONSE;
        }
    }
}
