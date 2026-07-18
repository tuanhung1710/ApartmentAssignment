package apartmentmanagement.controller.auth;

import apartmentmanagement.dao.DashboardStatsDAO;
import apartmentmanagement.dao.PublicAnnouncementDAO;
import apartmentmanagement.dao.UserDAO;
import apartmentmanagement.model.Announcement;
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
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Xác thực, hồ sơ cá nhân và quên mật khẩu (OTP demo).
 * <p>
 * URL: {@code /auth} (home, login, logout, forgot-password, privacy, terms)
 * và {@code /profile} (xem/sửa hồ sơ, đổi mật khẩu).
 */
@WebServlet(name = "AuthenController", urlPatterns = {"/auth", "/profile"})
public class AuthenController extends HttpServlet {

    /** OTP hết hạn sau 5 phút. */
    private static final int OTP_TTL_MS = 5 * 60 * 1000;
    /** Số lần nhập OTP sai tối đa trước khi khóa gửi lại. */
    private static final int OTP_MAX_ATTEMPTS = 5;
    
    /** Thời gian chờ trước khi được gửi OTP mới sau khi bị khóa. */
    private static final int OTP_RESEND_COOLDOWN_MS = 60 * 1000;
    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    private final UserDAO userDAO = new UserDAO();
    private final DashboardStatsDAO statsDAO = new DashboardStatsDAO();
    private final PublicAnnouncementDAO publicAnnouncementDAO = new PublicAnnouncementDAO();

    /**
     * Điều hướng GET theo {@code action}.
     * Mặc định: {@code /profile} → profile; {@code /auth} → home.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String action = request.getParameter("action");
        if (action == null) {
            // /profile mặc định xem hồ sơ; /auth mặc định trang chủ public
            action = "/profile".equals(servletPath) ? "profile" : "home";
        }

        switch (action) {
            case "home":
                handleHome(request, response);
                break;
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
            case "forgot-password":
                handleForgotPasswordForm(request, response);
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

    /**
     * Điều hướng POST: login, OTP quên mật khẩu, cập nhật hồ sơ, đổi mật khẩu.
     * Thiếu {@code action} → 400.
     */
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
            case "forgot-send-otp":
                handleForgotSendOtp(request, response);
                break;
            case "forgot-verify-otp":
                handleForgotVerifyOtp(request, response);
                break;
            case "forgot-reset":
                handleForgotReset(request, response);
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

    /** Trang chủ public; đã login → dashboard. */
    private void handleHome(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (getCurrentUser(request) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        List<Announcement> announcements = publicAnnouncementDAO.findPublished(8);
        request.setAttribute("announcements", announcements);
        request.getRequestDispatcher("/WEB-INF/views/auth/home.jsp").forward(request, response);
    }

    /** Trang chính sách bảo mật (public). */
    private void handlePrivacy(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/auth/privacy.jsp").forward(request, response);
    }

    /** Trang điều khoản sử dụng (public). */
    private void handleTerms(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/auth/terms.jsp").forward(request, response);
    }

    /** Form đăng nhập; chặn cache để back-button không lộ form sau login. */
    private void handleLoginForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Đã login → không cho xem form login (URL gõ lại / back sau khi server revalidate)
        if (getCurrentUser(request) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        preventLoginPageCache(response);
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    /**
     * Xử lý đăng nhập: validate, DAO.login, set session (xóa password khỏi object).
     * Phân biệt lỗi DB vs sai thông tin khi login fail.
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        if (getCurrentUser(request) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String username = trim(request.getParameter("username"));
        String password = request.getParameter("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            preventLoginPageCache(response);
            request.setAttribute("error", "Vui lòng nhập username và mật khẩu.");
            request.setAttribute("username", username);
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        User user = userDAO.login(username, password);
        if (user == null) {
            preventLoginPageCache(response);
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
        
        user.setPassword(null);
        HttpSession session = request.getSession(true);
        session.setAttribute(AppConstants.SESSION_USER, user);
        FlashUtil.success(request, "Đăng nhập thành công. Xin chào " + user.getFullName() + "!");
        preventLoginPageCache(response);
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    /** Đăng xuất: invalidate session + xóa cookie JSESSIONID. */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(AppConstants.SESSION_USER);
            session.invalidate();
        }
        jakarta.servlet.http.Cookie kill = new jakarta.servlet.http.Cookie("JSESSIONID", "");
        kill.setMaxAge(0);
        kill.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        kill.setHttpOnly(true);
        response.addCookie(kill);

        preventLoginPageCache(response);
        response.sendRedirect(request.getContextPath() + "/auth?action=home");
    }

