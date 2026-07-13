package apartmentmanagement.controller.auth;

import apartmentmanagement.dao.DashboardStatsDAO;
import apartmentmanagement.dao.PublicFeeLookupDAO;
import apartmentmanagement.dao.UserDAO;
import apartmentmanagement.model.MonthlyFee;
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
import java.util.List;

/**
 * Auth gộp: login, logout, profile, đổi mật khẩu + trang public (privacy/terms/fee-lookup).
 * Map URL bằng @WebServlet theo coding-standards (không khai báo web.xml).
 */
@WebServlet(name = "AuthenController", urlPatterns = {"/auth", "/profile"})
public class AuthenController extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final DashboardStatsDAO statsDAO = new DashboardStatsDAO();
    private final PublicFeeLookupDAO publicFeeLookupDAO = new PublicFeeLookupDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String action = request.getParameter("action");
        if (action == null) {
            // /profile mặc định xem hồ sơ; /auth mặc định form login
            action = "/profile".equals(servletPath) ? "profile" : "login";
        }

        switch (action) {
            case "login":
                handleLoginForm(request, response);
                break;
            case "logout":
                handleLogout(request, response);
                break;
            case "privacy":
                handlePrivacy(request, response);
                break;
            case "terms":
                handleTerms(request, response);
                break;
            case "fee-lookup":
                handleFeeLookup(request, response);
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

    // ==================== LOGIN / LOGOUT / PUBLIC LEGAL ====================

    private void handlePrivacy(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/auth/privacy.jsp").forward(request, response);
    }

    private void handleTerms(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/auth/terms.jsp").forward(request, response);
    }

    /**
     * Tra cứu phí dịch vụ công khai (không login).
     * Bắt buộc mã căn + SĐT khớp cư dân/thành viên hộ; chỉ trả phí đã công bố.
     */
    private void handleFeeLookup(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String apartmentCode = trim(request.getParameter("apartmentCode"));
        String phone = trim(request.getParameter("phone"));

        if (apartmentCode != null) {
            apartmentCode = apartmentCode.toUpperCase();
        }

        request.setAttribute("apartmentCode", apartmentCode);
        request.setAttribute("phone", phone);

        boolean hasCode = apartmentCode != null && !apartmentCode.isEmpty();
        boolean hasPhone = phone != null && !phone.isEmpty();

        // Lần đầu mở trang: chỉ form + bảng giá, chưa search
        if (!hasCode && !hasPhone) {
            request.setAttribute("searched", false);
            request.getRequestDispatcher("/WEB-INF/views/auth/fee-lookup.jsp").forward(request, response);
            return;
        }

        request.setAttribute("searched", true);

        if (!hasCode || !hasPhone) {
            request.setAttribute("error", "Vui lòng nhập đủ mã căn hộ và số điện thoại đăng ký.");
            request.getRequestDispatcher("/WEB-INF/views/auth/fee-lookup.jsp").forward(request, response);
            return;
        }

        if (!isValidPhone(phone)) {
            request.setAttribute("error", "Số điện thoại không hợp lệ (chỉ gồm 9–11 chữ số).");
            request.getRequestDispatcher("/WEB-INF/views/auth/fee-lookup.jsp").forward(request, response);
            return;
        }

        if (!publicFeeLookupDAO.verifyApartmentContact(apartmentCode, phone)) {
            // Thông báo chung — không tiết lộ căn có tồn tại hay không
            request.setAttribute("error",
                    "Không xác minh được thông tin. Kiểm tra lại mã căn hộ và SĐT đã đăng ký với Ban Quản Lý.");
            request.getRequestDispatcher("/WEB-INF/views/auth/fee-lookup.jsp").forward(request, response);
            return;
        }

        List<MonthlyFee> fees = publicFeeLookupDAO.findPublishedFees(apartmentCode, 12);
        request.setAttribute("fees", fees);
        if (fees == null || fees.isEmpty()) {
            request.setAttribute("info", "Đã xác minh căn hộ, nhưng chưa có hóa đơn phí đã công bố.");
        }

        request.getRequestDispatcher("/WEB-INF/views/auth/fee-lookup.jsp").forward(request, response);
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
            // Phân biệt: mất kết nối DB vs sai mật khẩu (cùng return null từ DAO)
            if (!userDAO.testConnection()) {
                request.setAttribute("error",
                        "Không kết nối được CSDL. Kiểm tra SQL Server đang chạy và user/password trong DBContext.");
            } else {
                request.setAttribute("error", "Sai tài khoản, mật khẩu hoặc tài khoản đã bị khóa.");
            }
            request.setAttribute("username", username);
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        // Không lưu password trong session
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

    // ==================== PROFILE (UC-AUTH-04, UC-AUTH-05) ====================

    private void handleProfileView(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User sessionUser = requireUser(request, response);
        if (sessionUser == null) {
            return;
        }
        // Lấy thông tin mới nhất từ DB
        User fresh = userDAO.findById(sessionUser.getUserId());
        if (fresh != null) {
            fresh.setPassword(null);
            request.setAttribute("profile", fresh);
        } else {
            request.setAttribute("profile", sessionUser);
        }
        // UC-COM-01: Resident xem căn hộ đang gán (nếu có)
        if (AppConstants.ROLE_RESIDENT.equals(sessionUser.getRole())) {
            String aptCode = statsDAO.findCurrentApartmentCodeByUserId(sessionUser.getUserId());
            request.setAttribute("myApartment", aptCode);
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
        if (fresh != null) {
            fresh.setPassword(null); // không đưa password ra request/JSP
            request.setAttribute("profile", fresh);
        } else {
            request.setAttribute("profile", sessionUser);
        }
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

        // Validate họ tên
        if (fullName == null || fullName.isEmpty()) {
            FlashUtil.error(request, "Họ tên không được để trống.");
            response.sendRedirect(request.getContextPath() + "/profile?action=edit-profile");
            return;
        }

        // Validate email cơ bản (nếu có nhập)
        if (email != null && !email.isEmpty() && !isValidEmail(email)) {
            FlashUtil.error(request, "Email không đúng định dạng.");
            response.sendRedirect(request.getContextPath() + "/profile?action=edit-profile");
            return;
        }

        // Validate SĐT: chỉ số, độ dài 9–11 (nếu có nhập)
        if (phone != null && !phone.isEmpty() && !isValidPhone(phone)) {
            FlashUtil.error(request, "Số điện thoại chỉ gồm chữ số (9–11 số).");
            response.sendRedirect(request.getContextPath() + "/profile?action=edit-profile");
            return;
        }

        // Chỉ cập nhật fullName, email, phone — không đụng role/username
        User update = User.builder()
                .userId(sessionUser.getUserId())
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .build();

        if (userDAO.updateProfile(update)) {
            // Cập nhật lại session để layout hiển thị tên/email mới
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

    // ==================== ĐỔI MẬT KHẨU (UC-AUTH-06) ====================

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

        // Kiểm tra mật khẩu cũ qua DAO
        if (!userDAO.checkPassword(sessionUser.getUserId(), oldPassword)) {
            FlashUtil.error(request, "Mật khẩu hiện tại không đúng.");
            response.sendRedirect(request.getContextPath() + "/profile?action=change-password");
            return;
        }

        if (userDAO.updatePassword(sessionUser.getUserId(), newPassword)) {
            FlashUtil.success(request, "Đổi mật khẩu thành công.");
            response.sendRedirect(request.getContextPath() + "/profile?action=change-password");
        } else {
            FlashUtil.error(request, "Đổi mật khẩu thất bại.");
            response.sendRedirect(request.getContextPath() + "/profile?action=change-password");
        }
    }

    // ==================== HELPER ====================

    /** Bắt buộc đã login; null nếu đã redirect về login. */
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

    /** Email cơ bản: có @ và dấu chấm sau @ */
    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < email.length() - 1;
    }

    /** SĐT: chỉ chữ số, dài 9–11 */
    private boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        if (phone.length() < 9 || phone.length() > 11) {
            return false;
        }
        for (int i = 0; i < phone.length(); i++) {
            if (!Character.isDigit(phone.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
