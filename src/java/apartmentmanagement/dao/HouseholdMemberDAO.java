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
 * DAO thành viên hộ ({@code household_members}):
 * CRUD, kiểm tra trùng CCCD/họ tên, soft/hard delete, sync từ owner/tenant.
 */
public class HouseholdMemberDAO extends DBContext {

    /**
     * Map ResultSet sang {@link HouseholdMember}.
     *
     * @param rs ResultSet đang trỏ tới dòng hợp lệ
     * @return entity đã map
     * @throws SQLException nếu đọc cột lỗi
     */
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

    /**
     * Chỉ thành viên đang active (hiển thị detail căn).
     *
     * @param apartmentId id căn
     * @return danh sách TV active, sort theo tên
     */
    public List<HouseholdMember> findActiveByApartmentId(int apartmentId) {
        List<HouseholdMember> list = new ArrayList<>();
        String sql = "SELECT * FROM household_members WHERE apartment_id = ? AND is_active = 1 "
                + "ORDER BY full_name";
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
            System.out.println("HouseholdMemberDAO.findActiveByApartmentId: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    /**
     * Alias {@link #findActiveByApartmentId} — API cũ chỉ lấy TV active.
     *
     * @param apartmentId id căn
     * @return danh sách TV active
     */
    public List<HouseholdMember> findByApartmentId(int apartmentId) {
        return findActiveByApartmentId(apartmentId);
    }

    /**
     * Tìm thành viên theo primary key (kể cả inactive).
     *
     * @param memberId id thành viên
     * @return entity hoặc null
     */
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

    /**
     * Đã có TV active cùng CCCD/CMND trên căn.
     *
     * @param apartmentId id căn
     * @param idNumber    số giấy tờ
     * @return true nếu trùng
     */
    public boolean existsActiveIdNumber(int apartmentId, String idNumber) {
        return existsActiveIdNumberExceptId(apartmentId, idNumber, null);
    }

    /**
     * Kiểm tra trùng CCCD active, có thể loại trừ 1 member khi edit.
     *
     * @param apartmentId    id căn
     * @param idNumber       số giấy tờ
     * @param exceptMemberId id bỏ qua (nullable)
     * @return true nếu trùng
     */
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

    /**
     * Thêm thành viên hộ mới.
     *
     * @param m entity (apartmentId, fullName, relationship…); isActive null → true
     * @return generated member_id; 0 nếu OK nhưng không lấy key; -1 lỗi
     */
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

    /**
     * Cập nhật thông tin TV (không đổi is_active).
     * Điều kiện WHERE gồm cả apartment_id để tránh sửa nhầm căn.
     *
     * @param m entity (memberId + apartmentId bắt buộc)
     * @return true nếu cập nhật thành công
     */
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
     * Soft delete (giữ row, is_active=0).
     *
     * @param memberId    id thành viên
     * @param apartmentId id căn (scope)
     * @return true nếu cập nhật thành công
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

    /**
     * Hard delete — gỡ hẳn khỏi thành viên hộ.
     *
     * @param memberId    id thành viên
     * @param apartmentId id căn (scope)
     * @return true nếu xóa thành công
     */
    public boolean hardDelete(int memberId, int apartmentId) {
        String sql = "DELETE FROM household_members WHERE member_id = ? AND apartment_id = ?";
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
            System.out.println("HouseholdMemberDAO.hardDelete: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Xóa TV active theo quan hệ (vd. "Chủ hộ" khi gỡ owner).
     *
     * @param apartmentId  id căn
     * @param relationship quan hệ cần xóa
     * @return số dòng xóa; -1 lỗi
     */
    public int hardDeleteByRelationship(int apartmentId, String relationship) {
        String sql = "DELETE FROM household_members WHERE apartment_id = ? AND relationship = ? AND is_active = 1";
        try {
            connection = getConnection();
            if (connection == null) {
                return -1;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            statement.setString(2, relationship);
            return statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("HouseholdMemberDAO.hardDeleteByRelationship: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    /**
     * Xóa TV active khớp họ tên + quan hệ (đồng bộ khi gỡ owner/thuê).
     *
     * @param apartmentId  id căn
     * @param fullName     họ tên
     * @param relationship quan hệ
     * @return số dòng xóa; 0 nếu tên rỗng; -1 lỗi
     */
    public int hardDeleteByNameAndRelationship(int apartmentId, String fullName, String relationship) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return 0;
        }
        String sql = "DELETE FROM household_members WHERE apartment_id = ? AND is_active = 1 "
                + "AND full_name = ? AND relationship = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return -1;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            statement.setString(2, fullName.trim());
            statement.setString(3, relationship);
            return statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("HouseholdMemberDAO.hardDeleteByNameAndRelationship: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    /**
     * Soft-delete toàn bộ TV active của căn (khi đổi owner — clear nhân khẩu cũ).
     *
     * @param apartmentId id căn
     * @return số dòng cập nhật; -1 lỗi
     */
    public int softDeleteAllActiveByApartment(int apartmentId) {
        String sql = "UPDATE household_members SET is_active = 0 "
                + "WHERE apartment_id = ? AND is_active = 1";
        try {
            connection = getConnection();
            if (connection == null) {
                return -1;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            return statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("HouseholdMemberDAO.softDeleteAllActiveByApartment: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    /**
     * Đã có thành viên active trùng họ tên trên căn (không phân biệt quan hệ).
     * So sánh không phân biệt hoa thường, trim khoảng trắng.
     *
     * @param apartmentId id căn
     * @param fullName    họ tên
     * @return true nếu trùng
     */
    public boolean existsActiveByFullName(int apartmentId, String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 FROM household_members "
                + "WHERE apartment_id = ? AND is_active = 1 AND LOWER(LTRIM(RTRIM(full_name))) = LOWER(?)";
        try {
            connection = getConnection();
            if (connection == null) {
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, apartmentId);
            statement.setString(2, fullName.trim());
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("HouseholdMemberDAO.existsActiveByFullName: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Upsert thành viên active theo (căn + họ tên + quan hệ) — sync từ owner/tenant.
     * Nếu đã có active cùng tên (bất kỳ quan hệ) → bỏ qua insert.
     * Nếu đã có đúng tên+quan hệ → trả memberId hiện có; không → insert.
     *
     * @param apartmentId  id căn
     * @param fullName     họ tên
     * @param relationship quan hệ (null/empty → "Thành viên")
     * @param phone        SĐT optional
     * @return memberId nếu đã có hoặc insert OK; -1 lỗi; 0 nếu đã tồn tại (tên bất kỳ quan hệ) và skip insert
     */
    public int ensureActiveMember(int apartmentId, String fullName, String relationship, String phone) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return -1;
        }
        String name = fullName.trim();
        String rel = relationship == null || relationship.isEmpty() ? "Thành viên" : relationship.trim();

        // Đã có trong thành viên hộ (cùng tên) → không thêm dòng mới
        if (existsActiveByFullName(apartmentId, name)) {
            return 0;
        }

        Integer existingId = null;
        String sqlFind = "SELECT member_id FROM household_members "
                + "WHERE apartment_id = ? AND is_active = 1 AND full_name = ? AND relationship = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                return -1;
            }
            statement = connection.prepareStatement(sqlFind);
            statement.setInt(1, apartmentId);
            statement.setString(2, name);
            statement.setString(3, rel);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                existingId = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("HouseholdMemberDAO.ensureActiveMember: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
        if (existingId != null) {
            return existingId;
        }
        return insert(HouseholdMember.builder()
                .apartmentId(apartmentId)
                .fullName(name)
                .relationship(rel)
                .phone(phone)
                .isActive(true)
                .build());
    }

    /** Chuỗi rỗng → null khi bind SQL (tránh lưu "" cho phone/CCCD). */
    private String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
