package ru.otus.contollers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.otus.model.User;
import ru.otus.services.UserService;
import ru.otus.utils.Contracts;

import java.util.Collections;
import java.util.Map;

@Controller
public class UserController {

    private final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = Contracts.ensureNonNullArgument(userService);
    }

    @GetMapping({"/"})
    public String usersPage(final Model model) {
        model.addAttribute("users", userService.findAll());
        return "users";
    }

    @GetMapping({"/api/user"})
    @ResponseBody
    public Map<String, Object> getUsers() {
        try {
            return Map.of(
                    "status", "success",
                    "users", userService.findAll()
            );
        } catch (final Exception exception) {
            LOGGER.error("Error fetching all users", exception);
            return Map.of(
                    "status", "error",
                    "users", Collections.emptyList()
            );
        }
    }

    @PostMapping({"/api/user"})
    @ResponseBody
    public Map<String, String> createUser(@RequestBody final UserDto user) {
        Contracts.requireNonNullArgument(user);

        try {
            userService.insert(user.toUser());
            return Map.of("status", "success");
        } catch (final Exception exception) {
            LOGGER.error("Error saving {}", user, exception);
            return Map.of("status", "error");
        }
    }

    private static class UserDto {
        private String login;
        private String password;
        private String role;

        User toUser() {
            return new User(login, password, User.Role.forRoleName(role));
        }

        @Override
        public String toString() {
            return "UserDto{" +
                    "login='" + login + '\'' +
                    ", password='" + password + '\'' +
                    ", role='" + role + '\'' +
                    '}';
        }
    }
}
