package apartmentmanagement.model;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bản ghi thông báo hệ thống (announcement).
 * {@code createdByName} là trường hiển thị từ JOIN, không map cột riêng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Announcement {
    private Integer announcementId;
    private String title;
    private String content;
    private String category;
    private Boolean isPublished;
    private Integer createdBy;
    private Timestamp publishedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    /** Tên người tạo (JOIN users) — chỉ dùng hiển thị */
    private String createdByName;
}
