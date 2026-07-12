package apartmentmanagement.controller.apartment;

import apartmentmanagement.dao.ApartmentDAO;
import apartmentmanagement.dao.ApartmentHistoryDAO;
import apartmentmanagement.dao.ApartmentResidentDAO;
import apartmentmanagement.dao.HouseholdMemberDAO;
import apartmentmanagement.model.Apartment;
import apartmentmanagement.model.ApartmentHistory;
import apartmentmanagement.model.ApartmentResident;
import apartmentmanagement.model.HouseholdMember;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Module căn hộ (TV2).
 * UC-APT-01..05: Thêm · Sửa · Vô hiệu/Xóa · List · Chi tiết.
 * Tuân thủ coding-standards.md (Jakarta, @WebServlet, action switch, DAO).
 */
@WebServlet(name = "ApartmentController", urlPatterns = {"/apartment"})
public class ApartmentController extends HttpServlet {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9_-]{0,19}$");
    private static final BigDecimal AREA_MIN = new BigDecimal("0.01");
    private static final BigDecimal AREA_MAX = new BigDecimal("10000");

    private final ApartmentDAO apartmentDAO = new ApartmentDAO();
    private final ApartmentResidentDAO apartmentResidentDAO = new ApartmentResidentDAO();
    private final HouseholdMemberDAO householdMemberDAO = new HouseholdMemberDAO();
    private final ApartmentHistoryDAO apartmentHistoryDAO = new ApartmentHistoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list";
        }

        switch (action) {
            case "list":
                handleList(request, response);
                break;
            case "create":
                handleCreateForm(request, response);
                break;
            case "edit":
                handleEditForm(request, response);
                break;
            case "detail":
                handleDetail(request, response);
                break;
            case "my":
                FlashUtil.error(request, "Chức năng căn hộ của tôi đang được phát triển.");
                response.sendRedirect(request.getContextPath() + "/dashboard");
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
            case "create":
                handleCreate(request, response);
                break;
            case "update":
                handleUpdate(request, response);
                break;
            case "deactivate":
                handleDeactivate(request, response);
                break;
            case "activate":
                handleActivate(request, response);
                break;
            case "delete":
                handleDelete(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    /**
     * UC-APT-04: danh sách + keyword + filter + sort + pagination.
     */
    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canViewList(user.getRole())) {
            request.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
            return;
        }

        String keyword = trim(request.getParameter("keyword"));
        String building = trim(request.getParameter("building"));
        String status = trim(request.getParameter("status"));
        String occupancyType = trim(request.getParameter("occupancyType"));
        String sort = trim(request.getParameter("sort"));
        String dir = trim(request.getParameter("dir"));

        if (sort == null || sort.isEmpty()) {
            sort = "building";
        }
        if (dir == null || dir.isEmpty()) {
            dir = "asc";
        }
        if (!"desc".equalsIgnoreCase(dir)) {
            dir = "asc";
        }

        int page = 1;
        try {
            page = Integer.parseInt(request.getParameter("page"));
        } catch (Exception ignored) {
        }
        if (page < 1) {
            page = 1;
        }
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;

        int totalItems = apartmentDAO.countWithFilters(keyword, building, status, occupancyType);
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil(totalItems * 1.0 / pageSize);
        if (page > totalPages) {
            page = totalPages;
        }

        List<Apartment> apartments = apartmentDAO.findWithFilters(
                keyword, building, status, occupancyType, sort, dir, page, pageSize);

        boolean hasFilter = (keyword != null && !keyword.isEmpty())
                || (building != null && !building.isEmpty())
                || (status != null && !status.isEmpty())
                || (occupancyType != null && !occupancyType.isEmpty());

        int fromIndex = totalItems == 0 ? 0 : (page - 1) * pageSize + 1;
        int toIndex = Math.min(page * pageSize, totalItems);

        FlashUtil.moveToRequest(request);
        request.setAttribute("apartments", apartments);
        request.setAttribute("canManage", canManage(user.getRole()));
        request.setAttribute("keyword", keyword == null ? "" : keyword);
        request.setAttribute("buildingFilter", building == null ? "" : building);
        request.setAttribute("statusFilter", status == null ? "" : status);
        request.setAttribute("occupancyFilter", occupancyType == null ? "" : occupancyType);
        request.setAttribute("sort", sort);
        request.setAttribute("dir", dir);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("fromIndex", fromIndex);
        request.setAttribute("toIndex", toIndex);
        request.setAttribute("hasFilter", hasFilter);
        request.setAttribute("pageTitle", "Danh sách căn hộ");
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/list.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền thêm căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        if (request.getAttribute("form") == null) {
            request.setAttribute("form", Apartment.builder()
                    .occupancyType(AppConstants.OCCUPANCY_OWNED)
                    .status(AppConstants.APT_STATUS_ACTIVE)
                    .build());
        }

        FlashUtil.moveToRequest(request);
        request.setAttribute("formMode", "create");
        request.setAttribute("pageTitle", "Thêm căn hộ");
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/form.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền thêm căn hộ.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        Apartment form = bindForm(request, false);
        List<String> errors = validateForCreate(form);

        if (!errors.isEmpty()) {
            forwardForm(request, response, form, errors, "create");
            return;
        }

        if (apartmentDAO.existsByCode(form.getApartmentCode())) {
            errors.add("Mã căn hộ đã tồn tại.");
            forwardForm(request, response, form, errors, "create");
            return;
        }

        int newId = apartmentDAO.insert(form);
        if (newId >= 0) {
            form.setApartmentId(newId > 0 ? newId : null);
            writeHistory(user, form, "CREATE", null, form.getStatus(), "Tạo căn hộ mới");
            FlashUtil.success(request, "Thêm căn hộ thành công.");
            if (newId > 0) {
                response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + newId);
            } else {
                response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            }
        } else {
            errors.add("Không thể thêm căn hộ. Vui lòng thử lại.");
            forwardForm(request, response, form, errors, "create");
        }
    }

    /**
     * UC-APT-05: chi tiết căn hộ — thông tin, chủ/thuê, thành viên, lịch sử, action theo role.
     */
    private void handleDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }

        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment apartment = apartmentDAO.findById(id);
        if (apartment == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        if (!canViewDetail(user, id)) {
            request.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
            return;
        }

        List<ApartmentResident> owners =
                apartmentResidentDAO.findByApartmentAndRoles(id, "OWNER");
        List<ApartmentResident> tenants =
                apartmentResidentDAO.findByApartmentAndRoles(id, "TENANT_REP", "TENANT");
        List<HouseholdMember> members = householdMemberDAO.findByApartmentId(id);
        List<ApartmentHistory> histories = apartmentHistoryDAO.findByApartmentId(id, 30);

        boolean manage = canManage(user.getRole());
        FlashUtil.moveToRequest(request);
        request.setAttribute("apartment", apartment);
        request.setAttribute("owners", owners);
        request.setAttribute("tenants", tenants);
        request.setAttribute("members", members);
        request.setAttribute("histories", histories);
        request.setAttribute("canManage", manage);
        request.setAttribute("pageTitle", "Chi tiết · " + apartment.getApartmentCode());
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/detail.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    /**
     * UC-APT-02: mở form cập nhật.
     */
    private void handleEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền cập nhật căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        // Khi validate fail đã set form — không ghi đè
        if (request.getAttribute("form") == null) {
            Apartment existing = apartmentDAO.findById(id);
            if (existing == null) {
                FlashUtil.error(request, "Không tìm thấy căn hộ.");
                response.sendRedirect(request.getContextPath() + "/apartment?action=list");
                return;
            }
            request.setAttribute("form", existing);
        }

        FlashUtil.moveToRequest(request);
        request.setAttribute("formMode", "edit");
        request.setAttribute("pageTitle", "Cập nhật căn hộ");
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/form.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    /**
     * UC-APT-02: submit cập nhật.
     */
    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền cập nhật căn hộ.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        Apartment form = bindForm(request, true);
        List<String> errors = new ArrayList<>();

        if (form.getApartmentId() == null || form.getApartmentId() <= 0) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment existing = apartmentDAO.findById(form.getApartmentId());
        if (existing == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        // Giữ mã căn gốc — không cho đổi (BR-U02)
        form.setApartmentCode(existing.getApartmentCode());

        errors.addAll(validateForUpdate(form));
        if (!errors.isEmpty()) {
            forwardForm(request, response, form, errors, "edit");
            return;
        }

        if (apartmentDAO.update(form)) {
            writeHistory(user, form, "UPDATE", existing.getStatus(), form.getStatus(), "Cập nhật thông tin căn hộ");
            FlashUtil.success(request, "Cập nhật căn hộ thành công.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + form.getApartmentId());
        } else {
            errors.add("Không thể cập nhật căn hộ. Vui lòng thử lại.");
            forwardForm(request, response, form, errors, "edit");
        }
    }

    /**
     * UC-APT-03: vô hiệu hóa căn (ACTIVE → INACTIVE). Soft disable — giữ lịch sử.
     */
    private void handleDeactivate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền vô hiệu hóa/xóa căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment apt = apartmentDAO.findById(id);
        if (apt == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        if (!AppConstants.APT_STATUS_ACTIVE.equals(apt.getStatus())) {
            audit(user, "DEACTIVATE", apt, apt.getStatus(), apt.getStatus(), "DENIED",
                    "Căn hộ đã ở trạng thái không hoạt động.");
            FlashUtil.error(request, "Căn hộ đã ở trạng thái không hoạt động.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        int currentResidents = apartmentDAO.countCurrentResidents(id);
        if (apartmentDAO.updateStatus(id, AppConstants.APT_STATUS_INACTIVE)) {
            String note = currentResidents > 0
                    ? "OK (còn " + currentResidents + " cư dân hiện tại — chỉ soft disable)"
                    : "OK";
            audit(user, "DEACTIVATE", apt, AppConstants.APT_STATUS_ACTIVE,
                    AppConstants.APT_STATUS_INACTIVE, "SUCCESS", note);
            writeHistory(user, apt, "DEACTIVATE", AppConstants.APT_STATUS_ACTIVE,
                    AppConstants.APT_STATUS_INACTIVE, note);
            FlashUtil.success(request, "Đã vô hiệu hóa căn hộ.");
        } else {
            audit(user, "DEACTIVATE", apt, AppConstants.APT_STATUS_ACTIVE,
                    AppConstants.APT_STATUS_ACTIVE, "ERROR", "updateStatus failed");
            FlashUtil.error(request, "Không thể thực hiện. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + id);
    }

    /**
     * UC-APT-03: kích hoạt lại (INACTIVE → ACTIVE).
     */
    private void handleActivate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền vô hiệu hóa/xóa căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment apt = apartmentDAO.findById(id);
        if (apt == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        if (!AppConstants.APT_STATUS_INACTIVE.equals(apt.getStatus())) {
            audit(user, "ACTIVATE", apt, apt.getStatus(), apt.getStatus(), "DENIED",
                    "Căn hộ đang hoạt động.");
            FlashUtil.error(request, "Căn hộ đang hoạt động.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        if (apartmentDAO.updateStatus(id, AppConstants.APT_STATUS_ACTIVE)) {
            audit(user, "ACTIVATE", apt, AppConstants.APT_STATUS_INACTIVE,
                    AppConstants.APT_STATUS_ACTIVE, "SUCCESS", "OK");
            writeHistory(user, apt, "ACTIVATE", AppConstants.APT_STATUS_INACTIVE,
                    AppConstants.APT_STATUS_ACTIVE, "Kích hoạt lại");
            FlashUtil.success(request, "Đã kích hoạt lại căn hộ.");
        } else {
            audit(user, "ACTIVATE", apt, AppConstants.APT_STATUS_INACTIVE,
                    AppConstants.APT_STATUS_INACTIVE, "ERROR", "updateStatus failed");
            FlashUtil.error(request, "Không thể thực hiện. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + id);
    }

    /**
     * UC-APT-03: xóa cứng — chỉ khi INACTIVE và không còn cư dân hiện tại.
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền vô hiệu hóa/xóa căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment apt = apartmentDAO.findById(id);
        if (apt == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        if (!AppConstants.APT_STATUS_INACTIVE.equals(apt.getStatus())) {
            audit(user, "DELETE", apt, apt.getStatus(), apt.getStatus(), "DENIED",
                    "Chỉ xóa được căn đã vô hiệu hóa.");
            FlashUtil.error(request, "Chỉ xóa được căn đã vô hiệu hóa.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        int currentResidents = apartmentDAO.countCurrentResidents(id);
        if (currentResidents > 0) {
            audit(user, "DELETE", apt, apt.getStatus(), apt.getStatus(), "DENIED",
                    "Còn " + currentResidents + " cư dân hiện tại");
            FlashUtil.error(request, "Không thể xóa: căn vẫn còn cư dân hiện tại. Hãy vô hiệu hóa hoặc gỡ cư dân trước.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        // BR-CON: module contracts chưa có bảng → bỏ qua check HĐ trong MVP

        if (apartmentDAO.deleteById(id)) {
            audit(user, "DELETE", apt, AppConstants.APT_STATUS_INACTIVE, "DELETED", "SUCCESS", "OK");
            FlashUtil.success(request, "Đã xóa căn hộ.");
        } else {
            audit(user, "DELETE", apt, apt.getStatus(), apt.getStatus(), "ERROR", "deleteById failed");
            FlashUtil.error(request, "Không thể thực hiện. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/apartment?action=list");
    }

    /**
     * Audit log MVP — ghi console Tomcat (BR-AUD). Format 1 dòng dễ grep.
     */
    private void audit(User actor, String action, Apartment apt,
            String fromStatus, String toStatus, String result, String message) {
        System.out.println(String.format(
                "AUDIT | ts=%d | userId=%s | username=%s | role=%s | action=%s | apartmentId=%s | code=%s | %s->%s | result=%s | msg=%s",
                System.currentTimeMillis(),
                actor.getUserId(),
                actor.getUsername(),
                actor.getRole(),
                action,
                apt != null ? apt.getApartmentId() : null,
                apt != null ? apt.getApartmentCode() : null,
                fromStatus,
                toStatus,
                result,
                message));
    }

    /** Ghi lịch sử DB (UC-APT-05). Lỗi bảng chưa có → bỏ qua, không chặn UC chính. */
    private void writeHistory(User actor, Apartment apt, String action,
            String fromStatus, String toStatus, String note) {
        if (apt == null || apt.getApartmentId() == null) {
            return;
        }
        try {
            apartmentHistoryDAO.insert(ApartmentHistory.builder()
                    .apartmentId(apt.getApartmentId())
                    .action(action)
                    .oldStatus(fromStatus)
                    .newStatus(toStatus)
                    .note(note)
                    .actorUserId(actor != null ? actor.getUserId() : null)
                    .actorName(actor != null ? actor.getFullName() : null)
                    .build());
        } catch (Exception e) {
            System.out.println("writeHistory skip: " + e.getMessage());
        }
    }

    private void forwardForm(HttpServletRequest request, HttpServletResponse response,
            Apartment form, List<String> errors, String mode)
            throws ServletException, IOException {
        request.setAttribute("errors", errors);
        request.setAttribute("form", form);
        request.setAttribute("formMode", mode);
        request.setAttribute("pageTitle", "edit".equals(mode) ? "Cập nhật căn hộ" : "Thêm căn hộ");
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/form.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    /**
     * @param forUpdate true = đọc thêm apartmentId
     */
    private Apartment bindForm(HttpServletRequest request, boolean forUpdate) {
        String code = trim(request.getParameter("apartmentCode"));
        String building = trim(request.getParameter("building"));
        String floorRaw = trim(request.getParameter("floorNumber"));
        String areaRaw = trim(request.getParameter("areaM2"));
        String occupancyType = trim(request.getParameter("occupancyType"));
        String status = trim(request.getParameter("status"));
        String notes = trim(request.getParameter("notes"));

        Integer floorNumber = null;
        if (floorRaw != null && !floorRaw.isEmpty()) {
            try {
                floorNumber = Integer.valueOf(floorRaw);
            } catch (NumberFormatException ignored) {
            }
        }

        BigDecimal areaM2 = null;
        if (areaRaw != null && !areaRaw.isEmpty()) {
            try {
                areaM2 = new BigDecimal(areaRaw.replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException | ArithmeticException ignored) {
            }
        }

        if (status == null || status.isEmpty()) {
            status = AppConstants.APT_STATUS_ACTIVE;
        }

        Integer apartmentId = null;
        if (forUpdate) {
            apartmentId = parseId(request.getParameter("apartmentId"));
        }

        return Apartment.builder()
                .apartmentId(apartmentId)
                .apartmentCode(code)
                .building(building)
                .floorNumber(floorNumber)
                .areaM2(areaM2)
                .occupancyType(occupancyType)
                .status(status)
                .notes(notes)
                .build();
    }

    private List<String> validateForCreate(Apartment form) {
        List<String> errors = new ArrayList<>();

        String code = form.getApartmentCode();
        if (code == null || code.isEmpty()) {
            errors.add("Vui lòng nhập mã căn hộ.");
        } else if (!CODE_PATTERN.matcher(code).matches()) {
            errors.add("Mã căn hộ chỉ gồm chữ, số, gạch ngang hoặc gạch dưới (tối đa 20 ký tự).");
        }

        errors.addAll(validateSharedFields(form));
        return errors;
    }

    /** Update: không validate/đổi mã căn (đã khóa). */
    private List<String> validateForUpdate(Apartment form) {
        return validateSharedFields(form);
    }

    private List<String> validateSharedFields(Apartment form) {
        List<String> errors = new ArrayList<>();

        String building = form.getBuilding();
        if (building == null || building.isEmpty()) {
            errors.add("Vui lòng nhập tòa nhà.");
        } else if (building.length() > 50) {
            errors.add("Tên tòa nhà tối đa 50 ký tự.");
        }

        Integer floor = form.getFloorNumber();
        if (floor == null || floor < 0 || floor > 200) {
            errors.add("Tầng phải là số nguyên từ 0 đến 200.");
        }

        BigDecimal area = form.getAreaM2();
        if (area == null || area.compareTo(AREA_MIN) < 0 || area.compareTo(AREA_MAX) > 0) {
            errors.add("Diện tích phải là số lớn hơn 0 (tối đa 10.000 m²).");
        }

        String occupancy = form.getOccupancyType();
        if (occupancy == null
                || !(AppConstants.OCCUPANCY_OWNED.equals(occupancy)
                || AppConstants.OCCUPANCY_RENTED.equals(occupancy))) {
            errors.add("Loại hình sử dụng không hợp lệ (OWNED / RENTED).");
        }

        String status = form.getStatus();
        if (status == null
                || !(AppConstants.APT_STATUS_ACTIVE.equals(status)
                || AppConstants.APT_STATUS_INACTIVE.equals(status))) {
            errors.add("Trạng thái không hợp lệ (ACTIVE / INACTIVE).");
        }

        String notes = form.getNotes();
        if (notes != null && notes.length() > 500) {
            errors.add("Ghi chú tối đa 500 ký tự.");
        }

        return errors;
    }

    private Integer parseId(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            int id = Integer.parseInt(raw.trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** ADMIN / MANAGER: thêm + sửa + disable */
    private boolean canManage(String role) {
        return AppConstants.ROLE_ADMIN.equals(role) || AppConstants.ROLE_MANAGER.equals(role);
    }

    private boolean canViewList(String role) {
        return AppConstants.ROLE_ADMIN.equals(role)
                || AppConstants.ROLE_MANAGER.equals(role)
                || AppConstants.ROLE_STAFF.equals(role);
    }

    /**
     * Chi tiết: ADMIN/MANAGER/STAFF xem mọi căn;
     * RESIDENT chỉ xem căn đang được gán (is_current).
     */
    private boolean canViewDetail(User user, int apartmentId) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        if (canViewList(user.getRole())) {
            return true;
        }
        if (AppConstants.ROLE_RESIDENT.equals(user.getRole())) {
            return apartmentResidentDAO.isCurrentResident(apartmentId, user.getUserId());
        }
        return false;
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
