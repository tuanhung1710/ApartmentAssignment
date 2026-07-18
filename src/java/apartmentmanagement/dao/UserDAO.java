package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.User;
import apartmentmanagement.util.DateTimeUtil;
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
            if (connection == null) {
                System.out.println("UserDAO.login error: cannot connect to database");
                return null;
            }
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
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, updated_at = ? WHERE user_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setTimestamp(4, DateTimeUtil.nowTimestamp());
            statement.setInt(5, user.getUserId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("UserDAO.updateProfile error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public boolean changePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ?, updated_at = ? WHERE user_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("UserDAO.changePassword error: cannot connect to database");
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, newPassword);
            statement.setTimestamp(2, DateTimeUtil.nowTimestamp());
            statement.setInt(3, userId);
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

    public List<User> findActiveStaff() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users "
                + "WHERE role = N'STAFF' AND is_active = 1 "
                + "ORDER BY full_name, user_id";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("UserDAO.findActiveStaff error: cannot connect to database");
                return list;
            }
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.findActiveStaff error: " + e.getMessage());
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
            if (connection == null) {
                System.out.println("UserDAO.insert error: cannot connect to database");
                return -1;
            }
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

    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("UserDAO.existsByUsername error: cannot connect to database");
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, username.trim());
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("UserDAO.existsByUsername error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public boolean updateStatus(int userId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ?, updated_at = ? WHERE user_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("UserDAO.updateStatus error: cannot connect to database");
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setBoolean(1, isActive);
            statement.setTimestamp(2, DateTimeUtil.nowTimestamp());
            statement.setInt(3, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("UserDAO.updateStatus error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public boolean updateByAdmin(User user) {
        String sql = "UPDATE users SET "
                + "full_name = ?, "
                + "role = ?, "
                + "department = ?, "
                + "is_active = ?, "
                + "updated_at = ? "
                + "WHERE user_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("UserDAO.updateByAdmin error: cannot connect to database");
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getRole());
            statement.setString(3, user.getDepartment());
            statement.setBoolean(4, user.getIsActive() == null || user.getIsActive());
            statement.setTimestamp(5, DateTimeUtil.nowTimestamp());
            statement.setInt(6, user.getUserId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("UserDAO.updateByAdmin error: " + e.getMessage());
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

    public List<User> findWithFilters(String keyword, String role, Boolean isActive,
                                      int page, int pageSize) {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM users WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        appendUserFilters(sql, params, keyword, role, isActive);
        sql.append("ORDER BY user_id ASC ");
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("UserDAO.findWithFilters error: cannot connect to database");
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
            System.out.println("UserDAO.findWithFilters error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public int countWithFilters(String keyword, String role, Boolean isActive) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendUserFilters(sql, params, keyword, role, isActive);

        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("UserDAO.countWithFilters error: cannot connect to database");
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
            System.out.println("UserDAO.countWithFilters error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    private void appendUserFilters(StringBuilder sql, List<Object> params,
                                   String keyword, String role, Boolean isActive) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (username LIKE ? OR full_name LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (role != null && !role.trim().isEmpty()) {
            sql.append("AND role = ? ");
            params.add(role.trim());
        }
        if (isActive != null) {
            sql.append("AND is_active = ? ");
            params.add(isActive);
        }
    }
}
