package apartmentmanagement.controller.auth;

import apartmentmanagement.dao.BuildingDAO;
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


@WebServlet(name = "DashboardController", urlPatterns = {"/dashboard"})
public class DashboardController extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final DashboardStatsDAO statsDAO = new DashboardStatsDAO();
    private final BuildingDAO buildingDAO = new BuildingDAO();

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
        
        request.setAttribute("contentPage", "/WEB-INF/views/auth/dashboard.jsp");
        request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
    }

    private void loadAdminStats(HttpServletRequest request) {

        request.setAttribute("totalUsers", userDAO.countAll());
        request.setAttribute("lockedUsers", userDAO.countByActive(false));

        request.setAttribute("totalApartments", statsDAO.countApartments());
        request.setAttribute("totalBuildings", buildingDAO.countAll());
    }

    private void loadManagerStats(HttpServletRequest request) {
        
        request.setAttribute("pendingRequests",
                statsDAO.countRequestsByStatus(AppConstants.STATUS_PENDING));
        int assigned = statsDAO.countRequestsByStatus(AppConstants.STATUS_ASSIGNED);
        int inProgress = statsDAO.countRequestsByStatus(AppConstants.STATUS_IN_PROGRESS);
        request.setAttribute("processingRequests", assigned + inProgress);
        
        request.setAttribute("draftFees", statsDAO.countDraftFees());
    }

    private void loadStaffStats(HttpServletRequest request, Integer userId) {
        int id = userId == null ? 0 : userId;
        
        request.setAttribute("assignedJobs", statsDAO.countAssignedToStaff(id));
        request.setAttribute("inProgressJobs", statsDAO.countInProgressByStaff(id));
        request.setAttribute("completedWeek", statsDAO.countCompletedLast7DaysByStaff(id));
    }

    private void loadResidentStats(HttpServletRequest request, Integer userId) {
        int id = userId == null ? 0 : userId;

        
        String aptCode = statsDAO.findCurrentApartmentCodeByUserId(id);
        request.setAttribute("myApartment", aptCode == null ? "Chưa gán căn hộ" : aptCode);

        
        String feeSummary = statsDAO.findLatestFeeSummaryForUser(id);
        request.setAttribute("latestFee", feeSummary == null ? "Chưa có phí" : feeSummary);

        
        request.setAttribute("openRequests", statsDAO.countOpenRequestsByUserId(id));
        
        request.setAttribute("newAnnouncements", statsDAO.countRecentAnnouncements());
    }
}
