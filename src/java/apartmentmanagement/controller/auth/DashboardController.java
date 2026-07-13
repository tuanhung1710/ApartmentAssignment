package apartmentmanagement.controller.auth;

import apartmentmanagement.dao.DashboardStatsDAO;
import apartmentmanagement.dao.UserDAO;
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

/**
 * Dashboard shell theo role (UC-COM-04) — ownership TV1.
 * Số liệu count qua DashboardStatsDAO (không dùng DAO module TV2–TV5).
 */
@WebServlet(name = "DashboardController", urlPatterns = {"/dashboard"})
public class DashboardController extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final DashboardStatsDAO statsDAO = new DashboardStatsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(AppConstants.SESSION_USER);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String role = user.getRole() == null ? "" : user.getRole();

        switch (role) {
            case AppConstants.ROLE_ADMIN:
                loadAdminStats(request);
                break;
            case AppConstants.ROLE_MANAGER:
                loadManagerStats(request);
                break;
            case AppConstants.ROLE_STAFF:
                loadStaffStats(request, user.getUserId());
                break;
            case AppConstants.ROLE_RESIDENT:
                loadResidentStats(request, user.getUserId());
                break;
            default:
                break;
        }

        FlashUtil.moveToRequest(request);
        request.setAttribute("pageTitle", "Dashboard");
        // 1 JSP theo style ApartmentManagement; số liệu vẫn load theo role ở trên
        request.setAttribute("contentPage", "/WEB-INF/views/auth/dashboard.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void loadAdminStats(HttpServletRequest request) {
        // User count thuộc UserDAO (TV1)
        request.setAttribute("totalUsers", userDAO.countAll());
        request.setAttribute("lockedUsers", userDAO.countByActive(false));
        // Apartment count: shell TV1; TODO TV2 có thể thay bằng ApartmentDAO.countAll()
        request.setAttribute("totalApartments", statsDAO.countApartments());
    }

    private void loadManagerStats(HttpServletRequest request) {
        // TODO TV4/TV5: chuyển sang RequestDAO khi module xong
        request.setAttribute("pendingRequests",
                statsDAO.countRequestsByStatus(AppConstants.STATUS_PENDING));
        int assigned = statsDAO.countRequestsByStatus(AppConstants.STATUS_ASSIGNED);
        int inProgress = statsDAO.countRequestsByStatus(AppConstants.STATUS_IN_PROGRESS);
        request.setAttribute("processingRequests", assigned + inProgress);
        // TODO TV3: MonthlyFeeDAO.countDraft()
        request.setAttribute("draftFees", statsDAO.countDraftFees());
    }

    private void loadStaffStats(HttpServletRequest request, Integer userId) {
        int id = userId == null ? 0 : userId;
        // TODO TV5: RequestDAO theo staff
        request.setAttribute("assignedJobs", statsDAO.countAssignedToStaff(id));
        request.setAttribute("inProgressJobs", statsDAO.countInProgressByStaff(id));
        request.setAttribute("completedWeek", statsDAO.countCompletedLast7DaysByStaff(id));
    }

    private void loadResidentStats(HttpServletRequest request, Integer userId) {
        int id = userId == null ? 0 : userId;

        // TODO TV2
        String aptCode = statsDAO.findCurrentApartmentCodeByUserId(id);
        request.setAttribute("myApartment", aptCode == null ? "Chưa gán căn hộ" : aptCode);

        // TODO TV3
        String feeSummary = statsDAO.findLatestFeeSummaryForUser(id);
        request.setAttribute("latestFee", feeSummary == null ? "Chưa có phí" : feeSummary);

        // TODO TV4
        request.setAttribute("openRequests", statsDAO.countOpenRequestsByUserId(id));
        // TODO TV5
        request.setAttribute("newAnnouncements", statsDAO.countRecentAnnouncements());
    }
}
