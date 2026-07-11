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
public class Request {
    private Integer requestId;
    private Integer apartmentId;
    private Integer createdBy;
    /** REPAIR | PARKING | MOVE_IN | MOVE_OUT | OTHER */
    private String requestType;
    private String title;
    private String description;
    private String locationDetail;
    private String urgency;
    private String vehicleType;
    private String plateNumber;
    private Timestamp scheduledAt;
    private String moveNote;
    private String status;
    private String rejectReason;
    private Integer assignedTo;
    private Integer approvedBy;
    private Timestamp approvedAt;
    private Timestamp completedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // display helpers (JOIN flat fields)
    private String apartmentCode;
    private String createdByName;
    private String assignedToName;
}
