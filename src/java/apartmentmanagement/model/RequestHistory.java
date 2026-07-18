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
public class RequestHistory {
    private Integer historyId;
    private Integer requestId;
    private Integer changedBy;
    private String oldStatus;
    private String newStatus;
    private String note;
    private Timestamp createdAt;

    // display helpers (JOIN flat fields)
    private String changedByName;
    private String changedByRole;

    public boolean isComment() {
        return oldStatus != null && oldStatus.equals(newStatus);
    }
}
