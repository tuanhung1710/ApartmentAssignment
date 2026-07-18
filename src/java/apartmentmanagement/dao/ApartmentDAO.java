package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.Apartment;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO căn hộ ({@code apartments}): CRUD, filter/sort/phân trang,
 * đồng bộ occupancy và các helper kiểm tra cư dân / thành viên hộ.
 */
public class ApartmentDAO extends DBContext {

    /**
     * Map một dòng ResultSet sang {@link Apartment}.
     * {@code member_count} và {@code building_id} là optional (tùy SELECT).
     *
     * @param rs ResultSet đang trỏ tới dòng hợp lệ
     * @return entity đã map
     * @throws SQLException nếu đọc cột bắt buộc lỗi
     */
    public Apartment getFromResultSet(ResultSet rs) throws SQLException {
        Integer memberCount = null;
        try {
            Object mc = rs.getObject("member_count");
            if (mc != null) {
                memberCount = rs.getInt("member_count");
            }
        } catch (SQLException ignored) {
            // SELECT * / findById không có alias member_count
        }
        Integer buildingId = null;
        try {
            Object bid = rs.getObject("building_id");
            if (bid != null) {
                buildingId = ((Number) bid).intValue();
            }
        } catch (SQLException ignored) {
            // cột building_id có thể chưa có trên schema cũ
        }
        return Apartment.builder()
                .apartmentId(rs.getInt("apartment_id"))
                .apartmentCode(rs.getString("apartment_code"))
                .buildingId(buildingId)
                .building(rs.getString("building"))
                .floorNumber(rs.getInt("floor_number"))
                .areaM2(rs.getBigDecimal("area_m2"))
                .occupancyType(rs.getString("occupancy_type"))
                .status(rs.getString("status"))
                .notes(rs.getString("notes"))
                .createdAt(rs.getTimestamp("created_at"))
                .updatedAt(rs.getTimestamp("updated_at"))
                .memberCount(memberCount)
                .build();
    }

