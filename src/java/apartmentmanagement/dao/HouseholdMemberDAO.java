package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.HouseholdMember;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HouseholdMemberDAO extends DBContext {

    public HouseholdMember getFromResultSet(ResultSet rs) throws SQLException {
        return HouseholdMember.builder()
                .memberId(rs.getInt("member_id"))
                .apartmentId(rs.getInt("apartment_id"))
                .fullName(rs.getString("full_name"))
                .relationship(rs.getString("relationship"))
                .phone(rs.getString("phone"))
                .idNumber(rs.getString("id_number"))
                .dateOfBirth(rs.getDate("date_of_birth"))
                .isActive(rs.getBoolean("is_active"))
                .createdAt(rs.getTimestamp("created_at"))
                .build();
    }

    public List<HouseholdMember> findByApartmentId(int apartmentId) {
        List<HouseholdMember> list = new ArrayList<>();
        String sql = "SELECT * FROM household_members WHERE apartment_id = ? "
                + "ORDER BY is_active DESC, full_name";
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
            System.out.println("HouseholdMemberDAO.findByApartmentId: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }
}
