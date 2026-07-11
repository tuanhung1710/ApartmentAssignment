package apartmentmanagement.model;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String createdByName;
}
