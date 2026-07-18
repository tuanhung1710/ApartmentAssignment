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
 * Servlet quản lý căn hộ: CRUD, occupancy, gán owner/tenant, thành viên hộ.
 * <p>
 * URL: {@code /apartment?action=...} — role ghi ADMIN/MANAGER; xem list STAFF+;
 * RESIDENT chỉ xem detail căn mình đang ở.
 */
@WebServlet(name = "ApartmentController", urlPatterns = {"/apartment"})
public class ApartmentController extends HttpServlet {

    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{9,12}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0?\\d{9,10}$");
    private static final Pattern BUILDING_TOKEN_PATTERN = Pattern.compile("(?i)(?:tòa\\s*|toa\\s*|building\\s*)?([A-Za-z0-9]+)");
    private static final BigDecimal AREA_MIN = new BigDecimal("15");
    private static final BigDecimal AREA_MAX = new BigDecimal("10000");
    /** Vai trò trong hộ (không dùng quan hệ gia đình). */
    private static final String[] RELATIONSHIP_OPTIONS = {
        "Chủ hộ", "Thành viên"
    };

    private final ApartmentDAO apartmentDAO = new ApartmentDAO();
    private final ApartmentResidentDAO apartmentResidentDAO = new ApartmentResidentDAO();
    private final HouseholdMemberDAO householdMemberDAO = new HouseholdMemberDAO();
    private final ApartmentHistoryDAO apartmentHistoryDAO = new ApartmentHistoryDAO();
    private final UserDAO userDAO = new UserDAO();

