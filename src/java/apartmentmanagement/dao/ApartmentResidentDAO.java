package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.ApartmentResident;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO gán cư dân – căn hộ (UC-APT-05 hiển thị chủ / thuê).
 */
public class ApartmentResidentDAO extends DBContext {

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
                .username(rs.getString("username"))
                .fullName(rs.getString("full_name"))
                .apartmentCode(null)
                .build();
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
}
