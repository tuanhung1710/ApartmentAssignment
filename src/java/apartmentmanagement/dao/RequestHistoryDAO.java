package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.RequestHistory;
import apartmentmanagement.util.DateTimeUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RequestHistoryDAO extends DBContext {

    public RequestHistory getFromResultSet(ResultSet rs) throws SQLException {
        return RequestHistory.builder()
                .historyId(rs.getInt("history_id"))
                .requestId(rs.getInt("request_id"))
                .changedBy(rs.getObject("changed_by") == null ? null : rs.getInt("changed_by"))
                .oldStatus(rs.getString("old_status"))
                .newStatus(rs.getString("new_status"))
                .note(rs.getString("note"))
                .createdAt(rs.getTimestamp("created_at"))
                .build();
    }

    private RequestHistory mapRow(ResultSet rs) throws SQLException {
        RequestHistory h = getFromResultSet(rs);
        h.setChangedByName(rs.getString("changed_by_name"));
        h.setChangedByRole(rs.getString("changed_by_role"));
        return h;
    }

    public int insert(RequestHistory history) {
        String sql = "INSERT INTO request_history "
                + "(request_id, changed_by, old_status, new_status, note, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, history.getRequestId());
            if (history.getChangedBy() == null) {
                statement.setObject(2, null);
            } else {
                statement.setInt(2, history.getChangedBy());
            }
            statement.setString(3, history.getOldStatus());
            statement.setString(4, history.getNewStatus());
            statement.setString(5, history.getNote());
            Timestamp sentAt = history.getCreatedAt() != null
                    ? history.getCreatedAt()
                    : DateTimeUtil.nowTimestamp();
            statement.setTimestamp(6, sentAt);
            int affected = statement.executeUpdate();
            if (affected > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
            return affected > 0 ? 0 : -1;
        } catch (SQLException e) {
            System.out.println("RequestHistoryDAO.insert (with created_at) error: " + e.getMessage()
                    + " → fallback default column");
            closeResources();
            return insertLegacy(history);
        } finally {
            if (connection != null || statement != null || resultSet != null) {
                closeResources();
            }
        }
    }

    private int insertLegacy(RequestHistory history) {
        String sql = "INSERT INTO request_history (request_id, changed_by, old_status, new_status, note) "
                + "VALUES (?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, history.getRequestId());
            if (history.getChangedBy() == null) {
                statement.setObject(2, null);
            } else {
                statement.setInt(2, history.getChangedBy());
            }
            statement.setString(3, history.getOldStatus());
            statement.setString(4, history.getNewStatus());
            statement.setString(5, history.getNote());
            int affected = statement.executeUpdate();
            if (affected > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
            return affected > 0 ? 0 : -1;
        } catch (SQLException e) {
            System.out.println("RequestHistoryDAO.insertLegacy error: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    public List<RequestHistory> findByRequestId(int requestId) {
        List<RequestHistory> list = new ArrayList<>();
        String sql = "SELECT rh.*, u.full_name AS changed_by_name, u.role AS changed_by_role "
                + "FROM request_history rh "
                + "LEFT JOIN users u ON u.user_id = rh.changed_by "
                + "WHERE rh.request_id = ? "
                + "ORDER BY rh.created_at ASC, rh.history_id ASC";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, requestId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("RequestHistoryDAO.findByRequestId error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    /** Status transitions only (exclude chat rows where old_status = new_status). */
    public List<RequestHistory> findStatusByRequestId(int requestId) {
        List<RequestHistory> list = new ArrayList<>();
        String sql = "SELECT rh.*, u.full_name AS changed_by_name, u.role AS changed_by_role "
                + "FROM request_history rh "
                + "LEFT JOIN users u ON u.user_id = rh.changed_by "
                + "WHERE rh.request_id = ? "
                + "AND (rh.old_status IS NULL OR rh.old_status <> rh.new_status) "
                + "ORDER BY rh.created_at ASC, rh.history_id ASC";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, requestId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("RequestHistoryDAO.findStatusByRequestId error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    /** Chat rows: old_status = new_status (no status change). */
    public List<RequestHistory> findCommentsByRequestId(int requestId) {
        List<RequestHistory> list = new ArrayList<>();
        String sql = "SELECT rh.*, u.full_name AS changed_by_name, u.role AS changed_by_role "
                + "FROM request_history rh "
                + "LEFT JOIN users u ON u.user_id = rh.changed_by "
                + "WHERE rh.request_id = ? "
                + "AND rh.old_status IS NOT NULL AND rh.old_status = rh.new_status "
                + "ORDER BY rh.created_at ASC, rh.history_id ASC";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, requestId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("RequestHistoryDAO.findCommentsByRequestId error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public List<RequestHistory> findCommentsAfter(int requestId, int afterHistoryId) {
        List<RequestHistory> list = new ArrayList<>();
        String sql = "SELECT rh.*, u.full_name AS changed_by_name, u.role AS changed_by_role "
                + "FROM request_history rh "
                + "LEFT JOIN users u ON u.user_id = rh.changed_by "
                + "WHERE rh.request_id = ? "
                + "AND rh.history_id > ? "
                + "AND rh.old_status IS NOT NULL AND rh.old_status = rh.new_status "
                + "ORDER BY rh.created_at ASC, rh.history_id ASC";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, requestId);
            statement.setInt(2, afterHistoryId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("RequestHistoryDAO.findCommentsAfter error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public RequestHistory findById(int historyId) {
        String sql = "SELECT rh.*, u.full_name AS changed_by_name, u.role AS changed_by_role "
                + "FROM request_history rh "
                + "LEFT JOIN users u ON u.user_id = rh.changed_by "
                + "WHERE rh.history_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, historyId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapRow(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("RequestHistoryDAO.findById error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }
}