    /**
     * Điều hướng GET theo {@code action} (mặc định {@code list}).
     */
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
            case "init-floor":
                handleInitFloorForm(request, response);
                break;
            case "edit":
                handleEditForm(request, response);
                break;
            case "detail":
                handleDetail(request, response);
                break;
            case "activate":
                handleActivateForm(request, response);
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
            case "my":
                FlashUtil.error(request, "Chức năng căn hộ của tôi đang được phát triển.");
                response.sendRedirect(request.getContextPath() + "/dashboard");
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    /**
     * Điều hướng POST theo {@code action}; thiếu action → 400.
     */
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
            case "init-floor":
                handleInitFloor(request, response);
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
     * Danh sách căn hộ có lọc/sort/phân trang.
     * Trước khi query: expire tenant quá hạn + reconcile occupancy toàn hệ thống.
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

        
        int expired = apartmentResidentDAO.expirePastDueTenants();
        if (expired > 0) {
            System.out.println("handleList: expired past-due tenants=" + expired);
        }
        int fixed = apartmentDAO.reconcileAllOccupancy();
        if (fixed > 0) {
            System.out.println("handleList: reconciled occupancy rows=" + fixed);
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

    /** Form thêm căn lẻ (mặc định INACTIVE + N/A). */
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
                    .occupancyType(AppConstants.OCCUPANCY_NA)
                    .status(AppConstants.APT_STATUS_INACTIVE)
                    .build());
        }

        FlashUtil.moveToRequest(request);
        request.setAttribute("formMode", "create");
        request.setAttribute("pageTitle", "Thêm căn hộ");
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/form.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    /**
     * Tạo căn lẻ: force INACTIVE/N/A, sinh mã theo tòa+tầng+unit,
     * chặn khi tầng đã đủ UNITS_PER_FLOOR.
     */
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
      
        form.setApartmentCode(null);
        form.setStatus(AppConstants.APT_STATUS_INACTIVE);
        form.setOccupancyType(AppConstants.OCCUPANCY_NA);

        List<String> errors = validateForCreate(form);
        if (!errors.isEmpty()) {
            forwardForm(request, response, form, errors, "create");
            return;
        }

        int onFloor = apartmentDAO.countByBuildingAndFloor(form.getBuilding(), form.getFloorNumber());
        if (onFloor >= AppConstants.UNITS_PER_FLOOR) {
            errors.add("Tầng này đã đủ " + AppConstants.UNITS_PER_FLOOR
                    + " căn. Không thể thêm lẻ.");
            forwardForm(request, response, form, errors, "create");
            return;
        }

        String generatedCode = generateApartmentCode(form.getBuilding(), form.getFloorNumber());
        if (generatedCode == null) {
            errors.add("Không thể sinh mã căn hộ (tầng đã đủ unit 01–0"
                    + AppConstants.UNITS_PER_FLOOR + " hoặc tòa/tầng không hợp lệ).");
            forwardForm(request, response, form, errors, "create");
            return;
        }
        form.setApartmentCode(generatedCode);

        if (apartmentDAO.existsByCode(form.getApartmentCode())) {
            errors.add("Đã tồn tại căn hộ với mã " + form.getApartmentCode() + ".");
            form.setApartmentCode(null);
            forwardForm(request, response, form, errors, "create");
            return;
        }

        int newId = apartmentDAO.insert(form);
        if (newId >= 0) {
            form.setApartmentId(newId > 0 ? newId : null);
            writeHistory(user, form, "CREATE", null, form.getStatus(),
                    "Tạo căn lẻ (INACTIVE + N/A)");
            FlashUtil.success(request, "Thêm căn hộ thành công. Mã: "
                    + formatApartmentIdentity(form) + " (INACTIVE · N/A).");
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

 
    /** Form khởi tạo đủ unit trên một tầng (mặc định 6 căn). */
    private void handleInitFloorForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền khởi tạo tầng.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }
        if (request.getAttribute("form") == null) {
            request.setAttribute("form", Apartment.builder()
                    .areaM2(new BigDecimal("50.00"))
                    .build());
        }
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Khởi tạo tầng (6 căn)");
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/init-floor.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    
    /**
     * Khởi tạo các unit còn thiếu trên tầng (INACTIVE · N/A), bỏ qua unit đã có mã.
     */
    private void handleInitFloor(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền khởi tạo tầng.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=list");
            return;
        }

        Apartment form = bindForm(request, false);
        form.setStatus(AppConstants.APT_STATUS_INACTIVE);
        form.setOccupancyType(AppConstants.OCCUPANCY_NA);
        form.setApartmentCode(null);

        List<String> errors = validateInitFloor(form);
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("form", form);
            request.setAttribute("pageTitle", "Khởi tạo tầng (6 căn)");
            request.setAttribute("contentPage", "/WEB-INF/views/apartment/init-floor.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
            return;
        }

        String token = extractBuildingToken(form.getBuilding());
        String codePrefix = token + "-" + String.format("%02d", form.getFloorNumber());
        java.util.Set<Integer> existing = apartmentDAO.findExistingUnitsByCodePrefix(codePrefix);

        int created = 0;
        List<String> createdCodes = new ArrayList<>();
        for (int unit = 1; unit <= AppConstants.UNITS_PER_FLOOR; unit++) {
            if (existing.contains(unit)) {
                continue;
            }
            String code = codePrefix + String.format("%02d", unit);
            if (apartmentDAO.existsByCode(code)) {
                continue;
            }
            Apartment row = Apartment.builder()
                    .apartmentCode(code)
                    .building(form.getBuilding())
                    .floorNumber(form.getFloorNumber())
                    .areaM2(form.getAreaM2())
                    .occupancyType(AppConstants.OCCUPANCY_NA)
                    .status(AppConstants.APT_STATUS_INACTIVE)
                    .notes(form.getNotes())
                    .build();
            int newId = apartmentDAO.insert(row);
            if (newId >= 0) {
                row.setApartmentId(newId > 0 ? newId : null);
                writeHistory(user, row, "CREATE", null, AppConstants.APT_STATUS_INACTIVE,
                        "Khởi tạo tầng unit " + String.format("%02d", unit));
                created++;
                createdCodes.add(code);
            }
        }

        if (created == 0) {
            if (existing.size() >= AppConstants.UNITS_PER_FLOOR) {
                FlashUtil.error(request, "Tầng này đã đủ " + AppConstants.UNITS_PER_FLOOR + " căn.");
            } else {
                FlashUtil.error(request, "Không tạo được căn nào. Kiểm tra DB / mã trùng.");
            }
        } else {
            FlashUtil.success(request, "Đã khởi tạo " + created + " căn (INACTIVE · N/A): "
                    + String.join(", ", createdCodes) + ".");
        }
        response.sendRedirect(request.getContextPath() + "/apartment?action=list"
                + "&building=" + java.net.URLEncoder.encode(form.getBuilding(), java.nio.charset.StandardCharsets.UTF_8));
    }

   
    /**
     * Chi tiết căn: owner/tenant hiện tại, TV hộ, lịch sử.
     * Expire tenant quá hạn trước khi load; RESIDENT chỉ xem căn mình.
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

      
        apartmentResidentDAO.expirePastDueTenants();

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
        String addMemberBlockReason = getAddMemberBlockReason(apartment);
        FlashUtil.moveToRequest(request);
        request.setAttribute("apartment", apartment);
        request.setAttribute("owners", owners);
        request.setAttribute("tenants", tenants);
        request.setAttribute("members", members);
        request.setAttribute("histories", histories);
        request.setAttribute("canManage", manage);
        request.setAttribute("canAddMember", manage && addMemberBlockReason == null);
        request.setAttribute("addMemberBlockReason", addMemberBlockReason);
        request.setAttribute("pageTitle", "Chi tiết · " + apartment.getApartmentCode());
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/detail.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    
    /**
     * Form gán/đổi chủ sở hữu (hoặc chủ nhà khi RENTED).
     * RENTED: không cho chọn từ thành viên hộ (landlord ≠ người ở).
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
        if (!AppConstants.APT_STATUS_ACTIVE.equals(apartment.getStatus())) {
            FlashUtil.error(request, "Chỉ gán chủ sở hữu cho căn đang ACTIVE. Hãy kích hoạt căn trước.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
            return;
        }
      
        String blockOwner = getAssignOwnerBlockReason(apartment);
        if (blockOwner != null) {
            FlashUtil.error(request, blockOwner);
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
            return;
        }

        ApartmentResident currentOwner = apartmentResidentDAO.findCurrentOwner(apartmentId);
        List<User> candidates = userDAO.findActiveUsers();
        boolean isRented = AppConstants.OCCUPANCY_RENTED.equals(apartment.getOccupancyType());
        // RENTED: chủ nhà = landlord — không cho chọn từ thành viên hộ
        List<HouseholdMember> householdMembers = isRented
                ? new ArrayList<>()
                : householdMemberDAO.findByApartmentId(apartmentId);

        FlashUtil.moveToRequest(request);
        request.setAttribute("apartment", apartment);
        request.setAttribute("currentOwner", currentOwner);
        request.setAttribute("candidateUsers", candidates);
        request.setAttribute("householdMembers", householdMembers);
        if (request.getAttribute("personSource") == null) {
            request.setAttribute("personSource", "existing");
        }
        if (request.getAttribute("startDate") == null) {
            request.setAttribute("startDate", LocalDate.now().toString());
        }
        String title = isRented
                ? "Gán chủ nhà · " + apartment.getApartmentCode()
                : "Gán chủ sở hữu · " + apartment.getApartmentCode();
        request.setAttribute("pageTitle", title);
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/assign-owner.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    
    /**
     * Gán/đổi OWNER: end owner cũ nếu đổi; dọn tenant sót khi không còn thuê.
     * OWNED → sync TV "Chủ hộ"; RENTED → landlord không vào TV hộ.
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
        Integer memberId = parseId(request.getParameter("memberId"));
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
        if (!AppConstants.APT_STATUS_ACTIVE.equals(apartment.getStatus())) {
            FlashUtil.error(request, "Chỉ gán chủ sở hữu cho căn đang ACTIVE. Hãy kích hoạt căn trước.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
            return;
        }
        String blockOwnerPost = getAssignOwnerBlockReason(apartment);
        if (blockOwnerPost != null) {
            FlashUtil.error(request, blockOwnerPost);
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
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

        boolean isRented = AppConstants.OCCUPANCY_RENTED.equals(apartment.getOccupancyType());
        // RENTED: cấm gán owner từ thành viên hộ (landlord ≠ người ở)
        if (isRented && "household".equalsIgnoreCase(personSource)) {
            personSource = "existing";
            memberId = null;
        }

        User ownerUser = null;
        boolean createdNewUser = false;
        if ("household".equalsIgnoreCase(personSource) && !isRented) {
            if (memberId == null) {
                errors.add("Vui lòng chọn thành viên hộ.");
            } else {
                HouseholdMember hm = householdMemberDAO.findById(memberId);
                if (hm == null || !apartmentId.equals(hm.getApartmentId())
                        || hm.getIsActive() == null || !hm.getIsActive()) {
                    errors.add("Thành viên hộ không hợp lệ.");
                } else {
                    String memberName = hm.getFullName() == null ? "" : hm.getFullName().trim();
                    String memberPhone = hm.getPhone();
                    User matched = userDAO.findActiveByFullName(memberName);
                    if (matched != null) {
                        userId = matched.getUserId();
                        ownerUser = matched;
                    } else if (errors.isEmpty()) {
                        int newUid = userDAO.createResidentQuick(memberName, memberPhone, null, null);
                        if (newUid <= 0) {
                            errors.add("Không thể tạo user từ thành viên hộ. Thử user hệ thống / người mới.");
                        } else {
                            userId = newUid;
                            ownerUser = userDAO.findById(newUid);
                            createdNewUser = true;
                        }
                    }
                }
            }
        } else if ("new".equalsIgnoreCase(personSource)) {
            if (newFullName == null || newFullName.isEmpty()) {
                errors.add("Vui lòng nhập họ và tên.");
            } else if (newFullName.length() < 2 || newFullName.length() > 100) {
                errors.add("Họ tên phải từ 2 đến 100 ký tự.");
            }
            if (newPhone == null || newPhone.isEmpty()) {
                errors.add("Vui lòng nhập số điện thoại.");
            } else if (!PHONE_PATTERN.matcher(newPhone).matches()) {
                errors.add("Số điện thoại không hợp lệ.");
            }
            if (errors.isEmpty()) {
                int newUid = userDAO.createResidentQuick(newFullName, newPhone, newEmail, newUsername);
                if (newUid <= 0) {
                    errors.add("Không thể tạo user mới (username trùng hoặc lỗi DB). Thử lại.");
                } else {
                    userId = newUid;
                    ownerUser = userDAO.findById(newUid);
                    createdNewUser = true;
                }
            }
        } else {
          
            if (userId == null && memberId != null && !isRented) {
                personSource = "household";
                HouseholdMember hm = householdMemberDAO.findById(memberId);
                if (hm == null || !apartmentId.equals(hm.getApartmentId())
                        || hm.getIsActive() == null || !hm.getIsActive()) {
                    errors.add("Thành viên hộ không hợp lệ.");
                } else {
                    String memberName = hm.getFullName() == null ? "" : hm.getFullName().trim();
                    User matched = userDAO.findActiveByFullName(memberName);
                    if (matched != null) {
                        userId = matched.getUserId();
                        ownerUser = matched;
                    } else if (errors.isEmpty()) {
                        int newUid = userDAO.createResidentQuick(memberName, hm.getPhone(), null, null);
                        if (newUid <= 0) {
                            errors.add("Không thể tạo user từ thành viên hộ.");
                        } else {
                            userId = newUid;
                            ownerUser = userDAO.findById(newUid);
                            createdNewUser = true;
                        }
                    }
                }
            } else if (userId == null) {
                errors.add(isRented
                        ? "Vui lòng chọn chủ nhà từ tìm kiếm hoặc tạo người mới."
                        : "Vui lòng chọn chủ sở hữu từ tìm kiếm hoặc tạo người mới.");
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
            errors.add(isRented
                    ? "User này đã là chủ nhà hiện tại của căn."
                    : "User này đã là chủ sở hữu hiện tại của căn.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("selectedUserId", userId);
            request.setAttribute("selectedMemberId", isRented ? null : memberId);
            request.setAttribute("personSource", personSource);
            request.setAttribute("newFullName", newFullName == null ? "" : newFullName);
            request.setAttribute("newPhone", newPhone == null ? "" : newPhone);
            request.setAttribute("newEmail", newEmail == null ? "" : newEmail);
            request.setAttribute("newUsername", newUsername == null ? "" : newUsername);
            request.setAttribute("startDate", startRaw != null ? startRaw : LocalDate.now().toString());
            request.setAttribute("apartment", apartment);
            request.setAttribute("currentOwner", currentOwner);
            request.setAttribute("candidateUsers", userDAO.findActiveUsers());
            request.setAttribute("householdMembers",
                    isRented ? new ArrayList<>() : householdMemberDAO.findByApartmentId(apartmentId));
            if (ownerUser != null && ownerUser.getFullName() != null) {
                request.setAttribute("searchLabel", ownerUser.getFullName());
            }
            request.setAttribute("pageTitle",
                    (isRented ? "Gán chủ nhà · " : "Gán chủ sở hữu · ") + apartment.getApartmentCode());
            request.setAttribute("contentPage", "/WEB-INF/views/apartment/assign-owner.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
            return;
        }

        // Gán chủ: nếu căn không còn / không có người thuê → sẽ thành OWNED, gỡ tenant nếu còn sót.
        // Nếu còn tenant (RENTED) → giữ tenant, chủ = chủ nhà cho thuê.
        boolean hasTenants = apartmentDAO.hasCurrentTenant(apartmentId);
        int clearedTenants = 0;
        if (!hasTenants) {
            clearedTenants = apartmentResidentDAO.deleteCurrentTenants(apartmentId);
            if (clearedTenants < 0) {
                String detail = apartmentResidentDAO.getLastError();
                FlashUtil.error(request, detail != null && !detail.isEmpty()
                        ? detail
                        : "Không thể dọn người thuê trước khi gán chủ sở hữu.");
                response.sendRedirect(request.getContextPath()
                        + "/apartment?action=assign-owner&id=" + apartmentId);
                return;
            }
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
        // RENTED: owner = chủ nhà (landlord) — KHÔNG vào thành viên hộ.
        // OWNED: owner = chủ ở → sync TV "Chủ hộ".
        boolean isRentedOcc = AppConstants.OCCUPANCY_RENTED.equals(apartment.getOccupancyType())
                || hasTenants;
        boolean alreadyInHousehold = false;
        int memberSync = -2;
        if (isRentedOcc) {
            // Không thêm landlord vào TV; gỡ dòng Chủ hộ nếu trước đó sync nhầm từ OWNED
            if (ownerName != null && !ownerName.trim().isEmpty()) {
                householdMemberDAO.hardDeleteByNameAndRelationship(apartmentId, ownerName, "Chủ hộ");
            }
            if (isChange && currentOwner != null) {
                String oldName = currentOwner.getFullName() != null
                        ? currentOwner.getFullName() : currentOwner.getUsername();
                if (oldName != null && !oldName.trim().isEmpty()
                        && (ownerName == null || !oldName.trim().equalsIgnoreCase(ownerName.trim()))) {
                    householdMemberDAO.hardDeleteByNameAndRelationship(apartmentId, oldName, "Chủ hộ");
                }
            }
            memberSync = -3; // sentinel: skip TV (landlord)
        } else {
            if (isChange && currentOwner != null) {
                String oldName = currentOwner.getFullName() != null
                        ? currentOwner.getFullName() : currentOwner.getUsername();
                if (oldName != null && !oldName.trim().isEmpty()
                        && (ownerName == null || !oldName.trim().equalsIgnoreCase(ownerName.trim()))) {
                    householdMemberDAO.hardDeleteByNameAndRelationship(apartmentId, oldName, "Chủ hộ");
                }
            }
            alreadyInHousehold = householdMemberDAO.existsActiveByFullName(apartmentId, ownerName);
            if (alreadyInHousehold) {
                memberSync = 0;
            } else {
                memberSync = householdMemberDAO.ensureActiveMember(
                        apartmentId, ownerName, "Chủ hộ", ownerUser.getPhone());
            }
        }

        String histAction = isChange ? "CHANGE_OWNER" : "ASSIGN_OWNER";
        String note = isChange
                ? "Đổi owner → " + ownerName + " (userId=" + userId + ")"
                : "Gán owner lần đầu → " + ownerName + " (userId=" + userId + ")";
        if (memberSync == -3) {
            note += " | TV: chủ nhà RENTED — không vào thành viên hộ";
        } else if (alreadyInHousehold || memberSync == 0) {
            note += " | TV: đã có trong thành viên hộ, không thêm mới";
        } else if (memberSync > 0) {
            note += " | TV: thêm dòng Chủ hộ";
        }
        boolean histOk = writeHistory(actor, apartment, histAction, "OWNER", "OWNER", note);
        audit(actor, histAction, apartment, "OWNER", "OWNER", "SUCCESS", note);

        String okMsg;
        if (isRentedOcc) {
            okMsg = isChange ? "Đổi chủ nhà thành công." : "Gán chủ nhà thành công.";
        } else {
            okMsg = isChange ? "Đổi chủ sở hữu thành công." : "Gán chủ sở hữu thành công.";
        }
        if (createdNewUser) {
            okMsg += " Đã tạo user RESIDENT @" + ownerUser.getUsername() + " (pass 123456).";
        }
        if (clearedTenants > 0) {
            okMsg += " Đã gỡ " + clearedTenants + " người thuê (căn OWNED không có thuê).";
        } else if (hasTenants || isRentedOcc) {
            okMsg += " Chủ nhà (landlord) không vào thành viên hộ.";
        }
        if (memberSync == -3) {
            // RENTED landlord: không sync / không báo message thành viên hộ
        } else if (alreadyInHousehold || memberSync == 0) {
            okMsg += " \"" + ownerName + "\" đã có trong thành viên hộ — không thêm mới.";
        } else if (memberSync > 0) {
            okMsg += " Đã thêm \"" + ownerName + "\" vào thành viên hộ (Chủ hộ).";
        }
        if (!histOk) {
            String he = apartmentHistoryDAO.getLastError();
            okMsg += " (Cảnh báo: chưa ghi được lịch sử"
                    + (he != null && !he.isEmpty() ? " — " + he : "") + ")";
        }
        String synced = syncOccupancyFromResidents(apartmentId);
        if (synced != null) {
            okMsg += " Loại hình → " + synced + ".";
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
        // RENTED: gỡ chủ nhà → giữ RENTED (ô chủ nhà trống, gán lại).
        // OWNED/khác trống → VACANT.
        boolean wasRented = AppConstants.OCCUPANCY_RENTED.equals(apartment.getOccupancyType());
        String synced = syncOccupancyFromResidents(apartmentId, !wasRented);
        if (wasRented) {
            okMsg += " Ô chủ nhà để trống — có thể gán lại.";
        } else if (synced != null) {
            okMsg += " Loại hình → " + synced + ".";
        }
        FlashUtil.success(request, okMsg);
        response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
    }

    /**
     * Form gán người thuê / đại diện thuê (căn ACTIVE, không bị chặn theo occupancy).
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
        if (!AppConstants.APT_STATUS_ACTIVE.equals(apartment.getStatus())) {
            FlashUtil.error(request, "Chỉ gán người thuê cho căn đang ACTIVE. Hãy kích hoạt căn trước.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
            return;
        }
        String blockTenant = getAssignTenantBlockReason(apartment);
        if (blockTenant != null) {
            FlashUtil.error(request, blockTenant);
            response.sendRedirect(request.getContextPath()
                    + "/apartment?action=detail&id=" + apartmentId);
            return;
        }

        forwardAssignTenantForm(request, response, apartment, null, null, null, null, null);
    }

    /**
     * Submit gán TENANT_REP / TENANT.
     * TENANT_REP: tối đa 1 current (end cũ rồi insert); TENANT: cho phép nhiều current.
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
        Integer memberId = parseId(request.getParameter("memberId"));
        String roleInApartment = trim(request.getParameter("roleInApartment"));
        String startRaw = trim(request.getParameter("startDate"));
        String endRaw = trim(request.getParameter("endDate"));
        String newFullName = trim(request.getParameter("newFullName"));
        String newPhone = trim(request.getParameter("newPhone"));
        String newEmail = trim(request.getParameter("newEmail"));
        String newUsername = trim(request.getParameter("newUsername"));
        String newIdNumber = trim(request.getParameter("newIdNumber"));

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
        if (!AppConstants.APT_STATUS_ACTIVE.equals(apartment.getStatus())) {
            FlashUtil.error(request, "Chỉ gán người thuê cho căn đang ACTIVE. Hãy kích hoạt căn trước.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
            return;
        }
        String blockTenantPost = getAssignTenantBlockReason(apartment);
        if (blockTenantPost != null) {
            FlashUtil.error(request, blockTenantPost);
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
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
        if ("household".equalsIgnoreCase(personSource)) {
            // Chọn TV hộ: khớp user theo họ tên; chưa có → tạo RESIDENT
            if (memberId == null) {
                errors.add("Vui lòng chọn thành viên hộ.");
            } else {
                HouseholdMember hm = householdMemberDAO.findById(memberId);
                if (hm == null || !apartmentId.equals(hm.getApartmentId())
                        || hm.getIsActive() == null || !hm.getIsActive()) {
                    errors.add("Thành viên hộ không hợp lệ.");
                } else {
                    String memberName = hm.getFullName() == null ? "" : hm.getFullName().trim();
                    String memberPhone = hm.getPhone();
                    User matched = userDAO.findActiveByFullName(memberName);
                    if (matched != null) {
                        userId = matched.getUserId();
                        tenantUser = matched;
                    } else if (errors.isEmpty()) {
                        int newUid = userDAO.createResidentQuick(
                                memberName, memberPhone, null, null);
                        if (newUid <= 0) {
                            errors.add("Không thể tạo user từ thành viên hộ. Thử User có sẵn / Người mới.");
                        } else {
                            userId = newUid;
                            tenantUser = userDAO.findById(newUid);
                            createdNewUser = true;
                        }
                    }
                }
            }
        } else if ("new".equalsIgnoreCase(personSource)) {
            if (newFullName == null || newFullName.isEmpty()) {
                errors.add("Vui lòng nhập họ và tên.");
            } else if (newFullName.length() < 2 || newFullName.length() > 100) {
                errors.add("Họ tên phải từ 2 đến 100 ký tự.");
            }
            if (newPhone == null || newPhone.isEmpty()) {
                errors.add("Vui lòng nhập số điện thoại.");
            } else if (!PHONE_PATTERN.matcher(newPhone).matches()) {
                errors.add("Số điện thoại không hợp lệ.");
            }
            if (newIdNumber != null && !newIdNumber.isEmpty()
                    && !CCCD_PATTERN.matcher(newIdNumber).matches()) {
                errors.add("CCCD phải gồm 9–12 chữ số.");
            }
            if (errors.isEmpty()) {
                int newUid = userDAO.createResidentQuick(newFullName, newPhone, newEmail, newUsername);
                if (newUid <= 0) {
                    errors.add("Không thể tạo user mới (username trùng hoặc lỗi DB). Thử lại.");
                } else {
                    userId = newUid;
                    tenantUser = userDAO.findById(newUid);
                    createdNewUser = true;
                }
            }
        } else {
            // existing | household (từ search select)
            if (userId == null && memberId == null) {
                errors.add("Vui lòng chọn người thuê từ kết quả tìm kiếm hoặc tạo người mới.");
            } else if (userId != null) {
                tenantUser = userDAO.findById(userId);
                if (tenantUser == null || tenantUser.getIsActive() == null || !tenantUser.getIsActive()) {
                    errors.add("User không hợp lệ hoặc đã bị khóa.");
                }
            } else if (memberId != null) {
                // Fallback: form gửi memberId với personSource=existing
                HouseholdMember hm = householdMemberDAO.findById(memberId);
                if (hm == null || !apartmentId.equals(hm.getApartmentId())
                        || hm.getIsActive() == null || !hm.getIsActive()) {
                    errors.add("Thành viên hộ không hợp lệ.");
                } else {
                    personSource = "household";
                    String memberName = hm.getFullName() == null ? "" : hm.getFullName().trim();
                    User matched = userDAO.findActiveByFullName(memberName);
                    if (matched != null) {
                        userId = matched.getUserId();
                        tenantUser = matched;
                    } else if (errors.isEmpty()) {
                        int newUid = userDAO.createResidentQuick(memberName, hm.getPhone(), null, null);
                        if (newUid <= 0) {
                            errors.add("Không thể tạo user từ thành viên hộ.");
                        } else {
                            userId = newUid;
                            tenantUser = userDAO.findById(newUid);
                            createdNewUser = true;
                        }
                    }
                }
            }
        }

        if (errors.isEmpty() && userId != null && roleInApartment != null
                && apartmentResidentDAO.isCurrentWithRole(apartmentId, userId, roleInApartment)) {
            errors.add("User này đã là người thuê/đại diện hiện tại với vai trò đã chọn.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("personSource", personSource);
            request.setAttribute("selectedMemberId", memberId);
            request.setAttribute("newFullName", newFullName == null ? "" : newFullName);
            request.setAttribute("newPhone", newPhone == null ? "" : newPhone);
            request.setAttribute("newEmail", newEmail == null ? "" : newEmail);
            request.setAttribute("newUsername", newUsername == null ? "" : newUsername);
            request.setAttribute("newIdNumber", newIdNumber == null ? "" : newIdNumber);
            if (tenantUser != null && tenantUser.getFullName() != null) {
                request.setAttribute("searchLabel", tenantUser.getFullName());
            } else if (newFullName != null) {
                request.setAttribute("searchLabel", newFullName);
            }
            forwardAssignTenantForm(request, response, apartment, errors, userId,
                    roleInApartment, startRaw, endRaw);
            return;
        }

        // RENTED: giữ OWNER (chủ nhà) + TENANT. Không gỡ owner.
        // Căn OWNED (chủ ở, không tenant) không cho gán thuê.
        if (AppConstants.OCCUPANCY_OWNED.equals(apartment.getOccupancyType())
                && apartmentDAO.hasCurrentOwner(apartmentId)
                && !apartmentDAO.hasCurrentTenant(apartmentId)) {
            FlashUtil.error(request,
                    "Căn OWNED (chủ ở) không gán người thuê. Gỡ owner hoặc chuyển sang cho thuê trước.");
            response.sendRedirect(request.getContextPath()
                    + "/apartment?action=detail&id=" + apartmentId);
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

        // Người được gán thuê phải xuất hiện trong thành viên hộ
        String tenantName = tenantUser.getFullName() != null
                ? tenantUser.getFullName() : tenantUser.getUsername();
        boolean alreadyInHousehold = householdMemberDAO.existsActiveByFullName(apartmentId, tenantName);
        int memberSync = -2;
        if (alreadyInHousehold) {
            memberSync = 0;
        } else {
            // Người thuê / đại diện thuê luôn có trong danh sách TV (vai trò Thành viên)
            memberSync = householdMemberDAO.ensureActiveMember(
                    apartmentId, tenantName, "Thành viên", tenantUser.getPhone());
        }
        // CCCD (tuỳ chọn) từ form tạo mới — gắn vào TV nếu vừa insert và có id_number
        String newIdNumberSaved = trim(request.getParameter("newIdNumber"));
        if (createdNewUser && memberSync > 0 && newIdNumberSaved != null && !newIdNumberSaved.isEmpty()
                && CCCD_PATTERN.matcher(newIdNumberSaved).matches()) {
            HouseholdMember justAdded = householdMemberDAO.findById(memberSync);
            if (justAdded != null) {
                justAdded.setIdNumber(newIdNumberSaved);
                householdMemberDAO.update(justAdded);
            }
        }

        String histAction = changeRep ? "CHANGE_TENANT_REP" : "ASSIGN_TENANT";
        if (!changeRep && AppConstants.APT_ROLE_TENANT_REP.equals(roleInApartment)) {
            histAction = "ASSIGN_TENANT_REP";
        }
        String note = roleInApartment + " → " + tenantName + " (userId=" + userId + ")"
                + " | start=" + startDate
                + (endDate != null ? " | end=" + endDate : " | end=mở");
        if (alreadyInHousehold || memberSync == 0) {
            note += " | TV: đã có trong thành viên hộ";
        } else if (memberSync > 0) {
            note += " | TV: thêm vào thành viên hộ";
        }
        boolean histOk = writeHistory(actor, apartment, histAction, roleInApartment, roleInApartment, note);
        audit(actor, histAction, apartment, roleInApartment, roleInApartment, "SUCCESS", note);

        String okMsg = changeRep
                ? "Đổi đại diện thuê thành công."
                : "Gán người thuê thành công.";
        if (createdNewUser && tenantUser != null) {
            okMsg += " Đã tạo user RESIDENT @" + tenantUser.getUsername() + " (pass 123456).";
        }
        if (alreadyInHousehold || memberSync == 0) {
            okMsg += " \"" + tenantName + "\" đã có trong thành viên hộ.";
        } else if (memberSync > 0) {
            okMsg += " Đã thêm \"" + tenantName + "\" vào thành viên hộ.";
        } else if (memberSync < 0) {
            okMsg += " (Cảnh báo: gán thuê OK nhưng chưa thêm được vào thành viên hộ.)";
        }
        if (!histOk) {
            String he = apartmentHistoryDAO.getLastError();
            okMsg += " (Cảnh báo: chưa ghi được lịch sử"
                    + (he != null && !he.isEmpty() ? " — " + he : "") + ")";
        }
        String synced = syncOccupancyFromResidents(apartmentId);
        if (synced != null) {
            okMsg += " Loại hình → " + synced + ".";
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
        // RENTED: gỡ thuê → giữ RENTED, ô thuê để trống — vẫn gán/đổi thuê được.
        // Căn khác: còn owner → OWNED; trống → VACANT.
        boolean wasRented = AppConstants.OCCUPANCY_RENTED.equals(apartment.getOccupancyType());
        String synced = syncOccupancyFromResidents(apartmentId, !wasRented);
        if (wasRented) {
            okMsg += " Ô người thuê để trống — có thể gán lại.";
        } else if (synced != null) {
            okMsg += " Loại hình → " + synced + ".";
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
        List<HouseholdMember> householdMembers = householdMemberDAO.findByApartmentId(apartmentId);
        request.setAttribute("candidateUsers", userDAO.findActiveUsers());
        request.setAttribute("householdMembers", householdMembers);
        if (request.getAttribute("personSource") == null) {
            // Form search mặc định existing; chọn TV hộ set personSource=household từ JS
            request.setAttribute("personSource", "existing");
        }
        request.setAttribute("selectedUserId", selectedUserId);
        request.setAttribute("selectedRole", selectedRole);
        request.setAttribute("startDate",
                startDate != null ? startDate : LocalDate.now().toString());
        request.setAttribute("endDate", endDate == null ? "" : endDate);
        if (request.getAttribute("newIdNumber") == null) {
            request.setAttribute("newIdNumber", "");
        }
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Gán người thuê · " + apartment.getApartmentCode());
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/assign-tenant.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    /** Form thêm thành viên hộ (căn đủ điều kiện occupancy). */
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

        String blockReason = getAddMemberBlockReason(apartment);
        if (blockReason != null) {
            FlashUtil.error(request, blockReason);
            response.sendRedirect(request.getContextPath()
                    + "/apartment?action=detail&id=" + apartmentId);
            return;
        }

        if (request.getAttribute("form") == null) {
            request.setAttribute("form", HouseholdMember.builder().build());
        }
        forwardAddMemberForm(request, response, apartment, null);
    }

    /**
     * Submit thêm thành viên hộ (fullName, relationship, CCCD, phone, DOB).
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

        String blockReason = getAddMemberBlockReason(apartment);
        if (blockReason != null) {
            FlashUtil.error(request, blockReason);
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
            errors.add("Vui lòng chọn vai trò (Chủ hộ hoặc Thành viên).");
        } else if (!isValidMemberRole(relationship)) {
            errors.add("Vai trò chỉ được là Chủ hộ hoặc Thành viên.");
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
        String synced = syncOccupancyFromResidents(apartmentId);
        String okMsg = "Thêm thành viên thành công.";
        if (synced != null) {
            okMsg += " Loại hình → " + synced + ".";
        }
        FlashUtil.success(request, okMsg);
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

    
    /** Form cập nhật thành viên hộ. */
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

    
    /** Submit cập nhật thành viên hộ (validate CCCD trùng trừ chính mình). */
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
            errors.add("Vui lòng chọn vai trò (Chủ hộ hoặc Thành viên).");
        } else if (!isValidMemberRole(relationship)) {
            errors.add("Vai trò chỉ được là Chủ hộ hoặc Thành viên.");
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
     * Xóa thành viên hộ.
     * Nếu TV = chủ sở hữu → gỡ OWNER; nếu = người thuê/đại diện thuê → chỉ gỡ đúng người đó khỏi gán thuê.
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

        // Snapshot trước khi xóa TV — dùng để cascade gỡ gán owner/thuê
        ApartmentResident currentOwner = apartmentResidentDAO.findCurrentOwner(apartmentId);
        boolean alsoOwner = isSamePersonAsResident(existing, currentOwner);

        List<ApartmentResident> matchedTenants = findMatchingTenants(apartmentId, existing);

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

        // Chỉ gỡ đúng người khớp TV khỏi gán thuê → chỗ gán thuê để trống (hoặc còn người khác)
        boolean tenantRemoved = clearTenantAssignmentForMember(
                actor, apartment, apartmentId, memberId, removedName, matchedTenants);

        String cascadeNote = "";
        if (ownerRemoved) {
            cascadeNote += " + gỡ OWNER";
        }
        if (tenantRemoved) {
            cascadeNote += " + gỡ gán thuê";
        }

        writeHistory(actor, apartment, "REMOVE_MEMBER", null, null,
                "Xóa TV#" + memberId + ": " + removedName
                + (rel.isEmpty() ? "" : " (" + rel + ")")
                + cascadeNote);
        audit(actor, "REMOVE_MEMBER", apartment, "ACTIVE", "DELETED", "SUCCESS",
                "memberId=" + memberId + cascadeNote);

        // RENTED: gỡ tên thuê/chủ → giữ RENTED (ô trống, gán lại).
        // Căn khác: force empty → VACANT/OWNED theo cư dân còn lại.
        boolean wasRented = AppConstants.OCCUPANCY_RENTED.equals(apartment.getOccupancyType());
        String synced = syncOccupancyFromResidents(apartmentId, !wasRented);
        String okMsg = buildRemoveMemberSuccessMessage(removedName, ownerRemoved, tenantRemoved);
        if (wasRented && (tenantRemoved || ownerRemoved)) {
            okMsg += " Phần gán để trống — có thể gán lại.";
        } else if (synced != null) {
            okMsg += " Loại hình → " + synced + ".";
        }
        FlashUtil.success(request, okMsg);
        response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + apartmentId);
    }

    /** Tìm TENANT_REP / TENANT hiện tại trùng với TV (theo họ tên / username). */
    private List<ApartmentResident> findMatchingTenants(int apartmentId, HouseholdMember member) {
        List<ApartmentResident> matched = new ArrayList<>();
        List<ApartmentResident> currentTenants = apartmentResidentDAO.findByApartmentAndRoles(
                apartmentId, AppConstants.APT_ROLE_TENANT_REP, AppConstants.APT_ROLE_TENANT);
        if (currentTenants == null || currentTenants.isEmpty()) {
            return matched;
        }
        for (ApartmentResident tenant : currentTenants) {
            if (isSamePersonAsResident(member, tenant)) {
                matched.add(tenant);
            }
        }
        return matched;
    }

    /**
     * Gỡ đúng user khớp TV khỏi gán thuê (TENANT_REP / TENANT).
     * Không gỡ người thuê khác; sau khi gỡ, mục gán thuê trống (hoặc còn người khác).
     */
    private boolean clearTenantAssignmentForMember(User actor, Apartment apartment,
            int apartmentId, int memberId, String removedName,
            List<ApartmentResident> matchedTenants) {
        if (matchedTenants == null || matchedTenants.isEmpty()) {
            return false;
        }

        int deletedCount = 0;
        StringBuilder labels = new StringBuilder();
        for (ApartmentResident tenant : matchedTenants) {
            String role = tenant.getRoleInApartment();
            Integer userId = tenant.getUserId();
            if (role == null || role.isEmpty() || userId == null) {
                continue;
            }
            int deleted = apartmentResidentDAO.deleteCurrentByUserAndRole(apartmentId, userId, role);
            if (deleted > 0) {
                deletedCount += deleted;
                if (labels.length() > 0) {
                    labels.append(", ");
                }
                String n = tenant.getFullName() != null ? tenant.getFullName() : tenant.getUsername();
                labels.append(n).append(" (").append(role).append(")");
            } else if (deleted < 0) {
                System.out.println("WARN handleRemoveMember: gỡ gán thuê fail role=" + role
                        + " userId=" + userId + " apartmentId=" + apartmentId
                        + " err=" + apartmentResidentDAO.getLastError());
            }
        }
        if (deletedCount <= 0) {
            return false;
        }

        writeHistory(actor, apartment, "REMOVE_TENANT", "TENANT", "NONE",
                "Gỡ gán thuê theo xóa TV \"" + removedName + "\" → " + labels
                + " (để trống / gán lại)");
        audit(actor, "REMOVE_TENANT", apartment, "TENANT", "NONE", "SUCCESS",
                "cascade from REMOVE_MEMBER memberId=" + memberId + " cleared=" + labels);
        return true;
    }

    private String buildRemoveMemberSuccessMessage(String removedName,
            boolean ownerRemoved, boolean tenantRemoved) {
        if (ownerRemoved && tenantRemoved) {
            return "Đã xóa thành viên \"" + removedName
                    + "\". Đã gỡ chủ sở hữu và gỡ tên khỏi gán thuê (để trống).";
        }
        if (ownerRemoved) {
            return "Đã xóa thành viên \"" + removedName
                    + "\" và gỡ luôn vai trò chủ sở hữu của người này.";
        }
        if (tenantRemoved) {
            return "Đã xóa thành viên \"" + removedName
                    + "\" và gỡ khỏi gán thuê. Ô người thuê để trống.";
        }
        return "Đã xóa thành viên \"" + removedName + "\" khỏi danh sách hộ.";
    }

    /**
     * TV trùng cư dân: so fullName / username.
     * RENTED: owner = landlord — không auto map quan hệ "Chủ hộ" sang OWNER.
     */
    private boolean isSamePersonAsResident(HouseholdMember member, ApartmentResident resident) {
        if (member == null || resident == null) {
            return false;
        }
        String memberName = member.getFullName() == null ? "" : member.getFullName().trim();
        if (memberName.isEmpty()) {
            return false;
        }
        String residentFull = resident.getFullName() == null ? "" : resident.getFullName().trim();
        String residentUser = resident.getUsername() == null ? "" : resident.getUsername().trim();
        return memberName.equalsIgnoreCase(residentFull) || memberName.equalsIgnoreCase(residentUser);
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

    
    /** Form sửa căn — chỉ area/notes (+ occupancy nếu ACTIVE). */
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

   
    /**
     * Cập nhật căn: khóa mã/tòa/tầng/status từ existing;
     * INACTIVE → occupancy N/A; ACTIVE cho chọn VACANT/OWNED/RENTED.
     * Sau update chỉ nâng occupancy theo cư dân (không ép trống về VACANT).
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

       
        // Khóa mã/tòa/tầng/status từ DB; form chỉ sửa area/notes (+ occupancy nếu ACTIVE)
        form.setApartmentCode(existing.getApartmentCode());
        form.setBuilding(existing.getBuilding());
        form.setFloorNumber(existing.getFloorNumber());
        form.setStatus(existing.getStatus());

        if (AppConstants.APT_STATUS_INACTIVE.equals(existing.getStatus())) {
            form.setOccupancyType(AppConstants.OCCUPANCY_NA);
        } else {
            // ACTIVE: giữ VACANT/OWNED/RENTED user chọn trên form Sửa
            if (AppConstants.OCCUPANCY_NA.equals(form.getOccupancyType())
                    || form.getOccupancyType() == null || form.getOccupancyType().isEmpty()) {
                form.setOccupancyType(AppConstants.OCCUPANCY_VACANT);
            }
            if (!isActiveOccupancy(form.getOccupancyType())) {
                errors.add("Loại hình ACTIVE phải là VACANT / OWNED / RENTED.");
            }
        }

        errors.addAll(validateForUpdate(form));
        if (!errors.isEmpty()) {
            forwardForm(request, response, form, errors, "edit");
            return;
        }

        if (apartmentDAO.update(form)) {
            // Chỉ nâng theo cư dân (tenant→RENTED, owner→OWNED); không ép OWNED/RENTED trống về VACANT
            String synced = syncOccupancyFromResidents(form.getApartmentId(), false);
            if (synced != null) {
                form.setOccupancyType(synced);
            }
            writeHistory(user, form, "UPDATE", existing.getStatus(), form.getStatus(),
                    "Cập nhật · occupancy=" + form.getOccupancyType());
            FlashUtil.success(request, "Cập nhật căn hộ " + formatApartmentIdentity(form)
                    + " thành công (ACTIVE · " + form.getOccupancyType() + ").");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + form.getApartmentId());
        } else {
            errors.add("Không thể cập nhật căn hộ. Vui lòng thử lại.");
            forwardForm(request, response, form, errors, "edit");
        }
    }


    /** Vô hiệu hóa căn ACTIVE → INACTIVE + N/A (soft, không gỡ cư dân). */
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
        if (apartmentDAO.updateStatusAndOccupancy(id,
                AppConstants.APT_STATUS_INACTIVE, AppConstants.OCCUPANCY_NA)) {
            String note = currentResidents > 0
                    ? "OK → N/A (còn " + currentResidents + " cư dân hiện tại — soft disable)"
                    : "OK → occupancy N/A";
            audit(user, "DEACTIVATE", apt, AppConstants.APT_STATUS_ACTIVE,
                    AppConstants.APT_STATUS_INACTIVE, "SUCCESS", note);
            writeHistory(user, apt, "DEACTIVATE", AppConstants.APT_STATUS_ACTIVE,
                    AppConstants.APT_STATUS_INACTIVE, note);
            FlashUtil.success(request, "Đã vô hiệu hóa căn " + formatApartmentIdentity(apt)
                    + " (INACTIVE · N/A).");
        } else {
            audit(user, "DEACTIVATE", apt, AppConstants.APT_STATUS_ACTIVE,
                    AppConstants.APT_STATUS_ACTIVE, "ERROR", "updateStatusAndOccupancy failed");
            FlashUtil.error(request, "Không thể thực hiện. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + id);
    }

    /** Form kích hoạt căn — bắt buộc chọn occupancy OWNED/RENTED/VACANT. */
    private void handleActivateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền kích hoạt căn hộ.");
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
            FlashUtil.error(request, "Căn hộ đang hoạt động.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + id);
            return;
        }

        FlashUtil.moveToRequest(request);
        request.setAttribute("apartment", apt);
        if (request.getAttribute("occupancyType") == null) {
            request.setAttribute("occupancyType", AppConstants.OCCUPANCY_VACANT);
        }
        request.setAttribute("pageTitle", "Kích hoạt căn hộ");
        request.setAttribute("contentPage", "/WEB-INF/views/apartment/activate.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    /** Kích hoạt căn INACTIVE → ACTIVE kèm loại hình đã chọn. */
    private void handleActivate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canManage(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền kích hoạt căn hộ.");
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
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + id);
            return;
        }

        String occupancy = trim(request.getParameter("occupancyType"));
        if (!isActiveOccupancy(occupancy)) {
            List<String> errors = new ArrayList<>();
            errors.add("Vui lòng chọn loại hình: OWNED / RENTED / VACANT.");
            request.setAttribute("errors", errors);
            request.setAttribute("occupancyType", occupancy);
            request.setAttribute("apartment", apt);
            request.setAttribute("pageTitle", "Kích hoạt căn hộ");
            request.setAttribute("contentPage", "/WEB-INF/views/apartment/activate.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
            return;
        }

        // Activate: lưu đúng loại hình user chọn (VACANT / OWNED / RENTED).
        // Không re-sync ngay — căn trống vẫn giữ OWNED/RENTED theo ý định kích hoạt.
        // Không auto gán owner/tenant — để trống, user gán sau; vẫn thêm TV hộ được.
        if (apartmentDAO.updateStatusAndOccupancy(id, AppConstants.APT_STATUS_ACTIVE, occupancy)) {
            audit(user, "ACTIVATE", apt, AppConstants.APT_STATUS_INACTIVE,
                    AppConstants.APT_STATUS_ACTIVE, "SUCCESS", "occupancy=" + occupancy);
            writeHistory(user, apt, "ACTIVATE", AppConstants.APT_STATUS_INACTIVE,
                    AppConstants.APT_STATUS_ACTIVE, "Kích hoạt · " + occupancy);
            String okMsg = "Đã kích hoạt căn " + formatApartmentIdentity(apt)
                    + " (ACTIVE · " + occupancy + ").";
            if (AppConstants.OCCUPANCY_RENTED.equals(occupancy)) {
                okMsg += " Chủ nhà / người thuê để trống — có thể thêm thành viên hộ rồi gán sau.";
            } else if (AppConstants.OCCUPANCY_OWNED.equals(occupancy)) {
                okMsg += " Chủ sở hữu để trống — có thể thêm thành viên hộ rồi gán owner sau.";
            }
            FlashUtil.success(request, okMsg);
            response.sendRedirect(request.getContextPath() + "/apartment?action=detail&id=" + id);
        } else {
            audit(user, "ACTIVATE", apt, AppConstants.APT_STATUS_INACTIVE,
                    AppConstants.APT_STATUS_INACTIVE, "ERROR", "updateStatusAndOccupancy failed");
            FlashUtil.error(request, "Không thể thực hiện. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/apartment?action=activate&id=" + id);
        }
    }

   
    /** Xóa cứng căn INACTIVE và không còn cư dân hiện tại. */
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

       

        String identity = formatApartmentIdentity(apt);
        if (apartmentDAO.deleteById(id)) {
            audit(user, "DELETE", apt, AppConstants.APT_STATUS_INACTIVE, "DELETED", "SUCCESS", "OK");
            FlashUtil.success(request, "Đã xóa căn " + identity + ".");
        } else {
            audit(user, "DELETE", apt, apt.getStatus(), apt.getStatus(), "ERROR", "deleteById failed");
            FlashUtil.error(request, "Không thể thực hiện. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/apartment?action=list");
    }

    
    /** Ghi audit trail ra console (user, action, apartment, from→to, result). */
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
     * Ghi lịch sử căn hộ qua DAO; trả {@code false} nếu thiếu data hoặc insert fail.
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

    /** Re-render form create/edit kèm errors. */
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
            status = AppConstants.APT_STATUS_INACTIVE;
        }
        if (occupancyType == null || occupancyType.isEmpty()) {
            occupancyType = AppConstants.OCCUPANCY_NA;
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

    /** Validate tạo căn lẻ (building token bắt buộc để sinh mã). */
    private List<String> validateForCreate(Apartment form) {
        return validateBaseFields(form, true);
    }

    /** Validate khởi tạo tầng. */
    private List<String> validateInitFloor(Apartment form) {
        return validateBaseFields(form, true);
    }

    /** Validate update + ràng buộc occupancy theo status. */
    private List<String> validateForUpdate(Apartment form) {
        List<String> errors = validateBaseFields(form, false);
        String occupancy = form.getOccupancyType();
        String status = form.getStatus();
        if (AppConstants.APT_STATUS_INACTIVE.equals(status)) {
            if (!AppConstants.OCCUPANCY_NA.equals(occupancy)) {
                errors.add("Căn INACTIVE chỉ dùng loại hình N/A.");
            }
        } else if (AppConstants.APT_STATUS_ACTIVE.equals(status)) {
            if (!isActiveOccupancy(occupancy)) {
                errors.add("Căn ACTIVE: loại hình phải là OWNED / RENTED / VACANT.");
            }
        } else {
            errors.add("Trạng thái không hợp lệ (ACTIVE / INACTIVE).");
        }
        return errors;
    }

    /** building + floor + area + notes; optionally require building token for code gen. */
    private List<String> validateBaseFields(Apartment form, boolean requireBuildingToken) {
        List<String> errors = new ArrayList<>();

        String building = form.getBuilding();
        if (building == null || building.isEmpty()) {
            errors.add("Vui lòng nhập tòa nhà.");
        } else if (building.length() > 50) {
            errors.add("Tên tòa nhà tối đa 50 ký tự.");
        } else if (requireBuildingToken && extractBuildingToken(building) == null) {
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

        String notes = form.getNotes();
        if (notes != null && notes.length() > 500) {
            errors.add("Ghi chú tối đa 500 ký tự.");
        }

        return errors;
    }

    /** OWNED / RENTED / VACANT. */
    private boolean isActiveOccupancy(String occupancy) {
        return AppConstants.OCCUPANCY_OWNED.equals(occupancy)
                || AppConstants.OCCUPANCY_RENTED.equals(occupancy)
                || AppConstants.OCCUPANCY_VACANT.equals(occupancy);
    }

    /** Occupancy hợp lệ gồm cả N/A (INACTIVE). */
    private boolean isValidOccupancy(String occupancy) {
        return isActiveOccupancy(occupancy) || AppConstants.OCCUPANCY_NA.equals(occupancy);
    }

    /**
     * VACANT: phải đổi loại hình (Sửa → OWNED/RENTED) trước khi gán owner/thuê.
     */
    private String getAssignOwnerBlockReason(Apartment apartment) {
        if (apartment == null) {
            return "Không tìm thấy căn hộ.";
        }
        String occ = apartment.getOccupancyType();
        if (AppConstants.OCCUPANCY_VACANT.equals(occ)
                || occ == null || occ.isEmpty()
                || AppConstants.OCCUPANCY_NA.equals(occ)) {
            return "Căn đang VACANT. Hãy Sửa căn và chọn loại hình OWNED (mua) hoặc RENTED (thuê) trước khi gán thông tin.";
        }
        return null;
    }

    /**
     * Chỉ gán thuê khi loại hình đã là RENTED (không gán từ VACANT / OWNED chủ ở).
     */
    private String getAssignTenantBlockReason(Apartment apartment) {
        if (apartment == null || apartment.getApartmentId() == null) {
            return "Không tìm thấy căn hộ.";
        }
        int apartmentId = apartment.getApartmentId();
        String occ = apartment.getOccupancyType();
        if (AppConstants.OCCUPANCY_VACANT.equals(occ)
                || occ == null || occ.isEmpty()
                || AppConstants.OCCUPANCY_NA.equals(occ)) {
            return "Căn đang VACANT. Hãy Sửa căn và chọn loại hình RENTED trước, rồi gán người thuê.";
        }
        if (AppConstants.OCCUPANCY_OWNED.equals(occ)
                && !apartmentDAO.hasCurrentTenant(apartmentId)) {
            return "Căn OWNED (chủ ở) không gán người thuê. "
                    + "Muốn cho thuê: Sửa loại hình → RENTED rồi gán thuê.";
        }
        if (!AppConstants.OCCUPANCY_RENTED.equals(occ)
                && !apartmentDAO.hasCurrentTenant(apartmentId)) {
            return "Chỉ gán người thuê khi loại hình là RENTED. Hãy Sửa căn trước.";
        }
        return null;
    }

    /**
     * Chặn thêm TV hộ khi chưa đủ người chính theo loại hình.
     * - OWNED: gán OWNER trước (owner auto vào TV) → mới thêm TV.
     * - RENTED: gán người thuê (và chủ nhà nếu có) trước → tenant auto vào TV → mới thêm TV.
     * - VACANT: phải đổi loại hình trước (không gán/thêm TV trực tiếp).
     * @return null nếu được thêm; message lỗi nếu chặn
     */
    private String getAddMemberBlockReason(Apartment apartment) {
        if (apartment == null || apartment.getApartmentId() == null) {
            return "Không tìm thấy căn hộ.";
        }
        int apartmentId = apartment.getApartmentId();
        String occ = apartment.getOccupancyType();
        boolean hasOwner = apartmentDAO.hasCurrentOwner(apartmentId);
        boolean hasTenant = apartmentDAO.hasCurrentTenant(apartmentId);

        if (AppConstants.OCCUPANCY_VACANT.equals(occ)
                || occ == null || occ.isEmpty()
                || AppConstants.OCCUPANCY_NA.equals(occ)) {
            return "Căn VACANT: hãy Sửa loại hình thành OWNED hoặc RENTED trước, "
                    + "rồi gán owner / người thuê, sau đó mới thêm thành viên hộ.";
        }
        if (AppConstants.OCCUPANCY_OWNED.equals(occ)) {
            if (!hasOwner) {
                return "Căn OWNED: hãy gán chủ sở hữu trước. "
                        + "Chủ sở hữu sẽ tự có trong thành viên hộ, sau đó mới thêm TV khác.";
            }
            return null;
        }
        if (AppConstants.OCCUPANCY_RENTED.equals(occ)) {
            if (!hasTenant) {
                return "Căn RENTED: hãy gán người thuê / đại diện thuê trước khi thêm thành viên hộ. "
                        + "Người thuê sẽ tự có trong thành viên hộ.";
            }
            return null;
        }
        if (!hasOwner && !hasTenant) {
            return "Hãy gán chủ sở hữu hoặc người thuê trước khi thêm thành viên hộ.";
        }
        return null;
    }

    /**
     * Tự đồng bộ occupancy theo cư dân thực tế (coding-standards).
     * - INACTIVE → N/A
     * - ACTIVE + TENANT/REP → RENTED
     * - ACTIVE đang RENTED (kể cả gỡ hết thuê/chủ) → giữ RENTED
     *   (detail vẫn hiện card Chủ nhà + Người thuê để gán/đổi)
     * - ACTIVE + OWNER only → OWNED
     * - ACTIVE + TV hộ (không role) → OWNED (trừ đang RENTED → giữ RENTED)
     * - ACTIVE trống:
     *   · forceEmptyToVacant=true (sau gỡ trên căn không RENTED) → VACANT
     *   · false: giữ OWNED/RENTED đã chọn lúc kích hoạt/sửa; còn lại → VACANT
     */
    private String syncOccupancyFromResidents(int apartmentId) {
        return syncOccupancyFromResidents(apartmentId, false);
    }

    private String syncOccupancyFromResidents(int apartmentId, boolean forceEmptyToVacant) {
        apartmentDAO.ensureOccupancyCheckConstraint();
        Apartment apt = apartmentDAO.findById(apartmentId);
        if (apt == null) {
            return null;
        }
        String current = apt.getOccupancyType();
        String target;
        if (!AppConstants.APT_STATUS_ACTIVE.equals(apt.getStatus())) {
            target = AppConstants.OCCUPANCY_NA;
        } else if (apartmentDAO.hasCurrentTenant(apartmentId)) {
            // Có người thuê → RENTED (OWNER nếu có = chủ nhà)
            target = AppConstants.OCCUPANCY_RENTED;
        } else if (AppConstants.OCCUPANCY_RENTED.equals(current)) {
            // Giữ RENTED khi gỡ/đổi thuê — ô gán thuê vẫn hiện trên detail
            target = AppConstants.OCCUPANCY_RENTED;
        } else if (apartmentDAO.hasCurrentOwner(apartmentId)) {
            target = AppConstants.OCCUPANCY_OWNED;
        } else if (apartmentDAO.countActiveMembers(apartmentId) > 0) {
            target = AppConstants.OCCUPANCY_OWNED;
        } else if (!forceEmptyToVacant
                && (AppConstants.OCCUPANCY_OWNED.equals(current)
                || AppConstants.OCCUPANCY_RENTED.equals(current))) {
            target = current;
        } else {
            target = AppConstants.OCCUPANCY_VACANT;
        }
        if (target.equals(current)) {
            return target;
        }
        if (apartmentDAO.updateOccupancy(apartmentId, target)) {
            return target;
        }
        System.out.println("syncOccupancyFromResidents FAIL apartmentId=" + apartmentId
                + " target=" + target + " current=" + current);
        return null;
    }

    /**
     * Nhãn ngắn cho flash/confirm: ưu tiên mã căn (vd. A-0203).
     * Mã / tòa / tầng hiển thị tách cột trên UI, không gộp "định danh".
     */
    private String formatApartmentIdentity(Apartment a) {
        if (a == null) {
            return "";
        }
        if (a.getApartmentCode() != null && !a.getApartmentCode().isEmpty()) {
            return a.getApartmentCode();
        }
        String building = a.getBuilding() == null ? "" : a.getBuilding();
        String floor = a.getFloorNumber() == null ? "" : String.valueOf(a.getFloorNumber());
        return (building + " tầng " + floor).trim();
    }

    /**
     * Sinh mã căn từ tòa + tầng + unit tăng dần (tối đa UNITS_PER_FLOOR = 6).
     * Format: {TOKEN}-{FF}{UU}  ví dụ tòa A tầng 2 unit 3 → A-0203
     */
    private String generateApartmentCode(String building, Integer floorNumber) {
        String token = extractBuildingToken(building);
        if (token == null || floorNumber == null || floorNumber < 0 || floorNumber > 200) {
            return null;
        }
        String floorPart = String.format("%02d", floorNumber);
        String codePrefix = token + "-" + floorPart;
        java.util.Set<Integer> existing = apartmentDAO.findExistingUnitsByCodePrefix(codePrefix);
        for (int unit = 1; unit <= AppConstants.UNITS_PER_FLOOR; unit++) {
            if (existing.contains(unit)) {
                continue;
            }
            String code = codePrefix + String.format("%02d", unit);
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

    /** Parse id > 0; null/invalid → {@code null}. */
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

    /** Vai trò TV hợp lệ: chỉ Chủ hộ / Thành viên. */
    private boolean isValidMemberRole(String relationship) {
        if (relationship == null) {
            return false;
        }
        for (String opt : RELATIONSHIP_OPTIONS) {
            if (opt.equals(relationship)) {
                return true;
            }
        }
        return false;
    }

    /** Quyền ghi căn hộ: ADMIN hoặc MANAGER. */
    private boolean canManage(String role) {
        return AppConstants.ROLE_ADMIN.equals(role) || AppConstants.ROLE_MANAGER.equals(role);
    }

    /** Quyền xem danh sách: ADMIN / MANAGER / STAFF. */
    private boolean canViewList(String role) {
        return AppConstants.ROLE_ADMIN.equals(role)
                || AppConstants.ROLE_MANAGER.equals(role)
                || AppConstants.ROLE_STAFF.equals(role);
    }

    
    /**
     * Xem detail: staff+ luôn được; RESIDENT chỉ khi là cư dân hiện tại của căn.
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

    /** Bắt buộc login; thiếu → redirect login, trả {@code null}. */
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
