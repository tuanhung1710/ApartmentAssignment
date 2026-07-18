package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.util.AppConstants;
import java.sql.SQLException;

/**
 * DAO thống kê nhẹ cho dashboard theo role (admin / staff / resident).
 * Các count trả 0 khi lỗi SQL để UI không crash.
 */
public class DashboardStatsDAO extends DBContext {

    /**
     * Tổng số căn hộ trong hệ thống.
     *
     * @return count; lỗi → 0
     */
    public int countApartments() {
        return safeCount("SELECT COUNT(*) FROM apartments", null);
    }

    /**
     * Đếm request theo status.
     *
     * @param status status request (vd. PENDING, ASSIGNED…)
     * @return count; lỗi → 0
     */
    public int countRequestsByStatus(String status) {
        return safeCount("SELECT COUNT(*) FROM requests WHERE status = ?", status);
    }

    /**
     * Số phiếu phí đang DRAFT (chưa chốt).
     *
     * @return count; lỗi → 0
     */
    public int countDraftFees() {
        return safeCount("SELECT COUNT(*) FROM monthly_fees WHERE status = ?", AppConstants.FEE_DRAFT);
    }

    /**
     * Request đã gán cho staff và còn status ASSIGNED.
     *
     * @param staffUserId id user staff
     * @return count; lỗi → 0
     */
    public int countAssignedToStaff(int staffUserId) {
        return safeCountIntString(
                "SELECT COUNT(*) FROM requests WHERE assigned_to = ? AND status = ?",
                staffUserId, AppConstants.STATUS_ASSIGNED);
    }

    /**
     * Request staff đang xử lý (IN_PROGRESS).
     *
     * @param staffUserId id user staff
     * @return count; lỗi → 0
     */
    public int countInProgressByStaff(int staffUserId) {
        return safeCountIntString(
                "SELECT COUNT(*) FROM requests WHERE assigned_to = ? AND status = ?",
                staffUserId, AppConstants.STATUS_IN_PROGRESS);
    }

    /**
     * Request staff hoàn thành trong 7 ngày gần nhất.
     *
     * @param staffUserId id user staff
     * @return count; lỗi → 0
     */
    public int countCompletedLast7DaysByStaff(int staffUserId) {
        String sql = "SELECT COUNT(*) FROM requests WHERE assigned_to = ? "
                + "AND status = ? AND completed_at IS NOT NULL "
                + "AND completed_at >= DATEADD(day, -7, SYSUTCDATETIME())";
        return safeCountIntString(sql, staffUserId, AppConstants.STATUS_COMPLETED);
    }

    /**
     * Mã căn hiện tại của resident (apartment_residents.is_current=1, mới nhất).
     *
     * @param userId id user
     * @return apartment_code hoặc null
     */
    public String findCurrentApartmentCodeByUserId(int userId) {
        String sql = "SELECT TOP 1 a.apartment_code "
                + "FROM apartments a "
                + "INNER JOIN apartment_residents ar ON a.apartment_id = ar.apartment_id "
                + "WHERE ar.user_id = ? AND ar.is_current = 1 "
                + "ORDER BY ar.start_date DESC";
        try {
            connection = getConnection();
            if (connection == null) {
                return null;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("apartment_code");
            }
        } catch (SQLException e) {
            System.out.println("DashboardStatsDAO.findCurrentApartmentCodeByUserId: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    /**
     * Tóm tắt phiếu phí mới nhất của căn mà user đang ở
     * (format: {@code MM/yyyy · amount đ · status}).
     *
     * @param userId id user resident
     * @return chuỗi tóm tắt hoặc null
     */
    public String findLatestFeeSummaryForUser(int userId) {
        String sql = "SELECT TOP 1 mf.fee_month, mf.fee_year, mf.total_amount, mf.status "
                + "FROM monthly_fees mf "
                + "INNER JOIN apartment_residents ar ON mf.apartment_id = ar.apartment_id "
                + "WHERE ar.user_id = ? AND ar.is_current = 1 "
                + "ORDER BY mf.fee_year DESC, mf.fee_month DESC, mf.fee_id DESC";
        try {
            connection = getConnection();
            if (connection == null) {
                return null;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int month = resultSet.getInt("fee_month");
                int year = resultSet.getInt("fee_year");
                java.math.BigDecimal total = resultSet.getBigDecimal("total_amount");
                String status = resultSet.getString("status");
                String totalText = total == null ? "0" : total.toPlainString();
                return month + "/" + year + " · " + totalText + " đ · " + status;
            }
        } catch (SQLException e) {
            System.out.println("DashboardStatsDAO.findLatestFeeSummaryForUser: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    /**
     * Số request do user tạo còn mở (không COMPLETED / CANCELLED / REJECTED).
     *
     * @param userId id user
     * @return count; lỗi → 0
     */
    public int countOpenRequestsByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM requests WHERE created_by = ? "
                + "AND status NOT IN (?, ?, ?)";
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setString(2, AppConstants.STATUS_COMPLETED);
            statement.setString(3, AppConstants.STATUS_CANCELLED);
            statement.setString(4, AppConstants.STATUS_REJECTED);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("DashboardStatsDAO.countOpenRequestsByUserId: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    /**
     * Số thông báo đã publish trong 30 ngày gần nhất
     * (hoặc published_at null nhưng is_published=1).
     *
     * @return count; lỗi → 0
     */
    public int countRecentAnnouncements() {
        String sql = "SELECT COUNT(*) FROM announcements "
                + "WHERE is_published = 1 "
                + "AND (published_at IS NULL OR published_at >= DATEADD(day, -30, SYSUTCDATETIME()))";
        return safeCount(sql, null);
    }

    /** COUNT(*) với 0 hoặc 1 tham số String; lỗi → 0. */
    private int safeCount(String sql, String stringParam) {
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql);
            if (stringParam != null) {
                statement.setString(1, stringParam);
            }
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("DashboardStatsDAO.safeCount: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    /** COUNT(*) với tham số (int, String); lỗi → 0. */
    private int safeCountIntString(String sql, int intParam, String stringParam) {
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, intParam);
            statement.setString(2, stringParam);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("DashboardStatsDAO.safeCountIntString: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }
}
