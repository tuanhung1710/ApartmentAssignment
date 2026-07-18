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
 * Bo loc xac thuc/phan quyen cho toan bo request.
 * <p>
 * Kiem tra dang nhap, chan truy cap theo vai tro (RBAC theo prefix URL),
 * va gan header chong cache cho cac trang nhay cam de tranh back-button
 * hien thi noi dung da het phien.
 * </p>
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    /** Duong dan public khop chinh xac (khong can dang nhap). */
    private static final Set<String> PUBLIC_EXACT = new HashSet<>(Arrays.asList(
            "/", "/index.html", "/index.jsp"
    ));

    /** Prefix public: auth va tai nguyen tinh. */
    private static final Set<String> PUBLIC_PREFIX = new HashSet<>(Arrays.asList(
            "/auth",
            "/assets/",
            "/css/",
            "/js/",
            "/images/"
    ));

    /**
     * Anh xa prefix URL -> tap role duoc phep.
     * Path khong khop rule nao duoc coi la cho phep (sau khi da dang nhap).
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
     * Xu ly moi request: public pass-through, bat buoc login, kiem tra role,
     * va chan user da login quay lai trang login.
     *
     * @param req   servlet request
     * @param res   servlet response
     * @param chain chuoi filter tiep theo
     * @throws IOException      loi I/O khi forward/redirect
     * @throws ServletException loi servlet khi forward
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        // Bo context path de so khop rule theo path ung dung
        String path = uri.substring(contextPath.length());
        if (path.isEmpty()) {
            path = "/";
        }

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(AppConstants.SESSION_USER);
        boolean loginRequest = isLoginRequest(request, path);
        boolean staticAsset = isStaticAsset(path);

        // User da dang nhap khong duoc vao lai form login
        if (loginRequest) {
            applyNoStoreHeaders(response);
            if (user != null) {
                response.sendRedirect(contextPath + "/dashboard");
                return;
            }
        }

        // Logout cung khong cache de tranh restore session cu tu bfcache
        if (isLogoutRequest(request, path)) {
            applyNoStoreHeaders(response);
        }

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Trang bao ve: no-store de back/forward khong hien noi dung het phien
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

    /** {@code /auth?action=login} — form/dang nhap. */
    private boolean isLoginRequest(HttpServletRequest request, String path) {
        if (!"/auth".equals(path)) {
            return false;
        }
        return "login".equals(request.getParameter("action"));
    }

    /** {@code /auth?action=logout} — dang xuat. */
    private boolean isLogoutRequest(HttpServletRequest request, String path) {
        if (!"/auth".equals(path)) {
            return false;
        }
        return "logout".equals(request.getParameter("action"));
    }

    /**
     * Gan header chong cache (no-store) cho response nhay cam.
     *
     * @param response HTTP response can gan header
     */
    private void applyNoStoreHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    /**
     * Nhan dien tai nguyen tinh theo extension hoac prefix thu muc.
     *
     * @param path path trong context (bat dau bang {@code /})
     * @return {@code true} neu la static asset
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
     * Path public: exact match, static asset, hoac thuoc prefix public.
     *
     * @param path path trong context
     * @return {@code true} neu khong bat buoc dang nhap
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
     * Kiem tra role co duoc phep voi path theo {@link #ROLE_RULES}.
     * Path khong khop prefix nao -> cho phep (da qua buoc dang nhap).
     *
     * @param path path trong context
     * @param role role cua user hien tai
     * @return {@code true} neu duoc phep truy cap
     */
    private boolean isRoleAllowed(String path, String role) {
        for (Map.Entry<String, Set<String>> entry : ROLE_RULES.entrySet()) {
            String prefix = entry.getKey();
            // Khop exact, sub-path, hoac query gan lien path (phong truong hop path con ?)
            if (path.equals(prefix) || path.startsWith(prefix + "/") || path.startsWith(prefix + "?")) {
                return entry.getValue().contains(role);
            }
        }
        // Khong co rule RBAC cho path nay -> chi can da login
        return true;
    }
}
