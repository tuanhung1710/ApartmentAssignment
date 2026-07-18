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
 * DAO gán user vào căn ({@code apartment_residents}):
 * vai trò OWNER / TENANT_REP / TENANT, đóng/gỡ role, hết hạn hợp đồng thuê.
 * <p>
 * Lưu ý UNIQUE {@code (apartment_id, user_id, role, is_current)}:
 * trước khi flip {@code is_current} 1→0 phải xóa bản ghi lịch sử cùng key.
 */
public class ApartmentResidentDAO extends DBContext {

    /** Lỗi SQL gần nhất (để controller hiện message rõ). */
    private String lastError;

    /**
     * @return lỗi gần nhất, hoặc null
     */
    public String getLastError() {
        return lastError;
    }

    private void clearError() {
        lastError = null;
    }

    /**
     * Map ResultSet sang {@link ApartmentResident} (join users nếu có username/full_name).
     *
     * @param rs ResultSet đang trỏ tới dòng hợp lệ
     * @return entity đã map
     * @throws SQLException nếu đọc cột bắt buộc lỗi
     */
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
                .apartmentCode(safeGetString(rs, "apartment_code"))
                .build();
    }

    /** Đọc cột optional (join) — thiếu cột → null, không ném lỗi. */
    private String safeGetString(ResultSet rs, String col) {
        try {
            return rs.getString(col);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Cư dân hiện tại của user (ưu tiên TENANT_REP), kèm mã căn.
     * Dùng cho module request (cư dân tạo/xem yêu cầu theo căn đang ở).
     *
     * @param userId id user
     * @return bản ghi current hoặc null
     */
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
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("ApartmentResidentDAO.findCurrentByUserId error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    /**
     * apartment_id hiện tại của user, hoặc null nếu chưa gán căn.
     *
     * @param userId id user
     * @return apartmentId hoặc null
     */
    public Integer findCurrentApartmentId(int userId) {
        ApartmentResident ar = findCurrentByUserId(userId);
        return ar == null ? null : ar.getApartmentId();
    }

    /**
     * Danh sách cư dân current theo một hoặc nhiều role.
     *
     * @param apartmentId id căn
     * @param roles       danh sách role (OWNER, TENANT_REP, TENANT…)
     * @return list (rỗng nếu roles rỗng hoặc không có bản ghi)
     */
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

    /**
     * OWNER đang current (bản ghi mới nhất nếu có nhiều).
     *
     * @param apartmentId id căn
     * @return owner hoặc null
     */
    public ApartmentResident findCurrentOwner(int apartmentId) {
        List<ApartmentResident> list = findByApartmentAndRoles(apartmentId, AppConstants.APT_ROLE_OWNER);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * TENANT_REP đang current (bản ghi mới nhất nếu có nhiều).
     *
     * @param apartmentId id căn
     * @return tenant rep hoặc null
     */
    public ApartmentResident findCurrentTenantRep(int apartmentId) {
        List<ApartmentResident> list = findByApartmentAndRoles(apartmentId, AppConstants.APT_ROLE_TENANT_REP);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * User có bất kỳ role current nào trên căn không.
     *
     * @param apartmentId id căn
     * @param userId      id user
     * @return true nếu đang là cư dân current
     */
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

    /**
     * User có đúng role current trên căn không.
     *
     * @param apartmentId     id căn
     * @param userId          id user
     * @param roleInApartment role cần kiểm tra
     * @return true nếu khớp
     */
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

    /**
     * Đóng (is_current=0) mọi OWNER current, ghi end_date.
     *
     * @param apartmentId id căn
     * @param endDate     ngày kết thúc
     * @return số dòng cập nhật; 0 nếu không có; -1 lỗi
     */
    public int endCurrentOwners(int apartmentId, Date endDate) {
        return endCurrentByRole(apartmentId, AppConstants.APT_ROLE_OWNER, endDate);
    }

    /**
     * Đóng (is_current=0) mọi TENANT_REP current, ghi end_date.
     *
     * @param apartmentId id căn
     * @param endDate     ngày kết thúc
     * @return số dòng cập nhật; 0 nếu không có; -1 lỗi
     */
    public int endCurrentTenantReps(int apartmentId, Date endDate) {
        return endCurrentByRole(apartmentId, AppConstants.APT_ROLE_TENANT_REP, endDate);
    }

    /**
     * Gỡ role hiện tại an toàn với UNIQUE (apartment_id, user_id, role, is_current):
     * <ol>
     *   <li>Xóa các bản ghi lịch sử cùng (căn, user, role, is_current=0) để tránh trùng key</li>
     *   <li>UPDATE is_current 1 → 0 + end_date</li>
     * </ol>
     *
     * @return số dòng đóng; 0 nếu không có current; -1 lỗi
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
     *
     * @param apartmentId id căn
     * @return số dòng xóa; -1 lỗi
     */
    public int deleteCurrentOwners(int apartmentId) {
        return deleteCurrentByRole(apartmentId, AppConstants.APT_ROLE_OWNER);
    }

    /**
     * Gỡ hẳn TENANT_REP + TENANT đang current.
     *
     * @param apartmentId id căn
     * @return số dòng xóa; -1 lỗi
     */
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

    /**
     * DELETE mọi bản ghi current theo role trên căn.
     *
     * @param apartmentId id căn
     * @param role        role cần gỡ
     * @return số dòng xóa; -1 lỗi
     */
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

    /**
     * Gỡ hẳn 1 cư dân current theo user + role (cascade xóa TV hộ phía controller).
     *
     * @param apartmentId id căn
     * @param userId      id user
     * @param role        role cần gỡ
     * @return số dòng xóa; 0 nếu role rỗng; -1 lỗi
     */
    public int deleteCurrentByUserAndRole(int apartmentId, int userId, String role) {
        clearError();
        if (role == null || role.isEmpty()) {
            return 0;
        }
        String sql = "DELETE FROM apartment_residents "
                + "WHERE apartment_id = ? AND user_id = ? AND role_in_apartment = ? AND is_current = 1";
        try {
            connection = getConnection();
            if (connection == null) {
                lastError = "Không kết nối được database.";
                return -1;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            statement.setInt(2, userId);
            statement.setString(3, role);
            return statement.executeUpdate();
        } catch (SQLException e) {
            lastError = friendlySqlError(e);
            System.out.println("ApartmentResidentDAO.deleteCurrentByUserAndRole: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    /**
     * Đóng hợp đồng thuê đã hết hạn (TENANT_REP / TENANT):
     * {@code is_current=1 AND end_date < hôm nay} → {@code is_current=0} (giữ end_date).
     * Xóa lịch sử trùng key trước khi flip để tránh vi phạm UNIQUE.
     *
     * @return số bản ghi đóng; lỗi → -1
     */
    public int expirePastDueTenants() {
        clearError();
        // Xóa lịch sử trùng key trước khi flip is_current 1→0 (UQ_ar_apartment_user_role)
        String deleteHistorySql
                = "DELETE h FROM apartment_residents h "
                + "INNER JOIN apartment_residents c "
                + "  ON c.apartment_id = h.apartment_id AND c.user_id = h.user_id "
                + " AND c.role_in_apartment = h.role_in_apartment "
                + "WHERE h.is_current = 0 AND c.is_current = 1 "
                + "  AND c.role_in_apartment IN (?, ?) "
                + "  AND c.end_date IS NOT NULL AND c.end_date < CAST(SYSUTCDATETIME() AS DATE)";
        String expireSql
                = "UPDATE apartment_residents SET is_current = 0 "
                + "WHERE is_current = 1 "
                + "  AND role_in_apartment IN (?, ?) "
                + "  AND end_date IS NOT NULL "
                + "  AND end_date < CAST(SYSUTCDATETIME() AS DATE)";
        try {
            connection = getConnection();
            if (connection == null) {
                lastError = "Không kết nối được database.";
                return -1;
            }
            statement = connection.prepareStatement(deleteHistorySql);
            statement.setString(1, AppConstants.APT_ROLE_TENANT_REP);
            statement.setString(2, AppConstants.APT_ROLE_TENANT);
            statement.executeUpdate();
            try {
                statement.close();
            } catch (SQLException ignored) {
            }
            statement = connection.prepareStatement(expireSql);
            statement.setString(1, AppConstants.APT_ROLE_TENANT_REP);
            statement.setString(2, AppConstants.APT_ROLE_TENANT);
            int n = statement.executeUpdate();
            if (n > 0) {
                System.out.println("ApartmentResidentDAO.expirePastDueTenants: closed=" + n);
            }
            return n;
        } catch (SQLException e) {
            lastError = friendlySqlError(e);
            System.out.println("ApartmentResidentDAO.expirePastDueTenants: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    /**
     * Gán OWNER mới (is_current=1, không end_date).
     *
     * @param apartmentId id căn
     * @param userId      id user
     * @param startDate   ngày bắt đầu
     * @return id bản ghi mới / id đã tồn tại; -1 lỗi
     */
    public int insertOwner(int apartmentId, int userId, Date startDate) {
        return insertResident(apartmentId, userId, AppConstants.APT_ROLE_OWNER, startDate, null);
    }

    /**
     * Gán TENANT_REP hoặc TENANT (có thể có end_date hợp đồng).
     *
     * @param apartmentId     id căn
     * @param userId          id user
     * @param roleInApartment TENANT_REP | TENANT
     * @param startDate       ngày bắt đầu
     * @param endDate         ngày kết thúc (nullable)
     * @return id bản ghi mới / id đã tồn tại; -1 lỗi
     */
    public int insertTenant(int apartmentId, int userId, String roleInApartment,
            Date startDate, Date endDate) {
        return insertResident(apartmentId, userId, roleInApartment, startDate, endDate);
    }

    /**
     * Insert cư dân current. Nếu đã có cùng (căn,user,role,current) → trả id cũ.
     * Xóa lịch sử is_current=0 cùng key trước insert để tránh conflict UNIQUE.
     */
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

    /** Chuyển SQLException sang message thân thiện cho UI. */
    private String friendlySqlError(SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (msg.contains("Invalid object name") && msg.toLowerCase().contains("apartment_residents")) {
            return "Bảng apartment_residents chưa có. Hãy chạy database/schema.sql rồi seed.sql";
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
