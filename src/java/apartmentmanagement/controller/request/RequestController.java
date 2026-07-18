package apartmentmanagement.controller.request;

import apartmentmanagement.dao.RequestDAO;
import apartmentmanagement.dao.RequestHistoryDAO;
import apartmentmanagement.dao.UserDAO;
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
import java.util.List;

/**
 * Module Request.
 * UC-PROC-01 list · UC-PROC-02 detail · UC-PROC-03 approve ·
 * UC-PROC-04 reject · UC-PROC-05 assign staff · UC-PROC-06 staff my tasks.
 */
@WebServlet(name = "RequestController", urlPatterns = {"/request"})
public class RequestController extends HttpServlet {

    private final RequestDAO requestDAO = new RequestDAO();
    private final UserDAO userDAO = new UserDAO();
    private final RequestHistoryDAO requestHistoryDAO = new RequestHistoryDAO();
    private final RequestChatService chatService = new RequestChatService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list";
        }

        switch (action) {
            case "list":
            case "manage":
                handleList(request, response);
                break;
            case "detail":
                handleDetail(request, response);
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

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canProcessRequest(user.getRole())) {
            request.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
            return;
        }

        String status = trimToNull(request.getParameter("status"));
        String requestType = trimToNull(request.getParameter("requestType"));
        int page = parsePage(request.getParameter("page"));
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;

        Integer assignedTo = resolveAssignedToFilter(user);

        List<Request> requests = requestDAO.findWithFilters(status, requestType, assignedTo, page, pageSize);
        int total = requestDAO.countWithFilters(status, requestType, assignedTo);
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / pageSize);

        if (page > totalPages) {
            page = totalPages;
            requests = requestDAO.findWithFilters(status, requestType, assignedTo, page, pageSize);
        }

        request.setAttribute("requests", requests);
        request.setAttribute("status", status);
        request.setAttribute("requestType", requestType);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", total);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("staffViewOnly", assignedTo != null);

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle",
                assignedTo != null ? "Việc được giao" : "Danh sách yêu cầu");
        request.setAttribute("contentPage", "/WEB-INF/views/request/requestList.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canProcessRequest(user.getRole())) {
            request.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
            return;
        }

        Integer requestId = parsePositiveInt(request.getParameter("id"));
        if (requestId == null) {
            FlashUtil.error(request, "Request ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        Request detail = requestDAO.findById(requestId);
        if (detail == null) {
            FlashUtil.error(request, "Không tìm thấy yêu cầu #" + requestId + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        if (AppConstants.ROLE_STAFF.equals(user.getRole())) {
            if (detail.getAssignedTo() == null
                    || !detail.getAssignedTo().equals(user.getUserId())) {
                FlashUtil.error(request, "Bạn chỉ được xem yêu cầu đã được giao cho mình.");
                response.sendRedirect(request.getContextPath() + "/request?action=list");
                return;
            }
        }

        List<RequestHistory> historyList = requestHistoryDAO.findStatusByRequestId(requestId);
        String processingNote = requestDAO.findLatestProcessingNote(requestId);
        if (processingNote == null || processingNote.trim().isEmpty()) {
            processingNote = detail.getRejectReason();
        }

        List<RequestHistory> comments = requestHistoryDAO.findCommentsByRequestId(requestId);
        int lastCommentId = 0;
        if (comments != null && !comments.isEmpty()) {
            RequestHistory last = comments.get(comments.size() - 1);
            if (last.getHistoryId() != null) {
                lastCommentId = last.getHistoryId();
            }
        }
        boolean canComment = chatService.canAccess(detail, user);

        if (canApproveRequest(user.getRole())
                && AppConstants.STATUS_APPROVED.equals(detail.getStatus())) {
            request.setAttribute("staffList", userDAO.findActiveStaff());
        }

        request.setAttribute("requestDetail", detail);
        request.setAttribute("reqItem", detail); // alias cho _comments.jsp
        request.setAttribute("historyList", historyList);
        request.setAttribute("processingNote", processingNote);
        request.setAttribute("comments", comments);
        request.setAttribute("lastCommentId", lastCommentId);
        request.setAttribute("canComment", canComment);

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Chi tiết yêu cầu #" + requestId);
        request.setAttribute("contentPage", "/WEB-INF/views/request/requestDetail.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void handleApprove(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }

        if (!canApproveRequest(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền phê duyệt yêu cầu.");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        Integer requestId = parsePositiveInt(request.getParameter("id"));
        if (requestId == null) {
            FlashUtil.error(request, "Request ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        Request current = requestDAO.findById(requestId);
        if (current == null) {
            FlashUtil.error(request, "Không tìm thấy yêu cầu #" + requestId + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        if (!AppConstants.STATUS_PENDING.equals(current.getStatus())) {
            FlashUtil.error(request, "Chỉ phê duyệt được yêu cầu đang PENDING. Trạng thái hiện tại: "
                    + current.getStatus() + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        String note = request.getParameter("note");
        boolean ok = requestDAO.approveRequest(requestId, user.getUserId(), note);
        if (ok) {
            FlashUtil.success(request, "Đã phê duyệt yêu cầu #" + requestId + ".");
        } else {
            FlashUtil.error(request, "Phê duyệt thất bại. Yêu cầu có thể đã được xử lý.");
        }
        response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
    }

    private void handleReject(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }

        if (!canApproveRequest(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền từ chối yêu cầu.");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        Integer requestId = parsePositiveInt(request.getParameter("id"));
        if (requestId == null) {
            FlashUtil.error(request, "Request ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        String rejectReason = trimToNull(request.getParameter("rejectReason"));
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
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        if (!AppConstants.STATUS_PENDING.equals(current.getStatus())) {
            FlashUtil.error(request, "Chỉ từ chối được yêu cầu đang PENDING. Trạng thái hiện tại: "
                    + current.getStatus() + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        boolean ok = requestDAO.rejectRequest(requestId, user.getUserId(), rejectReason);
        if (ok) {
            FlashUtil.success(request, "Đã từ chối yêu cầu #" + requestId + ".");
        } else {
            FlashUtil.error(request, "Từ chối thất bại. Yêu cầu có thể đã được xử lý.");
        }
        response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
    }

    private void handleAssign(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!canApproveRequest(user.getRole())) {
            FlashUtil.error(request, "Bạn không có quyền gán Staff.");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        Integer requestId = parsePositiveInt(request.getParameter("id"));
        if (requestId == null) {
            FlashUtil.error(request, "Request ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        Integer staffId = parsePositiveInt(request.getParameter("staffId"));
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
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        if (!AppConstants.STATUS_APPROVED.equals(current.getStatus())) {
            FlashUtil.error(request, "Chỉ gán Staff cho yêu cầu đang APPROVED. Trạng thái hiện tại: "
                    + current.getStatus() + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        String note = trimToNull(request.getParameter("note"));
        if (note == null) {
            note = "Gán cho " + staff.getFullName();
        }

        boolean ok = requestDAO.assignStaff(requestId, staffId, user.getUserId(), note);
        if (ok) {
            FlashUtil.success(request, "Đã gán Staff \"" + staff.getFullName()
                    + "\" cho yêu cầu #" + requestId + ".");
        } else {
            FlashUtil.error(request, "Gán Staff thất bại. Yêu cầu có thể đã được gán.");
        }
        response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
    }

    private void handleUpdateProgress(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!AppConstants.ROLE_STAFF.equals(user.getRole())) {
            FlashUtil.error(request, "Chỉ Staff được cập nhật tiến độ yêu cầu.");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        Integer requestId = parsePositiveInt(request.getParameter("id"));
        if (requestId == null) {
            FlashUtil.error(request, "Request ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        String newStatus = trimToNull(request.getParameter("newStatus"));
        if (newStatus == null
                || !(AppConstants.STATUS_IN_PROGRESS.equals(newStatus)
                || AppConstants.STATUS_COMPLETED.equals(newStatus))) {
            FlashUtil.error(request, "Trạng thái mới không hợp lệ. Chọn IN_PROGRESS hoặc COMPLETED.");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        String note = trimToNull(request.getParameter("note"));
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
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        if (current.getAssignedTo() == null
                || !current.getAssignedTo().equals(user.getUserId())) {
            FlashUtil.error(request, "Bạn chỉ được cập nhật yêu cầu đã giao cho mình.");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        String oldStatus = current.getStatus();
        if (!isValidProgressTransition(oldStatus, newStatus)) {
            FlashUtil.error(request, "Không thể chuyển từ " + oldStatus + " sang " + newStatus + ".");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        boolean ok = requestDAO.updateProgress(
                requestId, user.getUserId(), oldStatus, newStatus, note);
        if (ok) {
            FlashUtil.success(request, "Đã cập nhật tiến độ yêu cầu #" + requestId
                    + " → " + newStatus + ".");
        } else {
            FlashUtil.error(request, "Cập nhật tiến độ thất bại. Trạng thái có thể đã thay đổi.");
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

    private void handleComplete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        if (!AppConstants.ROLE_STAFF.equals(user.getRole())) {
            FlashUtil.error(request, "Chỉ Staff được hoàn thành yêu cầu được giao.");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        Integer requestId = parsePositiveInt(request.getParameter("id"));
        if (requestId == null) {
            FlashUtil.error(request, "Request ID không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        String note = trimToNull(request.getParameter("note"));
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
            response.sendRedirect(request.getContextPath() + "/request?action=list");
            return;
        }

        if (current.getAssignedTo() == null
                || !current.getAssignedTo().equals(user.getUserId())) {
            FlashUtil.error(request, "Bạn chỉ được hoàn thành yêu cầu đã giao cho mình.");
            response.sendRedirect(request.getContextPath() + "/request?action=list");
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

        boolean ok = requestDAO.completeRequest(requestId, user.getUserId(), oldStatus, note);
        if (ok) {
            FlashUtil.success(request, "Đã hoàn thành yêu cầu #" + requestId + ".");
        } else {
            FlashUtil.error(request, "Hoàn thành thất bại. Trạng thái có thể đã thay đổi.");
        }
        response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
    }

    private boolean canProcessRequest(String role) {
        return AppConstants.ROLE_MANAGER.equals(role)
                || AppConstants.ROLE_STAFF.equals(role)
                || AppConstants.ROLE_ADMIN.equals(role);
    }

    private boolean canApproveRequest(String role) {
        return AppConstants.ROLE_MANAGER.equals(role)
                || AppConstants.ROLE_ADMIN.equals(role);
    }

    private Integer resolveAssignedToFilter(User user) {
        if (user != null && AppConstants.ROLE_STAFF.equals(user.getRole())) {
            return user.getUserId();
        }
        return null;
    }

    private User requireUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(AppConstants.SESSION_USER);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
        }
        return user;
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

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
