package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.ApartmentResident;
import apartmentmanagement.util.AppConstants;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Gán user vào căn: OWNER / TENANT_REP / TENANT. */
public class ApartmentResidentDAO extends DBContext {

    /** Lỗi SQL gần nhất (để controller hiện message rõ). */
    private String lastError;

    public String getLastError() {
        return lastError;
    }

    private void clearError() {
        lastError = null;
    }

    public ApartmentResident getFromResultSet(ResultSet rs) throws SQLException {
        return ApartmentResident.builder()
                .id(rs.getInt("id"))
                .apartmentId(rs.getInt("apartment_id"))
                .userId(rs.getInt("user_id"))
                .roleInApartment(rs.getString("role_in_apartment"))
                .isCurrent(rs.getBoolean("is_current"))
                .startDate(rs.getDate("start_date"))
                .endDate(rs.getDate("end_date"))
                .createdAt(rs.getTimestamp("created_at"))
                .username(safeGetString(rs, "username"))
                .fullName(safeGetString(rs, "full_name"))
                .apartmentCode(null)
                .build();
    }

    private String safeGetString(ResultSet rs, String col) {
        try {
            return rs.getString(col);
        } catch (SQLException e) {
            return null;
        }
    }

    public List<ApartmentResident> findByApartmentAndRoles(int apartmentId, String... roles) {
        List<ApartmentResident> list = new ArrayList<>();
        if (roles == null || roles.length == 0) {
            return list;
        }
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < roles.length; i++) {
            if (i > 0) {
                in.append(',');
            }
            in.append('?');
        }
        String sql = "SELECT ar.*, u.username, u.full_name "
                + "FROM apartment_residents ar "
                + "LEFT JOIN users u ON u.user_id = ar.user_id "
                + "WHERE ar.apartment_id = ? AND ar.is_current = 1 "
                + "AND ar.role_in_apartment IN (" + in + ") "
                + "ORDER BY ar.start_date DESC, ar.id DESC";
        try {
            connection = getConnection();
            if (connection == null) {
                return list;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            for (int i = 0; i < roles.length; i++) {
                statement.setString(2 + i, roles[i]);
            }
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("ApartmentResidentDAO.findByApartmentAndRoles: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public ApartmentResident findCurrentOwner(int apartmentId) {
        List<ApartmentResident> list = findByApartmentAndRoles(apartmentId, AppConstants.APT_ROLE_OWNER);
        return list.isEmpty() ? null : list.get(0);
    }

    public ApartmentResident findCurrentTenantRep(int apartmentId) {
        List<ApartmentResident> list = findByApartmentAndRoles(apartmentId, AppConstants.APT_ROLE_TENANT_REP);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean isCurrentResident(int apartmentId, int userId) {
        String sql = "SELECT 1 FROM apartment_residents "
                + "WHERE apartment_id = ? AND user_id = ? AND is_current = 1";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            statement.setInt(2, userId);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("ApartmentResidentDAO.isCurrentResident: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public boolean isCurrentWithRole(int apartmentId, int userId, String roleInApartment) {
        String sql = "SELECT 1 FROM apartment_residents "
                + "WHERE apartment_id = ? AND user_id = ? AND role_in_apartment = ? AND is_current = 1";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            statement.setInt(2, userId);
            statement.setString(3, roleInApartment);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("ApartmentResidentDAO.isCurrentWithRole: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public int endCurrentOwners(int apartmentId, Date endDate) {
        return endCurrentByRole(apartmentId, AppConstants.APT_ROLE_OWNER, endDate);
    }

    public int endCurrentTenantReps(int apartmentId, Date endDate) {
        return endCurrentByRole(apartmentId, AppConstants.APT_ROLE_TENANT_REP, endDate);
    }

    /**
     * Gỡ role hiện tại an toàn với UNIQUE (apartment_id, user_id, role, is_current):
     * - Xóa các bản ghi lịch sử cùng (căn, user, role, is_current=0) để tránh trùng key
     * - Rồi UPDATE is_current 1 → 0
     */
    private int endCurrentByRole(int apartmentId, String role, Date endDate) {
        clearError();
        try {
            connection = getConnection();
            if (connection == null) {
                lastError = "Không kết nối được database.";
                return -1;
            }

            // 1) Lấy các user đang current với role này
            statement = connection.prepareStatement(
                    "SELECT user_id FROM apartment_residents "
                    + "WHERE apartment_id = ? AND role_in_apartment = ? AND is_current = 1");
            statement.setInt(1, apartmentId);
            statement.setString(2, role);
            resultSet = statement.executeQuery();
            List<Integer> userIds = new ArrayList<>();
            while (resultSet.next()) {
                userIds.add(resultSet.getInt(1));
            }
            try {
                resultSet.close();
            } catch (SQLException ignored) {
            }
            try {
                statement.close();
            } catch (SQLException ignored) {
            }

            if (userIds.isEmpty()) {
                return 0;
            }

            // 2) Xóa lịch sử is_current=0 cùng key (tránh UQ_ar_apartment_user_role)
            statement = connection.prepareStatement(
                    "DELETE FROM apartment_residents "
                    + "WHERE apartment_id = ? AND user_id = ? AND role_in_apartment = ? AND is_current = 0");
            for (Integer uid : userIds) {
                statement.setInt(1, apartmentId);
                statement.setInt(2, uid);
                statement.setString(3, role);
                statement.executeUpdate();
            }
            try {
                statement.close();
            } catch (SQLException ignored) {
            }

            // 3) Đóng bản ghi current
            statement = connection.prepareStatement(
                    "UPDATE apartment_residents SET is_current = 0, end_date = ? "
                    + "WHERE apartment_id = ? AND role_in_apartment = ? AND is_current = 1");
            statement.setDate(1, endDate);
            statement.setInt(2, apartmentId);
            statement.setString(3, role);
            return statement.executeUpdate();
        } catch (SQLException e) {
            lastError = friendlySqlError(e);
            System.out.println("ApartmentResidentDAO.endCurrentByRole(" + role + "): " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    /**
     * Gỡ hẳn OWNER hiện tại (DELETE) — dùng cho "Gỡ owner".
     * Không giữ is_current=0 nên không đụng UNIQUE (apt,user,role,0).
     */
    public int deleteCurrentOwners(int apartmentId) {
        return deleteCurrentByRole(apartmentId, AppConstants.APT_ROLE_OWNER);
    }

    /** Gỡ hẳn TENANT_REP + TENANT đang current. */
    public int deleteCurrentTenants(int apartmentId) {
        clearError();
        String sql = "DELETE FROM apartment_residents "
                + "WHERE apartment_id = ? AND is_current = 1 "
                + "AND role_in_apartment IN (?, ?)";
        try {
            connection = getConnection();
            if (connection == null) {
                lastError = "Không kết nối được database.";
                return -1;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            statement.setString(2, AppConstants.APT_ROLE_TENANT_REP);
            statement.setString(3, AppConstants.APT_ROLE_TENANT);
            return statement.executeUpdate();
        } catch (SQLException e) {
            lastError = friendlySqlError(e);
            System.out.println("ApartmentResidentDAO.deleteCurrentTenants: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    public int deleteCurrentByRole(int apartmentId, String role) {
        clearError();
        String sql = "DELETE FROM apartment_residents "
                + "WHERE apartment_id = ? AND role_in_apartment = ? AND is_current = 1";
        try {
            connection = getConnection();
            if (connection == null) {
                lastError = "Không kết nối được database.";
                return -1;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            statement.setString(2, role);
            return statement.executeUpdate();
        } catch (SQLException e) {
            lastError = friendlySqlError(e);
            System.out.println("ApartmentResidentDAO.deleteCurrentByRole(" + role + "): " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    public int insertOwner(int apartmentId, int userId, Date startDate) {
        return insertResident(apartmentId, userId, AppConstants.APT_ROLE_OWNER, startDate, null);
    }

    public int insertTenant(int apartmentId, int userId, String roleInApartment,
            Date startDate, Date endDate) {
        return insertResident(apartmentId, userId, roleInApartment, startDate, endDate);
    }

    private int insertResident(int apartmentId, int userId, String role,
            Date startDate, Date endDate) {
        clearError();
        try {
            connection = getConnection();
            if (connection == null) {
                lastError = "Không kết nối được database.";
                return -1;
            }

            // Nếu đã có row current cùng (căn,user,role) → không insert trùng
            statement = connection.prepareStatement(
                    "SELECT id FROM apartment_residents "
                    + "WHERE apartment_id = ? AND user_id = ? AND role_in_apartment = ? AND is_current = 1");
            statement.setInt(1, apartmentId);
            statement.setInt(2, userId);
            statement.setString(3, role);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int existingId = resultSet.getInt(1);
                lastError = "User đã được gán vai trò này trên căn.";
                return existingId > 0 ? existingId : -1;
            }
            try {
                resultSet.close();
            } catch (SQLException ignored) {
            }
            try {
                statement.close();
            } catch (SQLException ignored) {
            }

            // Xóa lịch sử is_current=0 cùng key trước khi insert current (tránh conflict khi flip)
            statement = connection.prepareStatement(
                    "DELETE FROM apartment_residents "
                    + "WHERE apartment_id = ? AND user_id = ? AND role_in_apartment = ? AND is_current = 0");
            statement.setInt(1, apartmentId);
            statement.setInt(2, userId);
            statement.setString(3, role);
            statement.executeUpdate();
            try {
                statement.close();
            } catch (SQLException ignored) {
            }

            String sql = "INSERT INTO apartment_residents "
                    + "(apartment_id, user_id, role_in_apartment, is_current, start_date, end_date) "
                    + "VALUES (?, ?, ?, 1, ?, ?)";
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, apartmentId);
            statement.setInt(2, userId);
            statement.setString(3, role);
            statement.setDate(4, startDate);
            if (endDate == null) {
                statement.setDate(5, null);
            } else {
                statement.setDate(5, endDate);
            }
            int affected = statement.executeUpdate();
            if (affected > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
            lastError = "Không ghi được bản ghi gán cư dân.";
            return -1;
        } catch (SQLException e) {
            lastError = friendlySqlError(e);
            System.out.println("ApartmentResidentDAO.insertResident: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    private String friendlySqlError(SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (msg.contains("Invalid object name") && msg.toLowerCase().contains("apartment_residents")) {
            return "Bảng apartment_residents chưa có. Hãy chạy database/apartment-detail-tables.sql";
        }
        if (msg.contains("Invalid object name")) {
            return "Thiếu bảng DB: " + msg;
        }
        if (msg.contains("UQ_ar_apartment_user_role") || msg.toLowerCase().contains("unique key")) {
            return "Trùng gán cư dân (cùng user + vai trò trên căn). Hãy gỡ/đổi owner trước hoặc chọn user khác.";
        }
        if (msg.toLowerCase().contains("foreign key") || msg.contains("REFERENCE")) {
            return "User/căn không hợp lệ (FK). Kiểm tra user_id tồn tại trong bảng users.";
        }
        if (msg.length() > 180) {
            return msg.substring(0, 180) + "…";
        }
        return msg.isEmpty() ? "Lỗi SQL khi gán cư dân." : msg;
    }
}
