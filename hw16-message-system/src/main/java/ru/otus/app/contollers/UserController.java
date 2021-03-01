package ru.otus.app.contollers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.app.dto.UserDto;
import ru.otus.app.services.UserService;
import ru.otus.utils.Contracts;

import java.util.Collections;

@RestController
public class UserController {

    private final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = Contracts.ensureNonNullArgument(userService);
    }

    @GetMapping({"/api/user"})
    public Response getUsers() {
        try {
            return new Response("ok", userService.findAll());
        } catch (final Exception exception) {
            LOGGER.error("Error fetching all users", exception);
            return new Response("error", Collections.emptyList());
        }
    }

    @PostMapping({"/api/user"})
    public Response createUser(@RequestBody final UserDto user) {
        Contracts.requireNonNullArgument(user);

        try {
            userService.insert(user);
            return new Response("ok", null);
        } catch (final Exception exception) {
            LOGGER.error("Error saving {}", user, exception);
            return new Response("error", null);
        }
    }
}
