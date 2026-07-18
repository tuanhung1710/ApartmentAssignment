package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.ApartmentResident;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


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
                .build();
    }

   
    public ApartmentResident findCurrentByUserId(int userId) {
        String sql = "SELECT TOP 1 ar.*, a.apartment_code "
                + "FROM apartment_residents ar "
                + "INNER JOIN apartments a ON a.apartment_id = ar.apartment_id "
                + "WHERE ar.user_id = ? AND ar.is_current = 1 "
                + "ORDER BY CASE ar.role_in_apartment WHEN N'TENANT_REP' THEN 0 ELSE 1 END, ar.id DESC";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                ApartmentResident ar = getFromResultSet(resultSet);
                ar.setApartmentCode(resultSet.getString("apartment_code"));
                return ar;
            }
        } catch (SQLException e) {
            System.out.println("ApartmentResidentDAO.findCurrentByUserId error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    public Integer findCurrentApartmentId(int userId) {
        ApartmentResident ar = findCurrentByUserId(userId);
        return ar == null ? null : ar.getApartmentId();
    }

    public List<ApartmentResident> findByApartmentId(int apartmentId) {
        List<ApartmentResident> list = new ArrayList<>();
        String sql = "SELECT ar.*, u.username, u.full_name, a.apartment_code "
                + "FROM apartment_residents ar "
                + "INNER JOIN users u ON u.user_id = ar.user_id "
                + "INNER JOIN apartments a ON a.apartment_id = ar.apartment_id "
                + "WHERE ar.apartment_id = ? AND ar.is_current = 1 "
                + "ORDER BY ar.role_in_apartment";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                ApartmentResident ar = getFromResultSet(resultSet);
                ar.setUsername(resultSet.getString("username"));
                ar.setFullName(resultSet.getString("full_name"));
                ar.setApartmentCode(resultSet.getString("apartment_code"));
                list.add(ar);
            }
        } catch (SQLException e) {
            System.out.println("ApartmentResidentDAO.findByApartmentId error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }
}
