package ru.otus.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.otus.model.MyUser;
import ru.otus.services.TemplateProcessor;
import ru.otus.utils.Contracts;

import java.io.IOException;
import java.util.Map;

public class OrdinaryUserServlet extends HttpServlet {

    private static final String PAGE_FILE = "ordinary_user.html";

    private final TemplateProcessor templateProcessor;

    public OrdinaryUserServlet(final TemplateProcessor templateProcessor) {
        this.templateProcessor = Contracts.ensureNonNullArgument(templateProcessor);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final MyUser user = (MyUser) req.getSession(false)
                .getAttribute("user");
        resp.setContentType("text/html");
        resp.getWriter()
                .println(
                        templateProcessor.getPage(
                                PAGE_FILE,
                                Map.of("login", user.getLogin())
                        )
                );
    }
}
