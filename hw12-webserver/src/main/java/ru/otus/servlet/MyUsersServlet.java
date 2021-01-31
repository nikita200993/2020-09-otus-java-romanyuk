package ru.otus.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ru.otus.model.MyUser;
import ru.otus.services.TemplateProcessor;
import ru.otus.utils.Contracts;

import java.io.IOException;
import java.util.Collections;

public class MyUsersServlet extends HttpServlet {

    private final TemplateProcessor templateProcessor;

    public MyUsersServlet(final TemplateProcessor templateProcessor) {
        this.templateProcessor = Contracts.ensureNonNull(templateProcessor);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final HttpSession session = Contracts.ensureNonNull(req.getSession(false));
        final MyUser user = (MyUser) Contracts.ensureNonNull(session.getAttribute("user"));
        Contracts.requireThat(MyUser.Role.ADMIN.equals(user.getRole()));
        resp.setContentType("text/html");
        resp.getWriter()
                .println(
                        templateProcessor.getPage(
                                "users.html",
                                Collections.emptyMap()
                        )
                );
    }
}
