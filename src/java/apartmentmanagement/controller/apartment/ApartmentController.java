package apartmentmanagement.controller.apartment;

import apartmentmanagement.dao.ApartmentDAO;
import apartmentmanagement.dao.ApartmentHistoryDAO;
import apartmentmanagement.dao.ApartmentResidentDAO;
import apartmentmanagement.dao.HouseholdMemberDAO;
import apartmentmanagement.dao.UserDAO;
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
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Module căn hộ (TV2).
 * UC-APT-01..10: CRUD căn · List · Detail · Owner · Tenant · TV · List TV + export.
 * Tuân thủ coding-standards.md (Jakarta, @WebServlet, action switch, DAO).
 */
@WebServlet(name = "ApartmentController", urlPatterns = {"/apartment"})
public class ApartmentController extends HttpServlet {

    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{9,12}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0?\\d{9,10}$");
    private static final Pattern BUILDING_TOKEN_PATTERN = Pattern.compile("(?i)(?:tòa\\s*|toa\\s*|building\\s*)?([A-Za-z0-9]+)");
    private static final BigDecimal AREA_MIN = new BigDecimal("15");
    private static final BigDecimal AREA_MAX = new BigDecimal("10000");
    private static final String[] RELATIONSHIP_OPTIONS = {
        "Chủ hộ", "Vợ/Chồng", "Con", "Cha/Mẹ", "Anh/Chị/Em", "Ông/Bà", "Cháu", "Khác"
    };

    private final ApartmentDAO apartmentDAO = new ApartmentDAO();
    private final ApartmentResidentDAO apartmentResidentDAO = new ApartmentResidentDAO();
    private final HouseholdMemberDAO householdMemberDAO = new HouseholdMemberDAO();
    private final ApartmentHistoryDAO apartmentHistoryDAO = new ApartmentHistoryDAO();
    private final UserDAO userDAO = new UserDAO();

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
            case "assign-owner":
                handleAssignOwnerForm(request, response);
                break;
            case "assign-tenant":
                handleAssignTenantForm(request, response);
                break;
            case "add-member":
                handleAddMemberForm(request, response);
                break;
            case "edit-member":
                handleEditMemberForm(request, response);
                break;
            case "members":
                handleMembers(request, response);
                break;
            case "export-members":
                handleExportMembers(request, response);
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
            case "assign-owner":
                handleAssignOwner(request, response);
                break;
            case "remove-owner":
                handleRemoveOwner(request, response);
                break;
            case "assign-tenant":
                handleAssignTenant(request, response);
                break;
            case "remove-tenant":
                handleRemoveTenant(request, response);
                break;
            case "add-member":
                handleAddMember(request, response);
                break;
            case "edit-member":
                handleEditMember(request, response);
                break;
            case "remove-member":
                handleRemoveMember(request, response);
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
        // Mã căn do hệ thống sinh từ tòa + tầng — bỏ qua input client
        form.setApartmentCode(null);

        List<String> errors = validateForCreate(form);
        if (!errors.isEmpty()) {
            forwardForm(request, response, form, errors, "create");
            return;
        }

        String generatedCode = generateApartmentCode(form.getBuilding(), form.getFloorNumber());
        if (generatedCode == null) {
            errors.add("Không thể sinh mã căn hộ. Vui lòng kiểm tra tòa nhà / tầng.");
            forwardForm(request, response, form, errors, "create");
            return;
        }
        form.setApartmentCode(generatedCode);

        if (apartmentDAO.existsByCode(form.getApartmentCode())) {
            errors.add("Đã tồn tại căn hộ với mã " + form.getApartmentCode()
                    + " (" + formatApartmentIdentity(form) + ").");
            form.setApartmentCode(null);
            forwardForm(request, response, form, errors, "create");
            return;
        }

