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

/**
 * DAO gán cư dân – căn hộ.
 * UC-APT-05 hiển thị · UC-APT-06 OWNER · UC-APT-07 TENANT / TENANT_REP.
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

    /** Đại diện thuê hiện tại (0..1). */
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

    /** User đã là current với đúng role trên căn? */
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

    /** Đóng mọi TENANT_REP hiện tại (BR-T04). */
    public int endCurrentTenantReps(int apartmentId, Date endDate) {
        return endCurrentByRole(apartmentId, AppConstants.APT_ROLE_TENANT_REP, endDate);
    }

    private int endCurrentByRole(int apartmentId, String role, Date endDate) {
        String sql = "UPDATE apartment_residents SET is_current = 0, end_date = ? "
                + "WHERE apartment_id = ? AND role_in_apartment = ? AND is_current = 1";
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql);
            statement.setDate(1, endDate);
            statement.setInt(2, apartmentId);
            statement.setString(3, role);
            return statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("ApartmentResidentDAO.endCurrentByRole(" + role + "): " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    public int insertOwner(int apartmentId, int userId, Date startDate) {
        return insertResident(apartmentId, userId, AppConstants.APT_ROLE_OWNER, startDate, null);
    }

    /**
     * Gán người thuê / đại diện thuê.
     *
     * @param roleInApartment TENANT_REP hoặc TENANT
     * @param endDate có thể null
     */
    public int insertTenant(int apartmentId, int userId, String roleInApartment,
            Date startDate, Date endDate) {
        return insertResident(apartmentId, userId, roleInApartment, startDate, endDate);
    }

    private int insertResident(int apartmentId, int userId, String role,
            Date startDate, Date endDate) {
        String sql = "INSERT INTO apartment_residents "
                + "(apartment_id, user_id, role_in_apartment, is_current, start_date, end_date) "
                + "VALUES (?, ?, ?, 1, ?, ?)";
        try {
            connection = getConnection();
            if (connection == null) {
                return -1;
            }
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
            return -1;
        } catch (SQLException e) {
            System.out.println("ApartmentResidentDAO.insertResident: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }
}
