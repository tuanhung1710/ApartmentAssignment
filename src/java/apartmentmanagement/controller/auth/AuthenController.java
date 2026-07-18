package apartmentmanagement.controller.auth;

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

@WebServlet(name = "AuthenController", urlPatterns = {"/auth", "/profile"})
public class AuthenController extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String action = request.getParameter("action");
        if (action == null) {
            action = "/profile".equals(servletPath) ? "profile" : "login";
        }

        switch (action) {
            case "login":
                handleLoginForm(request, response);
                break;
            case "logout":
                handleLogout(request, response);
                break;
            case "profile":
                handleProfileView(request, response);
                break;
            case "edit-profile":
                handleEditProfileForm(request, response);
                break;
            case "change-password":
                handleChangePasswordForm(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        switch (action) {
            case "login":
                handleLogin(request, response);
                break;
            case "update-profile":
                handleUpdateProfile(request, response);
                break;
            case "change-password":
                handleChangePassword(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void handleLoginForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AppConstants.SESSION_USER) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = trim(request.getParameter("username"));
        String password = request.getParameter("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập username và mật khẩu.");
            request.setAttribute("username", username);
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        User user = userDAO.login(username, password);
        if (user == null) {
            request.setAttribute("error", "Sai tài khoản, mật khẩu hoặc tài khoản đã bị khóa.");
            request.setAttribute("username", username);
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        user.setPassword(null);
        HttpSession session = request.getSession(true);
        session.setAttribute(AppConstants.SESSION_USER, user);
        FlashUtil.success(request, "Đăng nhập thành công. Xin chào " + user.getFullName() + "!");
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
    }

    private void handleProfileView(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User sessionUser = requireUser(request, response);
        if (sessionUser == null) {
            return;
        }
        User fresh = userDAO.findById(sessionUser.getUserId());
        if (fresh != null) {
            fresh.setPassword(null);
            request.setAttribute("profile", fresh);
        } else {
            request.setAttribute("profile", sessionUser);
        }
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Hồ sơ cá nhân");
        request.setAttribute("contentPage", "/WEB-INF/views/auth/profile.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleEditProfileForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User sessionUser = requireUser(request, response);
        if (sessionUser == null) {
            return;
        }
        User fresh = userDAO.findById(sessionUser.getUserId());
        request.setAttribute("profile", fresh != null ? fresh : sessionUser);
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Cập nhật hồ sơ");
        request.setAttribute("contentPage", "/WEB-INF/views/auth/edit-profile.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        User sessionUser = requireUser(request, response);
        if (sessionUser == null) {
            return;
        }

        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phone"));

        if (fullName == null || fullName.isEmpty()) {
            FlashUtil.error(request, "Họ tên không được để trống.");
            response.sendRedirect(request.getContextPath() + "/profile?action=edit-profile");
            return;
        }
        if (email != null && !email.isEmpty()) {
            if (email.length() > 100 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                FlashUtil.error(request, "Email không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/profile?action=edit-profile");
                return;
            }
        } else {
            email = null;
        }
        if (phone != null && !phone.isEmpty()) {
            if (!phone.matches("^0\\d{9}$")) {
                FlashUtil.error(request, "Số điện thoại phải đúng 10 chữ số và bắt đầu bằng 0.");
                response.sendRedirect(request.getContextPath() + "/profile?action=edit-profile");
                return;
            }
        } else {
            phone = null;
        }

        User update = User.builder()
                .userId(sessionUser.getUserId())
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .build();

        if (userDAO.updateProfile(update)) {
            User refreshed = userDAO.findById(sessionUser.getUserId());
            if (refreshed != null) {
                refreshed.setPassword(null);
                request.getSession().setAttribute(AppConstants.SESSION_USER, refreshed);
            }
            FlashUtil.success(request, "Cập nhật hồ sơ thành công.");
            response.sendRedirect(request.getContextPath() + "/profile");
        } else {
            FlashUtil.error(request, "Cập nhật hồ sơ thất bại.");
            response.sendRedirect(request.getContextPath() + "/profile?action=edit-profile");
        }
    }

    private void handleChangePasswordForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireUser(request, response) == null) {
            return;
        }
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Đổi mật khẩu");
        request.setAttribute("contentPage", "/WEB-INF/views/auth/change-password.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        User sessionUser = requireUser(request, response);
        if (sessionUser == null) {
            return;
        }

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (oldPassword == null || newPassword == null || confirmPassword == null
                || oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            FlashUtil.error(request, "Vui lòng nhập đầy đủ thông tin.");
            response.sendRedirect(request.getContextPath() + "/profile?action=change-password");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            FlashUtil.error(request, "Mật khẩu mới và xác nhận không khớp.");
            response.sendRedirect(request.getContextPath() + "/profile?action=change-password");
            return;
        }

        if (newPassword.length() < 6) {
            FlashUtil.error(request, "Mật khẩu mới tối thiểu 6 ký tự.");
            response.sendRedirect(request.getContextPath() + "/profile?action=change-password");
            return;
        }

        User dbUser = userDAO.findById(sessionUser.getUserId());
        if (dbUser == null || !oldPassword.equals(dbUser.getPassword())) {
            FlashUtil.error(request, "Mật khẩu hiện tại không đúng.");
            response.sendRedirect(request.getContextPath() + "/profile?action=change-password");
            return;
        }

        if (userDAO.changePassword(sessionUser.getUserId(), newPassword)) {
            FlashUtil.success(request, "Đổi mật khẩu thành công.");
            response.sendRedirect(request.getContextPath() + "/profile");
        } else {
            FlashUtil.error(request, "Đổi mật khẩu thất bại.");
            response.sendRedirect(request.getContextPath() + "/profile?action=change-password");
        }
    }

    private User requireUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(AppConstants.SESSION_USER);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
        }
        return user;
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
