package ru.otus.app.contollers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.otus.app.services.UserService;
import ru.otus.utils.Contracts;

@Controller
public class MainPage {

    private final UserService userService;

    public MainPage(final UserService userService) {
        this.userService = Contracts.ensureNonNullArgument(userService);
    }

    @GetMapping({"/"})
    public String usersPage(final Model model) {
        model.addAttribute("users", userService.findAll());
        return "users";
    }

    @GetMapping({"/websocket"})
    public String websocketAlternative() {
        return "users-websocket";
    }
}