    /** Xem hồ sơ; RESIDENT kèm mã căn hiện tại. */
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
        
        if (AppConstants.ROLE_RESIDENT.equals(sessionUser.getRole())) {
            String aptCode = statsDAO.findCurrentApartmentCodeByUserId(sessionUser.getUserId());
            request.setAttribute("myApartment", aptCode);
        }
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Hồ sơ cá nhân");
        request.setAttribute("contentPage", "/WEB-INF/views/auth/profile.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    /** Form cập nhật họ tên / SĐT. */
    private void handleEditProfileForm(HttpServletRequest request, HttpServletResponse response)
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
        request.setAttribute("pageTitle", "Cập nhật hồ sơ");
        request.setAttribute("contentPage", "/WEB-INF/views/auth/edit-profile.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    /**
     * Lưu hồ sơ (fullName bắt buộc, phone tùy chọn).
     * Thành công → refresh {@code SESSION_USER}.
     */
    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        User sessionUser = requireUser(request, response);
        if (sessionUser == null) {
            return;
        }

        String fullName = trim(request.getParameter("fullName"));
        String phone = trim(request.getParameter("phone"));
        
        if (fullName == null || fullName.isEmpty()) {
            FlashUtil.error(request, "Họ tên không được để trống.");
            response.sendRedirect(request.getContextPath() + "/profile?action=edit-profile");
            return;
        }

        if (phone != null && !phone.isEmpty() && !isValidPhone(phone)) {
            FlashUtil.error(request, "Số điện thoại chỉ gồm chữ số (9–11 số).");
            response.sendRedirect(request.getContextPath() + "/profile?action=edit-profile");
            return;
        }

        
        User update = User.builder()
                .userId(sessionUser.getUserId())
                .fullName(fullName)
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

    
    /**
     * Wizard quên mật khẩu (GET): step identify | otp | reset | done.
     * Guard theo payload OTP trong session (gửi / verified / hết hạn).
     */
    private void handleForgotPasswordForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (getCurrentUser(request) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        preventLoginPageCache(response);

        String step = trim(request.getParameter("step"));
        if (step == null || step.isEmpty() || "choose".equals(step) || "self".equals(step) || "report".equals(step)) {
            step = "identify";
        }

        HttpSession session = request.getSession(false);
        Map<String, Object> otpPayload = getForgotOtpPayload(session);

        if ("otp".equals(step)) {
            if (otpPayload == null || !Boolean.TRUE.equals(otpPayload.get("otpSent"))) {
                response.sendRedirect(request.getContextPath() + "/auth?action=forgot-password");
                return;
            }
            if (isOtpExpired(otpPayload)) {
                clearForgotOtp(session);
                request.setAttribute("error", "Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.");
                request.setAttribute("step", "identify");
                request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
                return;
            }
            fillOtpViewAttributes(request, otpPayload);
        } else if ("reset".equals(step)) {
            if (otpPayload == null || !Boolean.TRUE.equals(otpPayload.get("verified"))) {
                response.sendRedirect(request.getContextPath() + "/auth?action=forgot-password");
                return;
            }
            request.setAttribute("otpUsername", otpPayload.get("username"));
        } else if ("done".equals(step)) {
            // done chỉ hợp lệ sau POST (set attribute); GET thẳng → form xác thực
            if (request.getAttribute("doneTitle") == null) {
                step = "identify";
            }
        } else {
            step = "identify";
        }

        if ("identify".equals(step)) {
            applyOtpCooldownView(request, request.getSession(false));
        }

        request.setAttribute("step", step);
        request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
    }

    /**
     * Gửi OTP demo sau khi xác minh username + email/SĐT đăng ký.
     * Tôn trọng cooldown khóa gửi lại; OTP lưu session (không gửi SMS/email thật).
     */
    private void handleForgotSendOtp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (getCurrentUser(request) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String channel = trim(request.getParameter("channel"));
        String username = trim(request.getParameter("username"));
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email"));

        request.setAttribute("channel", channel);
        request.setAttribute("username", username);
        request.setAttribute("phone", phone);
        request.setAttribute("email", email);

        HttpSession existingSession = request.getSession(false);
        int cooldownLeft = getOtpCooldownRemainingSeconds(existingSession);
        if (cooldownLeft > 0) {
            request.setAttribute("error",
                    "Bạn đã nhập sai OTP quá số lần cho phép. Vui lòng chờ "
                            + cooldownLeft + " giây rồi gửi lại mã OTP mới.");
            request.setAttribute("step", "identify");
            applyOtpCooldownView(request, existingSession);
            preventLoginPageCache(response);
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        if (username == null || username.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập tên đăng nhập.");
            request.setAttribute("step", "identify");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }
        if (!"email".equals(channel) && !"phone".equals(channel)) {
            channel = "phone";
        }

        User user;
        String contactRaw;
        if ("email".equals(channel)) {
            if (email == null || email.isEmpty() || !isValidEmail(email)) {
                request.setAttribute("error", "Vui lòng nhập email đăng ký hợp lệ.");
                request.setAttribute("step", "identify");
                request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
                return;
            }
            user = userDAO.findActiveByUsernameAndEmail(username, email);
            contactRaw = email;
        } else {
            if (phone == null || phone.isEmpty() || !isValidPhone(phone)) {
                request.setAttribute("error", "Vui lòng nhập SĐT đăng ký hợp lệ (9–11 chữ số).");
                request.setAttribute("step", "identify");
                request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
                return;
            }
            user = userDAO.findActiveByUsernameAndPhone(username, phone);
            contactRaw = phone;
        }

        if (user == null) {
            request.setAttribute("error",
                    "Không xác minh được thông tin. Kiểm tra lại tên đăng nhập và "
                            + ("email".equals(channel) ? "email" : "số điện thoại")
                            + " đã đăng ký trên hệ thống.");
            request.setAttribute("step", "identify");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        String otp = generateOtp6();
        long now = System.currentTimeMillis();
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getUserId());
        payload.put("username", user.getUsername());
        payload.put("channel", channel);
        payload.put("contact", contactRaw);
        payload.put("otp", otp);
        payload.put("expiresAt", now + OTP_TTL_MS);
        payload.put("attempts", 0);
        payload.put("otpSent", true);
        payload.put("verified", false);

        HttpSession session = request.getSession(true);
        clearOtpCooldown(session);
        session.setAttribute(AppConstants.SESSION_FORGOT_OTP, payload);

        
        // Demo only: log OTP ra console (production sẽ gửi email/SMS)
        System.out.println("[ForgotPassword OTP demo] user=" + user.getUsername()
                + " channel=" + channel + " otp=" + otp);

        fillOtpViewAttributes(request, payload);
        request.setAttribute("info",
                "Đã tạo mã OTP demo. Trong môi trường production sẽ gửi qua "
                        + ("email".equals(channel) ? "email" : "SMS")
                        + "; hiện mã hiển thị ngay bên dưới.");
        request.setAttribute("step", "otp");
        preventLoginPageCache(response);
        request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
    }

    /**
     * Xác thực OTP: đếm attempts, vượt max → clear OTP + khóa resend cooldown.
     * Thành công → đánh dấu verified, xóa mã khỏi session.
     */
    private void handleForgotVerifyOtp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (getCurrentUser(request) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        HttpSession session = request.getSession(false);
        Map<String, Object> payload = getForgotOtpPayload(session);
        if (payload == null || !Boolean.TRUE.equals(payload.get("otpSent"))) {
            response.sendRedirect(request.getContextPath() + "/auth?action=forgot-password");
            return;
        }
        if (isOtpExpired(payload)) {
            clearForgotOtp(session);
            request.setAttribute("error", "Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.");
            request.setAttribute("step", "identify");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        String otpInput = trim(request.getParameter("otp"));
        String expected = String.valueOf(payload.get("otp"));
        int attempts = payload.get("attempts") instanceof Integer
                ? (Integer) payload.get("attempts") : 0;

        if (otpInput == null || !otpInput.equals(expected)) {
            attempts++;
            payload.put("attempts", attempts);
            session.setAttribute(AppConstants.SESSION_FORGOT_OTP, payload);

            if (attempts >= OTP_MAX_ATTEMPTS) {
                clearForgotOtp(session);
                lockOtpResend(session);
                int waitSec = OTP_RESEND_COOLDOWN_MS / 1000;
                request.setAttribute("error",
                        "Nhập sai OTP quá số lần cho phép. Vui lòng chờ " + waitSec
                                + " giây rồi gửi lại mã OTP mới.");
                request.setAttribute("step", "identify");
                applyOtpCooldownView(request, session);
                preventLoginPageCache(response);
                request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
                return;
            }

            fillOtpViewAttributes(request, payload);
            request.setAttribute("error",
                    "Mã OTP không đúng. Còn " + (OTP_MAX_ATTEMPTS - attempts) + " lần thử.");
            request.setAttribute("step", "otp");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        payload.put("verified", true);
        payload.put("otp", null); // không giữ OTP sau khi verify
        session.setAttribute(AppConstants.SESSION_FORGOT_OTP, payload);

        request.setAttribute("otpUsername", payload.get("username"));
        request.setAttribute("step", "reset");
        request.setAttribute("success", "Xác thực OTP thành công. Hãy đặt mật khẩu mới.");
        preventLoginPageCache(response);
        request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
    }

    /**
     * Đặt mật khẩu mới sau khi OTP verified; clear payload OTP khi thành công.
     */
    private void handleForgotReset(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (getCurrentUser(request) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        HttpSession session = request.getSession(false);
        Map<String, Object> payload = getForgotOtpPayload(session);
        if (payload == null || !Boolean.TRUE.equals(payload.get("verified"))) {
            response.sendRedirect(request.getContextPath() + "/auth?action=forgot-password");
            return;
        }

        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        request.setAttribute("otpUsername", payload.get("username"));

        if (newPassword == null || confirmPassword == null
                || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ mật khẩu mới.");
            request.setAttribute("step", "reset");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu mới và xác nhận không khớp.");
            request.setAttribute("step", "reset");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }
        if (newPassword.length() < 6) {
            request.setAttribute("error", "Mật khẩu mới tối thiểu 6 ký tự.");
            request.setAttribute("step", "reset");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        Integer userId = payload.get("userId") instanceof Integer
                ? (Integer) payload.get("userId") : null;
        if (userId == null || !userDAO.updatePassword(userId, newPassword)) {
            request.setAttribute("error", "Đổi mật khẩu thất bại. Vui lòng thử lại hoặc liên hệ hỗ trợ.");
            request.setAttribute("step", "reset");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        clearForgotOtp(session);
        request.setAttribute("step", "done");
        request.setAttribute("doneTitle", "Đổi mật khẩu thành công");
        request.setAttribute("doneMessage",
                "Bạn có thể đăng nhập bằng mật khẩu mới. Hãy giữ mật khẩu an toàn và không chia sẻ OTP với người khác.");
        preventLoginPageCache(response);
        request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
    }

    /** Đọc payload OTP quên mật khẩu từ session (kiểu Map). */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getForgotOtpPayload(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object raw = session.getAttribute(AppConstants.SESSION_FORGOT_OTP);
        if (raw instanceof Map) {
            return (Map<String, Object>) raw;
        }
        return null;
    }

    /** Xóa payload OTP khỏi session. */
    private void clearForgotOtp(HttpSession session) {
        if (session != null) {
            session.removeAttribute(AppConstants.SESSION_FORGOT_OTP);
        }
    }

    /** Khóa gửi OTP mới đến hết cooldown. */
    private void lockOtpResend(HttpSession session) {
        if (session == null) {
            return;
        }
        session.setAttribute(AppConstants.SESSION_FORGOT_OTP_LOCK,
                System.currentTimeMillis() + OTP_RESEND_COOLDOWN_MS);
    }

    /** Gỡ khóa cooldown gửi OTP. */
    private void clearOtpCooldown(HttpSession session) {
        if (session != null) {
            session.removeAttribute(AppConstants.SESSION_FORGOT_OTP_LOCK);
        }
    }

    /** Số giây còn lại phải chờ trước khi gửi OTP mới; 0 = được gửi. */
    private int getOtpCooldownRemainingSeconds(HttpSession session) {
        if (session == null) {
            return 0;
        }
        Object raw = session.getAttribute(AppConstants.SESSION_FORGOT_OTP_LOCK);
        if (!(raw instanceof Long)) {
            return 0;
        }
        long remainingMs = (Long) raw - System.currentTimeMillis();
        if (remainingMs <= 0) {
            clearOtpCooldown(session);
            return 0;
        }
        return (int) Math.ceil(remainingMs / 1000.0);
    }

    /** Gắn attribute UI khi đang trong cooldown OTP. */
    private void applyOtpCooldownView(HttpServletRequest request, HttpSession session) {
        int remaining = getOtpCooldownRemainingSeconds(session);
        if (remaining > 0) {
            request.setAttribute("otpCooldownSeconds", remaining);
            request.setAttribute("otpCooldownLocked", true);
        }
    }

    /** {@code true} nếu payload thiếu expiresAt hoặc đã quá hạn. */
    private boolean isOtpExpired(Map<String, Object> payload) {
        Object exp = payload.get("expiresAt");
        if (!(exp instanceof Long)) {
            return true;
        }
        return System.currentTimeMillis() > (Long) exp;
    }

    /** Attribute hiển thị bước nhập OTP (contact mask + demo OTP nếu còn). */
    private void fillOtpViewAttributes(HttpServletRequest request, Map<String, Object> payload) {
        request.setAttribute("otpUsername", payload.get("username"));
        request.setAttribute("otpChannel", payload.get("channel"));
        request.setAttribute("otpContactMasked", maskContact(
                String.valueOf(payload.get("channel")),
                String.valueOf(payload.get("contact"))));
        if (payload.get("otp") != null) {
            request.setAttribute("demoOtp", payload.get("otp"));
        }
    }

    /** Sinh OTP 6 chữ số (SecureRandom). */
    private String generateOtp6() {
        int n = OTP_RANDOM.nextInt(1_000_000);
        return String.format("%06d", n);
    }

    /** Che email/SĐT khi hiển thị (vd. a***@x.com, ***1234). */
    private String maskContact(String channel, String contact) {
        if (contact == null || contact.isEmpty() || "null".equals(contact)) {
            return "***";
        }
        if ("email".equals(channel)) {
            int at = contact.indexOf('@');
            if (at <= 1) {
                return "***" + contact.substring(Math.max(0, at));
            }
            return contact.charAt(0) + "***" + contact.substring(at);
        }
        // SĐT: giữ 4 số cuối
        if (contact.length() <= 4) {
            return "****";
        }
        return "***" + contact.substring(contact.length() - 4);
    }

    /** Form đổi mật khẩu (user đã login). */
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

    /**
     * Đổi mật khẩu: xác minh mật khẩu cũ, min 6 ký tự, khớp confirm.
     */
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


    /** User từ session, hoặc {@code null} nếu chưa login / sai kiểu. */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object attr = session.getAttribute(AppConstants.SESSION_USER);
        return attr instanceof User ? (User) attr : null;
    }

    
    /**
     * Bắt buộc đăng nhập; nếu thiếu thì redirect login và trả {@code null}.
     */
    private User requireUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
        }
        return user;
    }

    
    /**
     * Chặn cache trang auth/login để trình duyệt không phục hồi form sau logout/login.
     */
    private void preventLoginPageCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    
    /** Kiểm tra email tối thiểu: có {@code @} và domain có dấu chấm. */
    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < email.length() - 1;
    }

    
    /** SĐT 9–11 chữ số thuần. */
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
