package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.Request;
import apartmentmanagement.model.RequestHistory;
import apartmentmanagement.util.DateTimeUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RequestDAO extends DBContext {

    public Request getFromResultSet(ResultSet rs) throws SQLException {
        Integer assignedTo = (Integer) rs.getObject("assigned_to");
        Integer approvedBy = null;
        try {
            approvedBy = (Integer) rs.getObject("approved_by");
        } catch (SQLException ignored) {

        }

        Request.RequestBuilder builder = Request.builder()
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
                .assignedTo(assignedTo)
                .approvedBy(approvedBy)
                .approvedAt(rs.getTimestamp("approved_at"))
                .completedAt(rs.getTimestamp("completed_at"))
                .createdAt(rs.getTimestamp("created_at"))
                .updatedAt(rs.getTimestamp("updated_at"));

        try {
            builder.apartmentCode(rs.getString("apartment_code"));
        } catch (SQLException ignored) {
        }
        try {
            builder.createdByName(rs.getString("created_by_name"));
        } catch (SQLException ignored) {
        }
        try {
            builder.assignedToName(rs.getString("assigned_to_name"));
        } catch (SQLException ignored) {
        }

        return builder.build();
    }

    public List<Request> findWithFilters(String status, String requestType,
                                         Integer assignedTo, int page, int pageSize) {
        List<Request> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT r.*, a.apartment_code, "
                        + "creator.full_name AS created_by_name, "
                        + "staff.full_name AS assigned_to_name "
                        + "FROM requests r "
                        + "INNER JOIN apartments a ON r.apartment_id = a.apartment_id "
                        + "INNER JOIN users creator ON r.created_by = creator.user_id "
                        + "LEFT JOIN users staff ON r.assigned_to = staff.user_id "
                        + "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        appendListFilters(sql, params, status, requestType, assignedTo);
        sql.append("ORDER BY r.created_at DESC ");
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("RequestDAO.findWithFilters error: cannot connect to database");
                return list;
            }
            statement = connection.prepareStatement(sql.toString());
            int idx = 1;
            for (Object p : params) {
                statement.setObject(idx++, p);
            }
            int offset = Math.max(0, (page - 1) * pageSize);
            statement.setInt(idx++, offset);
            statement.setInt(idx, pageSize);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("RequestDAO.findWithFilters error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public int countWithFilters(String status, String requestType, Integer assignedTo) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM requests r WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        appendListFilters(sql, params, status, requestType, assignedTo);

        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("RequestDAO.countWithFilters error: cannot connect to database");
                return 0;
            }
            statement = connection.prepareStatement(sql.toString());
            int idx = 1;
            for (Object p : params) {
                statement.setObject(idx++, p);
            }
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

    private void appendListFilters(StringBuilder sql, List<Object> params,
                                   String status, String requestType, Integer assignedTo) {
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND r.status = ? ");
            params.add(status.trim());
        }
        if (requestType != null && !requestType.trim().isEmpty()) {
            sql.append("AND r.request_type = ? ");
            params.add(requestType.trim());
        }

        if (assignedTo != null) {
            sql.append("AND r.assigned_to = ? ");
            params.add(assignedTo);
        }
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM requests WHERE status = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("RequestDAO.countByStatus error: cannot connect to database");
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
                System.out.println("RequestDAO.countAll error: cannot connect to database");
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

    public Request findById(int requestId) {
        String sql = "SELECT r.*, a.apartment_code, "
                + "creator.full_name AS created_by_name, "
                + "staff.full_name AS assigned_to_name "
                + "FROM requests r "
                + "INNER JOIN apartments a ON r.apartment_id = a.apartment_id "
                + "INNER JOIN users creator ON r.created_by = creator.user_id "
                + "LEFT JOIN users staff ON r.assigned_to = staff.user_id "
                + "WHERE r.request_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("RequestDAO.findById error: cannot connect to database");
                return null;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, requestId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("RequestDAO.findById error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    public RequestHistory getHistoryFromResultSet(ResultSet rs) throws SQLException {
        return RequestHistory.builder()
                .historyId(rs.getInt("history_id"))
                .requestId(rs.getInt("request_id"))
                .changedBy((Integer) rs.getObject("changed_by"))
                .oldStatus(rs.getString("old_status"))
                .newStatus(rs.getString("new_status"))
                .note(rs.getString("note"))
                .createdAt(rs.getTimestamp("created_at"))
                .changedByName(rs.getString("changed_by_name"))
                .build();
    }

    public List<RequestHistory> findHistoryByRequestId(int requestId) {
        List<RequestHistory> list = new ArrayList<>();
        String sql = "SELECT h.*, u.full_name AS changed_by_name "
                + "FROM request_history h "
                + "LEFT JOIN users u ON h.changed_by = u.user_id "
                + "WHERE h.request_id = ? "
                + "ORDER BY h.created_at ASC, h.history_id ASC";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("RequestDAO.findHistoryByRequestId error: cannot connect to database");
                return list;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, requestId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getHistoryFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("RequestDAO.findHistoryByRequestId error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public String findLatestProcessingNote(int requestId) {
        String sql = "SELECT TOP 1 h.note "
                + "FROM request_history h "
                + "WHERE h.request_id = ? AND h.note IS NOT NULL AND LTRIM(RTRIM(h.note)) <> '' "
                + "ORDER BY h.created_at DESC, h.history_id DESC";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("RequestDAO.findLatestProcessingNote error: cannot connect to database");
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

    // Map: request_id | changed_by(User) | old_status(Previous) | new_status(Current)
    //      | note (Action + Note) | created_at(Updated Date)

    public static final String ACTION_APPROVE = "APPROVE";
    public static final String ACTION_REJECT = "REJECT";
    public static final String ACTION_ASSIGN = "ASSIGN";
    public static final String ACTION_UPDATE_PROGRESS = "UPDATE_PROGRESS";
    public static final String ACTION_COMPLETE = "COMPLETE";

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
            statement.setNull(3, java.sql.Types.NVARCHAR);
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
        // note DB max 1000
        String full = "[" + actionPart + "] " + msg;
        return full.length() > 1000 ? full.substring(0, 1000) : full;
    }

    // UC-PROC-03: Approve – ghi history APPROVE

    public boolean approveRequest(int requestId, int approvedBy, String note) {
        String updateSql = "UPDATE requests "
                + "SET status = N'APPROVED', "
                + "    approved_by = ?, "
                + "    approved_at = ?, "
                + "    updated_at = ? "
                + "WHERE request_id = ? AND status = N'PENDING'";

        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("RequestDAO.approveRequest error: cannot connect to database");
                return false;
            }
            connection.setAutoCommit(false);

            Timestamp now = DateTimeUtil.nowTimestamp();
            statement = connection.prepareStatement(updateSql);
            statement.setInt(1, approvedBy);
            statement.setTimestamp(2, now);
            statement.setTimestamp(3, now);
            statement.setInt(4, requestId);
            int affected = statement.executeUpdate();
            statement.close();
            statement = null;

            if (affected != 1) {
                connection.rollback();
                return false;
            }

            // UC-PROC-10
            insertProcessingHistory(requestId, approvedBy,
                    "PENDING", "APPROVED", ACTION_APPROVE,
                    (note == null || note.trim().isEmpty()) ? "Phê duyệt yêu cầu" : note);

            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("RequestDAO.approveRequest error: " + e.getMessage());
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("RequestDAO.approveRequest rollback error: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.out.println("RequestDAO.approveRequest setAutoCommit error: " + e.getMessage());
            }
            closeResources();
        }
    }

    // UC-PROC-04: Reject – ghi history REJECT

    public boolean rejectRequest(int requestId, int rejectedBy, String rejectReason) {
        String updateSql = "UPDATE requests "
                + "SET status = N'REJECTED', "
                + "    reject_reason = ?, "
                + "    approved_by = ?, "
                + "    approved_at = ?, "
                + "    updated_at = ? "
                + "WHERE request_id = ? AND status = N'PENDING'";

        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("RequestDAO.rejectRequest error: cannot connect to database");
                return false;
            }
            connection.setAutoCommit(false);

            String reason = rejectReason == null ? "" : rejectReason.trim();
            Timestamp now = DateTimeUtil.nowTimestamp();

            statement = connection.prepareStatement(updateSql);
            statement.setString(1, reason);
            statement.setInt(2, rejectedBy);
            statement.setTimestamp(3, now);
            statement.setTimestamp(4, now);
            statement.setInt(5, requestId);
            int affected = statement.executeUpdate();
            statement.close();
            statement = null;

            if (affected != 1) {
                connection.rollback();
                return false;
            }

            // UC-PROC-10
            insertProcessingHistory(requestId, rejectedBy,
                    "PENDING", "REJECTED", ACTION_REJECT, reason);

            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("RequestDAO.rejectRequest error: " + e.getMessage());
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("RequestDAO.rejectRequest rollback error: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.out.println("RequestDAO.rejectRequest setAutoCommit error: " + e.getMessage());
            }
            closeResources();
        }
    }

    // UC-PROC-05: Assign Staff – ghi history ASSIGN

    public boolean assignStaff(int requestId, int staffId, int assignedBy, String note) {
        String updateSql = "UPDATE requests "
                + "SET assigned_to = ?, "
                + "    status = N'ASSIGNED', "
                + "    updated_at = ? "
                + "WHERE request_id = ? AND status = N'APPROVED'";

        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("RequestDAO.assignStaff error: cannot connect to database");
                return false;
            }
            connection.setAutoCommit(false);

            statement = connection.prepareStatement(updateSql);
            statement.setInt(1, staffId);
            statement.setTimestamp(2, DateTimeUtil.nowTimestamp());
            statement.setInt(3, requestId);
            int affected = statement.executeUpdate();
            statement.close();
            statement = null;

            if (affected != 1) {
                connection.rollback();
                return false;
            }

            // UC-PROC-10
            insertProcessingHistory(requestId, assignedBy,
                    "APPROVED", "ASSIGNED", ACTION_ASSIGN,
                    (note == null || note.trim().isEmpty()) ? "Gán Staff xử lý" : note);

            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("RequestDAO.assignStaff error: " + e.getMessage());
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("RequestDAO.assignStaff rollback error: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.out.println("RequestDAO.assignStaff setAutoCommit error: " + e.getMessage());
            }
            closeResources();
        }
    }

    // UC-PROC-07: Update Progress – ghi history UPDATE_PROGRESS

    public boolean updateProgress(int requestId, int staffId, String oldStatus,
                                  String newStatus, String note) {
        boolean completed = "COMPLETED".equals(newStatus);
        String updateSql = "UPDATE requests SET "
                + "status = ?, "
                + (completed ? "completed_at = ?, " : "")
                + "updated_at = ? "
                + "WHERE request_id = ? "
                + "AND assigned_to = ? "
                + "AND status = ? "
                + "AND status IN (N'ASSIGNED', N'IN_PROGRESS')";

        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("RequestDAO.updateProgress error: cannot connect to database");
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
            statement.setString(idx, oldStatus);
            int affected = statement.executeUpdate();
            statement.close();
            statement = null;

            if (affected != 1) {
                connection.rollback();
                return false;
            }

            // UC-PROC-10
            insertProcessingHistory(requestId, staffId,
                    oldStatus, newStatus, ACTION_UPDATE_PROGRESS,
                    (note == null || note.trim().isEmpty())
                            ? ("Cập nhật tiến độ: " + newStatus) : note);

            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("RequestDAO.updateProgress error: " + e.getMessage());
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("RequestDAO.updateProgress rollback error: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.out.println("RequestDAO.updateProgress setAutoCommit error: " + e.getMessage());
            }
            closeResources();
        }
    }

    // UC-PROC-09: Complete – ghi history COMPLETE

    public boolean completeRequest(int requestId, int staffId, String oldStatus, String note) {
        String updateSql = "UPDATE requests SET "
                + "status = N'COMPLETED', "
                + "completed_at = ?, "
                + "updated_at = ? "
                + "WHERE request_id = ? "
                + "AND assigned_to = ? "
                + "AND status = ? "
                + "AND status IN (N'ASSIGNED', N'IN_PROGRESS')";

        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("RequestDAO.completeRequest error: cannot connect to database");
                return false;
            }
            connection.setAutoCommit(false);

            Timestamp now = DateTimeUtil.nowTimestamp();
            statement = connection.prepareStatement(updateSql);
            statement.setTimestamp(1, now);
            statement.setTimestamp(2, now);
            statement.setInt(3, requestId);
            statement.setInt(4, staffId);
            statement.setString(5, oldStatus);
            int affected = statement.executeUpdate();
            statement.close();
            statement = null;

            if (affected != 1) {
                connection.rollback();
                return false;
            }

            // UC-PROC-10
            insertProcessingHistory(requestId, staffId,
                    oldStatus, "COMPLETED", ACTION_COMPLETE,
                    (note == null || note.trim().isEmpty()) ? "Hoàn thành yêu cầu" : note);

            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("RequestDAO.completeRequest error: " + e.getMessage());
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("RequestDAO.completeRequest rollback error: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.out.println("RequestDAO.completeRequest setAutoCommit error: " + e.getMessage());
            }
            closeResources();
        }
    }
}
