package apartmentmanagement.controller.request;

import apartmentmanagement.dao.ApartmentResidentDAO;
import apartmentmanagement.dao.RequestDAO;
import apartmentmanagement.dao.RequestHistoryDAO;
import apartmentmanagement.dao.SystemSettingDAO;
import apartmentmanagement.dao.UserDAO;
import apartmentmanagement.model.ApartmentResident;
import apartmentmanagement.model.Request;
import apartmentmanagement.model.RequestHistory;
import apartmentmanagement.model.User;
import apartmentmanagement.util.AppConstants;
import apartmentmanagement.util.FlashUtil;
import apartmentmanagement.websocket.RequestChatService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TV4 resident (my/create/detail/cancel) + TV5 process (manage/approve/reject/assign/progress).
 */
@WebServlet(name = "RequestController", urlPatterns = {"/request"})
public class RequestController extends HttpServlet {

    private static final int PAGE_SIZE = AppConstants.DEFAULT_PAGE_SIZE;
    private static final DateTimeFormatter DT_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final RequestDAO requestDAO = new RequestDAO();
    private final RequestHistoryDAO historyDAO = new RequestHistoryDAO();
    private final ApartmentResidentDAO apartmentResidentDAO = new ApartmentResidentDAO();
    private final SystemSettingDAO settingDAO = new SystemSettingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final RequestChatService chatService = new RequestChatService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = defaultAction(request);
        }

        switch (action) {
            case "my":
                handleMyList(request, response);
                break;
            case "list":
                // RESIDENT → my list; staff/manager/admin → manage list
                if (isProcessor(currentUser(request))) {
                    handleManageList(request, response);
                } else {
                    handleMyList(request, response);
                }
                break;
            case "manage":
                handleManageList(request, response);
                break;
            case "create":
                handleCreateForm(request, response);
                break;
            case "detail":
                handleDetail(request, response);
                break;
            case "cancel":
                handleCancel(request, response);
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
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        switch (action) {
            case "create":
                handleCreateSubmit(request, response);
                break;
            case "cancel":
                handleCancel(request, response);
                break;
            case "approve":
                handleApprove(request, response);
                break;
            case "reject":
                handleReject(request, response);
                break;
            case "assign":
                handleAssign(request, response);
                break;
            case "update-progress":
                handleUpdateProgress(request, response);
                break;
            case "complete":
                handleComplete(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private String defaultAction(HttpServletRequest request) {
        User user = currentUser(request);
        if (isProcessor(user)) {
            return "manage";
        }
        return "my";
    }

    private User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute(AppConstants.SESSION_USER);
    }

    private boolean isProcessor(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        String role = user.getRole();
        return AppConstants.ROLE_ADMIN.equals(role)
                || AppConstants.ROLE_MANAGER.equals(role)
                || AppConstants.ROLE_STAFF.equals(role);
    }

    private boolean canApprove(String role) {
        return AppConstants.ROLE_MANAGER.equals(role)
                || AppConstants.ROLE_ADMIN.equals(role);
    }

    // -------------------------------------------------------------------------
    // TV5 manage list / process actions
    // -------------------------------------------------------------------------

    private void handleManageList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireProcessor(request, response);
        if (user == null) {
            return;
        }

        String status = emptyToNull(trim(request.getParameter("status")));
        String requestType = emptyToNull(trim(request.getParameter("requestType")));
        int page = parsePage(request.getParameter("page"));

        Integer assignedTo = AppConstants.ROLE_STAFF.equals(user.getRole())
                ? user.getUserId() : null;

        List<Request> requests = requestDAO.findWithFilters(
                status, requestType, assignedTo, page, PAGE_SIZE);
        int total = requestDAO.countWithFilters(status, requestType, assignedTo);
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / PAGE_SIZE);
        if (page > totalPages) {
            page = totalPages;
            requests = requestDAO.findWithFilters(status, requestType, assignedTo, page, PAGE_SIZE);
        }

        request.setAttribute("requests", requests);
        request.setAttribute("status", status);
        request.setAttribute("requestType", requestType);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", total);
        request.setAttribute("pageSize", PAGE_SIZE);
        request.setAttribute("staffViewOnly", assignedTo != null);

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle",
                assignedTo != null ? "Việc được giao" : "Xử lý yêu cầu");
        request.setAttribute("contentPage", "/WEB-INF/views/request/manage-list.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleApprove(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireProcessor(request, response);
        if (user == null) {
            return;
        }
        if (!canApprove(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền phê duyệt yêu cầu.");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }

        Integer requestId = parseId(request.getParameter("id"));
        if (requestId == null) {
            FlashUtil.error(request, "Request ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }

        Request current = requestDAO.findById(requestId);
        if (current == null) {
            FlashUtil.error(request, "Không tìm thấy yêu cầu #" + requestId + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }
        if (!AppConstants.STATUS_PENDING.equals(current.getStatus())) {
            FlashUtil.error(request, "Chỉ phê duyệt được yêu cầu PENDING. Hiện tại: "
                    + current.getStatus() + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        String note = request.getParameter("note");
        if (requestDAO.approveRequest(requestId, user.getUserId(), note)) {
            FlashUtil.success(request, "Đã phê duyệt yêu cầu #" + requestId + ".");
        } else {
            FlashUtil.error(request, "Phê duyệt thất bại. Yêu cầu có thể đã được xử lý.");
        }
        response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
    }

    private void handleReject(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireProcessor(request, response);
        if (user == null) {
            return;
        }
        if (!canApprove(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền từ chối yêu cầu.");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }

        Integer requestId = parseId(request.getParameter("id"));
        if (requestId == null) {
            FlashUtil.error(request, "Request ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }

        String rejectReason = emptyToNull(trim(request.getParameter("rejectReason")));
        if (rejectReason == null) {
            FlashUtil.error(request, "Vui lòng nhập lý do từ chối.");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }
        if (rejectReason.length() > 500) {
            FlashUtil.error(request, "Lý do từ chối tối đa 500 ký tự.");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        Request current = requestDAO.findById(requestId);
        if (current == null) {
            FlashUtil.error(request, "Không tìm thấy yêu cầu #" + requestId + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }
        if (!AppConstants.STATUS_PENDING.equals(current.getStatus())) {
            FlashUtil.error(request, "Chỉ từ chối được yêu cầu PENDING. Hiện tại: "
                    + current.getStatus() + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        if (requestDAO.rejectRequest(requestId, user.getUserId(), rejectReason)) {
            FlashUtil.success(request, "Đã từ chối yêu cầu #" + requestId + ".");
        } else {
            FlashUtil.error(request, "Từ chối thất bại. Yêu cầu có thể đã được xử lý.");
        }
        response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
    }

    private void handleAssign(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireProcessor(request, response);
        if (user == null) {
            return;
        }
        if (!canApprove(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền gán Staff.");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }

        Integer requestId = parseId(request.getParameter("id"));
        if (requestId == null) {
            FlashUtil.error(request, "Request ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }

        Integer staffId = parseId(request.getParameter("staffId"));
        if (staffId == null) {
            FlashUtil.error(request, "Vui lòng chọn Staff để gán.");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        User staff = userDAO.findById(staffId);
        if (staff == null
                || !AppConstants.ROLE_STAFF.equals(staff.getRole())
                || staff.getIsActive() == null
                || !staff.getIsActive()) {
            FlashUtil.error(request, "Staff không hợp lệ hoặc đã bị khóa.");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        Request current = requestDAO.findById(requestId);
        if (current == null) {
            FlashUtil.error(request, "Không tìm thấy yêu cầu #" + requestId + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }
        if (!AppConstants.STATUS_APPROVED.equals(current.getStatus())) {
            FlashUtil.error(request, "Chỉ gán Staff cho yêu cầu APPROVED. Hiện tại: "
                    + current.getStatus() + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        String note = emptyToNull(trim(request.getParameter("note")));
        if (note == null) {
            note = "Gán cho " + staff.getFullName();
        }

        if (requestDAO.assignStaff(requestId, staffId, user.getUserId(), note)) {
            FlashUtil.success(request, "Đã gán Staff \"" + staff.getFullName()
                    + "\" cho yêu cầu #" + requestId + ".");
        } else {
            FlashUtil.error(request, "Gán Staff thất bại. Yêu cầu có thể đã được gán.");
        }
        response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
    }

    private void handleUpdateProgress(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireProcessor(request, response);
        if (user == null) {
            return;
        }
        if (!AppConstants.ROLE_STAFF.equals(user.getRole())) {
            FlashUtil.error(request, "Chỉ Staff được cập nhật tiến độ yêu cầu.");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }

        Integer requestId = parseId(request.getParameter("id"));
        if (requestId == null) {
            FlashUtil.error(request, "Request ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }

        String newStatus = emptyToNull(trim(request.getParameter("newStatus")));
        if (newStatus == null
                || !(AppConstants.STATUS_IN_PROGRESS.equals(newStatus)
                || AppConstants.STATUS_COMPLETED.equals(newStatus))) {
            FlashUtil.error(request, "Trạng thái mới không hợp lệ. Chọn IN_PROGRESS hoặc COMPLETED.");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        String note = emptyToNull(trim(request.getParameter("note")));
        if (note == null) {
            FlashUtil.error(request, "Vui lòng nhập ghi chú tiến độ.");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }
        if (note.length() > 500) {
            FlashUtil.error(request, "Ghi chú tối đa 500 ký tự.");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        Request current = requestDAO.findById(requestId);
        if (current == null) {
            FlashUtil.error(request, "Không tìm thấy yêu cầu #" + requestId + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }
        if (current.getAssignedTo() == null || !current.getAssignedTo().equals(user.getUserId())) {
            FlashUtil.error(request, "Bạn chỉ được cập nhật yêu cầu đã giao cho mình.");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }

        String oldStatus = current.getStatus();
        if (!isValidProgressTransition(oldStatus, newStatus)) {
            FlashUtil.error(request, "Không thể chuyển từ " + oldStatus + " sang " + newStatus + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        if (requestDAO.updateProgress(requestId, user.getUserId(), oldStatus, newStatus, note)) {
            FlashUtil.success(request, "Đã cập nhật tiến độ yêu cầu #" + requestId
                    + " → " + newStatus + ".");
        } else {
            FlashUtil.error(request, "Cập nhật tiến độ thất bại. Trạng thái có thể đã thay đổi.");
        }
        response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
    }

    private void handleComplete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireProcessor(request, response);
        if (user == null) {
            return;
        }
        if (!AppConstants.ROLE_STAFF.equals(user.getRole())) {
            FlashUtil.error(request, "Chỉ Staff được hoàn thành yêu cầu được giao.");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }

        Integer requestId = parseId(request.getParameter("id"));
        if (requestId == null) {
            FlashUtil.error(request, "Request ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }

        String note = emptyToNull(trim(request.getParameter("note")));
        if (note == null) {
            FlashUtil.error(request, "Vui lòng nhập ghi chú hoàn thành.");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }
        if (note.length() > 500) {
            FlashUtil.error(request, "Ghi chú tối đa 500 ký tự.");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        Request current = requestDAO.findById(requestId);
        if (current == null) {
            FlashUtil.error(request, "Không tìm thấy yêu cầu #" + requestId + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }
        if (current.getAssignedTo() == null || !current.getAssignedTo().equals(user.getUserId())) {
            FlashUtil.error(request, "Bạn chỉ được hoàn thành yêu cầu đã giao cho mình.");
            response.sendRedirect(request.getContextPath() + "/request?action=manage");
            return;
        }

        String oldStatus = current.getStatus();
        if (!AppConstants.STATUS_ASSIGNED.equals(oldStatus)
                && !AppConstants.STATUS_IN_PROGRESS.equals(oldStatus)) {
            FlashUtil.error(request, "Chỉ hoàn thành được yêu cầu ASSIGNED hoặc IN_PROGRESS. Hiện tại: "
                    + oldStatus + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        if (requestDAO.completeRequest(requestId, user.getUserId(), oldStatus, note)) {
            FlashUtil.success(request, "Đã hoàn thành yêu cầu #" + requestId + ".");
        } else {
            FlashUtil.error(request, "Hoàn thành thất bại. Trạng thái có thể đã thay đổi.");
        }
        response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
    }

    private boolean isValidProgressTransition(String oldStatus, String newStatus) {
        if (oldStatus == null || newStatus == null) {
            return false;
        }
        if (AppConstants.STATUS_ASSIGNED.equals(oldStatus)) {
            return AppConstants.STATUS_IN_PROGRESS.equals(newStatus)
                    || AppConstants.STATUS_COMPLETED.equals(newStatus);
        }
        if (AppConstants.STATUS_IN_PROGRESS.equals(oldStatus)) {
            return AppConstants.STATUS_COMPLETED.equals(newStatus)
                    || AppConstants.STATUS_IN_PROGRESS.equals(newStatus);
        }
        return false;
    }

    private User requireProcessor(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return null;
        }
        if (!isProcessor(user)) {
            try {
                request.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
            } catch (ServletException e) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            return null;
        }
        return user;
    }

    private void handleMyList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireResident(request, response);
        if (user == null) {
            return;
        }

        String status = trim(request.getParameter("status"));
        String type = trim(request.getParameter("type"));
        String keyword = trim(request.getParameter("keyword"));
        int page = parsePage(request.getParameter("page"));

        List<Request> list = requestDAO.findByCreatedByWithFilters(
                user.getUserId(), emptyToNull(status), emptyToNull(type), keyword, page, PAGE_SIZE);
        int total = requestDAO.countByCreatedByWithFilters(
                user.getUserId(), emptyToNull(status), emptyToNull(type), keyword);
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / PAGE_SIZE);

        request.setAttribute("requests", list);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", total);
        request.setAttribute("filterStatus", status);
        request.setAttribute("filterType", type);
        request.setAttribute("filterKeyword", keyword);
        request.setAttribute("myApartment", apartmentResidentDAO.findCurrentByUserId(user.getUserId()));
        request.setAttribute("openCount", requestDAO.countOpenByCreatedBy(user.getUserId()));

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Yêu cầu của tôi");
        request.setAttribute("contentPage", "/WEB-INF/views/request/my-list.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireResident(request, response);
        if (user == null) {
            return;
        }

        ApartmentResident ar = apartmentResidentDAO.findCurrentByUserId(user.getUserId());
        if (ar == null) {
            FlashUtil.error(request, "Bạn chưa được gán căn hộ. Liên hệ ban quản lý.");
            response.sendRedirect(request.getContextPath() + "/request?action=my");
            return;
        }

        request.setAttribute("myApartment", ar);
        request.setAttribute("moveTimeStart", getSetting("move.time.start", "08:00"));
        request.setAttribute("moveTimeEnd", getSetting("move.time.end", "17:00"));
        request.setAttribute("moveAllowedDays", getSetting("move.allowed.days", "2,3,4,5,6,7"));
        request.setAttribute("formType", trim(request.getParameter("type")));

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Gửi yêu cầu");
        request.setAttribute("contentPage", "/WEB-INF/views/request/create.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleCreateSubmit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireResident(request, response);
        if (user == null) {
            return;
        }

        ApartmentResident ar = apartmentResidentDAO.findCurrentByUserId(user.getUserId());
        if (ar == null) {
            FlashUtil.error(request, "Bạn chưa được gán căn hộ. Liên hệ ban quản lý.");
            response.sendRedirect(request.getContextPath() + "/request?action=my");
            return;
        }

        String type = trim(request.getParameter("requestType"));
        String title = trim(request.getParameter("title"));
        String description = trim(request.getParameter("description"));

        if (!isValidType(type)) {
            FlashUtil.error(request, "Loại yêu cầu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/request?action=create");
            return;
        }
        if (title == null || title.isEmpty()) {
            FlashUtil.error(request, "Tiêu đề không được để trống.");
            response.sendRedirect(request.getContextPath() + "/request?action=create&type=" + type);
            return;
        }
        if (title.length() > 200) {
            FlashUtil.error(request, "Tiêu đề tối đa 200 ký tự.");
            response.sendRedirect(request.getContextPath() + "/request?action=create&type=" + type);
            return;
        }

        Request entity = Request.builder()
                .apartmentId(ar.getApartmentId())
                .createdBy(user.getUserId())
                .requestType(type)
                .title(title)
                .description(description)
                .status(AppConstants.STATUS_PENDING)
                .build();

        String validationError = fillTypeSpecificFields(entity, type, request);
        if (validationError != null) {
            FlashUtil.error(request, validationError);
            response.sendRedirect(request.getContextPath() + "/request?action=create&type=" + type);
            return;
        }

        int newId = requestDAO.insert(entity);
        if (newId <= 0) {
            FlashUtil.error(request, "Gửi yêu cầu thất bại. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/request?action=create&type=" + type);
            return;
        }

        historyDAO.insert(RequestHistory.builder()
                .requestId(newId)
                .changedBy(user.getUserId())
                .oldStatus(null)
                .newStatus(AppConstants.STATUS_PENDING)
                .note("Cư dân gửi yêu cầu")
                .build());

        FlashUtil.success(request, "Gửi yêu cầu thành công (#" + newId + ").");
        response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + newId);
    }

    private String fillTypeSpecificFields(Request entity, String type, HttpServletRequest request) {
        switch (type) {
            case AppConstants.TYPE_REPAIR: {
                String location = trim(request.getParameter("locationDetail"));
                String urgency = trim(request.getParameter("urgency"));
                if (location == null || location.isEmpty()) {
                    return "Vui lòng nhập vị trí hỏng hóc.";
                }
                if (urgency == null || urgency.isEmpty()) {
                    urgency = "MEDIUM";
                }
                if (!isValidUrgency(urgency)) {
                    return "Mức độ ưu tiên không hợp lệ.";
                }
                entity.setLocationDetail(location);
                entity.setUrgency(urgency);
                return null;
            }
            case AppConstants.TYPE_PARKING: {
                String vehicleType = trim(request.getParameter("vehicleType"));
                String plate = trim(request.getParameter("plateNumber"));
                if (vehicleType == null || vehicleType.isEmpty()) {
                    return "Vui lòng chọn loại xe.";
                }
                if (plate == null || plate.isEmpty()) {
                    return "Vui lòng nhập biển số xe.";
                }
                entity.setVehicleType(vehicleType);
                entity.setPlateNumber(plate.toUpperCase());
                return null;
            }
            case AppConstants.TYPE_MOVE_IN:
            case AppConstants.TYPE_MOVE_OUT: {
                String scheduledRaw = trim(request.getParameter("scheduledAt"));
                String moveNote = trim(request.getParameter("moveNote"));
                if (scheduledRaw == null || scheduledRaw.isEmpty()) {
                    return "Vui lòng chọn thời gian chuyển đồ.";
                }
                LocalDateTime scheduled;
                try {
                    scheduled = LocalDateTime.parse(scheduledRaw, DT_LOCAL);
                } catch (DateTimeParseException e) {
                    return "Thời gian chuyển đồ không hợp lệ.";
                }
                if (scheduled.isBefore(LocalDateTime.now())) {
                    return "Thời gian chuyển đồ phải ở tương lai.";
                }
                String moveErr = validateMoveWindow(scheduled);
                if (moveErr != null) {
                    return moveErr;
                }
                entity.setScheduledAt(Timestamp.valueOf(scheduled));
                entity.setMoveNote(moveNote);
                return null;
            }
            case AppConstants.TYPE_OTHER:
                return null;
            default:
                return "Loại yêu cầu không hỗ trợ.";
        }
    }

    private String validateMoveWindow(LocalDateTime scheduled) {
        String startStr = getSetting("move.time.start", "08:00");
        String endStr = getSetting("move.time.end", "17:00");
        String daysStr = getSetting("move.allowed.days", "2,3,4,5,6,7");

        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(startStr);
            end = LocalTime.parse(endStr);
        } catch (DateTimeParseException e) {
            start = LocalTime.of(8, 0);
            end = LocalTime.of(17, 0);
        }

        LocalTime t = scheduled.toLocalTime();
        if (t.isBefore(start) || t.isAfter(end)) {
            return "Ngoài khung giờ cho phép (" + startStr + "–" + endStr + "). Vui lòng chọn lại.";
        }

        // Seed: 2=T2 ... 8=CN → seedDay = java DayOfWeek + 1
        int seedDay = scheduled.getDayOfWeek().getValue() + 1;
        Set<Integer> allowed = new HashSet<>();
        for (String part : daysStr.split(",")) {
            try {
                allowed.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        if (!allowed.isEmpty() && !allowed.contains(seedDay)) {
            return "Ngày đã chọn không nằm trong lịch cho phép chuyển đồ (T2–T7).";
        }
        return null;
    }

    private void handleDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "Thiếu mã yêu cầu.");
            response.sendRedirect(request.getContextPath()
                    + (isProcessor(user) ? "/request?action=manage" : "/request?action=my"));
            return;
        }

        Request entity = requestDAO.findById(id);
        if (entity == null) {
            FlashUtil.error(request, "Không tìm thấy yêu cầu #" + id + ".");
            response.sendRedirect(request.getContextPath()
                    + (isProcessor(user) ? "/request?action=manage" : "/request?action=my"));
            return;
        }

        // ACL: resident chỉ xem request của mình; staff chỉ xem việc được gán; manager/admin xem tất cả
        if (AppConstants.ROLE_RESIDENT.equals(user.getRole())) {
            if (!user.getUserId().equals(entity.getCreatedBy())) {
                FlashUtil.error(request, "Không tìm thấy yêu cầu hoặc bạn không có quyền xem.");
                response.sendRedirect(request.getContextPath() + "/request?action=my");
                return;
            }
        } else if (AppConstants.ROLE_STAFF.equals(user.getRole())) {
            if (entity.getAssignedTo() == null || !entity.getAssignedTo().equals(user.getUserId())) {
                FlashUtil.error(request, "Bạn chỉ được xem yêu cầu đã được giao cho mình.");
                response.sendRedirect(request.getContextPath() + "/request?action=manage");
                return;
            }
        } else if (!isProcessor(user)) {
            try {
                request.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
            } catch (ServletException e) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        }

        List<RequestHistory> history = historyDAO.findStatusByRequestId(id);
        List<RequestHistory> comments = historyDAO.findCommentsByRequestId(id);
        int lastCommentId = 0;
        if (!comments.isEmpty() && comments.get(comments.size() - 1).getHistoryId() != null) {
            lastCommentId = comments.get(comments.size() - 1).getHistoryId();
        }

        boolean canComment = chatService.canAccess(entity, user);
        String processingNote = requestDAO.findLatestProcessingNote(id);
        if (processingNote == null || processingNote.trim().isEmpty()) {
            processingNote = entity.getRejectReason();
        }

        // Manager/Admin gán staff khi APPROVED
        if (canApprove(user.getRole())
                && AppConstants.STATUS_APPROVED.equals(entity.getStatus())) {
            request.setAttribute("staffList", userDAO.findActiveStaff());
        }

        request.setAttribute("reqItem", entity);
        request.setAttribute("requestDetail", entity); // alias cho manage-detail.jsp
        request.setAttribute("history", history);
        request.setAttribute("historyList", history);  // alias cho manage-detail.jsp
        request.setAttribute("comments", comments);
        request.setAttribute("lastCommentId", lastCommentId);
        request.setAttribute("canComment", canComment);
        request.setAttribute("processingNote", processingNote);
        request.setAttribute("canCancel",
                AppConstants.ROLE_RESIDENT.equals(user.getRole())
                        && AppConstants.STATUS_PENDING.equals(entity.getStatus()));
        request.setAttribute("manageMode", isProcessor(user));

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Chi tiết yêu cầu #" + id);
        String view = isProcessor(user)
                ? "/WEB-INF/views/request/manage-detail.jsp"
                : "/WEB-INF/views/request/detail.jsp";
        request.setAttribute("contentPage", view);
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleCancel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireResident(request, response);
        if (user == null) {
            return;
        }

        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            FlashUtil.error(request, "Thiếu mã yêu cầu.");
            response.sendRedirect(request.getContextPath() + "/request?action=my");
            return;
        }

        Request entity = requestDAO.findById(id);
        if (entity == null || !user.getUserId().equals(entity.getCreatedBy())) {
            FlashUtil.error(request, "Không tìm thấy yêu cầu hoặc bạn không có quyền hủy.");
            response.sendRedirect(request.getContextPath() + "/request?action=my");
            return;
        }
        if (!AppConstants.STATUS_PENDING.equals(entity.getStatus())) {
            FlashUtil.error(request, "Chỉ hủy được yêu cầu đang chờ duyệt (PENDING).");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + id);
            return;
        }

        if (requestDAO.cancel(id, user.getUserId())) {
            historyDAO.insert(RequestHistory.builder()
                    .requestId(id)
                    .changedBy(user.getUserId())
                    .oldStatus(AppConstants.STATUS_PENDING)
                    .newStatus(AppConstants.STATUS_CANCELLED)
                    .note("Cư dân hủy yêu cầu")
                    .build());
            FlashUtil.success(request, "Đã hủy yêu cầu #" + id + ".");
        } else {
            FlashUtil.error(request, "Hủy yêu cầu thất bại.");
        }
        response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + id);
    }

    private User requireResident(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(AppConstants.SESSION_USER);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return null;
        }
        if (!AppConstants.ROLE_RESIDENT.equals(user.getRole())) {
            try {
                request.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
            } catch (ServletException e) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            return null;
        }
        return user;
    }

    private String getSetting(String key, String defaultValue) {
        String v = settingDAO.getValue(key);
        return (v == null || v.isEmpty()) ? defaultValue : v;
    }

    private boolean isValidType(String type) {
        if (type == null) {
            return false;
        }
        return Arrays.asList(
                AppConstants.TYPE_REPAIR,
                AppConstants.TYPE_PARKING,
                AppConstants.TYPE_MOVE_IN,
                AppConstants.TYPE_MOVE_OUT,
                AppConstants.TYPE_OTHER
        ).contains(type);
    }

    private boolean isValidUrgency(String urgency) {
        return "LOW".equals(urgency) || "MEDIUM".equals(urgency) || "HIGH".equals(urgency);
    }

    private int parsePage(String raw) {
        try {
            int p = Integer.parseInt(raw);
            return p < 1 ? 1 : p;
        } catch (Exception e) {
            return 1;
        }
    }

    private Integer parseId(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    private String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
