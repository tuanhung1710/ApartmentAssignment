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
 * Bảo vệ URL theo session + role.
 * Public: /auth (login), assets tĩnh.
 * Role map mở rộng khi module thêm URL.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_EXACT = new HashSet<>(Arrays.asList(
            "/", "/index.html", "/index.jsp"
    ));

    private static final Set<String> PUBLIC_PREFIX = new HashSet<>(Arrays.asList(
            "/auth",
            "/assets/",
            "/css/",
            "/js/",
            "/images/"
    ));

    /** path prefix -> roles được phép (rỗng = mọi user đã login) */
    private static final Map<String, Set<String>> ROLE_RULES = new HashMap<>();

    static {
        ROLE_RULES.put("/admin", set(AppConstants.ROLE_ADMIN));
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

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.substring(contextPath.length());
        if (path.isEmpty()) {
            path = "/";
        }

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(AppConstants.SESSION_USER);

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

    private boolean isPublic(String path) {
        if (PUBLIC_EXACT.contains(path)) {
            return true;
        }
        for (String prefix : PUBLIC_PREFIX) {
            if (path.equals(prefix) || path.startsWith(prefix)) {
                return true;
            }
        }
        // file tĩnh phổ biến
        return path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".gif")
                || path.endsWith(".ico")
                || path.endsWith(".woff")
                || path.endsWith(".woff2");
    }

    private boolean isRoleAllowed(String path, String role) {
        for (Map.Entry<String, Set<String>> entry : ROLE_RULES.entrySet()) {
            String prefix = entry.getKey();
            if (path.equals(prefix) || path.startsWith(prefix + "/") || path.startsWith(prefix + "?")) {
                return entry.getValue().contains(role);
            }
        }
        // path chưa khai báo rule: cho user đã login (MVP)
        return true;
    }
}
