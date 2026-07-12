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
public class ApartmentHistory {
    private Integer historyId;
    private Integer apartmentId;
    private String action;
    private String oldStatus;
    private String newStatus;
    private String note;
    private Integer actorUserId;
    private String actorName;
    private Timestamp createdAt;
}
