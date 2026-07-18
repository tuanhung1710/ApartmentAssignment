package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.SystemSetting;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SystemSettingDAO extends DBContext {

    public SystemSetting getFromResultSet(ResultSet rs) throws SQLException {
        return SystemSetting.builder()
                .settingKey(rs.getString("setting_key"))
                .settingValue(rs.getString("setting_value"))
                .description(rs.getString("description"))
                .updatedBy(rs.getObject("updated_by") == null ? null : rs.getInt("updated_by"))
                .updatedAt(rs.getTimestamp("updated_at"))
                .build();
    }

    public String getValue(String key) {
        String sql = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, key);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            System.out.println("SystemSettingDAO.getValue error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    public SystemSetting findByKey(String key) {
        String sql = "SELECT * FROM system_settings WHERE setting_key = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, key);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("SystemSettingDAO.findByKey error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    public List<SystemSetting> findAll() {
        List<SystemSetting> list = new ArrayList<>();
        String sql = "SELECT * FROM system_settings ORDER BY setting_key";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("SystemSettingDAO.findAll error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }
}
