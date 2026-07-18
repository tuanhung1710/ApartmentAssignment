package apartmentmanagement.controller.admin;

import apartmentmanagement.dao.AnnouncementDAO;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet(name = "AdminController", urlPatterns = {"/admin"})
public class AdminController extends HttpServlet {

    private static final Set<String> ALLOWED_ROLES = new HashSet<>(Arrays.asList(
            AppConstants.ROLE_ADMIN,
            AppConstants.ROLE_MANAGER,
            AppConstants.ROLE_STAFF,
            AppConstants.ROLE_RESIDENT
    ));

    private final UserDAO userDAO = new UserDAO();
    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "users";
        }

        switch (action) {
            case "users":
            case "list":
            case "list-user":
                handleListUsers(request, response);
                break;
            case "add":
            case "add-user":
                handleAddUserForm(request, response);
                break;
            case "edit":
            case "edit-user":
                handleEditUserForm(request, response);
                break;
            case "detail":
            case "view":
            case "user-detail":
                handleUserDetail(request, response);
                break;
            case "lock":
                handleLockUser(request, response);
                break;
            case "unlock":
                handleUnlockUser(request, response);
                break;
            case "reset-password":
            case "reset":
                handleResetPassword(request, response);
                break;
            case "announcements":
            case "list-announcement":
                handleListAnnouncements(request, response);
                break;
            case "add-announcement":
                handleAddAnnouncementForm(request, response);
                break;
            case "edit-announcement":
                handleEditAnnouncementForm(request, response);
                break;
            case "delete-announcement":
                handleDeleteAnnouncement(request, response);
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
        if (action == null || action.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        switch (action) {
            case "add":
            case "add-user":
                handleAddUser(request, response);
                break;
            case "edit":
            case "edit-user":
                handleEditUser(request, response);
                break;
            case "lock":
                handleLockUser(request, response);
                break;
            case "unlock":
                handleUnlockUser(request, response);
                break;
            case "reset-password":
            case "reset":
                handleResetPassword(request, response);
                break;
            case "add-announcement":
                handleAddAnnouncement(request, response);
                break;
            case "edit-announcement":
                handleEditAnnouncement(request, response);
                break;
            case "delete-announcement":
                handleDeleteAnnouncement(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void handleListUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireAdmin(request, response);
        if (user == null) {
            return;
        }

        String keyword = trimToNull(request.getParameter("keyword"));
        String role = trimToNull(request.getParameter("role"));
        Boolean isActive = parseActiveStatus(request.getParameter("status"));
        int page = parsePage(request.getParameter("page"));
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;

        List<User> users = userDAO.findWithFilters(keyword, role, isActive, page, pageSize);
        int total = userDAO.countWithFilters(keyword, role, isActive);
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / pageSize);
        if (page > totalPages) {
            page = totalPages;
            users = userDAO.findWithFilters(keyword, role, isActive, page, pageSize);
        }

        request.setAttribute("users", users);
        request.setAttribute("keyword", keyword);
        request.setAttribute("role", role);
        request.setAttribute("status", trimToNull(request.getParameter("status")));
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", total);
        request.setAttribute("pageSize", pageSize);

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Quản lý người dùng");
        request.setAttribute("contentPage", "/WEB-INF/views/admin/listUser.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    // UC-ADM-01: Add User
    // GET  /admin?action=add
    // POST /admin  action=add

    private void handleAddUserForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAdmin(request, response) == null) {
            return;
        }

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Thêm người dùng");
        request.setAttribute("contentPage", "/WEB-INF/views/admin/addUser.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleAddUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAdmin(request, response) == null) {
            return;
        }

        String username = trimToNull(request.getParameter("username"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String fullName = trimToNull(request.getParameter("fullName"));
        String email = trimToNull(request.getParameter("email"));
        String phone = trimToNull(request.getParameter("phone"));
        String role = trimToNull(request.getParameter("role"));
        String statusRaw = trimToNull(request.getParameter("status"));
        String department = trimToNull(request.getParameter("department"));

        request.setAttribute("formUsername", username);
        request.setAttribute("formFullName", fullName);
        request.setAttribute("formEmail", email);
        request.setAttribute("formPhone", phone);
        request.setAttribute("formRole", role);
        request.setAttribute("formStatus", statusRaw == null ? "active" : statusRaw);
        request.setAttribute("formDepartment", department);

        String error = validateAddUser(username, password, confirmPassword, fullName, email, phone, role);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("pageTitle", "Thêm người dùng");
            request.setAttribute("contentPage", "/WEB-INF/views/admin/addUser.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
            return;
        }

        if (userDAO.existsByUsername(username)) {
            request.setAttribute("error", "Username đã tồn tại. Vui lòng chọn username khác.");
            request.setAttribute("pageTitle", "Thêm người dùng");
            request.setAttribute("contentPage", "/WEB-INF/views/admin/addUser.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
            return;
        }

        boolean isActive = !"locked".equalsIgnoreCase(statusRaw)
                && !"inactive".equalsIgnoreCase(statusRaw)
                && !"0".equals(statusRaw);

        User newUser = User.builder()
                .username(username)
                .password(password)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .role(role)
                .department(department)
                .isActive(isActive)
                .build();

        int newId = userDAO.insert(newUser);
        if (newId >= 0) {
            FlashUtil.success(request, "Thêm user \"" + username + "\" thành công"
                    + (newId > 0 ? " (ID #" + newId + ")." : "."));
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
        } else {
            request.setAttribute("error", "Thêm user thất bại. Vui lòng thử lại.");
            request.setAttribute("pageTitle", "Thêm người dùng");
            request.setAttribute("contentPage", "/WEB-INF/views/admin/addUser.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
        }
    }

    private String validateAddUser(String username, String password, String confirmPassword,
                                   String fullName, String email, String phone, String role) {
        if (username == null) {
            return "Username không được để trống.";
        }
        if (username.length() < 3 || username.length() > 50) {
            return "Username từ 3 đến 50 ký tự.";
        }
        if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            return "Username chỉ gồm chữ, số, dấu chấm, gạch dưới, gạch ngang.";
        }
        if (password == null || password.isEmpty()) {
            return "Password không được để trống.";
        }
        if (password.length() < 6) {
            return "Password tối thiểu 6 ký tự.";
        }
        if (password.length() > 255) {
            return "Password quá dài.";
        }
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            return "Xác nhận password không khớp.";
        }
        String common = validateUserCommonFields(fullName, email, phone, role);
        if (common != null) {
            return common;
        }
        return null;
    }

    // View User Detail
    // GET /admin?action=detail&id={userId}

    private void handleUserDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAdmin(request, response) == null) {
            return;
        }

        Integer userId = parsePositiveInt(request.getParameter("id"));
        if (userId == null) {
            FlashUtil.error(request, "User ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        User detailUser = userDAO.findById(userId);
        if (detailUser == null) {
            FlashUtil.error(request, "Không tìm thấy user #" + userId + ".");
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        detailUser.setPassword(null);

        request.setAttribute("detailUser", detailUser);
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Chi tiết user #" + userId);
        request.setAttribute("contentPage", "/WEB-INF/views/admin/userDetail.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    // UC-ADM-02: Edit User
    // GET  /admin?action=edit&id={userId}

    private void handleEditUserForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAdmin(request, response) == null) {
            return;
        }

        Integer userId = parsePositiveInt(request.getParameter("id"));
        if (userId == null) {
            FlashUtil.error(request, "User ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        User editUser = userDAO.findById(userId);
        if (editUser == null) {
            FlashUtil.error(request, "Không tìm thấy user #" + userId + ".");
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        request.setAttribute("editUser", editUser);
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Sửa người dùng #" + userId);
        request.setAttribute("contentPage", "/WEB-INF/views/admin/editUser.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleEditUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAdmin(request, response) == null) {
            return;
        }

        Integer userId = parsePositiveInt(request.getParameter("id"));
        if (userId == null) {
            FlashUtil.error(request, "User ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        User existing = userDAO.findById(userId);
        if (existing == null) {
            FlashUtil.error(request, "Không tìm thấy user #" + userId + ".");
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        User admin = (User) request.getSession().getAttribute(AppConstants.SESSION_USER);
        String fullName = trimToNull(request.getParameter("fullName"));
        String role = trimToNull(request.getParameter("role"));
        String statusRaw = trimToNull(request.getParameter("status"));
        String department = trimToNull(request.getParameter("department"));

        String error = validateUserCommonFields(fullName, null, null, role);
        if (error != null) {
            existing.setFullName(fullName);
            existing.setRole(role);
            existing.setDepartment(department);
            boolean active = !"locked".equalsIgnoreCase(statusRaw)
                    && !"inactive".equalsIgnoreCase(statusRaw)
                    && !"0".equals(statusRaw);
            existing.setIsActive(active);
            request.setAttribute("editUser", existing);
            request.setAttribute("error", error);
            request.setAttribute("pageTitle", "Sửa người dùng #" + userId);
            request.setAttribute("contentPage", "/WEB-INF/views/admin/editUser.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
            return;
        }

        boolean isActive = !"locked".equalsIgnoreCase(statusRaw)
                && !"inactive".equalsIgnoreCase(statusRaw)
                && !"0".equals(statusRaw);

        if (!isActive && isProtectedFromLock(existing, admin)) {
            isActive = true;
            FlashUtil.error(request, "Không thể khóa tài khoản ADMIN hoặc tài khoản đang đăng nhập. Status đã giữ Active.");
        }

        User update = User.builder()
                .userId(userId)
                .fullName(fullName)
                .role(role)
                .department(department)
                .isActive(isActive)
                .build();

        if (userDAO.updateByAdmin(update)) {
            FlashUtil.success(request, "Cập nhật user \"" + existing.getUsername()
                    + "\" (#" + userId + ") thành công.");
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
        } else {
            existing.setFullName(fullName);
            existing.setRole(role);
            existing.setDepartment(department);
            existing.setIsActive(isActive);
            request.setAttribute("editUser", existing);
            request.setAttribute("error", "Cập nhật user thất bại. Vui lòng thử lại.");
            request.setAttribute("pageTitle", "Sửa người dùng #" + userId);
            request.setAttribute("contentPage", "/WEB-INF/views/admin/editUser.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
        }
    }

    // UC-ADM-03: Lock / Unlock User
    // GET/POST /admin?action=lock|unlock&id={userId}

    private void handleLockUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        toggleUserActive(request, response, false);
    }

    private void handleUnlockUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        toggleUserActive(request, response, true);
    }

    /**
     * @param active true = Unlock (Active), false = Lock
     */
    private void toggleUserActive(HttpServletRequest request, HttpServletResponse response, boolean active)
            throws IOException, ServletException {
        User admin = requireAdmin(request, response);
        if (admin == null) {
            return;
        }

        Integer userId = parsePositiveInt(request.getParameter("id"));
        if (userId == null) {
            FlashUtil.error(request, "User ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        User target = userDAO.findById(userId);
        if (target == null) {
            FlashUtil.error(request, "Không tìm thấy user #" + userId + ".");
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        if (!active && isProtectedFromLock(target, admin)) {
            FlashUtil.error(request, "Không thể khóa tài khoản ADMIN hoặc tài khoản đang đăng nhập.");
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        boolean currentlyActive = target.getIsActive() != null && target.getIsActive();
        if (currentlyActive == active) {
            FlashUtil.success(request, "User \"" + target.getUsername() + "\" đã ở trạng thái "
                    + (active ? "Active" : "Locked") + ".");
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        if (userDAO.updateStatus(userId, active)) {
            if (active) {
                FlashUtil.success(request, "Đã mở khóa (Unlock) user \"" + target.getUsername()
                        + "\" (#" + userId + ").");
            } else {
                FlashUtil.success(request, "Đã khóa (Lock) user \"" + target.getUsername()
                        + "\" (#" + userId + ").");
            }
        } else {
            FlashUtil.error(request, (active ? "Unlock" : "Lock") + " user thất bại.");
        }
        response.sendRedirect(request.getContextPath() + "/admin?action=users");
    }

    // UC-ADM-05: Reset Password
    // GET/POST /admin?action=reset-password&id={userId}

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (requireAdmin(request, response) == null) {
            return;
        }

        Integer userId = parsePositiveInt(request.getParameter("id"));
        if (userId == null) {
            FlashUtil.error(request, "User ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        User target = userDAO.findById(userId);
        if (target == null) {
            FlashUtil.error(request, "Không tìm thấy user #" + userId + ".");
            response.sendRedirect(request.getContextPath() + "/admin?action=users");
            return;
        }

        String newPassword = AppConstants.DEFAULT_PASSWORD;
        if (userDAO.changePassword(userId, newPassword)) {
            FlashUtil.success(request, "Đã reset password user \"" + target.getUsername()
                    + "\" (#" + userId + "). Mật khẩu mới: " + newPassword);
        } else {
            FlashUtil.error(request, "Reset password thất bại. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/admin?action=users");
    }

    // UC-ADM-06: List Announcement
    // URL: /admin?action=announcements

    private void handleListAnnouncements(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAdmin(request, response) == null) {
            return;
        }

        String statusRaw = trimToNull(request.getParameter("status"));
        Boolean isPublished = null;
        if ("published".equalsIgnoreCase(statusRaw)) {
            isPublished = Boolean.TRUE;
        } else if ("draft".equalsIgnoreCase(statusRaw)) {
            isPublished = Boolean.FALSE;
        }

        int page = parsePage(request.getParameter("page"));
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;

        List<Announcement> announcements =
                announcementDAO.findWithFilters(isPublished, page, pageSize);
        int total = announcementDAO.countWithFilters(isPublished);
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / pageSize);
        if (page > totalPages) {
            page = totalPages;
            announcements = announcementDAO.findWithFilters(isPublished, page, pageSize);
        }

        request.setAttribute("announcements", announcements);
        request.setAttribute("status", statusRaw);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", total);
        request.setAttribute("pageSize", pageSize);

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Quản lý thông báo");
        request.setAttribute("contentPage", "/WEB-INF/views/admin/listAnnouncement.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    // UC-ADM-06: Add Announcement
    // GET  /admin?action=add-announcement
    // POST /admin action=add-announcement  (Title, Content, Status)

    private void handleAddAnnouncementForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAdmin(request, response) == null) {
            return;
        }
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Thêm thông báo");
        request.setAttribute("contentPage", "/WEB-INF/views/admin/addAnnouncement.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleAddAnnouncement(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = requireAdmin(request, response);
        if (admin == null) {
            return;
        }

        String title = trimToNull(request.getParameter("title"));
        String content = trimToNull(request.getParameter("content"));
        String statusRaw = trimToNull(request.getParameter("status"));
        String category = trimToNull(request.getParameter("category"));

        request.setAttribute("formTitle", title);
        request.setAttribute("formContent", content);
        request.setAttribute("formStatus", statusRaw == null ? "draft" : statusRaw);
        request.setAttribute("formCategory", category);

        String error = validateAnnouncement(title, content);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("pageTitle", "Thêm thông báo");
            request.setAttribute("contentPage", "/WEB-INF/views/admin/addAnnouncement.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
            return;
        }

        boolean published = "published".equalsIgnoreCase(statusRaw)
                || "1".equals(statusRaw)
                || "true".equalsIgnoreCase(statusRaw);

        Announcement announcement = Announcement.builder()
                .title(title)
                .content(content)
                .category(category)
                .isPublished(published)
                .createdBy(admin.getUserId())
                .build();

        int newId = announcementDAO.insert(announcement);
        if (newId >= 0) {
            FlashUtil.success(request, "Thêm thông báo thành công"
                    + (newId > 0 ? " (ID #" + newId + ")." : "."));
            response.sendRedirect(request.getContextPath() + "/admin?action=announcements");
        } else {
            request.setAttribute("error", "Thêm thông báo thất bại. Vui lòng thử lại.");
            request.setAttribute("pageTitle", "Thêm thông báo");
            request.setAttribute("contentPage", "/WEB-INF/views/admin/addAnnouncement.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
        }
    }

    // UC-ADM-06: Edit Announcement
    // GET  /admin?action=edit-announcement&id=
    // POST /admin action=edit-announcement  (Title, Content, Status)

    private void handleEditAnnouncementForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAdmin(request, response) == null) {
            return;
        }

        Integer id = parsePositiveInt(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "Announcement ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/admin?action=announcements");
            return;
        }

        Announcement editAnnouncement = announcementDAO.findById(id);
        if (editAnnouncement == null) {
            FlashUtil.error(request, "Không tìm thấy thông báo #" + id + ".");
            response.sendRedirect(request.getContextPath() + "/admin?action=announcements");
            return;
        }

        request.setAttribute("editAnnouncement", editAnnouncement);
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Sửa thông báo #" + id);
        request.setAttribute("contentPage", "/WEB-INF/views/admin/editAnnouncement.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleEditAnnouncement(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAdmin(request, response) == null) {
            return;
        }

        Integer id = parsePositiveInt(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "Announcement ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/admin?action=announcements");
            return;
        }

        Announcement existing = announcementDAO.findById(id);
        if (existing == null) {
            FlashUtil.error(request, "Không tìm thấy thông báo #" + id + ".");
            response.sendRedirect(request.getContextPath() + "/admin?action=announcements");
            return;
        }

        String title = trimToNull(request.getParameter("title"));
        String content = trimToNull(request.getParameter("content"));
        String statusRaw = trimToNull(request.getParameter("status"));
        String category = trimToNull(request.getParameter("category"));

        boolean published = "published".equalsIgnoreCase(statusRaw)
                || "1".equals(statusRaw)
                || "true".equalsIgnoreCase(statusRaw);

        existing.setTitle(title);
        existing.setContent(content);
        existing.setCategory(category);
        existing.setIsPublished(published);

        String error = validateAnnouncement(title, content);
        if (error != null) {
            request.setAttribute("editAnnouncement", existing);
            request.setAttribute("error", error);
            request.setAttribute("pageTitle", "Sửa thông báo #" + id);

            request.setAttribute("contentPage", "/WEB-INF/views/admin/editAnnouncement.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
            return;
        }

        Announcement update = Announcement.builder()
                .announcementId(id)
                .title(title)
                .content(content)
                .category(category)
                .isPublished(published)
                .build();

        if (announcementDAO.update(update)) {
            FlashUtil.success(request, "Cập nhật thông báo #" + id + " thành công.");
            response.sendRedirect(request.getContextPath() + "/admin?action=announcements");
        } else {
            request.setAttribute("editAnnouncement", existing);
            request.setAttribute("error", "Cập nhật thông báo thất bại. Vui lòng thử lại.");
            request.setAttribute("pageTitle", "Sửa thông báo #" + id);
            request.setAttribute("contentPage", "/WEB-INF/views/admin/editAnnouncement.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
        }
    }

    // UC-ADM-06: Delete Announcement
    // GET/POST /admin?action=delete-announcement&id=

    private void handleDeleteAnnouncement(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (requireAdmin(request, response) == null) {
            return;
        }

        Integer id = parsePositiveInt(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "Announcement ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/admin?action=announcements");
            return;
        }

        Announcement existing = announcementDAO.findById(id);
        if (existing == null) {
            FlashUtil.error(request, "Không tìm thấy thông báo #" + id + ".");
            response.sendRedirect(request.getContextPath() + "/admin?action=announcements");
            return;
        }

        if (announcementDAO.delete(id)) {
            FlashUtil.success(request, "Đã xóa thông báo \"" + existing.getTitle()
                    + "\" (#" + id + ").");
        } else {
            FlashUtil.error(request, "Xóa thông báo thất bại. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/admin?action=announcements");
    }

    private String validateAnnouncement(String title, String content) {
        if (title == null) {
            return "Title không được để trống.";
        }
        if (title.length() > 200) {
            return "Title tối đa 200 ký tự.";
        }
        if (content == null) {
            return "Content không được để trống.";
        }
        return null;
    }

    private String validateUserCommonFields(String fullName, String email, String phone, String role) {
        if (fullName == null) {
            return "Full Name không được để trống.";
        }
        if (fullName.length() > 100) {
            return "Full Name tối đa 100 ký tự.";
        }
        if (email != null) {
            if (email.length() > 100) {
                return "Email tối đa 100 ký tự.";
            }
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                return "Email không hợp lệ.";
            }
        }
        if (phone != null) {
            if (!phone.matches("^0\\d{9}$")) {
                return "Số điện thoại phải đúng 10 chữ số và bắt đầu bằng 0 (vd: 0912345678).";
            }
        }
        if (role == null || !ALLOWED_ROLES.contains(role)) {
            return "Role không hợp lệ. Chọn ADMIN / MANAGER / STAFF / RESIDENT.";
        }
        return null;
    }

    // Helpers

    private User requireAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(AppConstants.SESSION_USER);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return null;
        }
        if (!AppConstants.ROLE_ADMIN.equals(user.getRole())) {
            request.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
            return null;
        }
        return user;
    }

    private boolean isProtectedFromLock(User target, User currentAdmin) {
        if (target == null) {
            return true;
        }
        if (AppConstants.ROLE_ADMIN.equals(target.getRole())) {
            return true;
        }
        if (currentAdmin != null && currentAdmin.getUserId() != null
                && currentAdmin.getUserId().equals(target.getUserId())) {
            return true;
        }
        return false;
    }

    private int parsePage(String raw) {
        if (raw == null || raw.isEmpty()) {
            return 1;
        }
        try {
            int page = Integer.parseInt(raw);
            return page < 1 ? 1 : page;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private Boolean parseActiveStatus(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String s = raw.trim().toLowerCase();
        if ("active".equals(s) || "1".equals(s) || "true".equals(s)) {
            return Boolean.TRUE;
        }
        if ("locked".equals(s) || "inactive".equals(s) || "0".equals(s) || "false".equals(s)) {
            return Boolean.FALSE;
        }
        return null;
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private Integer parsePositiveInt(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
