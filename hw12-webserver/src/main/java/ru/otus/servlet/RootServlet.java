package ru.otus.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ru.otus.model.MyUser;

import java.io.IOException;

public class RootServlet extends HttpServlet {

    @Override
    protected void doGet(
            final HttpServletRequest req,
            final HttpServletResponse resp) throws IOException {
        final HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect("/login");
            return;
        }
        final MyUser user = (MyUser) session.getAttribute("user");
        if (MyUser.Role.ADMIN.equals(user.getRole())) {
            resp.sendRedirect("/users");
        } else {
            resp.sendRedirect("/ordinary");
        }
    }
}
