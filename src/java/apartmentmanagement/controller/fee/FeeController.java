package apartmentmanagement.controller.fee;

import apartmentmanagement.dao.FeeAssignmentDAO;
import apartmentmanagement.dao.FeeCategoryDAO;
import apartmentmanagement.dao.FeeDAO;
import apartmentmanagement.model.Fee;
import apartmentmanagement.model.FeeAssignment;
import apartmentmanagement.model.FeeCategory;
import apartmentmanagement.model.FeeScope;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "FeeController", urlPatterns = {"/fee"})
public class FeeController extends HttpServlet {

    private final FeeDAO feeDAO = new FeeDAO();
    private final FeeAssignmentDAO assignmentDAO = new FeeAssignmentDAO();
    private final FeeCategoryDAO categoryDAO = new FeeCategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = isResident(user) ? "my" : "list";
        }

        switch (action) {
            case "list":
                handleList(request, response, user);
                break;
            case "my":
                handleMyFees(request, response, user);
                break;
            case "detail":
            case "view":
                handleDetail(request, response, user);
                break;
            case "create":
                handleCreateForm(request, response, user);
                break;
            case "categories":
                handleCategories(request, response, user);
                break;
            case "assignment":
                handleAssignmentDetail(request, response, user);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        switch (action) {
            case "create":
                handleCreate(request, response, user);
                break;
            case "assign":
                handleAssign(request, response, user);
                break;
            case "publish":
                handlePublish(request, response, user);
                break;
            case "mark-paid":
                handleMarkPaid(request, response, user);
                break;
            case "mark-unpaid":
                handleMarkUnpaid(request, response, user);
                break;
            case "category-create":
                handleCategoryCreate(request, response, user);
                break;
            case "delete":
                handleDelete(request, response, user);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!canManageFees(user)) {
            forbid(request, response);
            return;
        }

        Integer categoryId = parseInteger(request.getParameter("categoryId"));
        String status = trim(request.getParameter("status"));
        if (status != null && status.isEmpty()) {
            status = null;
        }
        Integer feeMonth = parseInteger(request.getParameter("month"));
        Integer feeYear = parseInteger(request.getParameter("year"));
        if (feeMonth != null && (feeMonth < 1 || feeMonth > 12)) {
            feeMonth = null;
        }
        if (feeYear != null && (feeYear < 2000 || feeYear > 2100)) {
            feeYear = null;
        }
        String feeType = normalizeFeeType(request.getParameter("feeType"));

        int page = parsePage(request.getParameter("page"));
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;

        List<Fee> fees = feeDAO.findFees(categoryId, status, feeMonth, feeYear, feeType, page, pageSize);
        int total = feeDAO.countFees(categoryId, status, feeMonth, feeYear, feeType);
        int totalPages = total == 0 ? 1 : (int) Math.ceil(total / (double) pageSize);
        if (page > totalPages) {
            page = totalPages;
            fees = feeDAO.findFees(categoryId, status, feeMonth, feeYear, feeType, page, pageSize);
        }

        request.setAttribute("fees", fees);
        request.setAttribute("categories", categoryDAO.findAllActive());
        request.setAttribute("filterCategoryId", categoryId);
        request.setAttribute("filterStatus", status);
        request.setAttribute("filterMonth", feeMonth);
        request.setAttribute("filterYear", feeYear);
        request.setAttribute("filterFeeType", feeType);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", total);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("summary", feeDAO.getOverallSummary());
        request.setAttribute("unpaidCount", assignmentDAO.countUnpaid());

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Quản lý phí");
        request.setAttribute("contentPage", "/WEB-INF/views/fee/list.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!canManageFees(user)) {
            forbidRedirect(request, response);
            return;
        }
        Integer feeId = parseInteger(request.getParameter("id"));
        if (feeId == null) {
            feeId = parseInteger(request.getParameter("feeId"));
        }
        if (feeId == null) {
            FlashUtil.error(request, "Thiếu mã đợt phí.");
            response.sendRedirect(request.getContextPath() + "/fee?action=list");
            return;
        }

        Fee fee = feeDAO.findById(feeId);
        if (fee == null) {
            FlashUtil.error(request, "Không tìm thấy đợt phí.");
            response.sendRedirect(request.getContextPath() + "/fee?action=list");
            return;
        }

        if (feeDAO.deleteFee(feeId)) {
            FlashUtil.success(request, "Đã xóa đợt phí \"" + fee.getTitle() + "\".");
        } else {
            FlashUtil.error(request,
                    "Không xóa được. Đợt phí đã có căn thanh toán (PAID) hoặc không tồn tại.");
        }
        response.sendRedirect(request.getContextPath() + "/fee?action=list");
    }

    private void handleMyFees(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (isResident(user)) {
            Integer categoryId = parseInteger(request.getParameter("categoryId"));
            String feeType = normalizeFeeType(request.getParameter("feeType"));
            Integer feeMonth = parseInteger(request.getParameter("month"));
            Integer feeYear = parseInteger(request.getParameter("year"));
            if (feeMonth != null && (feeMonth < 1 || feeMonth > 12)) {
                feeMonth = null;
            }
            if (feeYear != null && (feeYear < 2000 || feeYear > 2100)) {
                feeYear = null;
            }
            String assignmentStatus = normalizeAssignmentStatus(request.getParameter("status"));
            int page = parsePage(request.getParameter("page"));
            int pageSize = AppConstants.DEFAULT_PAGE_SIZE;

            List<FeeAssignment> fees = assignmentDAO.findByResidentUser(
                    user.getUserId(), categoryId, feeType, feeMonth, feeYear, assignmentStatus, page, pageSize);
            int total = assignmentDAO.countByResidentUser(
                    user.getUserId(), categoryId, feeType, feeMonth, feeYear, assignmentStatus);
            int totalPages = total == 0 ? 1 : (int) Math.ceil(total / (double) pageSize);
            if (page > totalPages) {
                page = totalPages;
                fees = assignmentDAO.findByResidentUser(
                        user.getUserId(), categoryId, feeType, feeMonth, feeYear, assignmentStatus, page, pageSize);
            }

            request.setAttribute("assignments", fees);
            request.setAttribute("categories", categoryDAO.findAllActive());
            request.setAttribute("filterCategoryId", categoryId);
            request.setAttribute("filterFeeType", feeType);
            request.setAttribute("filterMonth", feeMonth);
            request.setAttribute("filterYear", feeYear);
            request.setAttribute("filterStatus", assignmentStatus);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalItems", total);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("summary", assignmentDAO.getResidentSummary(user.getUserId()));

            FlashUtil.moveToRequest(request);
            request.setAttribute("pageTitle", "Phí của tôi");
            request.setAttribute("contentPage", "/WEB-INF/views/fee/my-list.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
            return;
        }

        if (canManageFees(user)) {
            response.sendRedirect(request.getContextPath() + "/fee?action=list");
            return;
        }
        forbid(request, response);
    }

    private void handleDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!canManageFees(user)) {
            forbid(request, response);
            return;
        }

        Integer feeId = parseInteger(request.getParameter("id"));
        if (feeId == null) {
            FlashUtil.error(request, "Thiếu mã phí.");
            response.sendRedirect(request.getContextPath() + "/fee?action=list");
            return;
        }

        Fee fee = feeDAO.findById(feeId);
        if (fee == null) {
            FlashUtil.error(request, "Không tìm thấy khoản phí.");
            response.sendRedirect(request.getContextPath() + "/fee?action=list");
            return;
        }

        FeeScope scope = feeDAO.findScopeByFeeId(feeId);
        String keyword = trim(request.getParameter("q"));
        if (keyword != null && keyword.isEmpty()) {
            keyword = null;
        }
        String building = trim(request.getParameter("building"));
        if (building != null && building.isEmpty()) {
            building = null;
        }
        Integer floorNumber = parseInteger(request.getParameter("floor"));

        if (building == null) {
            floorNumber = null;
        }

        int page = parsePage(request.getParameter("page"));
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        List<FeeAssignment> assignments = assignmentDAO.findByFee(
                feeId, keyword, building, floorNumber, page, pageSize);
        int total = assignmentDAO.countByFee(feeId, keyword, building, floorNumber);
        int totalPages = total == 0 ? 1 : (int) Math.ceil(total / (double) pageSize);
        if (page > totalPages) {
            page = totalPages;
            assignments = assignmentDAO.findByFee(
                    feeId, keyword, building, floorNumber, page, pageSize);
        }

        List<String> filterBuildings = assignmentDAO.findDistinctBuildingsByFee(feeId);
        List<Integer> filterFloors = building != null
                ? assignmentDAO.findFloorsByFeeAndBuilding(feeId, building)
                : Collections.emptyList();

        request.setAttribute("fee", fee);
        request.setAttribute("scope", scope);
        request.setAttribute("assignments", assignments);
        request.setAttribute("filterQ", keyword);
        request.setAttribute("filterBuilding", building);
        request.setAttribute("filterFloor", floorNumber);
        request.setAttribute("filterBuildings", filterBuildings);
        request.setAttribute("filterFloors", filterFloors);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", total);

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Chi tiết phí");
        request.setAttribute("contentPage", "/WEB-INF/views/fee/detail.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleAssignmentDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        Integer id = parseInteger(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "Thiếu mã assignment.");
            redirectHome(request, response, user);
            return;
        }

        FeeAssignment a = assignmentDAO.findById(id);
        if (a == null) {
            FlashUtil.error(request, "Không tìm thấy khoản phí gán.");
            redirectHome(request, response, user);
            return;
        }

        if (isResident(user)) {
            if (!assignmentDAO.isCurrentResidentOfApartment(user.getUserId(), a.getApartmentId())) {
                forbid(request, response);
                return;
            }
            Fee parentFee = feeDAO.findById(a.getFeeId());
            if (parentFee == null
                    || !AppConstants.FEE_STATUS_PUBLISHED.equals(parentFee.getStatus())) {
                forbid(request, response);
                return;
            }
        } else if (!canManageFees(user)) {
            forbid(request, response);
            return;
        }

        request.setAttribute("assignment", a);
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Chi tiết phí căn hộ");
        request.setAttribute("contentPage", "/WEB-INF/views/fee/assignment-detail.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleCreateForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!canManageFees(user)) {
            forbid(request, response);
            return;
        }

        Calendar cal = Calendar.getInstance();
        request.setAttribute("categories", categoryDAO.findAllActive());
        request.setAttribute("buildings", feeDAO.findDistinctBuildings());
        request.setAttribute("apartments", feeDAO.findApartmentsForScope());
        request.setAttribute("formMonth", cal.get(Calendar.MONTH) + 1);
        request.setAttribute("formYear", cal.get(Calendar.YEAR));
        request.setAttribute("formFeeType", AppConstants.FEE_TYPE_MONTHLY);
        request.setAttribute("formScopeType", AppConstants.FEE_SCOPE_ALL);
        request.setAttribute("formTitle", "");
        request.setAttribute("formNote", "");
        request.setAttribute("formBuilding", "");

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Tạo khoản phí");
        request.setAttribute("contentPage", "/WEB-INF/views/fee/form.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!canManageFees(user)) {
            forbidRedirect(request, response);
            return;
        }

        Integer categoryId = parseInteger(request.getParameter("categoryId"));
        String title = trim(request.getParameter("title"));
        BigDecimal amount = parseMoney(request.getParameter("amount"));
        Integer feeMonth = parseInteger(request.getParameter("feeMonth"));
        Integer feeYear = parseInteger(request.getParameter("feeYear"));
        String feeType = trim(request.getParameter("feeType"));
        String note = trim(request.getParameter("note"));
        String scopeType = trim(request.getParameter("scopeType"));
        String building = trim(request.getParameter("building"));
        Integer floorNumber = parseInteger(request.getParameter("floorNumber"));
        Integer apartmentId = parseInteger(request.getParameter("apartmentId"));

        if (scopeType == null || scopeType.isEmpty()) {
            scopeType = AppConstants.FEE_SCOPE_ALL;
        }
        scopeType = scopeType.toUpperCase();

        if (feeType == null || feeType.isEmpty()) {
            feeType = AppConstants.FEE_TYPE_MONTHLY;
        }
        feeType = feeType.toUpperCase();
        if (AppConstants.FEE_TYPE_MONTHLY.equals(feeType)) {
            feeMonth = null;
        }

        String error = validateCreate(categoryId, title, amount, feeMonth, feeYear, feeType,
                scopeType, building, floorNumber, apartmentId);
        if (error != null) {
            request.setAttribute(AppConstants.FLASH_ERROR, error);
            refillCreateForm(request, categoryId, title, amount, feeMonth, feeYear, feeType, note,
                    scopeType, building, floorNumber, apartmentId);
            request.setAttribute("pageTitle", "Tạo khoản phí");
            request.setAttribute("contentPage", "/WEB-INF/views/fee/form.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
            return;
        }

        Fee fee = Fee.builder()
                .categoryId(categoryId)
                .title(title)
                .amount(amount)
                .feeMonth(feeMonth)
                .feeYear(feeYear)
                .feeType(feeType)
                .status(AppConstants.FEE_STATUS_DRAFT)
                .note(note)
                .createdBy(user.getUserId())
                .build();

        int feeId = feeDAO.insertFee(fee);
        if (feeId <= 0) {
            request.setAttribute(AppConstants.FLASH_ERROR,
                    "Tạo phí thất bại. Kiểm tra DB (bảng fees có cột fee_type?) hoặc log server.");
            refillCreateForm(request, categoryId, title, amount, feeMonth, feeYear, feeType, note,
                    scopeType, building, floorNumber, apartmentId);
            request.setAttribute("pageTitle", "Tạo khoản phí");
            request.setAttribute("contentPage", "/WEB-INF/views/fee/form.jsp");
            request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
            return;
        }

        boolean needBuilding = AppConstants.FEE_SCOPE_BUILDING.equals(scopeType)
                || AppConstants.FEE_SCOPE_FLOOR.equals(scopeType);
        FeeScope scope = FeeScope.builder()
                .feeId(feeId)
                .scopeType(scopeType)
                .building(needBuilding ? building : null)
                .floorNumber(AppConstants.FEE_SCOPE_FLOOR.equals(scopeType) ? floorNumber : null)
                .apartmentId(AppConstants.FEE_SCOPE_APARTMENT.equals(scopeType) ? apartmentId : null)
                .build();
        int scopeId = feeDAO.insertScope(scope);
        if (scopeId <= 0) {
            FlashUtil.error(request, "Tạo phí OK nhưng lưu phạm vi thất bại. Kiểm tra lại dữ liệu.");
            response.sendRedirect(request.getContextPath() + "/fee?action=detail&id=" + feeId);
            return;
        }

        FlashUtil.success(request,
                "Đã tạo phí \"" + title + "\" ở trạng thái DRAFT. Hãy gán căn rồi công bố.");
        response.sendRedirect(request.getContextPath() + "/fee?action=detail&id=" + feeId);
    }

    private void handleAssign(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!canManageFees(user)) {
            forbidRedirect(request, response);
            return;
        }
        Integer feeId = parseInteger(request.getParameter("id"));
        if (feeId == null) {
            feeId = parseInteger(request.getParameter("feeId"));
        }
        if (feeId == null) {
            FlashUtil.error(request, "Thiếu mã đợt phí.");
            response.sendRedirect(request.getContextPath() + "/fee?action=list");
            return;
        }

        Fee fee = feeDAO.findById(feeId);
        if (fee == null) {
            FlashUtil.error(request, "Không tìm thấy đợt phí.");
            response.sendRedirect(request.getContextPath() + "/fee?action=list");
            return;
        }
        if (!AppConstants.FEE_STATUS_DRAFT.equals(fee.getStatus())) {
            FlashUtil.error(request, "Chỉ gán căn khi đợt phí đang DRAFT (hiện: " + fee.getStatus() + ").");
            response.sendRedirect(request.getContextPath() + "/fee?action=detail&id=" + feeId);
            return;
        }

        FeeScope scope = feeDAO.findScopeByFeeId(feeId);
        if (scope == null) {
            FlashUtil.error(request, "Đợt phí chưa có phạm vi. Không thể gán căn.");
            response.sendRedirect(request.getContextPath() + "/fee?action=detail&id=" + feeId);
            return;
        }

        List<Integer> aptIds = feeDAO.resolveApartmentIds(
                scope.getScopeType(), scope.getBuilding(), scope.getFloorNumber(), scope.getApartmentId());
        if (aptIds.isEmpty()) {
            FlashUtil.error(request, "Không có căn nào trong phạm vi đã chọn.");
            response.sendRedirect(request.getContextPath() + "/fee?action=detail&id=" + feeId);
            return;
        }

        int[] result = assignmentDAO.createAssignments(feeId, aptIds, fee.getAmount());
        if (result[0] > 0) {
            feeDAO.setStatus(feeId, AppConstants.FEE_STATUS_ASSIGNED);
            FlashUtil.success(request,
                    "Đã gán " + result[0] + " căn · Bỏ qua " + result[1]
                    + ". Trạng thái: ASSIGNED. Có thể công bố cho cư dân.");
        } else {
            FlashUtil.error(request,
                    "Không gán được căn nào (đã gán trước / lỗi). Bỏ qua " + result[1] + ".");
        }
        response.sendRedirect(request.getContextPath() + "/fee?action=detail&id=" + feeId);
    }

    private void handlePublish(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!canManageFees(user)) {
            forbidRedirect(request, response);
            return;
        }
        Integer feeId = parseInteger(request.getParameter("id"));
        if (feeId == null) {
            feeId = parseInteger(request.getParameter("feeId"));
        }
        if (feeId == null) {
            FlashUtil.error(request, "Thiếu mã đợt phí.");
            response.sendRedirect(request.getContextPath() + "/fee?action=list");
            return;
        }

        Fee fee = feeDAO.findById(feeId);
        if (fee == null) {
            FlashUtil.error(request, "Không tìm thấy đợt phí.");
            response.sendRedirect(request.getContextPath() + "/fee?action=list");
            return;
        }
        if (!AppConstants.FEE_STATUS_ASSIGNED.equals(fee.getStatus())) {
            FlashUtil.error(request,
                    "Chỉ công bố khi đợt phí đang ASSIGNED (đã gán căn). Hiện: " + fee.getStatus() + ".");
            response.sendRedirect(request.getContextPath() + "/fee?action=detail&id=" + feeId);
            return;
        }

        int assigned = assignmentDAO.countByFee(feeId);
        if (assigned <= 0) {
            FlashUtil.error(request, "Chưa có căn nào được gán. Hãy gán căn trước khi công bố.");
            response.sendRedirect(request.getContextPath() + "/fee?action=detail&id=" + feeId);
            return;
        }

        if (feeDAO.updateStatus(feeId, AppConstants.FEE_STATUS_ASSIGNED, AppConstants.FEE_STATUS_PUBLISHED)) {
            FlashUtil.success(request, "Đã công bố \"" + fee.getTitle() + "\". Cư dân có thể xem trên Phí của tôi.");
        } else {
            FlashUtil.error(request, "Công bố thất bại (trạng thái đã đổi?).");
        }
        response.sendRedirect(request.getContextPath() + "/fee?action=detail&id=" + feeId);
    }

    private void handleCategories(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!canManageFees(user)) {
            forbid(request, response);
            return;
        }
        request.setAttribute("categories", categoryDAO.findAll());
        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Danh mục phí");
        request.setAttribute("contentPage", "/WEB-INF/views/fee/categories.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleCategoryCreate(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!canManageFees(user)) {
            forbidRedirect(request, response);
            return;
        }
        String name = trim(request.getParameter("name"));
        String description = trim(request.getParameter("description"));
        if (name == null || name.isEmpty()) {
            FlashUtil.error(request, "Tên danh mục không được trống.");
            response.sendRedirect(request.getContextPath() + "/fee?action=categories");
            return;
        }
        int id = categoryDAO.insert(FeeCategory.builder()
                .name(name)
                .description(description)
                .isActive(true)
                .build());
        if (id > 0) {
            FlashUtil.success(request, "Đã thêm danh mục \"" + name + "\".");
        } else {
            FlashUtil.error(request, "Thêm danh mục thất bại (trùng tên?).");
        }
        response.sendRedirect(request.getContextPath() + "/fee?action=categories");
    }

    private void handleMarkPaid(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!canManageFees(user)) {
            forbidRedirect(request, response);
            return;
        }
        Integer id = parseInteger(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "Thiếu mã assignment.");
            response.sendRedirect(request.getContextPath() + "/fee?action=list");
            return;
        }
        FeeAssignment assignment = assignmentDAO.findById(id);
        if (assignment == null) {
            FlashUtil.error(request, "Không tìm thấy assignment.");
            response.sendRedirect(request.getContextPath() + "/fee?action=list");
            return;
        }
        Fee parent = feeDAO.findById(assignment.getFeeId());
        if (parent == null || !AppConstants.FEE_STATUS_PUBLISHED.equals(parent.getStatus())) {
            FlashUtil.error(request, "Chỉ thu tiền khi đợt phí đã PUBLISHED.");
            Integer feeId = parent != null ? parent.getFeeId() : parseInteger(request.getParameter("feeId"));
            if (feeId != null) {
                response.sendRedirect(request.getContextPath() + "/fee?action=detail&id=" + feeId);
            } else {
                response.sendRedirect(request.getContextPath() + "/fee?action=assignment&id=" + id);
            }
            return;
        }
        if (assignmentDAO.markPaid(id, user.getUserId(), trim(request.getParameter("note")))) {
            FlashUtil.success(request, "Đã đánh dấu thanh toán.");
        } else {
            FlashUtil.error(request, "Không thể đánh dấu đã TT.");
        }
        Integer feeId = parseInteger(request.getParameter("feeId"));
        if (feeId != null) {
            response.sendRedirect(request.getContextPath() + "/fee?action=detail&id=" + feeId);
        } else {
            response.sendRedirect(request.getContextPath() + "/fee?action=assignment&id=" + id);
        }
    }

    private void handleMarkUnpaid(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!canManageFees(user)) {
            forbidRedirect(request, response);
            return;
        }
        Integer id = parseInteger(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "Thiếu mã assignment.");
            response.sendRedirect(request.getContextPath() + "/fee?action=list");
            return;
        }
        if (assignmentDAO.markUnpaid(id)) {
            FlashUtil.success(request, "Đã đánh dấu chưa thanh toán.");
        } else {
            FlashUtil.error(request, "Không thể đánh dấu chưa TT.");
        }
        Integer feeId = parseInteger(request.getParameter("feeId"));
        if (feeId != null) {
            response.sendRedirect(request.getContextPath() + "/fee?action=detail&id=" + feeId);
        } else {
            response.sendRedirect(request.getContextPath() + "/fee?action=assignment&id=" + id);
        }
    }

    private String validateCreate(Integer categoryId, String title, BigDecimal amount,
                                  Integer feeMonth, Integer feeYear, String feeType,
                                  String scopeType, String building, Integer floor, Integer apartmentId) {
        if (categoryId == null || categoryDAO.findById(categoryId) == null) {
            return "Vui lòng chọn danh mục phí hợp lệ.";
        }
        if (title == null || title.isEmpty()) {
            return "Tên khoản phí không được trống.";
        }
        if (amount == null || amount.compareTo(new BigDecimal("10000")) < 0) {
            return "Số tiền không hợp lệ (≥ 10.000).";
        }
        if (!AppConstants.FEE_TYPE_MONTHLY.equals(feeType)
                && !AppConstants.FEE_TYPE_ONE_TIME.equals(feeType)) {
            return "Loại phí không hợp lệ.";
        }
        if (feeYear == null || feeYear < 2000 || feeYear > 2100) {
            return "Vui lòng nhập năm hợp lệ (2000–2100).";
        }
        if (AppConstants.FEE_TYPE_ONE_TIME.equals(feeType)) {
            if (feeMonth == null || feeMonth < 1 || feeMonth > 12) {
                return "Vui lòng chọn tháng áp dụng (1–12).";
            }
        } else {
            if (feeMonth != null && (feeMonth < 1 || feeMonth > 12)) {
                return "Tháng không hợp lệ.";
            }
        }
        if (!AppConstants.FEE_SCOPE_ALL.equals(scopeType)
                && !AppConstants.FEE_SCOPE_BUILDING.equals(scopeType)
                && !AppConstants.FEE_SCOPE_FLOOR.equals(scopeType)
                && !AppConstants.FEE_SCOPE_APARTMENT.equals(scopeType)) {
            return "Phạm vi không hợp lệ.";
        }
        if (AppConstants.FEE_SCOPE_BUILDING.equals(scopeType)
                || AppConstants.FEE_SCOPE_FLOOR.equals(scopeType)) {
            if (building == null || building.isEmpty()) {
                return "Vui lòng chọn tòa nhà.";
            }
        }
        if (AppConstants.FEE_SCOPE_FLOOR.equals(scopeType) && floor == null) {
            return "Vui lòng chọn tầng.";
        }
        if (AppConstants.FEE_SCOPE_APARTMENT.equals(scopeType) && apartmentId == null) {
            return "Vui lòng chọn căn hộ.";
        }
        return null;
    }

    private void refillCreateForm(HttpServletRequest request,
                                  Integer categoryId, String title, BigDecimal amount,
                                  Integer feeMonth, Integer feeYear, String feeType, String note,
                                  String scopeType, String building, Integer floor, Integer apartmentId) {
        Calendar cal = Calendar.getInstance();
        request.setAttribute("categories", categoryDAO.findAllActive());
        request.setAttribute("buildings", feeDAO.findDistinctBuildings());
        request.setAttribute("apartments", feeDAO.findApartmentsForScope());
        request.setAttribute("formCategoryId", categoryId);
        request.setAttribute("formTitle", title != null ? title : "");
        if (amount != null) {
            request.setAttribute("formAmount", amount);
        }
        request.setAttribute("formNote", note != null ? note : "");
        if (AppConstants.FEE_TYPE_ONE_TIME.equals(feeType)) {
            request.setAttribute("formMonth",
                    feeMonth != null ? feeMonth : cal.get(Calendar.MONTH) + 1);
        } else if (feeMonth != null) {
            request.setAttribute("formMonth", feeMonth);
        } else {
            request.setAttribute("formMonth", cal.get(Calendar.MONTH) + 1);
        }
        request.setAttribute("formYear", feeYear != null ? feeYear : cal.get(Calendar.YEAR));
        request.setAttribute("formFeeType",
                feeType != null && !feeType.isEmpty() ? feeType : AppConstants.FEE_TYPE_MONTHLY);
        request.setAttribute("formScopeType",
                scopeType != null && !scopeType.isEmpty() ? scopeType : AppConstants.FEE_SCOPE_ALL);
        request.setAttribute("formBuilding", building != null ? building : "");
        request.setAttribute("formFloor", floor);
        request.setAttribute("formApartmentId", apartmentId);
    }

    private User requireUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(AppConstants.SESSION_USER);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
        }
        return user;
    }

    private boolean isResident(User user) {
        return AppConstants.ROLE_RESIDENT.equals(user.getRole());
    }

    private boolean canManageFees(User user) {
        String role = user.getRole();
        return AppConstants.ROLE_MANAGER.equals(role)
                || AppConstants.ROLE_STAFF.equals(role)
                || AppConstants.ROLE_ADMIN.equals(role);
    }

    private void forbid(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
    }

    private void forbidRedirect(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        FlashUtil.error(request, "Bạn không có quyền thực hiện thao tác này.");
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private void redirectHome(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (isResident(user)) {
            response.sendRedirect(request.getContextPath() + "/fee?action=my");
        } else {
            response.sendRedirect(request.getContextPath() + "/fee?action=list");
        }
    }

    private String normalizeFeeType(String raw) {
        String feeType = trim(raw);
        if (feeType == null || feeType.isEmpty()) {
            return null;
        }
        feeType = feeType.toUpperCase();
        if (AppConstants.FEE_TYPE_MONTHLY.equals(feeType)
                || AppConstants.FEE_TYPE_ONE_TIME.equals(feeType)) {
            return feeType;
        }
        return null;
    }

    private String normalizeAssignmentStatus(String raw) {
        String status = trim(raw);
        if (status == null || status.isEmpty()) {
            return null;
        }
        status = status.toUpperCase();
        if (AppConstants.ASSIGNMENT_PAID.equals(status)
                || AppConstants.ASSIGNMENT_UNPAID.equals(status)) {
            return status;
        }
        return null;
    }

    private int parsePage(String s) {
        Integer page = parseInteger(s);
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private Integer parseInteger(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseMoney(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(s.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
