package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.Announcement;
import apartmentmanagement.util.DateTimeUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementDAO extends DBContext {

    public Announcement getFromResultSet(ResultSet rs) throws SQLException {
        Announcement.AnnouncementBuilder builder = Announcement.builder()
                .announcementId(rs.getInt("announcement_id"))
                .title(rs.getString("title"))
                .content(rs.getString("content"))
                .category(rs.getString("category"))
                .isPublished(rs.getBoolean("is_published"))
                .createdBy((Integer) rs.getObject("created_by"))
                .publishedAt(rs.getTimestamp("published_at"))
                .createdAt(rs.getTimestamp("created_at"))
                .updatedAt(rs.getTimestamp("updated_at"));

        try {
            builder.createdByName(rs.getString("created_by_name"));
        } catch (SQLException ignored) {
            // optional JOIN field
        }
        return builder.build();
    }

    public List<Announcement> findWithFilters(Boolean isPublished, int page, int pageSize) {
        List<Announcement> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.*, u.full_name AS created_by_name "
                        + "FROM announcements a "
                        + "LEFT JOIN users u ON a.created_by = u.user_id "
                        + "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (isPublished != null) {
            sql.append("AND a.is_published = ? ");
            params.add(isPublished);
        }
        sql.append("ORDER BY a.created_at DESC, a.announcement_id DESC ");
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("AnnouncementDAO.findWithFilters error: cannot connect to database");
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
            System.out.println("AnnouncementDAO.findWithFilters error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list;
    }

    public int countWithFilters(Boolean isPublished) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM announcements a WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (isPublished != null) {
            sql.append("AND a.is_published = ? ");
            params.add(isPublished);
        }

        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("AnnouncementDAO.countWithFilters error: cannot connect to database");
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
            System.out.println("AnnouncementDAO.countWithFilters error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return 0;
    }

    public Announcement findById(int announcementId) {
        String sql = "SELECT a.*, u.full_name AS created_by_name "
                + "FROM announcements a "
                + "LEFT JOIN users u ON a.created_by = u.user_id "
                + "WHERE a.announcement_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("AnnouncementDAO.findById error: cannot connect to database");
                return null;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, announcementId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("AnnouncementDAO.findById error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    public int insert(Announcement announcement) {
        String sql = "INSERT INTO announcements "
                + "(title, content, category, is_published, created_by, published_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("AnnouncementDAO.insert error: cannot connect to database");
                return -1;
            }
            statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, announcement.getTitle());
            statement.setString(2, announcement.getContent());
            statement.setString(3, announcement.getCategory());
            boolean published = announcement.getIsPublished() != null && announcement.getIsPublished();
            statement.setBoolean(4, published);
            if (announcement.getCreatedBy() != null) {
                statement.setInt(5, announcement.getCreatedBy());
            } else {
                statement.setNull(5, java.sql.Types.INTEGER);
            }
            if (published) {
                statement.setTimestamp(6, DateTimeUtil.nowTimestamp());
            } else {
                statement.setNull(6, java.sql.Types.TIMESTAMP);
            }
            int affected = statement.executeUpdate();
            if (affected > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
            return affected > 0 ? 0 : -1;
        } catch (SQLException e) {
            System.out.println("AnnouncementDAO.insert error: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    public boolean update(Announcement announcement) {
        String sql = "UPDATE announcements SET "
                + "title = ?, "
                + "content = ?, "
                + "category = ?, "
                + "is_published = ?, "
                + "published_at = CASE "
                + "    WHEN ? = 1 AND published_at IS NULL THEN ? "
                + "    WHEN ? = 1 THEN published_at "
                + "    ELSE NULL "
                + "END, "
                + "updated_at = ? "
                + "WHERE announcement_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("AnnouncementDAO.update error: cannot connect to database");
                return false;
            }
            boolean published = announcement.getIsPublished() != null && announcement.getIsPublished();
            Timestamp now = DateTimeUtil.nowTimestamp();
            statement = connection.prepareStatement(sql);
            statement.setString(1, announcement.getTitle());
            statement.setString(2, announcement.getContent());
            statement.setString(3, announcement.getCategory());
            statement.setBoolean(4, published);
            statement.setBoolean(5, published);
            statement.setTimestamp(6, now);
            statement.setBoolean(7, published);
            statement.setTimestamp(8, now);
            statement.setInt(9, announcement.getAnnouncementId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("AnnouncementDAO.update error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    public boolean delete(int announcementId) {
        String sql = "DELETE FROM announcements WHERE announcement_id = ?";
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("AnnouncementDAO.delete error: cannot connect to database");
                return false;
            }
            statement = connection.prepareStatement(sql);
            statement.setInt(1, announcementId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("AnnouncementDAO.delete error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }
}
