package ru.otus.servlet;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.model.MyUser;
import ru.otus.services.MyUserService;
import ru.otus.utils.Contracts;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MyUsersApiServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyUsersApiServlet.class);
    private static final String JSON_MIME_TYPE = "application/json;charset=UTF-8";

    private final MyUserService userService;
    private final Gson gson;

    public MyUsersApiServlet(final MyUserService userService, final Gson gson) {
        this.userService = Contracts.ensureNonNullArgument(userService);
        this.gson = Contracts.ensureNonNullArgument(gson);
    }

    @Override
    protected void doGet(
            final HttpServletRequest req,
            final HttpServletResponse resp) throws IOException {
        if (req.getParameterMap().isEmpty()) {
            processGetAll(resp);
        } else {
            processUserCreation(req, resp);
        }
    }

    private void processGetAll(final HttpServletResponse resp) throws IOException {
        final List<MyUser> users = userService.findAll();
        resp.setContentType(JSON_MIME_TYPE);
        resp.getWriter()
                .println(gson.toJson(users));
    }

    private void processUserCreation(
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        final String login = request.getParameter("login");
        final String password = request.getParameter("password");
        final String role = request.getParameter("role");
        response.setContentType(JSON_MIME_TYPE);
        if (areValidParams(login, password, role)) {
            try {
                userService.insert(
                        new MyUser(login, password, MyUser.Role.forRoleName(role))
                );
                response.getWriter()
                        .println(gson.toJson(Map.of("status", "created")));
            } catch (final Exception exception) {
                LOGGER.error("Unable to insert user", exception);
                response.getWriter()
                        .println(gson.toJson(Map.of("status", "service unavailable")));
            }
        } else {
            response.getWriter()
                    .println(gson.toJson(Map.of("status", "bad request")));
        }
    }

    private static boolean areValidParams(
            final String login,
            final String password,
            final String role) {
        return login != null && !login.isBlank()
                && password != null && !password.isBlank()
                && MyUser.Role.isValidRole(role);
    }
}
