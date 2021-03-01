package ru.otus.app.contollers;

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

@RestController
public class UserController {

    private final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

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
    public ResponseEntity<String> createUser(@RequestBody final UserDto user) {
        Contracts.requireNonNullArgument(user);

        try {
            userService.insert(user);
            return new ResponseEntity<>("ok", HttpStatus.OK);
        } catch (final Exception exception) {
            LOGGER.error("Error saving {}", user, exception);
            return new ResponseEntity<>("error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
