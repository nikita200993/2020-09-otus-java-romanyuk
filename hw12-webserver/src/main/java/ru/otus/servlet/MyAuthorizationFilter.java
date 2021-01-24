package ru.otus.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ru.otus.services.TemplateProcessor;
import ru.otus.utils.Contracts;

import java.io.IOException;
import java.util.Map;
import java.util.function.Predicate;

public class MyAuthorizationFilter extends HttpFilter {

    private final Predicate<HttpSession> authorizer;
    private final TemplateProcessor templateProcessor;

    public MyAuthorizationFilter(
            final Predicate<HttpSession> authorizer,
            final TemplateProcessor templateProcessor) {
        this.authorizer = Contracts.ensureNonNullArgument(authorizer);
        this.templateProcessor = Contracts.ensureNonNullArgument(templateProcessor);
    }

    @Override
    protected void doFilter(
            final HttpServletRequest req,
            final HttpServletResponse res,
            final FilterChain chain) throws IOException, ServletException {
        final HttpSession session = req.getSession(false);
        if (session == null) {
            res.sendRedirect("/login");
        } else if (!authorizer.test(session)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("text/html");
            res.getWriter()
                    .println(
                            templateProcessor.getPage(
                                    "error.html",
                                    Map.of(
                                            "status", HttpServletResponse.SC_UNAUTHORIZED,
                                            "message", "Do not have privileges for accessing url: " + req.getRequestURL())
                            )
                    );
        } else {
            chain.doFilter(req, res);
        }
    }
}
