package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.Request;
import apartmentmanagement.util.AppConstants;
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
