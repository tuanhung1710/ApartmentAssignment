package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.Apartment;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO căn hộ: CRUD, filter/sort/page, status, delete. */
public class ApartmentDAO extends DBContext {

    public Apartment getFromResultSet(ResultSet rs) throws SQLException {
        return Apartment.builder()
                .apartmentId(rs.getInt("apartment_id"))
                .apartmentCode(rs.getString("apartment_code"))
                .building(rs.getString("building"))
                .floorNumber(rs.getInt("floor_number"))
                .areaM2(rs.getBigDecimal("area_m2"))
                .occupancyType(rs.getString("occupancy_type"))
                .status(rs.getString("status"))
                .notes(rs.getString("notes"))
                .createdAt(rs.getTimestamp("created_at"))
                .updatedAt(rs.getTimestamp("updated_at"))
                .build();
    }

    public boolean existsByCode(String apartmentCode) {
        String sql = "SELECT 1 FROM apartments WHERE apartment_code = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, apartmentCode);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.existsByCode error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Số căn cùng tòa + tầng (để sinh số thứ tự unit trong mã căn).
     */
    public int countByBuildingAndFloor(String building, int floorNumber) {
        String sql = "SELECT COUNT(*) FROM apartments WHERE building = ? AND floor_number = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, building);
            statement.setInt(2, floorNumber);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.countByBuildingAndFloor error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    /**
     * @return generated apartment_id, 0 nếu insert OK nhưng không lấy được key, -1 nếu lỗi
     */
    public int insert(Apartment apartment) {
        String sql = "INSERT INTO apartments "
                + "(apartment_code, building, floor_number, area_m2, occupancy_type, status, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("ApartmentDAO.insert error: connection is null");
                return -1;
            }
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, apartment.getApartmentCode());
            statement.setString(2, apartment.getBuilding());
            statement.setInt(3, apartment.getFloorNumber());
            statement.setBigDecimal(4, apartment.getAreaM2());
            statement.setString(5, apartment.getOccupancyType());
            statement.setString(6, apartment.getStatus());
            if (apartment.getNotes() == null || apartment.getNotes().isEmpty()) {
                statement.setString(7, null);
            } else {
                statement.setString(7, apartment.getNotes());
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
            System.out.println("ApartmentDAO.insert error: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    public List<Apartment> findAll() {
        return findWithFilters(null, null, null, null, "building", "asc", 1, 1000);
    }

    /**
     * UC-APT-04: list có keyword + filter + sort + phân trang (SQL Server OFFSET/FETCH).
     */
    public List<Apartment> findWithFilters(String keyword, String building, String status,
            String occupancyType, String sort, String dir, int page, int pageSize) {
        List<Apartment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM apartments WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, keyword, building, status, occupancyType);
        sql.append(" ORDER BY ").append(resolveSortColumn(sort)).append(" ")
                .append(resolveSortDir(dir))
                .append(", apartment_id ASC");
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
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.findWithFilters error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public int countWithFilters(String keyword, String building, String status, String occupancyType) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM apartments WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, keyword, building, status, occupancyType);
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
            System.out.println("ApartmentDAO.countWithFilters error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    private void appendFilters(StringBuilder sql, List<Object> params,
            String keyword, String building, String status, String occupancyType) {
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (apartment_code LIKE ? OR building LIKE ? OR ISNULL(notes,'') LIKE ?)");
            String like = "%" + keyword + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (building != null && !building.isEmpty()) {
            sql.append(" AND building LIKE ?");
            params.add("%" + building + "%");
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (occupancyType != null && !occupancyType.isEmpty()) {
            sql.append(" AND occupancy_type = ?");
            params.add(occupancyType);
        }
    }

    /** Whitelist sort column — chống SQL injection. */
    private String resolveSortColumn(String sort) {
        if (sort == null) {
            return "building";
        }
        switch (sort) {
            case "code":
                return "apartment_code";
            case "building":
                return "building";
            case "floor":
                return "floor_number";
            case "area":
                return "area_m2";
            case "occupancy":
                return "occupancy_type";
            case "status":
                return "status";
            default:
                return "building";
        }
    }

    private String resolveSortDir(String dir) {
        return "desc".equalsIgnoreCase(dir) ? "DESC" : "ASC";
    }

    public Apartment findById(int apartmentId) {
        String sql = "SELECT * FROM apartments WHERE apartment_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.findById error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    /**
     * Cập nhật căn hộ – không đổi apartment_code / building / floor_number (UC-APT-02).
     * Định danh nghiệp vụ: [tòa] - [tầng] [mã căn].
     *
     * @return true nếu có ít nhất 1 dòng được cập nhật
     */
    public boolean update(Apartment apartment) {
        String sql = "UPDATE apartments SET area_m2 = ?, occupancy_type = ?, status = ?, notes = ?, "
                + "updated_at = SYSUTCDATETIME() WHERE apartment_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("ApartmentDAO.update error: connection is null");
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setBigDecimal(1, apartment.getAreaM2());
            statement.setString(2, apartment.getOccupancyType());
            statement.setString(3, apartment.getStatus());
            if (apartment.getNotes() == null || apartment.getNotes().isEmpty()) {
                statement.setString(4, null);
            } else {
                statement.setString(4, apartment.getNotes());
            }
            statement.setInt(5, apartment.getApartmentId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.update error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM apartments";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.countAll error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    /**
     * UC-APT-03: đổi trạng thái ACTIVE / INACTIVE (disable / activate).
     */
    public boolean updateStatus(int apartmentId, String status) {
        String sql = "UPDATE apartments SET status = ?, updated_at = SYSUTCDATETIME() WHERE apartment_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, status);
            statement.setInt(2, apartmentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.updateStatus error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Đếm cư dân hiện tại gắn căn (bảng apartment_residents).
     * Nếu bảng chưa có / lỗi SQL → trả 0 (MVP an toàn, hard delete vẫn theo status).
     */
    public int countCurrentResidents(int apartmentId) {
        String sql = "SELECT COUNT(*) FROM apartment_residents WHERE apartment_id = ? AND is_current = 1";
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            // Bảng chưa tạo hoặc schema khác — không chặn disable; hard delete coi như 0 cư dân
            System.out.println("ApartmentDAO.countCurrentResidents warning: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    /**
     * Hard delete – chỉ gọi sau khi controller đã check BR (INACTIVE + không cư dân…).
     */
    public boolean deleteById(int apartmentId) {
        String sql = "DELETE FROM apartments WHERE apartment_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.deleteById error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }
}
