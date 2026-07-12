package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.ApartmentHistory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ApartmentHistoryDAO extends DBContext {

    public ApartmentHistory getFromResultSet(ResultSet rs) throws SQLException {
        return ApartmentHistory.builder()
                .historyId(rs.getInt("history_id"))
                .apartmentId(rs.getInt("apartment_id"))
                .action(rs.getString("action"))
                .oldStatus(rs.getString("old_status"))
                .newStatus(rs.getString("new_status"))
                .note(rs.getString("note"))
                .actorUserId(rs.getObject("actor_user_id") != null ? rs.getInt("actor_user_id") : null)
                .actorName(rs.getString("actor_name"))
                .createdAt(rs.getTimestamp("created_at"))
                .build();
    }

    public List<ApartmentHistory> findByApartmentId(int apartmentId, int limit) {
        List<ApartmentHistory> list = new ArrayList<>();
        if (limit < 1) {
            limit = 20;
        }
        String sql = "SELECT TOP (" + limit + ") * FROM apartment_history "
                + "WHERE apartment_id = ? ORDER BY created_at DESC, history_id DESC";
        try {
            connection = getConnection();
            if (connection == null) {
                return list;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("ApartmentHistoryDAO.findByApartmentId: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public int insert(ApartmentHistory h) {
        String sql = "INSERT INTO apartment_history "
                + "(apartment_id, action, old_status, new_status, note, actor_user_id, actor_name) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            if (connection == null) {
                return -1;
            }
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, h.getApartmentId());
            statement.setString(2, h.getAction());
            statement.setString(3, h.getOldStatus());
            statement.setString(4, h.getNewStatus());
            statement.setString(5, h.getNote());
            if (h.getActorUserId() == null) {
                statement.setObject(6, null);
            } else {
                statement.setInt(6, h.getActorUserId());
            }
            statement.setString(7, h.getActorName());
            int affected = statement.executeUpdate();
            if (affected > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
            return -1;
        } catch (SQLException e) {
            System.out.println("ApartmentHistoryDAO.insert: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }
}
