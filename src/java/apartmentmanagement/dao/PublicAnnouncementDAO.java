package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.Announcement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class PublicAnnouncementDAO extends DBContext {

    public Announcement getFromResultSet(java.sql.ResultSet rs) throws SQLException {
        return Announcement.builder()
                .announcementId(rs.getInt("announcement_id"))
                .title(rs.getString("title"))
                .content(rs.getString("content"))
                .category(rs.getString("category"))
                .isPublished(rs.getBoolean("is_published"))
                .createdBy(rs.getObject("created_by") == null ? null : rs.getInt("created_by"))
                .publishedAt(rs.getTimestamp("published_at"))
                .createdAt(rs.getTimestamp("created_at"))
                .updatedAt(rs.getTimestamp("updated_at"))
                .build();
    }

    
    public List<Announcement> findPublished(int limit) {
        List<Announcement> list = new ArrayList<>();
        String sql = """
                SELECT TOP 12
                    announcement_id, title, content, category, is_published,
                    created_by, published_at, created_at, updated_at
                FROM announcements
                WHERE is_published = 1
                ORDER BY
                    CASE WHEN published_at IS NULL THEN 1 ELSE 0 END,
                    published_at DESC,
                    created_at DESC
                """;
        try {
            connection = getConnection();
            if (connection == null) {
                return list;
            }
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
                if (limit > 0 && list.size() >= limit) {
                    break;
                }
            }
        } catch (SQLException e) {
            System.out.println("PublicAnnouncementDAO.findPublished error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }
}
