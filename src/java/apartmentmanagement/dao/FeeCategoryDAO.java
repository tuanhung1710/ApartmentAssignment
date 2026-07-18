package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.FeeCategory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FeeCategoryDAO extends DBContext {

    public FeeCategory getFromResultSet(ResultSet rs) throws SQLException {
        return FeeCategory.builder()
                .categoryId(rs.getInt("category_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .isActive(rs.getBoolean("is_active"))
                .createdAt(rs.getTimestamp("created_at"))
                .build();
    }

    public List<FeeCategory> findAllActive() {
        List<FeeCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM fee_categories WHERE is_active = 1 ORDER BY name";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("FeeCategoryDAO.findAllActive error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public List<FeeCategory> findAll() {
        List<FeeCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM fee_categories ORDER BY is_active DESC, name";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("FeeCategoryDAO.findAll error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public FeeCategory findById(int categoryId) {
        String sql = "SELECT * FROM fee_categories WHERE category_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("FeeCategoryDAO.findById error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    public int insert(FeeCategory cat) {
        String sql = "INSERT INTO fee_categories (name, description, is_active) VALUES (?, ?, ?)";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, cat.getName());
            statement.setString(2, cat.getDescription());
            statement.setBoolean(3, cat.getIsActive() == null || cat.getIsActive());
            int affected = statement.executeUpdate();
            if (affected > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("FeeCategoryDAO.insert error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return -1;
    }

    public boolean update(FeeCategory cat) {
        String sql = "UPDATE fee_categories SET name = ?, description = ?, is_active = ? WHERE category_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, cat.getName());
            statement.setString(2, cat.getDescription());
            statement.setBoolean(3, cat.getIsActive() != null && cat.getIsActive());
            statement.setInt(4, cat.getCategoryId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("FeeCategoryDAO.update error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }
}
