package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO tài khoản hệ thống ({@code users}): đăng nhập, hồ sơ, mật khẩu,
 * CRUD cơ bản và helper tạo/tìm user khi gán owner/tenant.
 */
public class UserDAO extends DBContext {

    /**
     * Map một dòng ResultSet sang {@link User}.
     *
     * @param rs result set đang trỏ tới dòng hiện tại
     * @return user đã map
     * @throws SQLException nếu đọc cột lỗi
     */
    public User getFromResultSet(ResultSet rs) throws SQLException {
        return User.builder()
                .userId(rs.getInt("user_id"))
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .fullName(rs.getString("full_name"))
                .email(rs.getString("email"))
                .phone(rs.getString("phone"))
                .role(rs.getString("role"))
                .department(rs.getString("department"))
                .isActive(rs.getBoolean("is_active"))
                .createdAt(rs.getTimestamp("created_at"))
                .updatedAt(rs.getTimestamp("updated_at"))
                .build();
    }

    /**
     * Xác thực đăng nhập: khớp username + password và tài khoản đang active.
     *
     * @param username tên đăng nhập
     * @param password mật khẩu (so khớp trực tiếp như schema hiện tại)
     * @return user nếu hợp lệ; {@code null} nếu sai hoặc lỗi
     */
    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = 1";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.login error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    /**
     * Tìm user theo khóa chính.
     *
     * @param userId mã user
     * @return user hoặc {@code null}
     */
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.findById error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    /**
     * Tìm user theo username (không lọc trạng thái active).
     *
     * @param username tên đăng nhập
     * @return user hoặc {@code null}
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.findByUsername error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    /**
     * Quên mật khẩu: lấy user active theo username.
     *
     * @param username tên đăng nhập
     * @return user active hoặc {@code null}
     */
    public User findActiveByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.findActiveByUsername error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    /**
     * Quên mật khẩu: khớp username + email (không phân biệt hoa thường).
     *
     * @param username tên đăng nhập
     * @param email    email xác minh
     * @return user active khớp hoặc {@code null}
     */
    public User findActiveByUsernameAndEmail(String username, String email) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1 "
                + "AND email IS NOT NULL AND LOWER(email) = LOWER(?)";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, email);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.findActiveByUsernameAndEmail error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    /**
     * Quên mật khẩu: khớp username + số điện thoại.
     *
     * @param username tên đăng nhập
     * @param phone    SĐT xác minh
     * @return user active khớp hoặc {@code null}
     */
    public User findActiveByUsernameAndPhone(String username, String phone) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1 "
                + "AND phone IS NOT NULL AND phone = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, phone);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.findActiveByUsernameAndPhone error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    /**
     * Cập nhật hồ sơ (họ tên, SĐT; email tùy chọn).
     * Nếu {@code email == null} thì không đụng cột email — AuthenController TV1
     * chỉ gửi fullName+phone, tránh xóa email cũ.
     *
     * @param user user mang userId và các field cần cập nhật
     * @return {@code true} nếu cập nhật thành công
     */
    public boolean updateProfile(User user) {
        String sql = user.getEmail() != null
                ? "UPDATE users SET full_name = ?, email = ?, phone = ?, updated_at = SYSUTCDATETIME() WHERE user_id = ?"
                : "UPDATE users SET full_name = ?, phone = ?, updated_at = SYSUTCDATETIME() WHERE user_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            if (user.getEmail() != null) {
                statement.setString(1, user.getFullName());
                statement.setString(2, user.getEmail());
                statement.setString(3, user.getPhone());
                statement.setInt(4, user.getUserId());
            } else {
                // Không ghi đè email khi caller không truyền
                statement.setString(1, user.getFullName());
                statement.setString(2, user.getPhone());
                statement.setInt(3, user.getUserId());
            }
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("UserDAO.updateProfile error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Kiểm tra mật khẩu hiện tại của user (đổi mật khẩu / xác minh).
     *
     * @param userId      mã user
     * @param oldPassword mật khẩu cần đối chiếu
     * @return {@code true} nếu khớp
     */
    public boolean checkPassword(int userId, String oldPassword) {
        String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setString(2, oldPassword);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("UserDAO.checkPassword error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Đổi mật khẩu theo userId.
     *
     * @param userId      mã user
     * @param newPassword mật khẩu mới
     * @return {@code true} nếu cập nhật thành công
     */
    public boolean changePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ?, updated_at = SYSUTCDATETIME() WHERE user_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, newPassword);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("UserDAO.changePassword error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Alias {@link #changePassword(int, String)} cho flow forgot-reset (AuthenController).
     *
     * @param userId      mã user
     * @param newPassword mật khẩu mới
     * @return {@code true} nếu cập nhật thành công
     */
    public boolean updatePassword(int userId, String newPassword) {
        return changePassword(userId, newPassword);
    }

    /**
     * Lấy toàn bộ user, sắp theo user_id.
     *
     * @return danh sách user (rỗng nếu lỗi)
     */
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.findAll error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    /**
     * User đang active — nguồn dropdown gán owner/tenant.
     * Ưu tiên role RESIDENT, sau đó role khác; sắp full_name, username.
     *
     * @return danh sách user active (rỗng nếu lỗi / không có kết nối)
     */
    public List<User> findActiveUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_active = 1 "
                + "ORDER BY CASE WHEN role = 'RESIDENT' THEN 0 ELSE 1 END, full_name, username";
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
            System.out.println("UserDAO.findActiveUsers error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    /**
     * Tìm một user active theo họ tên (không phân biệt hoa thường, trim).
     * Dùng khi gán owner/thuê từ thành viên hộ đã có sẵn.
     * Ưu tiên RESIDENT nếu trùng tên nhiều người.
     *
     * @param fullName họ tên cần khớp
     * @return user active đầu tiên khớp hoặc {@code null}
     */
    public User findActiveByFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT TOP 1 * FROM users WHERE is_active = 1 "
                + "AND LOWER(LTRIM(RTRIM(full_name))) = LOWER(?) "
                + "ORDER BY CASE WHEN role = 'RESIDENT' THEN 0 ELSE 1 END, user_id";
        try {
            connection = getConnection();
            if (connection == null) {
                return null;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, fullName.trim());
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.findActiveByFullName error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    /**
     * Thêm user mới; trả về identity vừa sinh.
     *
     * @param user dữ liệu insert ({@code isActive == null} → mặc định true)
     * @return userId mới; {@code 0} nếu insert OK nhưng không lấy được key; {@code -1} nếu lỗi
     */
    public int insert(User user) {
        String sql = "INSERT INTO users (username, password, full_name, email, phone, role, department, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getRole());
            statement.setString(7, user.getDepartment());
            // null isActive → coi như active khi tạo mới
            statement.setBoolean(8, user.getIsActive() == null || user.getIsActive());
            int affected = statement.executeUpdate();
            if (affected > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
            return affected > 0 ? 0 : -1;
        } catch (SQLException e) {
            System.out.println("UserDAO.insert error: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    /**
     * Tạo nhanh user RESIDENT active (gán owner/tenant người mới).
     * Sinh username unique từ hint/fullName; password mặc định {@code 123456} (demo PRJ301).
     *
     * @param fullName     họ tên bắt buộc
     * @param phone        SĐT (nullable)
     * @param email        email (nullable)
     * @param usernameHint gợi ý username; null/blank → slug từ fullName
     * @return userId mới, hoặc {@code -1} nếu lỗi / trùng username hết suffix
     */
    public int createResidentQuick(String fullName, String phone, String email, String usernameHint) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return -1;
        }
        String base = slugUsername(usernameHint != null && !usernameHint.trim().isEmpty()
                ? usernameHint : fullName);
        if (base.isEmpty()) {
            base = "resident";
        }
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        // Thêm suffix số cho đến khi username trống (giới hạn độ dài cột)
        String username = base;
        int suffix = 1;
        while (findByUsername(username) != null && suffix < 1000) {
            String s = String.valueOf(suffix++);
            int maxBase = Math.max(1, 50 - 1 - s.length());
            username = (base.length() > maxBase ? base.substring(0, maxBase) : base) + s;
        }
        if (findByUsername(username) != null) {
            return -1;
        }
        return insert(User.builder()
                .username(username)
                .password("123456")
                .fullName(fullName.trim())
                .phone(phone == null || phone.trim().isEmpty() ? null : phone.trim())
                .email(email == null || email.trim().isEmpty() ? null : email.trim())
                .role("RESIDENT")
                .department(null)
                .isActive(true)
                .build());
    }

    /** Chuẩn hóa chuỗi thành username ASCII (bỏ dấu tiếng Việt, ký tự đặc biệt). */
    private String slugUsername(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim().toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9]+", "")
                .replaceAll("^[^a-z0-9]+", "");
        if (s.isEmpty()) {
            // fallback nếu fullName toàn dấu/ký tự lạ
            s = "user" + System.currentTimeMillis() % 100000;
        }
        return s;
    }

    /**
     * Bật/tắt tài khoản (is_active).
     *
     * @param userId   mã user
     * @param isActive trạng thái mới
     * @return {@code true} nếu cập nhật thành công
     */
    public boolean updateStatus(int userId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ?, updated_at = SYSUTCDATETIME() WHERE user_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setBoolean(1, isActive);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("UserDAO.updateStatus error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Đếm tổng số user.
     *
     * @return số lượng; 0 nếu lỗi
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM users";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.countAll error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    /**
     * Đếm user theo trạng thái active/inactive.
     *
     * @param active {@code true} = active, {@code false} = inactive
     * @return số lượng; 0 nếu lỗi
     */
    public int countByActive(boolean active) {
        String sql = "SELECT COUNT(*) FROM users WHERE is_active = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setBoolean(1, active);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.countByActive error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }
}
