package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO extends DBContext {

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

    public boolean updateProfile(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, updated_at = SYSUTCDATETIME() WHERE user_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setInt(4, user.getUserId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("UserDAO.updateProfile error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

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
     * User đang active — dùng dropdown gán owner/tenant (UC-APT-06).
     * Ưu tiên RESIDENT trước, sau đó role khác.
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
     * username unique; password mặc định 123456 (demo PRJ301).
     * @return userId mới, hoặc -1 nếu lỗi / trùng username
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
