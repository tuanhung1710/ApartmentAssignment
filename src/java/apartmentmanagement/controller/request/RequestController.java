package apartmentmanagement.controller.request;

import apartmentmanagement.dao.ApartmentResidentDAO;
import apartmentmanagement.dao.RequestDAO;
import apartmentmanagement.dao.RequestHistoryDAO;
import apartmentmanagement.dao.SystemSettingDAO;
import apartmentmanagement.model.ApartmentResident;
import apartmentmanagement.model.Request;
import apartmentmanagement.model.RequestHistory;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet(name = "RequestController", urlPatterns = {"/request"})
public class RequestController extends HttpServlet {

    private static final int PAGE_SIZE = AppConstants.DEFAULT_PAGE_SIZE;
    private static final DateTimeFormatter DT_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final RequestDAO requestDAO = new RequestDAO();
    private final RequestHistoryDAO historyDAO = new RequestHistoryDAO();
    private final ApartmentResidentDAO apartmentResidentDAO = new ApartmentResidentDAO();
    private final SystemSettingDAO settingDAO = new SystemSettingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "my";
        }

        switch (action) {
            case "my":
            case "list":
                handleMyList(request, response);
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
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
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
            FlashUtil.error(request, "Không tìm thấy yêu cầu hoặc bạn không có quyền xem.");
            response.sendRedirect(request.getContextPath() + "/request?action=my");
            return;
        }

        List<RequestHistory> history = historyDAO.findStatusByRequestId(id);
        List<RequestHistory> comments = historyDAO.findCommentsByRequestId(id);
        int lastCommentId = 0;
        if (!comments.isEmpty() && comments.get(comments.size() - 1).getHistoryId() != null) {
            lastCommentId = comments.get(comments.size() - 1).getHistoryId();
        }

        request.setAttribute("reqItem", entity);
        request.setAttribute("history", history);
        request.setAttribute("comments", comments);
        request.setAttribute("lastCommentId", lastCommentId);
        request.setAttribute("canComment", true);
        request.setAttribute("canCancel", AppConstants.STATUS_PENDING.equals(entity.getStatus()));

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Chi tiết yêu cầu #" + id);
        request.setAttribute("contentPage", "/WEB-INF/views/request/detail.jsp");
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
