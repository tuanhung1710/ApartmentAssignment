package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.Request;
import apartmentmanagement.util.AppConstants;
import apartmentmanagement.util.DateTimeUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


public class RequestDAO extends DBContext {

    public Request getFromResultSet(ResultSet rs) throws SQLException {
        return Request.builder()
                .requestId(rs.getInt("request_id"))
                .apartmentId(rs.getInt("apartment_id"))
                .createdBy(rs.getInt("created_by"))
                .requestType(rs.getString("request_type"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .locationDetail(rs.getString("location_detail"))
                .urgency(rs.getString("urgency"))
                .vehicleType(rs.getString("vehicle_type"))
                .plateNumber(rs.getString("plate_number"))
                .scheduledAt(rs.getTimestamp("scheduled_at"))
                .moveNote(rs.getString("move_note"))
                .status(rs.getString("status"))
                .rejectReason(rs.getString("reject_reason"))
                .assignedTo(rs.getObject("assigned_to") == null ? null : rs.getInt("assigned_to"))
                .approvedBy(rs.getObject("approved_by") == null ? null : rs.getInt("approved_by"))
                .approvedAt(rs.getTimestamp("approved_at"))
                .completedAt(rs.getTimestamp("completed_at"))
                .createdAt(rs.getTimestamp("created_at"))
                .updatedAt(rs.getTimestamp("updated_at"))
                .build();
    }

    private void mapJoinFields(Request req, ResultSet rs) throws SQLException {
        try {
            req.setApartmentCode(rs.getString("apartment_code"));
        } catch (SQLException ignored) {
        }
        try {
            req.setCreatedByName(rs.getString("created_by_name"));
        } catch (SQLException ignored) {
        }
        try {
            req.setAssignedToName(rs.getString("assigned_to_name"));
        } catch (SQLException ignored) {
        }
    }

    private static final String SELECT_WITH_JOINS =
            "SELECT r.*, a.apartment_code, "
            + "cu.full_name AS created_by_name, "
            + "au.full_name AS assigned_to_name "
            + "FROM requests r "
            + "INNER JOIN apartments a ON a.apartment_id = r.apartment_id "
            + "INNER JOIN users cu ON cu.user_id = r.created_by "
            + "LEFT JOIN users au ON au.user_id = r.assigned_to ";

    public int insert(Request request) {
        // Ghi created_at realtime (tránh DEFAULT UTC của SQL Server)
        String sql = "INSERT INTO requests ("
                + "apartment_id, created_by, request_type, title, description, "
                + "location_detail, urgency, vehicle_type, plate_number, "
                + "scheduled_at, move_note, status, created_at"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, request.getApartmentId());
            statement.setInt(2, request.getCreatedBy());
            statement.setString(3, request.getRequestType());
            statement.setString(4, request.getTitle());
            statement.setString(5, request.getDescription());
            statement.setString(6, request.getLocationDetail());
            statement.setString(7, request.getUrgency());
            statement.setString(8, request.getVehicleType());
            statement.setString(9, request.getPlateNumber());
            if (request.getScheduledAt() == null) {
                statement.setNull(10, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(10, request.getScheduledAt());
            }
            statement.setString(11, request.getMoveNote());
            statement.setString(12, request.getStatus() == null
                    ? AppConstants.STATUS_PENDING : request.getStatus());
            Timestamp sentAt = request.getCreatedAt() != null
                    ? request.getCreatedAt()
                    : apartmentmanagement.util.DateTimeUtil.nowTimestamp();
            request.setCreatedAt(sentAt);
            statement.setTimestamp(13, sentAt);

            int affected = statement.executeUpdate();
            if (affected > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
            return affected > 0 ? 0 : -1;
        } catch (SQLException e) {
            System.out.println("RequestDAO.insert (with created_at) error: " + e.getMessage()
                    + " → fallback default column");
            closeResources();
            return insertLegacy(request);
        } finally {
            if (connection != null || statement != null || resultSet != null) {
                closeResources();
            }
        }
    }

    private int insertLegacy(Request request) {
        String sql = "INSERT INTO requests ("
                + "apartment_id, created_by, request_type, title, description, "
                + "location_detail, urgency, vehicle_type, plate_number, "
                + "scheduled_at, move_note, status"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, request.getApartmentId());
            statement.setInt(2, request.getCreatedBy());
            statement.setString(3, request.getRequestType());
            statement.setString(4, request.getTitle());
            statement.setString(5, request.getDescription());
            statement.setString(6, request.getLocationDetail());
            statement.setString(7, request.getUrgency());
            statement.setString(8, request.getVehicleType());
            statement.setString(9, request.getPlateNumber());
            if (request.getScheduledAt() == null) {
                statement.setNull(10, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(10, request.getScheduledAt());
            }
            statement.setString(11, request.getMoveNote());
            statement.setString(12, request.getStatus() == null
                    ? AppConstants.STATUS_PENDING : request.getStatus());
            if (request.getCreatedAt() == null) {
                request.setCreatedAt(apartmentmanagement.util.DateTimeUtil.nowTimestamp());
            }
            int affected = statement.executeUpdate();
            if (affected > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
            return affected > 0 ? 0 : -1;
        } catch (SQLException e) {
            System.out.println("RequestDAO.insertLegacy error: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    public Request findById(int requestId) {
        String sql = SELECT_WITH_JOINS + "WHERE r.request_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, requestId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Request req = getFromResultSet(resultSet);
                mapJoinFields(req, resultSet);
                return req;
            }
        } catch (SQLException e) {
            System.out.println("RequestDAO.findById error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    public List<Request> findByCreatedByWithFilters(int userId, String status, String requestType,
            String keyword, int page, int pageSize) {
        List<Request> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_WITH_JOINS + "WHERE r.created_by = ? ");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        appendFilters(sql, params, status, requestType, keyword);
        sql.append(" ORDER BY r.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(Math.max(0, (page - 1) * pageSize));
        params.add(pageSize);

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql.toString());
            setParams(params);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Request req = getFromResultSet(resultSet);
                mapJoinFields(req, resultSet);
                list.add(req);
            }
        } catch (SQLException e) {
            System.out.println("RequestDAO.findByCreatedByWithFilters error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public int countByCreatedByWithFilters(int userId, String status, String requestType, String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM requests r WHERE r.created_by = ? ");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        appendFilters(sql, params, status, requestType, keyword);
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql.toString());
            setParams(params);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("RequestDAO.countByCreatedByWithFilters error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    public int countOpenByCreatedBy(int userId) {
        String sql = "SELECT COUNT(*) FROM requests WHERE created_by = ? "
                + "AND status NOT IN (?, ?, ?)";
        try {
            connection = getConnection();
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
            System.out.println("RequestDAO.countOpenByCreatedBy error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    public boolean cancel(int requestId, int userId) {
        String sql = "UPDATE requests SET status = ?, updated_at = SYSUTCDATETIME() "
                + "WHERE request_id = ? AND created_by = ? AND status = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, AppConstants.STATUS_CANCELLED);
            statement.setInt(2, requestId);
            statement.setInt(3, userId);
            statement.setString(4, AppConstants.STATUS_PENDING);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("RequestDAO.cancel error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    // -------------------------------------------------------------------------
    // TV5 – xử lý request (Manager/Admin/Staff)
    // -------------------------------------------------------------------------

    public static final String ACTION_APPROVE = "APPROVE";
    public static final String ACTION_REJECT = "REJECT";
    public static final String ACTION_ASSIGN = "ASSIGN";
    public static final String ACTION_UPDATE_PROGRESS = "UPDATE_PROGRESS";
    public static final String ACTION_COMPLETE = "COMPLETE";

    /**
     * Danh sách request cho màn manage (lọc status/type/assignedTo + phân trang).
     * {@code assignedTo != null} → chỉ việc giao cho staff đó.
     */
    public List<Request> findWithFilters(String status, String requestType,
            Integer assignedTo, int page, int pageSize) {
        List<Request> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_WITH_JOINS + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendManageFilters(sql, params, status, requestType, assignedTo);
        sql.append(" ORDER BY r.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, pageSize);
        params.add((safePage - 1) * safeSize);
        params.add(safeSize);

        try {
            connection = getConnection();
            if (connection == null) {
                return list;
            }
            statement = connection.prepareStatement(sql.toString());
            setParams(params);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Request req = getFromResultSet(resultSet);
                mapJoinFields(req, resultSet);
                list.add(req);
            }
        } catch (SQLException e) {
            System.out.println("RequestDAO.findWithFilters error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public int countWithFilters(String status, String requestType, Integer assignedTo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM requests r WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendManageFilters(sql, params, status, requestType, assignedTo);
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql.toString());
            setParams(params);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("RequestDAO.countWithFilters error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM requests WHERE status = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, status);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("RequestDAO.countByStatus error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM requests";
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("RequestDAO.countAll error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    /** Ghi chú xử lý gần nhất (note history không rỗng). */
    public String findLatestProcessingNote(int requestId) {
        String sql = "SELECT TOP 1 h.note "
                + "FROM request_history h "
                + "WHERE h.request_id = ? AND h.note IS NOT NULL AND LTRIM(RTRIM(h.note)) <> '' "
                + "ORDER BY h.created_at DESC, h.history_id DESC";
        try {
            connection = getConnection();
            if (connection == null) {
                return null;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, requestId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("note");
            }
        } catch (SQLException e) {
            System.out.println("RequestDAO.findLatestProcessingNote error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    /** PENDING → APPROVED (+ history). */
    public boolean approveRequest(int requestId, int approvedBy, String note) {
        String updateSql = "UPDATE requests SET status = ?, approved_by = ?, approved_at = ?, updated_at = ? "
                + "WHERE request_id = ? AND status = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            connection.setAutoCommit(false);
            Timestamp now = DateTimeUtil.nowTimestamp();
            statement = connection.prepareStatement(updateSql);
            statement.setString(1, AppConstants.STATUS_APPROVED);
            statement.setInt(2, approvedBy);
            statement.setTimestamp(3, now);
            statement.setTimestamp(4, now);
            statement.setInt(5, requestId);
            statement.setString(6, AppConstants.STATUS_PENDING);
            int affected = statement.executeUpdate();
            statement.close();
            statement = null;
            if (affected != 1) {
                connection.rollback();
                return false;
            }
            insertProcessingHistory(requestId, approvedBy,
                    AppConstants.STATUS_PENDING, AppConstants.STATUS_APPROVED,
                    ACTION_APPROVE,
                    (note == null || note.trim().isEmpty()) ? "Phê duyệt yêu cầu" : note);
            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("RequestDAO.approveRequest error: " + e.getMessage());
            rollbackQuietly();
            return false;
        } finally {
            restoreAutoCommit();
            closeResources();
        }
    }

    /** PENDING → REJECTED (+ reject_reason, history). */
    public boolean rejectRequest(int requestId, int rejectedBy, String rejectReason) {
        String updateSql = "UPDATE requests SET status = ?, reject_reason = ?, approved_by = ?, "
                + "approved_at = ?, updated_at = ? WHERE request_id = ? AND status = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            connection.setAutoCommit(false);
            String reason = rejectReason == null ? "" : rejectReason.trim();
            Timestamp now = DateTimeUtil.nowTimestamp();
            statement = connection.prepareStatement(updateSql);
            statement.setString(1, AppConstants.STATUS_REJECTED);
            statement.setString(2, reason);
            statement.setInt(3, rejectedBy);
            statement.setTimestamp(4, now);
            statement.setTimestamp(5, now);
            statement.setInt(6, requestId);
            statement.setString(7, AppConstants.STATUS_PENDING);
            int affected = statement.executeUpdate();
            statement.close();
            statement = null;
            if (affected != 1) {
                connection.rollback();
                return false;
            }
            insertProcessingHistory(requestId, rejectedBy,
                    AppConstants.STATUS_PENDING, AppConstants.STATUS_REJECTED,
                    ACTION_REJECT, reason);
            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("RequestDAO.rejectRequest error: " + e.getMessage());
            rollbackQuietly();
            return false;
        } finally {
            restoreAutoCommit();
            closeResources();
        }
    }

    /** APPROVED → ASSIGNED (+ assigned_to, history). */
    public boolean assignStaff(int requestId, int staffId, int assignedBy, String note) {
        String updateSql = "UPDATE requests SET assigned_to = ?, status = ?, updated_at = ? "
                + "WHERE request_id = ? AND status = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(updateSql);
            statement.setInt(1, staffId);
            statement.setString(2, AppConstants.STATUS_ASSIGNED);
            statement.setTimestamp(3, DateTimeUtil.nowTimestamp());
            statement.setInt(4, requestId);
            statement.setString(5, AppConstants.STATUS_APPROVED);
            int affected = statement.executeUpdate();
            statement.close();
            statement = null;
            if (affected != 1) {
                connection.rollback();
                return false;
            }
            insertProcessingHistory(requestId, assignedBy,
                    AppConstants.STATUS_APPROVED, AppConstants.STATUS_ASSIGNED,
                    ACTION_ASSIGN,
                    (note == null || note.trim().isEmpty()) ? "Gán Staff xử lý" : note);
            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("RequestDAO.assignStaff error: " + e.getMessage());
            rollbackQuietly();
            return false;
        } finally {
            restoreAutoCommit();
            closeResources();
        }
    }

    /**
     * Staff cập nhật tiến độ (ASSIGNED/IN_PROGRESS → IN_PROGRESS/COMPLETED).
     * Chỉ staff được gán mới update được.
     */
    public boolean updateProgress(int requestId, int staffId, String oldStatus,
            String newStatus, String note) {
        boolean completed = AppConstants.STATUS_COMPLETED.equals(newStatus);
        String updateSql = "UPDATE requests SET status = ?, "
                + (completed ? "completed_at = ?, " : "")
                + "updated_at = ? "
                + "WHERE request_id = ? AND assigned_to = ? AND status = ? "
                + "AND status IN (?, ?)";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            connection.setAutoCommit(false);
            Timestamp now = DateTimeUtil.nowTimestamp();
            statement = connection.prepareStatement(updateSql);
            int idx = 1;
            statement.setString(idx++, newStatus);
            if (completed) {
                statement.setTimestamp(idx++, now);
            }
            statement.setTimestamp(idx++, now);
            statement.setInt(idx++, requestId);
            statement.setInt(idx++, staffId);
            statement.setString(idx++, oldStatus);
            statement.setString(idx++, AppConstants.STATUS_ASSIGNED);
            statement.setString(idx, AppConstants.STATUS_IN_PROGRESS);
            int affected = statement.executeUpdate();
            statement.close();
            statement = null;
            if (affected != 1) {
                connection.rollback();
                return false;
            }
            insertProcessingHistory(requestId, staffId, oldStatus, newStatus,
                    ACTION_UPDATE_PROGRESS,
                    (note == null || note.trim().isEmpty())
                            ? ("Cập nhật tiến độ: " + newStatus) : note);
            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("RequestDAO.updateProgress error: " + e.getMessage());
            rollbackQuietly();
            return false;
        } finally {
            restoreAutoCommit();
            closeResources();
        }
    }

    /** Staff hoàn thành nhanh (ASSIGNED/IN_PROGRESS → COMPLETED). */
    public boolean completeRequest(int requestId, int staffId, String oldStatus, String note) {
        String updateSql = "UPDATE requests SET status = ?, completed_at = ?, updated_at = ? "
                + "WHERE request_id = ? AND assigned_to = ? AND status = ? "
                + "AND status IN (?, ?)";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            connection.setAutoCommit(false);
            Timestamp now = DateTimeUtil.nowTimestamp();
            statement = connection.prepareStatement(updateSql);
            statement.setString(1, AppConstants.STATUS_COMPLETED);
            statement.setTimestamp(2, now);
            statement.setTimestamp(3, now);
            statement.setInt(4, requestId);
            statement.setInt(5, staffId);
            statement.setString(6, oldStatus);
            statement.setString(7, AppConstants.STATUS_ASSIGNED);
            statement.setString(8, AppConstants.STATUS_IN_PROGRESS);
            int affected = statement.executeUpdate();
            statement.close();
            statement = null;
            if (affected != 1) {
                connection.rollback();
                return false;
            }
            insertProcessingHistory(requestId, staffId, oldStatus,
                    AppConstants.STATUS_COMPLETED, ACTION_COMPLETE,
                    (note == null || note.trim().isEmpty()) ? "Hoàn thành yêu cầu" : note);
            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("RequestDAO.completeRequest error: " + e.getMessage());
            rollbackQuietly();
            return false;
        } finally {
            restoreAutoCommit();
            closeResources();
        }
    }

    private void appendManageFilters(StringBuilder sql, List<Object> params,
            String status, String requestType, Integer assignedTo) {
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND r.status = ? ");
            params.add(status.trim());
        }
        if (requestType != null && !requestType.trim().isEmpty()) {
            sql.append(" AND r.request_type = ? ");
            params.add(requestType.trim());
        }
        if (assignedTo != null) {
            sql.append(" AND r.assigned_to = ? ");
            params.add(assignedTo);
        }
    }

    private void insertProcessingHistory(int requestId, int changedBy,
            String previousStatus, String currentStatus,
            String action, String note) throws SQLException {
        String sql = "INSERT INTO request_history "
                + "(request_id, changed_by, old_status, new_status, note, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String historyNote = buildHistoryNote(action, note);
        statement = connection.prepareStatement(sql);
        statement.setInt(1, requestId);
        statement.setInt(2, changedBy);
        if (previousStatus == null || previousStatus.trim().isEmpty()) {
            statement.setNull(3, Types.NVARCHAR);
        } else {
            statement.setString(3, previousStatus);
        }
        statement.setString(4, currentStatus);
        statement.setString(5, historyNote);
        statement.setTimestamp(6, DateTimeUtil.nowTimestamp());
        statement.executeUpdate();
        statement.close();
        statement = null;
    }

    private String buildHistoryNote(String action, String note) {
        String actionPart = (action == null || action.trim().isEmpty())
                ? "PROCESS" : action.trim().toUpperCase();
        String msg = (note == null || note.trim().isEmpty()) ? "" : note.trim();
        if (msg.isEmpty()) {
            return "[" + actionPart + "]";
        }
        String full = "[" + actionPart + "] " + msg;
        return full.length() > 1000 ? full.substring(0, 1000) : full;
    }

    private void rollbackQuietly() {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException ex) {
            System.out.println("RequestDAO rollback error: " + ex.getMessage());
        }
    }

    private void restoreAutoCommit() {
        try {
            if (connection != null) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("RequestDAO setAutoCommit error: " + e.getMessage());
        }
    }

    private void appendFilters(StringBuilder sql, List<Object> params,
            String status, String requestType, String keyword) {
        if (status != null && !status.isEmpty()) {
            sql.append(" AND r.status = ? ");
            params.add(status);
        }
        if (requestType != null && !requestType.isEmpty()) {
            sql.append(" AND r.request_type = ? ");
            params.add(requestType);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (r.title LIKE ? OR r.description LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
    }

    private void setParams(List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object p = params.get(i);
            if (p instanceof Integer) {
                statement.setInt(i + 1, (Integer) p);
            } else if (p instanceof Timestamp) {
                statement.setTimestamp(i + 1, (Timestamp) p);
            } else {
                statement.setString(i + 1, p == null ? null : p.toString());
            }
        }
    }
}
