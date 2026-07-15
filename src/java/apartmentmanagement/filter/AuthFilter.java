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

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(AppConstants.SESSION_USER);
        boolean loginRequest = isLoginRequest(request, path);
        boolean staticAsset = isStaticAsset(path);

        
        if (loginRequest) {
            applyNoStoreHeaders(response);
            if (user != null) {
                response.sendRedirect(contextPath + "/dashboard");
                return;
            }
        }

        
        if (isLogoutRequest(request, path)) {
            applyNoStoreHeaders(response);
        }

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        
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

    
    private boolean isLoginRequest(HttpServletRequest request, String path) {
        if (!"/auth".equals(path)) {
            return false;
        }
        return "login".equals(request.getParameter("action"));
    }

    private boolean isLogoutRequest(HttpServletRequest request, String path) {
        if (!"/auth".equals(path)) {
            return false;
        }
        return "logout".equals(request.getParameter("action"));
    }

    
    private void applyNoStoreHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

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

    private boolean isRoleAllowed(String path, String role) {
        for (Map.Entry<String, Set<String>> entry : ROLE_RULES.entrySet()) {
            String prefix = entry.getKey();
            if (path.equals(prefix) || path.startsWith(prefix + "/") || path.startsWith(prefix + "?")) {
                return entry.getValue().contains(role);
            }
        }
        
        return true;
    }
}
