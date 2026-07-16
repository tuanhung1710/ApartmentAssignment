package apartmentmanagement.controller.admin;

import apartmentmanagement.dao.BuildingDAO;
import apartmentmanagement.model.Apartment;
import apartmentmanagement.model.Building;
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
 * CRUD tòa nhà — TV1 master data.
 * URL: /building?action=list|create|edit|detail|save|deactivate|activate|delete
 * Role ghi: ADMIN, MANAGER · Role xem: ADMIN, MANAGER, STAFF
 */
@WebServlet(name = "BuildingController", urlPatterns = {"/building"})
public class BuildingController extends HttpServlet {

    private final BuildingDAO buildingDAO = new BuildingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }

        switch (action) {
            case "list" -> handleList(request, response);
            case "create" -> handleCreateForm(request, response, user);
            case "edit" -> handleEditForm(request, response, user);
            case "detail" -> handleDetail(request, response);
            case "deactivate" -> handleDeactivate(request, response, user);
            case "activate" -> handleActivate(request, response, user);
            case "delete" -> handleDelete(request, response, user);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "save";
        }

        switch (action) {
            case "save" -> handleSave(request, response, user);
            case "deactivate" -> handleDeactivate(request, response, user);
            case "activate" -> handleActivate(request, response, user);
            case "delete" -> handleDelete(request, response, user);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /* ===================== handlers ===================== */

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = trim(request.getParameter("q"));
        String status = trim(request.getParameter("status"));
        int page = parsePage(request.getParameter("page"));
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;

        List<Building> buildings = buildingDAO.findWithFilters(keyword, status, page, pageSize);
        int total = buildingDAO.countWithFilters(keyword, status);
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / pageSize);
        if (page > totalPages) {
            page = totalPages;
            buildings = buildingDAO.findWithFilters(keyword, status, page, pageSize);
        }

        request.setAttribute("buildings", buildings);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", total);
        request.setAttribute("filterQ", keyword == null ? "" : keyword);
        request.setAttribute("filterStatus", status == null ? "" : status);
        request.setAttribute("activeCount", buildingDAO.countByStatus(AppConstants.STATUS_ACTIVE));
        request.setAttribute("inactiveCount", buildingDAO.countByStatus(AppConstants.STATUS_INACTIVE));
        request.setAttribute("totalBuildings", buildingDAO.countAll());

        FlashUtil.moveToRequest(request);
        forward(request, response, "Quản lý tòa nhà", "/WEB-INF/views/admin/building-list.jsp");
    }

    private void handleCreateForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!canWrite(user)) {
            forbidden(request, response);
            return;
        }
        request.setAttribute("formMode", "create");
        request.setAttribute("building", Building.builder().status(AppConstants.STATUS_ACTIVE).build());
        FlashUtil.moveToRequest(request);
        forward(request, response, "Thêm tòa nhà", "/WEB-INF/views/admin/building-form.jsp");
    }

    private void handleEditForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!canWrite(user)) {
            forbidden(request, response);
            return;
        }
        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "Thiếu mã tòa nhà.");
            response.sendRedirect(request.getContextPath() + "/building?action=list");
            return;
        }
        Building building = buildingDAO.findById(id);
        if (building == null) {
            FlashUtil.error(request, "Không tìm thấy tòa nhà.");
            response.sendRedirect(request.getContextPath() + "/building?action=list");
            return;
        }
        request.setAttribute("formMode", "edit");
        request.setAttribute("building", building);
        FlashUtil.moveToRequest(request);
        forward(request, response, "Sửa tòa nhà", "/WEB-INF/views/admin/building-form.jsp");
    }

    private void handleDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "Thiếu mã tòa nhà.");
            response.sendRedirect(request.getContextPath() + "/building?action=list");
            return;
        }
        Building building = buildingDAO.findById(id);
        if (building == null) {
            FlashUtil.error(request, "Không tìm thấy tòa nhà.");
            response.sendRedirect(request.getContextPath() + "/building?action=list");
            return;
        }

        // Filter căn hộ thuộc tòa: status ACTIVE | INACTIVE | (blank = tất cả)
        String aptStatus = trim(request.getParameter("aptStatus"));
        if (aptStatus != null
                && !AppConstants.STATUS_ACTIVE.equals(aptStatus)
                && !AppConstants.STATUS_INACTIVE.equals(aptStatus)) {
            aptStatus = null;
        }
        String aptQ = trim(request.getParameter("aptQ"));
        int aptPage = parsePage(request.getParameter("aptPage"));
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;

        List<Apartment> apartments = buildingDAO.findApartmentsByBuilding(
                id, aptStatus, aptQ, aptPage, pageSize);
        int aptTotal = buildingDAO.countApartmentsByBuilding(id, aptStatus, aptQ);
        int aptTotalPages = aptTotal == 0 ? 1 : (int) Math.ceil((double) aptTotal / pageSize);
        if (aptPage > aptTotalPages) {
            aptPage = aptTotalPages;
            apartments = buildingDAO.findApartmentsByBuilding(
                    id, aptStatus, aptQ, aptPage, pageSize);
        }

        int aptActive = buildingDAO.countApartmentsByBuildingStatus(id, AppConstants.STATUS_ACTIVE);
        int aptInactive = buildingDAO.countApartmentsByBuildingStatus(id, AppConstants.STATUS_INACTIVE);
        int aptAll = buildingDAO.countApartmentsByBuildingStatus(id, null);

        request.setAttribute("building", building);
        request.setAttribute("apartments", apartments);
        request.setAttribute("aptCurrentPage", aptPage);
        request.setAttribute("aptTotalPages", aptTotalPages);
        request.setAttribute("aptTotalItems", aptTotal);
        request.setAttribute("filterAptStatus", aptStatus == null ? "" : aptStatus);
        request.setAttribute("filterAptQ", aptQ == null ? "" : aptQ);
        request.setAttribute("aptActiveCount", aptActive);
        request.setAttribute("aptInactiveCount", aptInactive);
        request.setAttribute("aptAllCount", aptAll);

        FlashUtil.moveToRequest(request);
        forward(request, response, "Chi tiết tòa " + building.getBuildingCode(),
                "/WEB-INF/views/admin/building-detail.jsp");
    }

    private void handleSave(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!canWrite(user)) {
            forbidden(request, response);
            return;
        }

        Integer id = parseId(request.getParameter("buildingId"));
        boolean isEdit = id != null;

        String code = trim(request.getParameter("buildingCode"));
        String name = trim(request.getParameter("buildingName"));
        String address = trim(request.getParameter("address"));
        String description = trim(request.getParameter("description"));
        String status = trim(request.getParameter("status"));
        Integer floors = parsePositiveInt(request.getParameter("totalFloors"));

        Building form = Building.builder()
                .buildingId(id)
                .buildingCode(code)
                .buildingName(name)
                .address(address)
                .totalFloors(floors)
                .description(description)
                .status(status == null || status.isBlank() ? AppConstants.STATUS_ACTIVE : status)
                .build();

        String error = validate(form, isEdit);
        if (error != null) {
            request.setAttribute("formError", error);
            request.setAttribute("formMode", isEdit ? "edit" : "create");
            request.setAttribute("building", form);
            FlashUtil.moveToRequest(request);
            forward(request, response, isEdit ? "Sửa tòa nhà" : "Thêm tòa nhà",
                    "/WEB-INF/views/admin/building-form.jsp");
            return;
        }

        form.setBuildingCode(code.toUpperCase());

        if (isEdit) {
            Building existing = buildingDAO.findById(id);
            if (existing == null) {
                FlashUtil.error(request, "Tòa nhà không còn tồn tại.");
                response.sendRedirect(request.getContextPath() + "/building?action=list");
                return;
            }
            if (buildingDAO.update(form)) {
                FlashUtil.success(request, "Đã cập nhật tòa " + form.getBuildingCode() + ".");
                response.sendRedirect(request.getContextPath() + "/building?action=detail&id=" + id);
            } else {
                FlashUtil.error(request, "Cập nhật thất bại. Mã tòa có thể bị trùng.");
                response.sendRedirect(request.getContextPath() + "/building?action=edit&id=" + id);
            }
        } else {
            int newId = buildingDAO.insert(form);
            if (newId > 0) {
                FlashUtil.success(request, "Đã thêm tòa " + form.getBuildingCode() + ".");
                response.sendRedirect(request.getContextPath() + "/building?action=detail&id=" + newId);
            } else {
                FlashUtil.error(request, "Thêm tòa thất bại. Kiểm tra mã tòa không trùng.");
                response.sendRedirect(request.getContextPath() + "/building?action=create");
            }
        }
    }

    private void handleDeactivate(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!canWrite(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "Thiếu mã tòa nhà.");
        } else if (buildingDAO.deactivate(id)) {
            FlashUtil.success(request, "Đã ngưng hoạt động tòa nhà (soft-delete).");
        } else {
            FlashUtil.error(request, "Không thể ngưng tòa nhà.");
        }
        response.sendRedirect(request.getContextPath() + "/building?action=list");
    }

    private void handleActivate(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!canWrite(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "Thiếu mã tòa nhà.");
        } else if (buildingDAO.activate(id)) {
            FlashUtil.success(request, "Đã kích hoạt lại tòa nhà.");
        } else {
            FlashUtil.error(request, "Không thể kích hoạt tòa nhà.");
        }
        response.sendRedirect(request.getContextPath() + "/building?action=list");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!canWrite(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "Thiếu mã tòa nhà.");
            response.sendRedirect(request.getContextPath() + "/building?action=list");
            return;
        }
        int result = buildingDAO.deleteIfEmpty(id);
        switch (result) {
            case 1 -> FlashUtil.success(request, "Đã xóa tòa nhà (không còn căn hộ).");
            case -1 -> FlashUtil.error(request,
                    "Không thể xóa: tòa còn căn hộ. Hãy ngưng hoạt động hoặc chuyển căn trước.");
            case -2 -> FlashUtil.error(request, "Không tìm thấy tòa nhà.");
            default -> FlashUtil.error(request, "Xóa tòa nhà thất bại.");
        }
        response.sendRedirect(request.getContextPath() + "/building?action=list");
    }

    /* ===================== validation / helpers ===================== */

    private String validate(Building b, boolean isEdit) {
        if (b.getBuildingCode() == null || b.getBuildingCode().isBlank()) {
            return "Mã tòa không được để trống.";
        }
        String code = b.getBuildingCode().trim();
        if (code.length() > 20) {
            return "Mã tòa tối đa 20 ký tự.";
        }
        if (!code.matches("(?i)[A-Z0-9\\-]+")) {
            return "Mã tòa chỉ gồm chữ, số và dấu gạch ngang.";
        }
        if (b.getBuildingName() == null || b.getBuildingName().isBlank()) {
            return "Tên tòa không được để trống.";
        }
        if (b.getBuildingName().trim().length() > 100) {
            return "Tên tòa tối đa 100 ký tự.";
        }
        if (b.getTotalFloors() != null && (b.getTotalFloors() < 1 || b.getTotalFloors() > 200)) {
            return "Số tầng phải từ 1 đến 200.";
        }
        String status = b.getStatus();
        if (status == null
                || (!AppConstants.STATUS_ACTIVE.equals(status)
                && !AppConstants.STATUS_INACTIVE.equals(status))) {
            return "Trạng thái không hợp lệ.";
        }
        Integer exceptId = isEdit ? b.getBuildingId() : null;
        if (buildingDAO.existsCodeExceptId(code, exceptId)) {
            return "Mã tòa \"" + code.toUpperCase() + "\" đã tồn tại.";
        }
        return null;
    }

    private User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(AppConstants.SESSION_USER);
    }

    private boolean canWrite(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return AppConstants.ROLE_ADMIN.equals(user.getRole())
                || AppConstants.ROLE_MANAGER.equals(user.getRole());
    }

    private void forbidden(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response,
            String pageTitle, String contentPage) throws ServletException, IOException {
        request.setAttribute("pageTitle", pageTitle);
        request.setAttribute("contentPage", contentPage);
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static int parsePage(String raw) {
        try {
            int p = Integer.parseInt(raw);
            return p < 1 ? 1 : p;
        } catch (Exception e) {
            return 1;
        }
    }

    private static Integer parseId(String raw) {
        try {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            int id = Integer.parseInt(raw.trim());
            return id < 1 ? null : id;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parsePositiveInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int v = Integer.parseInt(raw.trim());
            return v < 1 ? null : v;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