        int newId = apartmentDAO.insert(form);
        if (newId >= 0) {
            form.setApartmentId(newId > 0 ? newId : null);
            writeHistory(user, form, "CREATE", null, form.getStatus(), "Tạo căn hộ mới");
            FlashUtil.success(request, "Thêm căn hộ thành công. Mã: "
                    + formatApartmentIdentity(form) + ".");
            if (newId > 0) {
                response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + newId);
            } else {
                response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            }
        } else {
            errors.add("Không thể thêm căn hộ. Vui lòng thử lại.");
            form.setApartmentCode(null);
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
     * UC-APT-06: form gán / đổi chủ sở hữu.
     */
    private void handleAssignOwnerForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền gán chủ sở hữu.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Integer apartmentId = parseId(request.getParameter("id"));
        if (apartmentId == null) {
            apartmentId = parseId(request.getParameter("apartmentId"));
        }
        if (apartmentId == null) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment apartment = apartmentDAO.findById(apartmentId);
        if (apartment == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        ApartmentResident currentOwner = apartmentResidentDAO.findCurrentOwner(apartmentId);
        List<User> candidates = userDAO.findActiveUsers();

        FlashUtil.moveToRequest(request);
        request.setAttribute("apartment", apartment);
        request.setAttribute("currentOwner", currentOwner);
        request.setAttribute("candidateUsers", candidates);
        request.setAttribute("householdMembers", householdMemberDAO.findByApartmentId(apartmentId));
        if (request.getAttribute("personSource") == null) {
            request.setAttribute("personSource", "existing");
        }
        if (request.getAttribute("startDate") == null) {
            request.setAttribute("startDate", LocalDate.now().toString());
        }
        request.setAttribute("pageTitle", "Gán chủ sở hữu · " + apartment.getApartmentCode());
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/assign-owner.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    /**
     * UC-APT-06: submit gán / đổi owner.
     * BR: tối đa 1 owner hiện tại; owner cũ is_current=0 + end_date; owner mới insert current.
     */
    private void handleAssignOwner(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User actor = requireUser(request, response);
        if (actor == null) {
            return;
        }
        if (!canManage(actor.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền gán chủ sở hữu.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        Integer apartmentId = parseId(request.getParameter("apartmentId"));
        String personSource = trim(request.getParameter("personSource"));
        if (personSource == null || personSource.isEmpty()) {
            personSource = "existing";
        }
        Integer userId = parseId(request.getParameter("userId"));
        String startRaw = trim(request.getParameter("startDate"));
        String newFullName = trim(request.getParameter("newFullName"));
        String newPhone = trim(request.getParameter("newPhone"));
        String newEmail = trim(request.getParameter("newEmail"));
        String newUsername = trim(request.getParameter("newUsername"));

        List<String> errors = new ArrayList<>();
        Apartment apartment = null;

        if (apartmentId == null) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }
        apartment = apartmentDAO.findById(apartmentId);
        if (apartment == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Date startDate = null;
        if (startRaw == null || startRaw.isEmpty()) {
            startDate = Date.valueOf(LocalDate.now());
        } else {
            try {
                startDate = Date.valueOf(LocalDate.parse(startRaw));
            } catch (Exception e) {
                errors.add("Ngày bắt đầu không hợp lệ.");
            }
        }

        User ownerUser = null;
        boolean createdNewUser = false;
        if ("new".equalsIgnoreCase(personSource)) {
            if (newFullName == null || newFullName.isEmpty()) {
                errors.add("Vui lòng nhập họ tên người mới.");
            } else if (newFullName.length() < 2 || newFullName.length() > 100) {
                errors.add("Họ tên phải từ 2 đến 100 ký tự.");
            }
            if (newPhone != null && !newPhone.isEmpty() && !PHONE_PATTERN.matcher(newPhone).matches()) {
                errors.add("Số điện thoại không hợp lệ.");
            }
            if (errors.isEmpty()) {
                int newUid = userDAO.createResidentQuick(newFullName, newPhone, newEmail, newUsername);
                if (newUid <= 0) {
                    errors.add("Không thể tạo user mới (username trùng hoặc lỗi DB). Thử username khác.");
                } else {
                    userId = newUid;
                    ownerUser = userDAO.findById(newUid);
                    createdNewUser = true;
                }
            }
        } else {
            if (userId == null) {
                errors.add("Vui lòng chọn chủ sở hữu hoặc chọn Người mới.");
            } else {
                ownerUser = userDAO.findById(userId);
                if (ownerUser == null || ownerUser.getIsActive() == null || !ownerUser.getIsActive()) {
                    errors.add("User không hợp lệ hoặc đã bị khóa.");
                }
            }
        }

        ApartmentResident currentOwner = apartmentResidentDAO.findCurrentOwner(apartmentId);
        if (errors.isEmpty() && currentOwner != null && userId != null
                && userId.equals(currentOwner.getUserId())) {
            errors.add("User này đã là chủ sở hữu hiện tại của căn.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("selectedUserId", userId);
            request.setAttribute("personSource", personSource);
            request.setAttribute("newFullName", newFullName == null ? "" : newFullName);
            request.setAttribute("newPhone", newPhone == null ? "" : newPhone);
            request.setAttribute("newEmail", newEmail == null ? "" : newEmail);
            request.setAttribute("newUsername", newUsername == null ? "" : newUsername);
            request.setAttribute("startDate", startRaw != null ? startRaw : LocalDate.now().toString());
            request.setAttribute("apartment", apartment);
            request.setAttribute("currentOwner", currentOwner);
            request.setAttribute("candidateUsers", userDAO.findActiveUsers());
            request.setAttribute("householdMembers", householdMemberDAO.findByApartmentId(apartmentId));
            request.setAttribute("pageTitle", "Gán chủ sở hữu · " + apartment.getApartmentCode());
            request.setAttribute("contentPage", "/WEB-INF/views/apartment/assign-owner.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
            return;
        }

        boolean isChange = currentOwner != null;
        if (isChange) {
            int ended = apartmentResidentDAO.endCurrentOwners(apartmentId, startDate);
            if (ended < 0) {
                String detail = apartmentResidentDAO.getLastError();
                FlashUtil.error(request, detail != null && !detail.isEmpty()
                        ? detail
                        : "Không thể gán chủ sở hữu. Vui lòng thử lại.");
                response.sendRedirect(request.getContextPath()
                        + "/apartment?action=assign-owner&id=" + apartmentId);
                return;
            }
            // Không soft-delete toàn bộ TV — chỉ sync dòng Chủ hộ bên dưới
        }

        int newId = apartmentResidentDAO.insertOwner(apartmentId, userId, startDate);
        if (newId < 0) {
            String detail = apartmentResidentDAO.getLastError();
            FlashUtil.error(request, detail != null && !detail.isEmpty()
                    ? detail
                    : "Không thể gán chủ sở hữu. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath()
                    + "/apartment?action=assign-owner&id=" + apartmentId);
            return;
        }

        String ownerName = ownerUser.getFullName() != null ? ownerUser.getFullName() : ownerUser.getUsername();
        // Sync TV "Chủ hộ": nếu đã có trong thành viên hộ (trùng tên) → không thêm mới
        if (isChange && currentOwner != null) {
            String oldName = currentOwner.getFullName() != null
                    ? currentOwner.getFullName() : currentOwner.getUsername();
            // Chỉ gỡ dòng "Chủ hộ" của owner cũ nếu khác tên owner mới
            if (oldName != null && !oldName.trim().isEmpty()
                    && (ownerName == null || !oldName.trim().equalsIgnoreCase(ownerName.trim()))) {
                householdMemberDAO.hardDeleteByNameAndRelationship(apartmentId, oldName, "Chủ hộ");
            }
        }
        boolean alreadyInHousehold = householdMemberDAO.existsActiveByFullName(apartmentId, ownerName);
        int memberSync = -2;
        if (alreadyInHousehold) {
            memberSync = 0; // đã có → skip insert
        } else {
            memberSync = householdMemberDAO.ensureActiveMember(
                    apartmentId, ownerName, "Chủ hộ", ownerUser.getPhone());
        }

        String histAction = isChange ? "CHANGE_OWNER" : "ASSIGN_OWNER";
        String note = isChange
                ? "Đổi owner → " + ownerName + " (userId=" + userId + ")"
                : "Gán owner lần đầu → " + ownerName + " (userId=" + userId + ")";
        if (alreadyInHousehold || memberSync == 0) {
            note += " | TV: đã có trong thành viên hộ, không thêm mới";
        } else if (memberSync > 0) {
            note += " | TV: thêm dòng Chủ hộ";
        }
        boolean histOk = writeHistory(actor, apartment, histAction, "OWNER", "OWNER", note);
        audit(actor, histAction, apartment, "OWNER", "OWNER", "SUCCESS", note);

        String okMsg = isChange ? "Đổi chủ sở hữu thành công." : "Gán chủ sở hữu thành công.";
        if (createdNewUser) {
            okMsg += " Đã tạo user RESIDENT @" + ownerUser.getUsername() + " (pass 123456).";
        }
        if (alreadyInHousehold || memberSync == 0) {
            okMsg += " \"" + ownerName + "\" đã có trong thành viên hộ — không thêm mới.";
        } else if (memberSync > 0) {
            okMsg += " Đã thêm \"" + ownerName + "\" vào thành viên hộ (Chủ hộ).";
        }
        if (!histOk) {
            String he = apartmentHistoryDAO.getLastError();
            okMsg += " (Cảnh báo: chưa ghi được lịch sử"
                    + (he != null && !he.isEmpty() ? " — " + he : "") + ")";
        }
        FlashUtil.success(request, okMsg);
        response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
    }

    /**
     * Chỉ gỡ chủ sở hữu (end current OWNER).
     * Thành viên hộ gỡ riêng bằng nút Xóa ở bảng Thành viên hộ — không xóa cùng lúc.
     */
    private void handleRemoveOwner(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User actor = requireUser(request, response);
        if (actor == null) {
            return;
        }
        if (!canManage(actor.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền gỡ chủ sở hữu.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        Integer apartmentId = parseId(request.getParameter("apartmentId"));
        if (apartmentId == null) {
            apartmentId = parseId(request.getParameter("id"));
        }
        if (apartmentId == null) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment apartment = apartmentDAO.findById(apartmentId);
        if (apartment == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        ApartmentResident currentOwner = apartmentResidentDAO.findCurrentOwner(apartmentId);
        if (currentOwner == null) {
            FlashUtil.error(request, "Căn hộ chưa có chủ sở hữu để gỡ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
            return;
        }

        // Xóa hẳn row OWNER current — tránh UNIQUE (apt,user,OWNER,0) khi đã có lịch sử
        int deleted = apartmentResidentDAO.deleteCurrentOwners(apartmentId);
        if (deleted < 0) {
            String detail = apartmentResidentDAO.getLastError();
            FlashUtil.error(request, detail != null && !detail.isEmpty()
                    ? detail
                    : "Không thể gỡ chủ sở hữu. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
            return;
        }
        if (deleted == 0) {
            FlashUtil.error(request, "Căn hộ chưa có chủ sở hữu để gỡ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
            return;
        }

        String ownerName = currentOwner.getFullName() != null
                ? currentOwner.getFullName()
                : currentOwner.getUsername();
        String note = "Gỡ owner → " + ownerName + " (userId=" + currentOwner.getUserId() + ")";
        boolean histOk = writeHistory(actor, apartment, "REMOVE_OWNER", "OWNER", "NONE", note);
        audit(actor, "REMOVE_OWNER", apartment, "OWNER", "NONE", "SUCCESS", note);

        String okMsg = "Đã gỡ chủ sở hữu. Thành viên hộ vẫn giữ — gỡ riêng ở bảng Thành viên hộ nếu cần.";
        if (!histOk) {
            String he = apartmentHistoryDAO.getLastError();
            okMsg += " (Cảnh báo: chưa ghi được lịch sử"
                    + (he != null && !he.isEmpty() ? " — " + he : "") + ")";
        }
        FlashUtil.success(request, okMsg);
        response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
    }

    /**
     * UC-APT-07: form gán người thuê / đại diện thuê.
     */
    private void handleAssignTenantForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền gán người thuê.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Integer apartmentId = parseId(request.getParameter("id"));
        if (apartmentId == null) {
            apartmentId = parseId(request.getParameter("apartmentId"));
        }
        if (apartmentId == null) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment apartment = apartmentDAO.findById(apartmentId);
        if (apartment == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        forwardAssignTenantForm(request, response, apartment, null, null, null, null, null);
    }

    /**
     * UC-APT-07: submit gán TENANT_REP / TENANT.
     * TENANT_REP: max 1 current — end old then insert.
     * TENANT: cho phép nhiều current.
     */
    private void handleAssignTenant(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User actor = requireUser(request, response);
        if (actor == null) {
            return;
        }
        if (!canManage(actor.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền gán người thuê.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        Integer apartmentId = parseId(request.getParameter("apartmentId"));
        String personSource = trim(request.getParameter("personSource"));
        if (personSource == null || personSource.isEmpty()) {
            personSource = "existing";
        }
        Integer userId = parseId(request.getParameter("userId"));
        String roleInApartment = trim(request.getParameter("roleInApartment"));
        String startRaw = trim(request.getParameter("startDate"));
        String endRaw = trim(request.getParameter("endDate"));
        String newFullName = trim(request.getParameter("newFullName"));
        String newPhone = trim(request.getParameter("newPhone"));
        String newEmail = trim(request.getParameter("newEmail"));
        String newUsername = trim(request.getParameter("newUsername"));

        if (apartmentId == null) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }
        Apartment apartment = apartmentDAO.findById(apartmentId);
        if (apartment == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        List<String> errors = new ArrayList<>();
        boolean createdNewUser = false;

        if (roleInApartment == null
                || !(AppConstants.APT_ROLE_TENANT_REP.equals(roleInApartment)
                || AppConstants.APT_ROLE_TENANT.equals(roleInApartment))) {
            errors.add("Vai trò thuê không hợp lệ (TENANT_REP / TENANT).");
        }

        Date startDate = null;
        if (startRaw == null || startRaw.isEmpty()) {
            startDate = Date.valueOf(LocalDate.now());
            startRaw = LocalDate.now().toString();
        } else {
            try {
                startDate = Date.valueOf(LocalDate.parse(startRaw));
            } catch (Exception e) {
                errors.add("Ngày bắt đầu không hợp lệ.");
            }
        }

        Date endDate = null;
        if (endRaw != null && !endRaw.isEmpty()) {
            try {
                endDate = Date.valueOf(LocalDate.parse(endRaw));
            } catch (Exception e) {
                errors.add("Ngày kết thúc không hợp lệ.");
            }
        }

        if (startDate != null && endDate != null && endDate.before(startDate)) {
            errors.add("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.");
        }
        if (endDate != null && endDate.before(Date.valueOf(LocalDate.now()))) {
            errors.add("Ngày kết thúc không được trước hôm nay khi gán mới.");
        }

        User tenantUser = null;
        if ("new".equalsIgnoreCase(personSource)) {
            if (newFullName == null || newFullName.isEmpty()) {
                errors.add("Vui lòng nhập họ tên người mới.");
            } else if (newFullName.length() < 2 || newFullName.length() > 100) {
                errors.add("Họ tên phải từ 2 đến 100 ký tự.");
            }
            if (newPhone != null && !newPhone.isEmpty() && !PHONE_PATTERN.matcher(newPhone).matches()) {
                errors.add("Số điện thoại không hợp lệ.");
            }
            if (errors.isEmpty()) {
                int newUid = userDAO.createResidentQuick(newFullName, newPhone, newEmail, newUsername);
                if (newUid <= 0) {
                    errors.add("Không thể tạo user mới (username trùng hoặc lỗi DB). Thử username khác.");
                } else {
                    userId = newUid;
                    tenantUser = userDAO.findById(newUid);
                    createdNewUser = true;
                }
            }
        } else {
            if (userId == null) {
                errors.add("Vui lòng chọn người thuê hoặc chọn Người mới.");
            } else {
                tenantUser = userDAO.findById(userId);
                if (tenantUser == null || tenantUser.getIsActive() == null || !tenantUser.getIsActive()) {
                    errors.add("User không hợp lệ hoặc đã bị khóa.");
                }
            }
        }

        if (errors.isEmpty() && userId != null && roleInApartment != null
                && apartmentResidentDAO.isCurrentWithRole(apartmentId, userId, roleInApartment)) {
            errors.add("User này đã là người thuê/đại diện hiện tại với vai trò đã chọn.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("personSource", personSource);
            request.setAttribute("newFullName", newFullName == null ? "" : newFullName);
            request.setAttribute("newPhone", newPhone == null ? "" : newPhone);
            request.setAttribute("newEmail", newEmail == null ? "" : newEmail);
            request.setAttribute("newUsername", newUsername == null ? "" : newUsername);
            forwardAssignTenantForm(request, response, apartment, errors, userId,
                    roleInApartment, startRaw, endRaw);
            return;
        }

        boolean changeRep = false;
        if (AppConstants.APT_ROLE_TENANT_REP.equals(roleInApartment)) {
            ApartmentResident currentRep = apartmentResidentDAO.findCurrentTenantRep(apartmentId);
            if (currentRep != null && !userId.equals(currentRep.getUserId())) {
                changeRep = true;
                int ended = apartmentResidentDAO.endCurrentTenantReps(apartmentId, startDate);
                if (ended < 0) {
                    String detail = apartmentResidentDAO.getLastError();
                    FlashUtil.error(request, detail != null && !detail.isEmpty()
                            ? detail
                            : "Không thể gán người thuê. Vui lòng thử lại.");
                    response.sendRedirect(request.getContextPath()
                            + "/apartment?action=assign-tenant&id=" + apartmentId);
                    return;
                }
            }
        }

        int newId = apartmentResidentDAO.insertTenant(
                apartmentId, userId, roleInApartment, startDate, endDate);
        if (newId < 0) {
            String detail = apartmentResidentDAO.getLastError();
            FlashUtil.error(request, detail != null && !detail.isEmpty()
                    ? detail
                    : "Không thể gán người thuê. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath()
                    + "/apartment?action=assign-tenant&id=" + apartmentId);
            return;
        }

        // Không auto đồng bộ household_members — thành viên hộ quản lý riêng
        String tenantName = tenantUser.getFullName() != null ? tenantUser.getFullName() : tenantUser.getUsername();

        String histAction = changeRep ? "CHANGE_TENANT_REP" : "ASSIGN_TENANT";
        if (!changeRep && AppConstants.APT_ROLE_TENANT_REP.equals(roleInApartment)) {
            histAction = "ASSIGN_TENANT_REP";
        }
        String note = roleInApartment + " → " + tenantName + " (userId=" + userId + ")"
                + " | start=" + startDate
                + (endDate != null ? " | end=" + endDate : " | end=mở");
        boolean histOk = writeHistory(actor, apartment, histAction, roleInApartment, roleInApartment, note);
        audit(actor, histAction, apartment, roleInApartment, roleInApartment, "SUCCESS", note);

        String okMsg = changeRep
                ? "Đổi đại diện thuê thành công."
                : "Gán người thuê thành công.";
        if (createdNewUser && tenantUser != null) {
            okMsg += " Đã tạo user RESIDENT @" + tenantUser.getUsername() + " (pass 123456).";
        }
        if (!histOk) {
            String he = apartmentHistoryDAO.getLastError();
            okMsg += " (Cảnh báo: chưa ghi được lịch sử"
                    + (he != null && !he.isEmpty() ? " — " + he : "") + ")";
        }
        FlashUtil.success(request, okMsg);
        response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
    }

    /**
     * Gỡ toàn bộ người thuê / đại diện thuê hiện tại (giống gỡ owner).
     * Không đụng thành viên hộ.
     */
    private void handleRemoveTenant(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User actor = requireUser(request, response);
        if (actor == null) {
            return;
        }
        if (!canManage(actor.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền gỡ người thuê.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        Integer apartmentId = parseId(request.getParameter("apartmentId"));
        if (apartmentId == null) {
            apartmentId = parseId(request.getParameter("id"));
        }
        if (apartmentId == null) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment apartment = apartmentDAO.findById(apartmentId);
        if (apartment == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        List<ApartmentResident> currentTenants = apartmentResidentDAO.findByApartmentAndRoles(
                apartmentId, AppConstants.APT_ROLE_TENANT_REP, AppConstants.APT_ROLE_TENANT);
        if (currentTenants == null || currentTenants.isEmpty()) {
            FlashUtil.error(request, "Căn hộ chưa có người thuê để gỡ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
            return;
        }

        int deleted = apartmentResidentDAO.deleteCurrentTenants(apartmentId);
        if (deleted < 0) {
            String detail = apartmentResidentDAO.getLastError();
            FlashUtil.error(request, detail != null && !detail.isEmpty()
                    ? detail
                    : "Không thể gỡ người thuê. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
            return;
        }
        if (deleted == 0) {
            FlashUtil.error(request, "Căn hộ chưa có người thuê để gỡ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
            return;
        }

        StringBuilder names = new StringBuilder();
        for (int i = 0; i < currentTenants.size(); i++) {
            ApartmentResident t = currentTenants.get(i);
            String n = t.getFullName() != null ? t.getFullName() : t.getUsername();
            if (i > 0) {
                names.append(", ");
            }
            names.append(n).append(" (").append(t.getRoleInApartment()).append(")");
        }
        String note = "Gỡ người thuê → " + names + " (xóa " + deleted + " bản ghi)";
        boolean histOk = writeHistory(actor, apartment, "REMOVE_TENANT", "TENANT", "NONE", note);
        audit(actor, "REMOVE_TENANT", apartment, "TENANT", "NONE", "SUCCESS", note);

        String okMsg = "Đã gỡ người thuê / đại diện thuê. Thành viên hộ không bị xóa.";
        if (!histOk) {
            String he = apartmentHistoryDAO.getLastError();
            okMsg += " (Cảnh báo: chưa ghi được lịch sử"
                    + (he != null && !he.isEmpty() ? " — " + he : "") + ")";
        }
        FlashUtil.success(request, okMsg);
        response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
    }

    private void forwardAssignTenantForm(HttpServletRequest request, HttpServletResponse response,
            Apartment apartment, List<String> errors, Integer selectedUserId,
            String selectedRole, String startDate, String endDate)
            throws ServletException, IOException {
        int apartmentId = apartment.getApartmentId();
        if (errors != null) {
            request.setAttribute("errors", errors);
        }
        request.setAttribute("apartment", apartment);
        request.setAttribute("currentTenantRep", apartmentResidentDAO.findCurrentTenantRep(apartmentId));
        request.setAttribute("currentTenants",
                apartmentResidentDAO.findByApartmentAndRoles(apartmentId, AppConstants.APT_ROLE_TENANT));
        request.setAttribute("candidateUsers", userDAO.findActiveUsers());
        request.setAttribute("householdMembers", householdMemberDAO.findByApartmentId(apartmentId));
        if (request.getAttribute("personSource") == null) {
            request.setAttribute("personSource", "existing");
        }
        request.setAttribute("selectedUserId", selectedUserId);
        request.setAttribute("selectedRole", selectedRole);
        request.setAttribute("startDate",
                startDate != null ? startDate : LocalDate.now().toString());
        request.setAttribute("endDate", endDate == null ? "" : endDate);
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Gán người thuê · " + apartment.getApartmentCode());
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/assign-tenant.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    /**
     * UC-APT-08: form thêm thành viên sinh sống.
     */
    private void handleAddMemberForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền thêm thành viên.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Integer apartmentId = parseId(request.getParameter("id"));
        if (apartmentId == null) {
            apartmentId = parseId(request.getParameter("apartmentId"));
        }
        if (apartmentId == null) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment apartment = apartmentDAO.findById(apartmentId);
        if (apartment == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        if (request.getAttribute("form") == null) {
            request.setAttribute("form", HouseholdMember.builder().build());
        }
        forwardAddMemberForm(request, response, apartment, null);
    }

    /**
     * UC-APT-08: submit thêm thành viên (fullName, relationship, CCCD, phone, DOB).
     */
    private void handleAddMember(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User actor = requireUser(request, response);
        if (actor == null) {
            return;
        }
        if (!canManage(actor.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền thêm thành viên.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        Integer apartmentId = parseId(request.getParameter("apartmentId"));
        if (apartmentId == null) {
            FlashUtil.error(request, "ID căn hộ không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }
        Apartment apartment = apartmentDAO.findById(apartmentId);
        if (apartment == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        String fullName = trim(request.getParameter("fullName"));
        String relationship = trim(request.getParameter("relationship"));
        String idNumber = trim(request.getParameter("idNumber"));
        String phone = trim(request.getParameter("phone"));
        String dobRaw = trim(request.getParameter("dateOfBirth"));

        List<String> errors = new ArrayList<>();
        Date dob = null;

        if (fullName == null || fullName.isEmpty()) {
            errors.add("Vui lòng nhập họ tên.");
        } else if (fullName.length() < 2 || fullName.length() > 100) {
            errors.add("Họ tên phải từ 2 đến 100 ký tự.");
        }

        if (relationship == null || relationship.isEmpty()) {
            errors.add("Vui lòng chọn quan hệ.");
        } else if (relationship.length() > 50) {
            errors.add("Quan hệ tối đa 50 ký tự.");
        }

        if (idNumber != null && !idNumber.isEmpty() && !CCCD_PATTERN.matcher(idNumber).matches()) {
            errors.add("CCCD/CMND phải gồm 9–12 chữ số.");
        }

        if (phone != null && !phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            errors.add("Số điện thoại không hợp lệ.");
        }

        if (dobRaw != null && !dobRaw.isEmpty()) {
            try {
                LocalDate ld = LocalDate.parse(dobRaw);
                if (ld.isAfter(LocalDate.now())) {
                    errors.add("Ngày sinh không được ở tương lai.");
                } else {
                    dob = Date.valueOf(ld);
                }
            } catch (Exception e) {
                errors.add("Ngày sinh không hợp lệ.");
            }
        }

        if (errors.isEmpty() && idNumber != null && !idNumber.isEmpty()
                && householdMemberDAO.existsActiveIdNumber(apartmentId, idNumber)) {
            errors.add("CCCD đã tồn tại trên căn này.");
        }

        HouseholdMember form = HouseholdMember.builder()
                .apartmentId(apartmentId)
                .fullName(fullName)
                .relationship(relationship)
                .idNumber(idNumber)
                .phone(phone)
                .dateOfBirth(dob)
                .isActive(true)
                .build();

        if (!errors.isEmpty()) {
            request.setAttribute("form", form);
            request.setAttribute("dobValue", dobRaw == null ? "" : dobRaw);
            forwardAddMemberForm(request, response, apartment, errors);
            return;
        }

        int newId = householdMemberDAO.insert(form);
        if (newId < 0) {
            errors.add("Không thể thêm thành viên. Vui lòng thử lại.");
            request.setAttribute("form", form);
            request.setAttribute("dobValue", dobRaw == null ? "" : dobRaw);
            forwardAddMemberForm(request, response, apartment, errors);
            return;
        }

        writeHistory(actor, apartment, "ADD_MEMBER", null, null,
                "Thêm TV: " + fullName + " (" + relationship + ")");
        FlashUtil.success(request, "Thêm thành viên thành công.");
        response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
    }

    private void forwardAddMemberForm(HttpServletRequest request, HttpServletResponse response,
            Apartment apartment, List<String> errors) throws ServletException, IOException {
        if (errors != null) {
            request.setAttribute("errors", errors);
        }
        request.setAttribute("apartment", apartment);
        request.setAttribute("relationshipOptions", RELATIONSHIP_OPTIONS);
        if (request.getAttribute("form") == null) {
            request.setAttribute("form", HouseholdMember.builder().build());
        }
        if (request.getAttribute("dobValue") == null) {
            request.setAttribute("dobValue", "");
        }
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Thêm thành viên · " + apartment.getApartmentCode());
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/add-member.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    
    private void handleEditMemberForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền cập nhật thành viên.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Integer apartmentId = parseId(request.getParameter("apartmentId"));
        if (apartmentId == null) {
            apartmentId = parseId(request.getParameter("id"));
        }
        Integer memberId = parseId(request.getParameter("memberId"));
        if (apartmentId == null || memberId == null) {
            FlashUtil.error(request, "Tham số thành viên không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment apartment = apartmentDAO.findById(apartmentId);
        if (apartment == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        if (request.getAttribute("form") == null) {
            HouseholdMember member = householdMemberDAO.findById(memberId);
            if (member == null) {
                FlashUtil.error(request, "Không tìm thấy thành viên.");
                response.sendRedirect(request.getContextPath()
                        + "/apartment?action=detail&id=" + apartmentId);
                return;
            }
            if (!apartmentId.equals(member.getApartmentId())) {
                FlashUtil.error(request, "Thành viên không thuộc căn này.");
                response.sendRedirect(request.getContextPath()
                        + "/apartment?action=detail&id=" + apartmentId);
                return;
            }
            request.setAttribute("form", member);
            request.setAttribute("dobValue",
                    member.getDateOfBirth() == null ? "" : member.getDateOfBirth().toString());
        }
        forwardEditMemberForm(request, response, apartment, null);
    }

    
    private void handleEditMember(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User actor = requireUser(request, response);
        if (actor == null) {
            return;
        }
        if (!canManage(actor.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền cập nhật thành viên.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        Integer apartmentId = parseId(request.getParameter("apartmentId"));
        Integer memberId = parseId(request.getParameter("memberId"));
        if (apartmentId == null || memberId == null) {
            FlashUtil.error(request, "Tham số thành viên không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment apartment = apartmentDAO.findById(apartmentId);
        if (apartment == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        HouseholdMember existing = householdMemberDAO.findById(memberId);
        if (existing == null) {
            FlashUtil.error(request, "Không tìm thấy thành viên.");
            response.sendRedirect(request.getContextPath()
                    + "/apartment?action=detail&id=" + apartmentId);
            return;
        }
        if (!apartmentId.equals(existing.getApartmentId())) {
            FlashUtil.error(request, "Thành viên không thuộc căn này.");
            response.sendRedirect(request.getContextPath()
                    + "/apartment?action=detail&id=" + apartmentId);
            return;
        }

        String fullName = trim(request.getParameter("fullName"));
        String relationship = trim(request.getParameter("relationship"));
        String idNumber = trim(request.getParameter("idNumber"));
        String phone = trim(request.getParameter("phone"));
        String dobRaw = trim(request.getParameter("dateOfBirth"));

        List<String> errors = new ArrayList<>();
        Date dob = null;

        if (fullName == null || fullName.isEmpty()) {
            errors.add("Vui lòng nhập họ tên.");
        } else if (fullName.length() < 2 || fullName.length() > 100) {
            errors.add("Họ tên phải từ 2 đến 100 ký tự.");
        }
        if (relationship == null || relationship.isEmpty()) {
            errors.add("Vui lòng chọn quan hệ.");
        } else if (relationship.length() > 50) {
            errors.add("Quan hệ tối đa 50 ký tự.");
        }
        if (idNumber != null && !idNumber.isEmpty() && !CCCD_PATTERN.matcher(idNumber).matches()) {
            errors.add("CCCD/CMND phải gồm 9–12 chữ số.");
        }
        if (phone != null && !phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            errors.add("Số điện thoại không hợp lệ.");
        }
        if (dobRaw != null && !dobRaw.isEmpty()) {
            try {
                LocalDate ld = LocalDate.parse(dobRaw);
                if (ld.isAfter(LocalDate.now())) {
                    errors.add("Ngày sinh không được ở tương lai.");
                } else {
                    dob = Date.valueOf(ld);
                }
            } catch (Exception e) {
                errors.add("Ngày sinh không hợp lệ.");
            }
        }
        if (errors.isEmpty() && idNumber != null && !idNumber.isEmpty()
                && householdMemberDAO.existsActiveIdNumberExceptId(apartmentId, idNumber, memberId)) {
            errors.add("CCCD đã tồn tại trên căn này.");
        }

        HouseholdMember form = HouseholdMember.builder()
                .memberId(memberId)
                .apartmentId(apartmentId)
                .fullName(fullName)
                .relationship(relationship)
                .idNumber(idNumber)
                .phone(phone)
                .dateOfBirth(dob)
                .isActive(existing.getIsActive())
                .build();

        if (!errors.isEmpty()) {
            request.setAttribute("form", form);
            request.setAttribute("dobValue", dobRaw == null ? "" : dobRaw);
            forwardEditMemberForm(request, response, apartment, errors);
            return;
        }

        if (!householdMemberDAO.update(form)) {
            errors.add("Không thể thực hiện. Vui lòng thử lại.");
            request.setAttribute("form", form);
            request.setAttribute("dobValue", dobRaw == null ? "" : dobRaw);
            forwardEditMemberForm(request, response, apartment, errors);
            return;
        }

        writeHistory(actor, apartment, "UPDATE_MEMBER", null, null,
                "Sửa TV#" + memberId + ": " + fullName);
        audit(actor, "UPDATE_MEMBER", apartment, null, null, "SUCCESS",
                "memberId=" + memberId + " name=" + fullName);
        FlashUtil.success(request, "Cập nhật thành viên thành công.");
        response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
    }

    
    /**
     * UC-APT-09: Xóa thành viên hộ.
     * Nếu TV đó cũng là chủ sở hữu hiện tại (quan hệ Chủ hộ / trùng tên owner)
     * → gỡ luôn gán OWNER trong apartment_residents.
     */
    private void handleRemoveMember(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User actor = requireUser(request, response);
        if (actor == null) {
            return;
        }
        if (!canManage(actor.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền gỡ thành viên.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        Integer apartmentId = parseId(request.getParameter("apartmentId"));
        Integer memberId = parseId(request.getParameter("memberId"));
        if (apartmentId == null || memberId == null) {
            FlashUtil.error(request, "Tham số thành viên không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment apartment = apartmentDAO.findById(apartmentId);
        if (apartment == null) {
            FlashUtil.error(request, "Không tìm thấy căn hộ.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        HouseholdMember existing = householdMemberDAO.findById(memberId);
        if (existing == null) {
            FlashUtil.error(request, "Không tìm thấy thành viên.");
            response.sendRedirect(request.getContextPath()
                    + "/apartment?action=detail&id=" + apartmentId);
            return;
        }
        if (!apartmentId.equals(existing.getApartmentId())) {
            FlashUtil.error(request, "Thành viên không thuộc căn này.");
            response.sendRedirect(request.getContextPath()
                    + "/apartment?action=detail&id=" + apartmentId);
            return;
        }

        ApartmentResident currentOwner = apartmentResidentDAO.findCurrentOwner(apartmentId);
        boolean alsoOwner = isSamePersonAsOwner(existing, currentOwner);

        String removedName = existing.getFullName();
        String rel = existing.getRelationship() == null ? "" : existing.getRelationship();

        if (!householdMemberDAO.hardDelete(memberId, apartmentId)) {
            audit(actor, "REMOVE_MEMBER", apartment, null, null, "ERROR", "hardDelete failed");
            FlashUtil.error(request, "Không thể xóa thành viên. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
            return;
        }

        boolean ownerRemoved = false;
        if (alsoOwner && currentOwner != null) {
            int deleted = apartmentResidentDAO.deleteCurrentOwners(apartmentId);
            if (deleted > 0) {
                ownerRemoved = true;
                String ownerLabel = currentOwner.getFullName() != null
                        ? currentOwner.getFullName() : currentOwner.getUsername();
                writeHistory(actor, apartment, "REMOVE_OWNER", "OWNER", "NONE",
                        "Gỡ owner theo xóa TV \"" + removedName + "\" → " + ownerLabel
                        + " (userId=" + currentOwner.getUserId() + ")");
                audit(actor, "REMOVE_OWNER", apartment, "OWNER", "NONE", "SUCCESS",
                        "cascade from REMOVE_MEMBER memberId=" + memberId);
            } else if (deleted < 0) {
                System.out.println("WARN handleRemoveMember: xóa TV OK nhưng gỡ owner fail apartmentId="
                        + apartmentId + " err=" + apartmentResidentDAO.getLastError());
            }
        }

        writeHistory(actor, apartment, "REMOVE_MEMBER", null, null,
                "Xóa TV#" + memberId + ": " + removedName
                + (rel.isEmpty() ? "" : " (" + rel + ")")
                + (ownerRemoved ? " + gỡ OWNER" : ""));
        audit(actor, "REMOVE_MEMBER", apartment, "ACTIVE", "DELETED", "SUCCESS",
                "memberId=" + memberId + (ownerRemoved ? " + REMOVE_OWNER" : ""));

        if (ownerRemoved) {
            FlashUtil.success(request, "Đã xóa thành viên \"" + removedName
                    + "\" và gỡ luôn vai trò chủ sở hữu của người này.");
        } else {
            FlashUtil.success(request, "Đã xóa thành viên \"" + removedName + "\" khỏi danh sách hộ.");
        }
        response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
    }

    /**
     * TV trùng chủ sở hữu hiện tại: quan hệ "Chủ hộ" hoặc trùng fullName/username owner.
     */
    private boolean isSamePersonAsOwner(HouseholdMember member, ApartmentResident owner) {
        if (member == null || owner == null) {
            return false;
        }
        String rel = member.getRelationship() == null ? "" : member.getRelationship().trim();
        if ("Chủ hộ".equalsIgnoreCase(rel)) {
            return true;
        }
        String memberName = member.getFullName() == null ? "" : member.getFullName().trim();
        if (memberName.isEmpty()) {
            return false;
        }
        String ownerFull = owner.getFullName() == null ? "" : owner.getFullName().trim();
        String ownerUser = owner.getUsername() == null ? "" : owner.getUsername().trim();
        return memberName.equalsIgnoreCase(ownerFull) || memberName.equalsIgnoreCase(ownerUser);
    }

    private void forwardEditMemberForm(HttpServletRequest request, HttpServletResponse response,
            Apartment apartment, List<String> errors) throws ServletException, IOException {
        if (errors != null) {
            request.setAttribute("errors", errors);
        }
        request.setAttribute("apartment", apartment);
        request.setAttribute("relationshipOptions", RELATIONSHIP_OPTIONS);
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Cập nhật thành viên · " + apartment.getApartmentCode());
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/edit-member.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    
    private void handleMembers(HttpServletRequest request, HttpServletResponse response)
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
        String relationship = trim(request.getParameter("relationship"));
        String statusFilter = trim(request.getParameter("status"));
        String building = trim(request.getParameter("building"));

        int page = 1;
        try {
            page = Integer.parseInt(request.getParameter("page"));
        } catch (Exception ignored) {
        }
        if (page < 1) {
            page = 1;
        }
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;

        int totalItems = householdMemberDAO.countMembersWithFilters(
                keyword, relationship, statusFilter, building);
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil(totalItems * 1.0 / pageSize);
        if (page > totalPages) {
            page = totalPages;
        }

        List<HouseholdMember> members = householdMemberDAO.findMembersWithFilters(
                keyword, relationship, statusFilter, building, page, pageSize);

        boolean hasFilter = (keyword != null && !keyword.isEmpty())
                || (relationship != null && !relationship.isEmpty())
                || (statusFilter != null && !statusFilter.isEmpty())
                || (building != null && !building.isEmpty());

        int fromIndex = totalItems == 0 ? 0 : (page - 1) * pageSize + 1;
        int toIndex = Math.min(page * pageSize, totalItems);

        FlashUtil.moveToRequest(request);
        request.setAttribute("members", members);
        request.setAttribute("canManage", canManage(user.getRole()));
        request.setAttribute("keyword", keyword == null ? "" : keyword);
        request.setAttribute("relationshipFilter", relationship == null ? "" : relationship);
        request.setAttribute("statusFilter", statusFilter == null ? "" : statusFilter);
        request.setAttribute("buildingFilter", building == null ? "" : building);
        request.setAttribute("relationshipOptions", RELATIONSHIP_OPTIONS);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("fromIndex", fromIndex);
        request.setAttribute("toIndex", toIndex);
        request.setAttribute("hasFilter", hasFilter);
        request.setAttribute("pageTitle", "Danh sách thành viên");
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/members.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    
    private void handleExportMembers(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền xuất Excel danh sách thành viên.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=members");
            return;
        }

        String keyword = trim(request.getParameter("keyword"));
        String relationship = trim(request.getParameter("relationship"));
        String statusFilter = trim(request.getParameter("status"));
        String building = trim(request.getParameter("building"));

        List<HouseholdMember> rows = householdMemberDAO.findMembersForExport(
                keyword, relationship, statusFilter, building);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"household-members.csv\"");

        
        java.io.PrintWriter out = response.getWriter();
        out.write('﻿');
        out.println("memberId,fullName,relationship,phone,idNumber,dateOfBirth,identity,apartmentCode,building,floor,status");
        for (HouseholdMember m : rows) {
            String identity = (m.getBuilding() == null ? "" : m.getBuilding())
                    + " - "
                    + (m.getFloorNumber() == null ? "" : m.getFloorNumber())
                    + " "
                    + (m.getApartmentCode() == null ? "" : m.getApartmentCode());
            out.print(csv(m.getMemberId()));
            out.print(',');
            out.print(csv(m.getFullName()));
            out.print(',');
            out.print(csv(m.getRelationship()));
            out.print(',');
            out.print(csv(m.getPhone()));
            out.print(',');
            out.print(csv(m.getIdNumber()));
            out.print(',');
            out.print(csv(m.getDateOfBirth() == null ? "" : m.getDateOfBirth().toString()));
            out.print(',');
            out.print(csv(identity.trim()));
            out.print(',');
            out.print(csv(m.getApartmentCode()));
            out.print(',');
            out.print(csv(m.getBuilding()));
            out.print(',');
            out.print(csv(m.getFloorNumber()));
            out.print(',');
            out.print(csv(m.getIsActive() != null && m.getIsActive() ? "ACTIVE" : "INACTIVE"));
            out.println();
        }
        out.flush();
    }

    private String csv(Object v) {
        if (v == null) {
            return "\"\"";
        }
        String s = String.valueOf(v).replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    
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

       
        // Định danh căn: [tòa] - [tầng] [mã] — không cho đổi sau khi tạo
        form.setApartmentCode(existing.getApartmentCode());
        form.setBuilding(existing.getBuilding());
        form.setFloorNumber(existing.getFloorNumber());

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

       

        if (apartmentDAO.deleteById(id)) {
            audit(user, "DELETE", apt, AppConstants.APT_STATUS_INACTIVE, "DELETED", "SUCCESS", "OK");
            FlashUtil.success(request, "Đã xóa căn hộ.");
        } else {
            audit(user, "DELETE", apt, apt.getStatus(), apt.getStatus(), "ERROR", "deleteById failed");
            FlashUtil.error(request, "Không thể thực hiện. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/apartment?action=list");
    }

    
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

 
    /**
     * Ghi apartment_history. Trả true nếu insert OK.
     * Tự ensure bảng; null status được ghi là NULL (không crash).
     */
    private boolean writeHistory(User actor, Apartment apt, String action,
            String fromStatus, String toStatus, String note) {
        if (apt == null || apt.getApartmentId() == null || action == null || action.isEmpty()) {
            System.out.println("writeHistory skip: missing apartment/action");
            return false;
        }
        try {
            int id = apartmentHistoryDAO.insert(ApartmentHistory.builder()
                    .apartmentId(apt.getApartmentId())
                    .action(action.trim())
                    .oldStatus(fromStatus)
                    .newStatus(toStatus)
                    .note(note)
                    .actorUserId(actor != null ? actor.getUserId() : null)
                    .actorName(actor != null
                            ? (actor.getFullName() != null ? actor.getFullName() : actor.getUsername())
                            : "Hệ thống")
                    .build());
            if (id < 0) {
                System.out.println("writeHistory fail: " + apartmentHistoryDAO.getLastError());
                return false;
            }
            System.out.println("writeHistory OK: apt=" + apt.getApartmentId()
                    + " action=" + action + " id=" + id);
            return true;
        } catch (Exception e) {
            System.out.println("writeHistory exception: " + e.getMessage());
            e.printStackTrace();
            return false;
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
     * Create: không bind apartmentCode (server tự sinh).
     */
    private Apartment bindForm(HttpServletRequest request, boolean forUpdate) {
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
                .apartmentCode(null) // create: generate; update: ghi đè từ existing
                .building(building)
                .floorNumber(floorNumber)
                .areaM2(areaM2)
                .occupancyType(occupancyType)
                .status(status)
                .notes(notes)
                .build();
    }

    private List<String> validateForCreate(Apartment form) {
        // Mã căn tự sinh sau validate — không yêu cầu user nhập
        return validateSharedFields(form);
    }

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
        } else if (extractBuildingToken(building) == null) {
            errors.add("Tên tòa nhà phải chứa chữ hoặc số để sinh mã căn (vd: A, Tòa B).");
        }

        Integer floor = form.getFloorNumber();
        if (floor == null || floor < 0 || floor > 200) {
            errors.add("Tầng phải là số nguyên từ 0 đến 200.");
        }

        BigDecimal area = form.getAreaM2();
        if (area == null || area.compareTo(AREA_MIN) < 0 || area.compareTo(AREA_MAX) > 0) {
            errors.add("Diện tích phải từ 15 m² trở lên (tối đa 10.000 m²).");
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

    /**
     * Format hiển thị định danh: [tên tòa] - [số tầng] [mã căn]
     * Ví dụ: A - 4 A-0401
     */
    private String formatApartmentIdentity(Apartment a) {
        if (a == null) {
            return "";
        }
        String building = a.getBuilding() == null ? "" : a.getBuilding();
        String floor = a.getFloorNumber() == null ? "" : String.valueOf(a.getFloorNumber());
        String code = a.getApartmentCode() == null ? "" : a.getApartmentCode();
        return building + " - " + floor + " " + code;
    }

    /**
     * Sinh mã căn từ tòa + tầng + số thứ tự unit trên tầng.
     * Format: {TOKEN}-{FF}{UU}  ví dụ tòa A tầng 4 unit 1 → A-0401
     * Token lấy từ tên tòa (A, Tòa B → B). Unit tăng nếu mã đã tồn tại.
     */
    private String generateApartmentCode(String building, Integer floorNumber) {
        String token = extractBuildingToken(building);
        if (token == null || floorNumber == null) {
            return null;
        }
        int startUnit = apartmentDAO.countByBuildingAndFloor(building, floorNumber) + 1;
        if (startUnit < 1) {
            startUnit = 1;
        }
        for (int unit = startUnit; unit <= 99; unit++) {
            String code = token + "-"
                    + String.format("%02d", floorNumber)
                    + String.format("%02d", unit);
            if (code.length() > 20) {
                return null;
            }
            if (!apartmentDAO.existsByCode(code)) {
                return code;
            }
        }
        return null;
    }

    /** Lấy token tòa để ghép mã: "Tòa A" / "A" → "A"; "B2" → "B2". */
    private String extractBuildingToken(String building) {
        if (building == null) {
            return null;
        }
        String trimmed = building.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        java.util.regex.Matcher m = BUILDING_TOKEN_PATTERN.matcher(trimmed);
        if (m.find()) {
            String token = m.group(1).toUpperCase();
            if (token.length() > 5) {
                token = token.substring(0, 5);
            }
            return token;
        }
        String cleaned = trimmed.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (cleaned.isEmpty()) {
            return null;
        }
        return cleaned.length() > 5 ? cleaned.substring(0, 5) : cleaned;
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

   
    private boolean canManage(String role) {
        return AppConstants.ROLE_ADMIN.equals(role) || AppConstants.ROLE_MANAGER.equals(role);
    }

    private boolean canViewList(String role) {
        return AppConstants.ROLE_ADMIN.equals(role)
                || AppConstants.ROLE_MANAGER.equals(role)
                || AppConstants.ROLE_STAFF.equals(role);
    }

    
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