    /**
     * Kiểm tra mã căn đã tồn tại chưa.
     *
     * @param apartmentCode mã căn
     * @return true nếu đã có
     */
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
     * Số căn cùng tòa + tầng (fallback đếm khi không parse được mã).
     *
     * @param building    tên/mã tòa (cột building)
     * @param floorNumber số tầng
     * @return số căn; lỗi → 0
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
     * Unit tiếp theo cho mã căn format {@code {TOKEN}-{FF}{UU}} (vd. A-0203).
     * Lấy max unit từ các mã có prefix {@code {TOKEN}-{FF}}, rồi +1.
     * Ví dụ: đã có A-0201, A-0202 → trả 3.
     *
     * @param codePrefix prefix không gồm unit, vd. "A-02"
     * @return unit kế tiếp (&gt;= 1); lỗi → 1
     */
    public int findNextUnitByCodePrefix(String codePrefix) {
        if (codePrefix == null || codePrefix.isEmpty()) {
            return 1;
        }
        String sql = "SELECT apartment_code FROM apartments WHERE apartment_code LIKE ?";
        int maxUnit = 0;
        try {
            connection = getConnection();
            if (connection == null) {
                return 1;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, codePrefix + "%");
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String code = resultSet.getString(1);
                if (code == null || !code.startsWith(codePrefix)) {
                    continue;
                }
                String unitPart = code.substring(codePrefix.length());
                // Chỉ nhận unit đúng 2 chữ số (01..99)
                if (unitPart.length() == 2 && unitPart.chars().allMatch(Character::isDigit)) {
                    int unit = Integer.parseInt(unitPart);
                    if (unit > maxUnit) {
                        maxUnit = unit;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.findNextUnitByCodePrefix error: " + e.getMessage());
            return 1;
        } finally {
            closeResources();
        }
        return maxUnit + 1;
    }

    /**
     * Thêm căn hộ mới. Ưu tiên INSERT kèm {@code building_id}; fallback schema cũ không có cột.
     *
     * @param apartment dữ liệu căn (code, building, floor, area, occupancy, status, notes)
     * @return generated apartment_id; 0 nếu insert OK nhưng không lấy được key; -1 nếu lỗi
     */
    public int insert(Apartment apartment) {
        ensureOccupancyCheckConstraint();
        // building_id optional (schema unified có cột; DB cũ có thể chưa)
        String sqlWithBid = "INSERT INTO apartments "
                + "(apartment_code, building_id, building, floor_number, area_m2, occupancy_type, status, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlNoBid = "INSERT INTO apartments "
                + "(apartment_code, building, floor_number, area_m2, occupancy_type, status, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("ApartmentDAO.insert error: connection is null");
                return -1;
            }
            Integer buildingId = apartment.getBuildingId();
            if (buildingId == null) {
                buildingId = resolveBuildingId(connection, apartment.getBuilding());
            }
            try {
                statement = connection.prepareStatement(sqlWithBid, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, apartment.getApartmentCode());
                if (buildingId == null) {
                    statement.setObject(2, null);
                } else {
                    statement.setInt(2, buildingId);
                }
                statement.setString(3, apartment.getBuilding());
                statement.setInt(4, apartment.getFloorNumber());
                statement.setBigDecimal(5, apartment.getAreaM2());
                statement.setString(6, apartment.getOccupancyType());
                statement.setString(7, apartment.getStatus());
                if (apartment.getNotes() == null || apartment.getNotes().isEmpty()) {
                    statement.setString(8, null);
                } else {
                    statement.setString(8, apartment.getNotes());
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
            } catch (SQLException exWithBid) {
                // Fallback DB chưa có building_id
                System.out.println("ApartmentDAO.insert with building_id failed, fallback: " + exWithBid.getMessage());
                closeQuietly(statement, resultSet);
                statement = connection.prepareStatement(sqlNoBid, Statement.RETURN_GENERATED_KEYS);
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
            }
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.insert error: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    /** Map building string → buildings.building_id (exact code hoặc token 1 ký tự). */
    private Integer resolveBuildingId(Connection conn, String building) {
        if (conn == null || building == null || building.trim().isEmpty()) {
            return null;
        }
        String code = building.trim();
        String sql = "SELECT TOP 1 building_id FROM buildings WHERE building_code = ? "
                + "OR building_code = LEFT(?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            // bảng buildings chưa có — bỏ qua
        }
        return null;
    }

    /** Đóng ResultSet/PreparedStatement im lặng và gỡ reference instance. */
    private void closeQuietly(PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ignored) {
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException ignored) {
        }
        this.statement = null;
        this.resultSet = null;
    }

    /**
     * Lấy tối đa 1000 căn (sort tòa ASC) — tiện cho dropdown/legacy.
     *
     * @return danh sách căn
     */
    public List<Apartment> findAll() {
        return findWithFilters(null, null, null, null, "building", "asc", 1, 1000);
    }

    /**
     * Danh sách căn có keyword + filter + sort + phân trang (SQL Server OFFSET/FETCH).
     * Kèm {@code member_count} = số household_members {@code is_active=1}.
     *
     * @param keyword       tìm theo mã / tòa / notes (nullable)
     * @param building      filter tòa (LIKE, nullable)
     * @param status        ACTIVE | INACTIVE (nullable)
     * @param occupancyType OWNED | RENTED | VACANT | N/A (nullable)
     * @param sort          code|building|floor|area|occupancy|status|members
     * @param dir           asc|desc
     * @param page          trang (≥1)
     * @param pageSize      kích thước trang
     * @return danh sách căn trang hiện tại
     */
    public List<Apartment> findWithFilters(String keyword, String building, String status,
            String occupancyType, String sort, String dir, int page, int pageSize) {
        List<Apartment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.*, "
                + "(SELECT COUNT(*) FROM household_members hm "
                + " WHERE hm.apartment_id = a.apartment_id AND hm.is_active = 1) AS member_count "
                + "FROM apartments a WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFiltersPrefixed(sql, params, keyword, building, status, occupancyType, "a");
        sql.append(" ORDER BY ").append(resolveSortColumnPrefixed(sort, "a")).append(" ")
                .append(resolveSortDir(dir))
                .append(", a.apartment_id ASC");
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

    /**
     * Đếm căn theo cùng bộ filter với {@link #findWithFilters}.
     *
     * @return tổng số bản ghi khớp; lỗi → 0
     */
    public int countWithFilters(String keyword, String building, String status, String occupancyType) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM apartments a WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFiltersPrefixed(sql, params, keyword, building, status, occupancyType, "a");
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

    /** Gắn điều kiện filter list căn (không alias bảng). */
    private void appendFilters(StringBuilder sql, List<Object> params,
            String keyword, String building, String status, String occupancyType) {
        appendFiltersPrefixed(sql, params, keyword, building, status, occupancyType, null);
    }

    /** Gắn điều kiện filter list căn; {@code alias} null = không prefix cột. */
    private void appendFiltersPrefixed(StringBuilder sql, List<Object> params,
            String keyword, String building, String status, String occupancyType, String alias) {
        String p = (alias == null || alias.isEmpty()) ? "" : alias + ".";
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (").append(p).append("apartment_code LIKE ? OR ")
                    .append(p).append("building LIKE ? OR ISNULL(")
                    .append(p).append("notes,'') LIKE ?)");
            String like = "%" + keyword + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (building != null && !building.isEmpty()) {
            sql.append(" AND ").append(p).append("building LIKE ?");
            params.add("%" + building + "%");
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND ").append(p).append("status = ?");
            params.add(status);
        }
        if (occupancyType != null && !occupancyType.isEmpty()) {
            sql.append(" AND ").append(p).append("occupancy_type = ?");
            params.add(occupancyType);
        }
    }

    /** Whitelist sort column — chống SQL injection. */
    private String resolveSortColumn(String sort) {
        return resolveSortColumnPrefixed(sort, null);
    }

    /** Whitelist sort column (có thể kèm alias bảng). */
    private String resolveSortColumnPrefixed(String sort, String alias) {
        String p = (alias == null || alias.isEmpty()) ? "" : alias + ".";
        if (sort == null) {
            return p + "building";
        }
        switch (sort) {
            case "code":
                return p + "apartment_code";
            case "building":
                return p + "building";
            case "floor":
                return p + "floor_number";
            case "area":
                return p + "area_m2";
            case "occupancy":
                return p + "occupancy_type";
            case "status":
                return p + "status";
            case "members":
                return "member_count";
            default:
                return p + "building";
        }
    }

    /** Chuẩn hóa chiều sort: chỉ {@code DESC} khi dir=desc; còn lại {@code ASC}. */
    private String resolveSortDir(String dir) {
        return "desc".equalsIgnoreCase(dir) ? "DESC" : "ASC";
    }

    /**
     * Tìm căn theo primary key.
     *
     * @param apartmentId id căn
     * @return căn hoặc null
     */
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
     * Cập nhật căn hộ – không đổi apartment_code / building / floor_number.
     * UI hiển thị tách: Mã căn | Tòa | Tầng.
     *
     * @param apartment entity (cần apartmentId + area/occupancy/status/notes)
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

    /**
     * Tổng số căn trong hệ thống.
     *
     * @return count; lỗi → 0
     */
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
     * Đổi trạng thái ACTIVE / INACTIVE (disable / activate).
     *
     * @param apartmentId id căn
     * @param status      ACTIVE | INACTIVE
     * @return true nếu cập nhật thành công
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
     * Đổi status + occupancy cùng lúc (activate / deactivate).
     *
     * @param apartmentId   id căn
     * @param status        ACTIVE | INACTIVE
     * @param occupancyType OWNED | RENTED | VACANT | N/A
     * @return true nếu cập nhật thành công
     */
    public boolean updateStatusAndOccupancy(int apartmentId, String status, String occupancyType) {
        ensureOccupancyCheckConstraint();
        String sql = "UPDATE apartments SET status = ?, occupancy_type = ?, "
                + "updated_at = SYSUTCDATETIME() WHERE apartment_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, status);
            statement.setString(2, occupancyType);
            statement.setInt(3, apartmentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.updateStatusAndOccupancy error: " + e.getMessage()
                    + " id=" + apartmentId + " status=" + status + " occ=" + occupancyType);
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Chỉ cập nhật occupancy_type (auto-sync). VACANT → xóa notes; INACTIVE giữ notes.
     *
     * @param apartmentId   id căn
     * @param occupancyType giá trị occupancy mới
     * @return true nếu cập nhật thành công
     */
    public boolean updateOccupancy(int apartmentId, String occupancyType) {
        ensureOccupancyCheckConstraint();
        // VACANT = trống sẵn sàng: không giữ ghi chú demo / lý do ngừng
        boolean clearNotes = "VACANT".equals(occupancyType);
        String sql = clearNotes
                ? "UPDATE apartments SET occupancy_type = ?, notes = NULL, updated_at = SYSUTCDATETIME() WHERE apartment_id = ?"
                : "UPDATE apartments SET occupancy_type = ?, updated_at = SYSUTCDATETIME() WHERE apartment_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, occupancyType);
            statement.setInt(2, apartmentId);
            int n = statement.executeUpdate();
            if (n <= 0) {
                System.out.println("ApartmentDAO.updateOccupancy: 0 rows id=" + apartmentId
                        + " occ=" + occupancyType);
            }
            return n > 0;
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.updateOccupancy error: " + e.getMessage()
                    + " id=" + apartmentId + " occ=" + occupancyType);
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Số thành viên hộ active trên căn.
     *
     * @param apartmentId id căn
     * @return count; lỗi → 0
     */
    public int countActiveMembers(int apartmentId) {
        String sql = "SELECT COUNT(*) FROM household_members WHERE apartment_id = ? AND is_active = 1";
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
            System.out.println("ApartmentDAO.countActiveMembers warning: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    /**
     * Có OWNER đang current trên căn hay không.
     *
     * @param apartmentId id căn
     * @return true nếu có
     */
    public boolean hasCurrentOwner(int apartmentId) {
        String sql = "SELECT 1 FROM apartment_residents "
                + "WHERE apartment_id = ? AND is_current = 1 AND role_in_apartment = 'OWNER'";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.hasCurrentOwner warning: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Có TENANT_REP hoặc TENANT đang current trên căn hay không.
     *
     * @param apartmentId id căn
     * @return true nếu có
     */
    public boolean hasCurrentTenant(int apartmentId) {
        String sql = "SELECT 1 FROM apartment_residents "
                + "WHERE apartment_id = ? AND is_current = 1 "
                + "AND role_in_apartment IN ('TENANT_REP', 'TENANT')";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.hasCurrentTenant warning: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /** Chỉ ensure CHECK 1 lần / JVM — tránh DROP/ADD mỗi request list. */
    private static volatile boolean occupancyCheckReady = false;

    /**
     * Đảm bảo CHECK occupancy cho phép OWNED|RENTED|VACANT|N/A.
     * Bảng cũ chỉ OWNED|RENTED → mọi UPDATE VACANT/N/A đều fail.
     * Thứ tự: DROP → (caller UPDATE data) → ADD; method này chỉ DROP (và ADD nếu đã safe).
     *
     * @return true nếu sẵn sàng cho UPDATE occupancy (kể cả khi ADD chưa gắn được)
     */
    public boolean ensureOccupancyCheckConstraint() {
        if (occupancyCheckReady) {
            return true;
        }
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            Statement st = connection.createStatement();
            try {
                // Drop CHECK cũ (OWNED|RENTED only) nếu còn
                st.execute(
                        "IF EXISTS (SELECT 1 FROM sys.check_constraints "
                        + "WHERE name = N'CK_apartments_occupancy' "
                        + "AND parent_object_id = OBJECT_ID(N'dbo.apartments')) "
                        + "ALTER TABLE dbo.apartments DROP CONSTRAINT CK_apartments_occupancy");
                // Thử gắn CHECK mới ngay; nếu fail (data lạ) reconcile sẽ UPDATE rồi gọi lại
                try {
                    st.execute(
                            "ALTER TABLE dbo.apartments ADD CONSTRAINT CK_apartments_occupancy "
                            + "CHECK (occupancy_type IN (N'OWNED', N'RENTED', N'VACANT', N'N/A'))");
                    occupancyCheckReady = true;
                    System.out.println("ApartmentDAO.ensureOccupancyCheckConstraint: OK");
                    return true;
                } catch (SQLException addEx) {
                    // Có thể constraint đã tồn tại đúng, hoặc data chưa hợp lệ
                    System.out.println("ApartmentDAO.ensureOccupancyCheckConstraint ADD: "
                            + addEx.getMessage());
                    // Vẫn cho UPDATE chạy khi đã DROP
                    return true;
                }
            } finally {
                st.close();
            }
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.ensureOccupancyCheckConstraint: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /** Gắn lại CHECK occupancy nếu đã DROP mà chưa ADD được. */
    private void reapplyOccupancyCheckIfMissing() {
        try {
            connection = getConnection();
            if (connection == null) {
                return;
            }
            Statement st = connection.createStatement();
            try {
                st.execute(
                        "IF NOT EXISTS (SELECT 1 FROM sys.check_constraints "
                        + "WHERE name = N'CK_apartments_occupancy' "
                        + "AND parent_object_id = OBJECT_ID(N'dbo.apartments')) "
                        + "ALTER TABLE dbo.apartments ADD CONSTRAINT CK_apartments_occupancy "
                        + "CHECK (occupancy_type IN (N'OWNED', N'RENTED', N'VACANT', N'N/A'))");
                occupancyCheckReady = true;
            } finally {
                st.close();
            }
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.reapplyOccupancyCheckIfMissing: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    /**
     * Sửa hàng loạt occupancy (khi mở list):
     * <ol>
     *   <li>INACTIVE → N/A</li>
     *   <li>ACTIVE + TENANT/REP → RENTED</li>
     *   <li>ACTIVE + OWNER only (không tenant) → OWNED
     *       (không đụng căn đang RENTED — giữ layout gán thuê trống)</li>
     *   <li>ACTIVE + TV hộ, không role → OWNED (giữ RENTED nếu đã chọn)</li>
     *   <li>ACTIVE trống + occupancy lạ/N/A/null → VACANT
     *       (KHÔNG ép OWNED/RENTED trống về VACANT)</li>
     *   <li>VACANT còn notes → xóa notes</li>
     * </ol>
     *
     * @return tổng số dòng đã UPDATE qua các bước
     */
    public int reconcileAllOccupancy() {
        ensureOccupancyCheckConstraint();

        int total = 0;
        connection = getConnection();
        if (connection == null) {
            System.out.println("ApartmentDAO.reconcileAllOccupancy: connection null");
            return 0;
        }
        try {
            // 1) INACTIVE → N/A
            total += runUpdateSafe(connection,
                    "UPDATE apartments SET occupancy_type = N'N/A', updated_at = SYSUTCDATETIME() "
                    + "WHERE status = N'INACTIVE' AND ISNULL(occupancy_type, N'') <> N'N/A'");

            // 2) ACTIVE + tenant → RENTED (giữ OWNER nếu có)
            total += runUpdateSafe(connection,
                    "UPDATE a SET a.occupancy_type = N'RENTED', a.updated_at = SYSUTCDATETIME() "
                    + "FROM apartments a "
                    + "WHERE a.status = N'ACTIVE' "
                    + "AND EXISTS (SELECT 1 FROM apartment_residents r "
                    + "  WHERE r.apartment_id = a.apartment_id AND r.is_current = 1 "
                    + "  AND r.role_in_apartment IN (N'TENANT_REP', N'TENANT')) "
                    + "AND ISNULL(a.occupancy_type, N'') <> N'RENTED'");

            // 3) ACTIVE + owner only (không tenant) → OWNED
            //    Không đụng căn đang RENTED (ô thuê trống — gán lại được).
            total += runUpdateSafe(connection,
                    "UPDATE a SET a.occupancy_type = N'OWNED', a.updated_at = SYSUTCDATETIME() "
                    + "FROM apartments a "
                    + "WHERE a.status = N'ACTIVE' "
                    + "AND EXISTS (SELECT 1 FROM apartment_residents r "
                    + "  WHERE r.apartment_id = a.apartment_id AND r.is_current = 1 "
                    + "  AND r.role_in_apartment = N'OWNER') "
                    + "AND NOT EXISTS (SELECT 1 FROM apartment_residents r "
                    + "  WHERE r.apartment_id = a.apartment_id AND r.is_current = 1 "
                    + "  AND r.role_in_apartment IN (N'TENANT_REP', N'TENANT')) "
                    + "AND ISNULL(a.occupancy_type, N'') NOT IN (N'OWNED', N'RENTED')");

            // 4) ACTIVE + TV hộ, không role → OWNED (giữ RENTED nếu đã chọn)
            total += runUpdateSafe(connection,
                    "UPDATE a SET a.occupancy_type = N'OWNED', a.updated_at = SYSUTCDATETIME() "
                    + "FROM apartments a "
                    + "WHERE a.status = N'ACTIVE' "
                    + "AND NOT EXISTS (SELECT 1 FROM apartment_residents r "
                    + "  WHERE r.apartment_id = a.apartment_id AND r.is_current = 1) "
                    + "AND EXISTS (SELECT 1 FROM household_members hm "
                    + "  WHERE hm.apartment_id = a.apartment_id AND hm.is_active = 1) "
                    + "AND ISNULL(a.occupancy_type, N'') NOT IN (N'OWNED', N'RENTED')");

            // 5) ACTIVE trống + occupancy không hợp lệ (null / N/A / lạ) → VACANT
            //    Giữ OWNED / RENTED / VACANT đã chọn lúc kích hoạt hoặc form Sửa.
            total += runUpdateSafe(connection,
                    "UPDATE a SET a.occupancy_type = N'VACANT', a.notes = NULL, a.updated_at = SYSUTCDATETIME() "
                    + "FROM apartments a "
                    + "WHERE a.status = N'ACTIVE' "
                    + "AND NOT EXISTS (SELECT 1 FROM apartment_residents r "
                    + "  WHERE r.apartment_id = a.apartment_id AND r.is_current = 1) "
                    + "AND NOT EXISTS (SELECT 1 FROM household_members hm "
                    + "  WHERE hm.apartment_id = a.apartment_id AND hm.is_active = 1) "
                    + "AND ISNULL(a.occupancy_type, N'') NOT IN (N'VACANT', N'OWNED', N'RENTED')");

            // 5b) Mọi VACANT còn notes → xóa (INACTIVE giữ notes)
            total += runUpdateSafe(connection,
                    "UPDATE apartments SET notes = NULL, updated_at = SYSUTCDATETIME() "
                    + "WHERE occupancy_type = N'VACANT' AND notes IS NOT NULL");

            System.out.println("ApartmentDAO.reconcileAllOccupancy: updated rows=" + total);
        } finally {
            closeResources();
        }

        reapplyOccupancyCheckIfMissing();
        return total;
    }

    /** UPDATE từng bước — lỗi 1 bước không chặn bước sau. */
    private int runUpdateSafe(Connection conn, String sql) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.runUpdateSafe error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Các unit (1..99) đã có mã đúng format {@code {prefix}{UU}} trên prefix tòa-tầng (vd. A-03).
     *
     * @param codePrefix prefix mã căn (vd. "A-03")
     * @return tập unit đã dùng
     */
    public java.util.Set<Integer> findExistingUnitsByCodePrefix(String codePrefix) {
        java.util.Set<Integer> units = new java.util.HashSet<>();
        if (codePrefix == null || codePrefix.isEmpty()) {
            return units;
        }
        String sql = "SELECT apartment_code FROM apartments WHERE apartment_code LIKE ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return units;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, codePrefix + "%");
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String code = resultSet.getString(1);
                if (code == null || !code.startsWith(codePrefix)) {
                    continue;
                }
                String unitPart = code.substring(codePrefix.length());
                if (unitPart.length() == 2 && unitPart.chars().allMatch(Character::isDigit)) {
                    units.add(Integer.parseInt(unitPart));
                }
            }
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.findExistingUnitsByCodePrefix error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return units;
    }

    /**
     * Đếm cư dân hiện tại gắn căn (bảng apartment_residents).
     * Nếu bảng chưa có / lỗi SQL → trả 0 (MVP an toàn, hard delete vẫn theo status).
     *
     * @param apartmentId id căn
     * @return số cư dân current; lỗi → 0
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
     *
     * @param apartmentId id căn
     * @return true nếu xóa thành công
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
