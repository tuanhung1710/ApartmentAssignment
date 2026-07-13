package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.HouseholdMember;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Thành viên hộ – UC-APT-05/08/09 (list · thêm · sửa · soft delete).
 */
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

    /**
     * UC-APT-10: list thành viên + search/filter + phân trang (JOIN apartments).
     *
     * @param statusFilter ACTIVE | INACTIVE | null/empty = all
     */
    public List<HouseholdMember> findMembersWithFilters(String keyword, String relationship,
            String statusFilter, String building, int page, int pageSize) {
        List<HouseholdMember> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT hm.*, a.apartment_code, a.building "
                + "FROM household_members hm "
                + "INNER JOIN apartments a ON a.apartment_id = hm.apartment_id "
                + "WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendMemberFilters(sql, params, keyword, relationship, statusFilter, building);
        sql.append(" ORDER BY hm.is_active DESC, a.building, a.apartment_code, hm.full_name");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        try {
            connection = getConnection();
            if (connection == null) {
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
                HouseholdMember m = getFromResultSet(resultSet);
                m.setApartmentCode(resultSet.getString("apartment_code"));
                m.setBuilding(resultSet.getString("building"));
                list.add(m);
            }
        } catch (SQLException e) {
            System.out.println("HouseholdMemberDAO.findMembersWithFilters: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public int countMembersWithFilters(String keyword, String relationship,
            String statusFilter, String building) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM household_members hm "
                + "INNER JOIN apartments a ON a.apartment_id = hm.apartment_id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendMemberFilters(sql, params, keyword, relationship, statusFilter, building);
        try {
            connection = getConnection();
            if (connection == null) {
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
            System.out.println("HouseholdMemberDAO.countMembersWithFilters: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    /** Export: cùng filter, không phân trang (giới hạn an toàn 5000). */
    public List<HouseholdMember> findMembersForExport(String keyword, String relationship,
            String statusFilter, String building) {
        return findMembersWithFilters(keyword, relationship, statusFilter, building, 1, 5000);
    }

    private void appendMemberFilters(StringBuilder sql, List<Object> params,
            String keyword, String relationship, String statusFilter, String building) {
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (hm.full_name LIKE ? OR ISNULL(hm.phone,'') LIKE ? "
                    + "OR ISNULL(hm.id_number,'') LIKE ? OR a.apartment_code LIKE ?)");
            String like = "%" + keyword + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (relationship != null && !relationship.isEmpty()) {
            sql.append(" AND hm.relationship = ?");
            params.add(relationship);
        }
        if ("ACTIVE".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND hm.is_active = 1");
        } else if ("INACTIVE".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND hm.is_active = 0");
        }
        if (building != null && !building.isEmpty()) {
            sql.append(" AND a.building LIKE ?");
            params.add("%" + building + "%");
        }
    }

    public HouseholdMember findById(int memberId) {
        String sql = "SELECT * FROM household_members WHERE member_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return null;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, memberId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("HouseholdMemberDAO.findById: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    public boolean existsActiveIdNumber(int apartmentId, String idNumber) {
        return existsActiveIdNumberExceptId(apartmentId, idNumber, null);
    }

    /** CCCD trùng active trên căn, trừ chính member đang sửa. */
    public boolean existsActiveIdNumberExceptId(int apartmentId, String idNumber, Integer exceptMemberId) {
        if (idNumber == null || idNumber.isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 FROM household_members "
                + "WHERE apartment_id = ? AND id_number = ? AND is_active = 1";
        if (exceptMemberId != null) {
            sql += " AND member_id <> ?";
        }
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            statement.setString(2, idNumber);
            if (exceptMemberId != null) {
                statement.setInt(3, exceptMemberId);
            }
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("HouseholdMemberDAO.existsActiveIdNumberExceptId: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public int insert(HouseholdMember m) {
        String sql = "INSERT INTO household_members "
                + "(apartment_id, full_name, relationship, phone, id_number, date_of_birth, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            if (connection == null) {
                return -1;
            }
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, m.getApartmentId());
            statement.setString(2, m.getFullName());
            statement.setString(3, m.getRelationship());
            statement.setString(4, emptyToNull(m.getPhone()));
            statement.setString(5, emptyToNull(m.getIdNumber()));
            if (m.getDateOfBirth() == null) {
                statement.setDate(6, null);
            } else {
                statement.setDate(6, m.getDateOfBirth());
            }
            statement.setBoolean(7, m.getIsActive() == null || m.getIsActive());
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
            System.out.println("HouseholdMemberDAO.insert: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    /** UC-APT-09 Update. */
    public boolean update(HouseholdMember m) {
        String sql = "UPDATE household_members SET full_name = ?, relationship = ?, phone = ?, "
                + "id_number = ?, date_of_birth = ? WHERE member_id = ? AND apartment_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, m.getFullName());
            statement.setString(2, m.getRelationship());
            statement.setString(3, emptyToNull(m.getPhone()));
            statement.setString(4, emptyToNull(m.getIdNumber()));
            if (m.getDateOfBirth() == null) {
                statement.setDate(5, null);
            } else {
                statement.setDate(5, m.getDateOfBirth());
            }
            statement.setInt(6, m.getMemberId());
            statement.setInt(7, m.getApartmentId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("HouseholdMemberDAO.update: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Soft delete — Remove (BR-U03): is_active = 0, giữ row.
     */
    public boolean softDelete(int memberId, int apartmentId) {
        String sql = "UPDATE household_members SET is_active = 0 "
                + "WHERE member_id = ? AND apartment_id = ? AND is_active = 1";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, memberId);
            statement.setInt(2, apartmentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("HouseholdMemberDAO.softDelete: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    private String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
