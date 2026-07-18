package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.Apartment;
import apartmentmanagement.model.Building;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


public class BuildingDAO extends DBContext {

    public Building getFromResultSet(ResultSet rs) throws SQLException {
        Object floorsObj = rs.getObject("total_floors");
        Integer floors = floorsObj == null ? null : ((Number) floorsObj).intValue();
        Building.BuildingBuilder builder = Building.builder()
                .buildingId(rs.getInt("building_id"))
                .buildingCode(rs.getString("building_code"))
                .buildingName(rs.getString("building_name"))
                .address(rs.getString("address"))
                .totalFloors(floors)
                .description(rs.getString("description"))
                .status(rs.getString("status"))
                .createdAt(rs.getTimestamp("created_at"))
                .updatedAt(rs.getTimestamp("updated_at"));

        try {
            Object count = rs.getObject("apartment_count");
            if (count != null) {
                builder.apartmentCount(((Number) count).intValue());
            }
        } catch (SQLException ignored) {
            // cột optional khi SELECT không join count
        }
        return builder.build();
    }

    public Building findById(int buildingId) {
        String sql = "SELECT b.*, "
                + "(SELECT COUNT(*) FROM apartments a WHERE a.building_id = b.building_id) AS apartment_count "
                + "FROM buildings b WHERE b.building_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return null;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, buildingId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("BuildingDAO.findById error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    public Building findByCode(String buildingCode) {
        if (buildingCode == null || buildingCode.isBlank()) {
            return null;
        }
        String sql = "SELECT b.*, "
                + "(SELECT COUNT(*) FROM apartments a WHERE a.building_id = b.building_id) AS apartment_count "
                + "FROM buildings b WHERE UPPER(b.building_code) = UPPER(?)";
        try {
            connection = getConnection();
            if (connection == null) {
                return null;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, buildingCode.trim());
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("BuildingDAO.findByCode error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }
    
    public List<Building> findWithFilters(String keyword, String status, int page, int pageSize) {
        List<Building> list = new ArrayList<>();
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        int offset = (page - 1) * pageSize;

        StringBuilder sql = new StringBuilder(
                "SELECT b.*, "
                + "(SELECT COUNT(*) FROM apartments a WHERE a.building_id = b.building_id) AS apartment_count "
                + "FROM buildings b WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, keyword, status);
        sql.append(" ORDER BY b.building_code OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(pageSize);

        try {
            connection = getConnection();
            if (connection == null) {
                return list;
            }
            statement = connection.prepareStatement(sql.toString());
            bindParams(params);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("BuildingDAO.findWithFilters error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public int countWithFilters(String keyword, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM buildings b WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, keyword, status);

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql.toString());
            bindParams(params);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("BuildingDAO.countWithFilters error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }


    public List<Building> findAllActive() {
        List<Building> list = new ArrayList<>();
        String sql = "SELECT b.*, "
                + "(SELECT COUNT(*) FROM apartments a WHERE a.building_id = b.building_id) AS apartment_count "
                + "FROM buildings b WHERE b.status = N'ACTIVE' ORDER BY b.building_code";
        try {
            connection = getConnection();
            if (connection == null) {
                return list;
            }
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("BuildingDAO.findAllActive error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public int insert(Building building) {
        String sql = "INSERT INTO buildings (building_code, building_name, address, total_floors, "
                + "description, status) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            if (connection == null) {
                return -1;
            }
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, building.getBuildingCode());
            statement.setString(2, building.getBuildingName());
            statement.setString(3, building.getAddress());
            if (building.getTotalFloors() == null) {
                statement.setNull(4, Types.INTEGER);
            } else {
                statement.setInt(4, building.getTotalFloors());
            }
            statement.setString(5, building.getDescription());
            statement.setString(6, building.getStatus() == null ? "ACTIVE" : building.getStatus());

            int affected = statement.executeUpdate();
            if (affected > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("BuildingDAO.insert error: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
        return -1;
    }

    public boolean update(Building building) {
        String sql = "UPDATE buildings SET building_code = ?, building_name = ?, address = ?, "
                + "total_floors = ?, description = ?, status = ?, updated_at = SYSUTCDATETIME() "
                + "WHERE building_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, building.getBuildingCode());
            statement.setString(2, building.getBuildingName());
            statement.setString(3, building.getAddress());
            if (building.getTotalFloors() == null) {
                statement.setNull(4, Types.INTEGER);
            } else {
                statement.setInt(4, building.getTotalFloors());
            }
            statement.setString(5, building.getDescription());
            statement.setString(6, building.getStatus());
            statement.setInt(7, building.getBuildingId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("BuildingDAO.update error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /** Soft-delete: INACTIVE. Không xóa cứng nếu còn căn hộ. */
    public boolean deactivate(int buildingId) {
        String sql = "UPDATE buildings SET status = N'INACTIVE', updated_at = SYSUTCDATETIME() "
                + "WHERE building_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, buildingId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("BuildingDAO.deactivate error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public boolean activate(int buildingId) {
        String sql = "UPDATE buildings SET status = N'ACTIVE', updated_at = SYSUTCDATETIME() "
                + "WHERE building_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, buildingId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("BuildingDAO.activate error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    
    public int deleteIfEmpty(int buildingId) {
        Building existing = findById(buildingId);
        if (existing == null) {
            return -2;
        }
        int aptCount = existing.getApartmentCount() == null ? 0 : existing.getApartmentCount();
        if (aptCount > 0) {
            return -1;
        }
        String sql = "DELETE FROM buildings WHERE building_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, buildingId);
            return statement.executeUpdate() > 0 ? 1 : 0;
        } catch (SQLException e) {
            System.out.println("BuildingDAO.deleteIfEmpty error: " + e.getMessage());
            return 0;
        } finally {
            closeResources();
        }
    }

    public boolean existsCodeExceptId(String code, Integer exceptId) {
        if (code == null || code.isBlank()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM buildings WHERE UPPER(building_code) = UPPER(?) "
                + (exceptId == null ? "" : "AND building_id <> ?");
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, code.trim());
            if (exceptId != null) {
                statement.setInt(2, exceptId);
            }
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("BuildingDAO.existsCodeExceptId error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return false;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM buildings";
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("BuildingDAO.countAll error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM buildings WHERE status = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, status);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("BuildingDAO.countByStatus error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    
    public List<Apartment> findApartmentsByBuilding(int buildingId, String status,
            String occupancyType, String keyword, int page, int pageSize) {
        List<Apartment> list = new ArrayList<>();
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        int offset = (page - 1) * pageSize;

        StringBuilder sql = new StringBuilder(
                "SELECT apartment_id, apartment_code, building_id, building, floor_number, "
                + "area_m2, occupancy_type, status, notes, created_at, updated_at "
                + "FROM apartments WHERE building_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(buildingId);
        appendApartmentFilters(sql, params, status, occupancyType, keyword);
        sql.append(" ORDER BY apartment_code OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(pageSize);

        try {
            connection = getConnection();
            if (connection == null) {
                return list;
            }
            statement = connection.prepareStatement(sql.toString());
            bindParams(params);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(mapApartment(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("BuildingDAO.findApartmentsByBuilding error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public int countApartmentsByBuilding(int buildingId, String status,
            String occupancyType, String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM apartments WHERE building_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(buildingId);
        appendApartmentFilters(sql, params, status, occupancyType, keyword);

        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql.toString());
            bindParams(params);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("BuildingDAO.countApartmentsByBuilding error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    /** Đếm căn theo tòa + status (không keyword) — badge filter. */
    public int countApartmentsByBuildingStatus(int buildingId, String status) {
        String sql = "SELECT COUNT(*) FROM apartments WHERE building_id = ?"
                + (status == null || status.isBlank() ? "" : " AND status = ?");
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, buildingId);
            if (status != null && !status.isBlank()) {
                statement.setString(2, status.trim());
            }
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("BuildingDAO.countApartmentsByBuildingStatus error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    /**
     * Đếm căn ACTIVE theo occupancy_type (OWNED | RENTED | VACANT).
     * Chỉ meaningful khi status = ACTIVE.
     */
    public int countApartmentsByBuildingOccupancy(int buildingId, String occupancyType) {
        if (occupancyType == null || occupancyType.isBlank()) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM apartments "
                + "WHERE building_id = ? AND status = N'ACTIVE' AND occupancy_type = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return 0;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, buildingId);
            statement.setString(2, occupancyType.trim());
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("BuildingDAO.countApartmentsByBuildingOccupancy error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    /**
     * status + occupancyType + keyword.
     * occupancyType chỉ áp khi status = ACTIVE (INACTIVE → loại hình = N/A, không filter type).
     */
    private void appendApartmentFilters(StringBuilder sql, List<Object> params,
            String status, String occupancyType, String keyword) {
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ? ");
            params.add(status.trim());
        }
        // Chỉ filter loại hình khi ACTIVE — tránh lẫn với INACTIVE
        if (occupancyType != null && !occupancyType.isBlank()
                && "ACTIVE".equalsIgnoreCase(status == null ? "" : status.trim())) {
            sql.append(" AND occupancy_type = ? ");
            params.add(occupancyType.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (apartment_code LIKE ? OR notes LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
    }

    private Apartment mapApartment(ResultSet rs) throws SQLException {
        Object bid = rs.getObject("building_id");
        Object floor = rs.getObject("floor_number");
        BigDecimal area = rs.getBigDecimal("area_m2");
        return Apartment.builder()
                .apartmentId(rs.getInt("apartment_id"))
                .apartmentCode(rs.getString("apartment_code"))
                .buildingId(bid == null ? null : ((Number) bid).intValue())
                .building(rs.getString("building"))
                .floorNumber(floor == null ? null : ((Number) floor).intValue())
                .areaM2(area)
                .occupancyType(rs.getString("occupancy_type"))
                .status(rs.getString("status"))
                .notes(rs.getString("notes"))
                .createdAt(rs.getTimestamp("created_at"))
                .updatedAt(rs.getTimestamp("updated_at"))
                .build();
    }

    private void appendFilters(StringBuilder sql, List<Object> params, String keyword, String status) {
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (b.building_code LIKE ? OR b.building_name LIKE ? OR b.address LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND b.status = ? ");
            params.add(status.trim());
        }
    }

    private void bindParams(List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object p = params.get(i);
            if (p instanceof Integer) {
                statement.setInt(i + 1, (Integer) p);
            } else {
                statement.setString(i + 1, p == null ? null : p.toString());
            }
        }
    }
}
