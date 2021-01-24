package ru.otus.server;

import com.google.gson.Gson;
import org.eclipse.jetty.rewrite.handler.RedirectPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.otus.helpers.FileSystemHelper;
import ru.otus.model.MyUser;
import ru.otus.services.MyUserService;
import ru.otus.services.TemplateProcessor;
import ru.otus.servlet.MyAuthorizationFilter;
import ru.otus.servlet.MyLoginServlet;
import ru.otus.servlet.MyUsersApiServlet;
import ru.otus.servlet.MyUsersServlet;
import ru.otus.servlet.OrdinaryUserServlet;
import ru.otus.servlet.RootServlet;
import ru.otus.utils.Contracts;

public class MyUsersWebServer implements UsersWebServer {

    private static final String COMMON_RESOURCES_DIR = "static";

    private final MyUserService myUserService;
    private final Gson gson;
    private final TemplateProcessor templateProcessor;
    private final Server server;

    public MyUsersWebServer(
            final int port,
            final MyUserService myUserService,
            final Gson gson,
            final TemplateProcessor templateProcessor) {
        this.myUserService = Contracts.ensureNonNullArgument(myUserService);
        this.gson = Contracts.ensureNonNullArgument(gson);
        this.templateProcessor = Contracts.ensureNonNullArgument(templateProcessor);
        server = new Server(port);
    }

    @Override
    public void start() throws Exception {
        if (server.getHandlers().length == 0) {
            initContext();
        }
        server.start();
    }

    @Override
    public void join() throws Exception {
        server.join();
    }

    @Override
    public void stop() throws Exception {
        server.stop();
    }

    private Server initContext() {
        final ResourceHandler resourceHandler = createResourceHandler();
        final ServletContextHandler servletContextHandler = createServletContextHandler();
        servletContextHandler.setWelcomeFiles(new String[]{"root"});
        final HandlerList handlers = new HandlerList();
        handlers.addHandler(resourceHandler);
        handlers.addHandler(applySecurity(servletContextHandler));
        final RewriteHandler rewriteHandler = new RewriteHandler();
        rewriteHandler.setHandler(handlers);
        final RedirectPatternRule rootRule = new RedirectPatternRule();
        rootRule.setPattern("");
        rootRule.setLocation("/root");
        rootRule.setTerminating(true);
        rewriteHandler.addRule(rootRule);
        rewriteHandler.setRewriteRequestURI(true);
        rewriteHandler.setRewritePathInfo(true);
        rewriteHandler.setHandler(handlers);
        server.setHandler(rewriteHandler);
        rewriteHandler.setServer(server);
        return server;
    }

    protected Handler applySecurity(final ServletContextHandler servletContextHandler) {
        final var adminFilter = new MyAuthorizationFilter(
                (session) -> {
                    final var user = (MyUser) Contracts.ensureNonNullArgument(session.getAttribute("user"));
                    return MyUser.Role.ADMIN.equals(user.getRole());
                },
                templateProcessor
        );
        servletContextHandler.addFilter(new FilterHolder(adminFilter), "/users", null);
        servletContextHandler.addFilter(new FilterHolder(adminFilter), "/api/user/*", null);
        final var ordinaryFilter = new MyAuthorizationFilter((session) -> true, templateProcessor);
        servletContextHandler.addFilter(new FilterHolder(ordinaryFilter), "/ordinary", null);
        return servletContextHandler;
    }

    private ResourceHandler createResourceHandler() {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setResourceBase(FileSystemHelper.localFileNameOrResourceNameToFullPath(COMMON_RESOURCES_DIR));
        return resourceHandler;
    }

    private ServletContextHandler createServletContextHandler() {
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.addServlet(
                new ServletHolder(new RootServlet()),
                "/root");
        servletContextHandler.addServlet(
                new ServletHolder(new MyLoginServlet(templateProcessor, myUserService)),
                "/login");
        servletContextHandler.addServlet(
                new ServletHolder(new MyUsersServlet(templateProcessor)),
                "/users");
        servletContextHandler.addServlet(
                new ServletHolder(new OrdinaryUserServlet(templateProcessor)),
                "/ordinary"
        );
        servletContextHandler.addServlet(
                new ServletHolder(new MyUsersApiServlet(myUserService, gson)),
                "/api/user/*"
        );
        return servletContextHandler;
    }
}
