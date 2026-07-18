package apartmentmanagement.filter;

import apartmentmanagement.model.User;
import apartmentmanagement.util.AppConstants;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Bộ lọc xác thực/phân quyền cho toàn bộ request.
 * <p>
 * Kiểm tra đăng nhập, chặn truy cập theo vai trò (RBAC theo prefix URL),
 * và gắn header chống cache cho các trang nhạy cảm để tránh back-button
 * hiển thị nội dung đã hết phiên.
 * </p>
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    /** Đường dẫn public khớp chính xác (không cần đăng nhập). */
    private static final Set<String> PUBLIC_EXACT = new HashSet<>(Arrays.asList(
            "/", "/index.html", "/index.jsp"
    ));

    /** Prefix public: auth và tài nguyên tĩnh. */
    private static final Set<String> PUBLIC_PREFIX = new HashSet<>(Arrays.asList(
            "/auth",
            "/assets/",
            "/css/",
            "/js/",
            "/images/"
    ));

    /**
     * Ánh xạ prefix URL → tập role được phép.
     * Path không khớp rule nào được coi là cho phép (sau khi đã đăng nhập).
     */
    private static final Map<String, Set<String>> ROLE_RULES = new HashMap<>();

    static {
        ROLE_RULES.put("/admin", set(AppConstants.ROLE_ADMIN));
        ROLE_RULES.put("/building", set(
                AppConstants.ROLE_ADMIN,
                AppConstants.ROLE_MANAGER,
                AppConstants.ROLE_STAFF
        ));
        ROLE_RULES.put("/apartment", set(
                AppConstants.ROLE_ADMIN,
                AppConstants.ROLE_MANAGER,
                AppConstants.ROLE_STAFF,
                AppConstants.ROLE_RESIDENT
        ));
        ROLE_RULES.put("/fee", set(
                AppConstants.ROLE_ADMIN,
                AppConstants.ROLE_MANAGER,
                AppConstants.ROLE_STAFF,
                AppConstants.ROLE_RESIDENT
        ));
        ROLE_RULES.put("/request", set(
                AppConstants.ROLE_ADMIN,
                AppConstants.ROLE_MANAGER,
                AppConstants.ROLE_STAFF,
                AppConstants.ROLE_RESIDENT
        ));
        ROLE_RULES.put("/dashboard", set(
                AppConstants.ROLE_ADMIN,
                AppConstants.ROLE_MANAGER,
                AppConstants.ROLE_STAFF,
                AppConstants.ROLE_RESIDENT
        ));
        ROLE_RULES.put("/profile", set(
                AppConstants.ROLE_ADMIN,
                AppConstants.ROLE_MANAGER,
                AppConstants.ROLE_STAFF,
                AppConstants.ROLE_RESIDENT
        ));

    }

    private static Set<String> set(String... roles) {
        return new HashSet<>(Arrays.asList(roles));
    }

    /**
     * Xử lý mỗi request: public pass-through, bắt buộc login, kiểm tra role,
     * và chặn user đã login quay lại trang login.
     *
     * @param req   servlet request
     * @param res   servlet response
     * @param chain chuỗi filter tiếp theo
     * @throws IOException      lỗi I/O khi forward/redirect
     * @throws ServletException lỗi servlet khi forward
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        // Bỏ context path để so khớp rule theo path ứng dụng
        String path = uri.substring(contextPath.length());
        if (path.isEmpty()) {
            path = "/";
        }

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(AppConstants.SESSION_USER);
        boolean loginRequest = isLoginRequest(request, path);
        boolean staticAsset = isStaticAsset(path);

        // User đã đăng nhập không được vào lại form login
        if (loginRequest) {
            applyNoStoreHeaders(response);
            if (user != null) {
                response.sendRedirect(contextPath + "/dashboard");
                return;
            }
        }

        // Logout cũng không cache để tránh restore session cũ từ bfcache
        if (isLogoutRequest(request, path)) {
            applyNoStoreHeaders(response);
        }

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Trang bảo vệ: no-store để back/forward không hiện nội dung hết phiên
        if (!staticAsset) {
            applyNoStoreHeaders(response);
        }

        if (user == null) {
            response.sendRedirect(contextPath + "/auth?action=login");
            return;
        }

        if (!isRoleAllowed(path, user.getRole())) {
            request.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
            return;
        }

        chain.doFilter(request, response);
    }

    /** {@code /auth?action=login} — form/đăng nhập. */
    private boolean isLoginRequest(HttpServletRequest request, String path) {
        if (!"/auth".equals(path)) {
            return false;
        }
        return "login".equals(request.getParameter("action"));
    }

    /** {@code /auth?action=logout} — đăng xuất. */
    private boolean isLogoutRequest(HttpServletRequest request, String path) {
        if (!"/auth".equals(path)) {
            return false;
        }
        return "logout".equals(request.getParameter("action"));
    }

    /**
     * Gắn header chống cache (no-store) cho response nhạy cảm.
     *
     * @param response HTTP response cần gắn header
     */
    private void applyNoStoreHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    /**
     * Nhận diện tài nguyên tĩnh theo extension hoặc prefix thư mục.
     *
     * @param path path trong context (bắt đầu bằng {@code /})
     * @return {@code true} nếu là static asset
     */
    private boolean isStaticAsset(String path) {
        return path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".gif")
                || path.endsWith(".ico")
                || path.endsWith(".woff")
                || path.endsWith(".woff2")
                || path.startsWith("/assets/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/");
    }

    /**
     * Path public: exact match, static asset, hoặc thuộc prefix public.
     *
     * @param path path trong context
     * @return {@code true} nếu không bắt buộc đăng nhập
     */
    private boolean isPublic(String path) {
        if (PUBLIC_EXACT.contains(path) || isStaticAsset(path)) {
            return true;
        }
        for (String prefix : PUBLIC_PREFIX) {
            if (path.equals(prefix) || path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra role có được phép với path theo {@link #ROLE_RULES}.
     * Path không khớp prefix nào → cho phép (đã qua bước đăng nhập).
     *
     * @param path path trong context
     * @param role role của user hiện tại
     * @return {@code true} nếu được phép truy cập
     */
    private boolean isRoleAllowed(String path, String role) {
        for (Map.Entry<String, Set<String>> entry : ROLE_RULES.entrySet()) {
            String prefix = entry.getKey();
            // Khớp exact, sub-path, hoặc query gắn liền path (phòng trường hợp path còn ?)
            if (path.equals(prefix) || path.startsWith(prefix + "/") || path.startsWith(prefix + "?")) {
                return entry.getValue().contains(role);
            }
        }
        // Không có rule RBAC cho path này → chỉ cần đã login
        return true;
    }
}
