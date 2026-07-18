package apartmentmanagement.controller.auth;

import apartmentmanagement.dao.RequestDAO;
import apartmentmanagement.dao.UserDAO;
import apartmentmanagement.model.User;
import apartmentmanagement.util.AppConstants;
import apartmentmanagement.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "DashboardController", urlPatterns = {"/dashboard"})
public class DashboardController extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final RequestDAO requestDAO = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(AppConstants.SESSION_USER);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        if (AppConstants.ROLE_ADMIN.equals(user.getRole())) {
            request.setAttribute("totalUsers", userDAO.countAll());
            request.setAttribute("lockedUsers", userDAO.countByActive(false));
        }

        if (AppConstants.ROLE_RESIDENT.equals(user.getRole())) {
            request.setAttribute("openRequests", requestDAO.countOpenByCreatedBy(user.getUserId()));
        }

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Dashboard");
        request.setAttribute("contentPage", "/WEB-INF/views/auth/dashboard.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }
}
