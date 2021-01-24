package ru.otus.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ru.otus.model.MyUser;
import ru.otus.services.MyUserService;
import ru.otus.services.TemplateProcessor;
import ru.otus.utils.Contracts;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class MyLoginServlet extends HttpServlet {

    private static final String LOGIN_PAGE_FILE = "login.html";

    private final TemplateProcessor templateProcessor;
    private final MyUserService myUserService;

    public MyLoginServlet(final TemplateProcessor templateProcessor, final MyUserService myUserService) {
        this.templateProcessor = Contracts.ensureNonNullArgument(templateProcessor);
        this.myUserService = Contracts.ensureNonNullArgument(myUserService);
    }

    @Override
    protected void doGet(
            final HttpServletRequest req,
            final HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.getWriter()
                .println(templateProcessor.getPage(LOGIN_PAGE_FILE, Collections.emptyMap()));
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // should be validated
        final String login = req.getParameter("login");
        final String password = req.getParameter("password");
        final Optional<MyUser> userOptional = myUserService.findByLogin(login);
        final Optional<String> passwordFromDatabase = userOptional
                .map(MyUser::getPassword);
        if (passwordFromDatabase.isEmpty() || !passwordFromDatabase.get().equals(password)) {
            resp.setContentType("text/html");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter()
                    .println(
                            templateProcessor.getPage(LOGIN_PAGE_FILE, Map.of("badCreds", true))
                    );
            return;
        }
        final MyUser user = userOptional.get();
        final HttpSession oldSession = req.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        final HttpSession newSession = req.getSession();
        newSession.setAttribute("user", user);
        if (MyUser.Role.ADMIN.equals(user.getRole())) {
            resp.sendRedirect("/users");
        } else {
            resp.sendRedirect("/ordinary");
        }
    }
}
