package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.ApartmentHistory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Lịch sử thao tác căn hộ (apartment_history). */
public class ApartmentHistoryDAO extends DBContext {

    private String lastError;
    private static volatile boolean tableEnsured = false;

    public String getLastError() {
        return lastError;
    }

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

    /** Tạo bảng nếu chưa có (tránh quên chạy SQL). */
    public boolean ensureTable() {
        if (tableEnsured) {
            return true;
        }
        String ddl = "IF OBJECT_ID(N'dbo.apartment_history', N'U') IS NULL "
                + "CREATE TABLE dbo.apartment_history ("
                + " history_id INT IDENTITY(1,1) PRIMARY KEY,"
                + " apartment_id INT NOT NULL,"
                + " action NVARCHAR(50) NOT NULL,"
                + " old_status NVARCHAR(20) NULL,"
                + " new_status NVARCHAR(20) NULL,"
                + " note NVARCHAR(500) NULL,"
                + " actor_user_id INT NULL,"
                + " actor_name NVARCHAR(100) NULL,"
                + " created_at DATETIME2 NOT NULL CONSTRAINT DF_ah_created_auto DEFAULT (SYSUTCDATETIME())"
                + ")";
        try {
            connection = getConnection();
            if (connection == null) {
                lastError = "Không kết nối được database.";
                return false;
            }
            statement = connection.prepareStatement(ddl);
            statement.execute();
            tableEnsured = true;
            return true;
        } catch (SQLException e) {
            lastError = e.getMessage();
            System.out.println("ApartmentHistoryDAO.ensureTable: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public List<ApartmentHistory> findByApartmentId(int apartmentId, int limit) {
        List<ApartmentHistory> list = new ArrayList<>();
        if (limit < 1) {
            limit = 50;
        }
        ensureTable();
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
            lastError = e.getMessage();
        } finally {
            closeResources();
        }
        return list;
    }

    /**
     * @return id > 0 | 0 OK no key | -1 fail
     */
    public int insert(ApartmentHistory h) {
        lastError = null;
        if (!ensureTable()) {
            return -1;
        }
        String sql = "INSERT INTO apartment_history "
                + "(apartment_id, action, old_status, new_status, note, actor_user_id, actor_name) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            if (connection == null) {
                lastError = "Không kết nối được database.";
                return -1;
            }
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, h.getApartmentId());
            statement.setString(2, h.getAction());
            setNullableString(3, h.getOldStatus());
            setNullableString(4, h.getNewStatus());
            setNullableString(5, h.getNote());
            if (h.getActorUserId() == null) {
                statement.setNull(6, Types.INTEGER);
            } else {
                statement.setInt(6, h.getActorUserId());
            }
            setNullableString(7, h.getActorName());
            int affected = statement.executeUpdate();
            if (affected > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
            lastError = "Insert history không ảnh hưởng dòng nào.";
            return -1;
        } catch (SQLException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("Invalid object name") && msg.toLowerCase().contains("apartment_history")) {
                lastError = "Bảng apartment_history chưa có. Chạy database/schema.sql rồi seed.sql";
                tableEnsured = false;
            } else {
                lastError = msg;
            }
            System.out.println("ApartmentHistoryDAO.insert: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    private void setNullableString(int index, String value) throws SQLException {
        if (value == null || value.isEmpty()) {
            statement.setNull(index, Types.NVARCHAR);
        } else {
            statement.setString(index, value);
        }
    }
}
