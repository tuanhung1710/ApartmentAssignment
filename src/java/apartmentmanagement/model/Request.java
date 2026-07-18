package apartmentmanagement.model;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Yêu cầu cư dân/ban quản lý (sửa chữa, gửi xe, chuyển vào/ra, khác).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private Integer requestId;
    private Integer apartmentId;
    /** userId người tạo yêu cầu. */
    private Integer createdBy;
    /** REPAIR | PARKING | MOVE_IN | MOVE_OUT | OTHER */
    private String requestType;
    private String title;
    private String description;
    private String locationDetail;
    private String urgency;
    /** Dùng khi requestType = PARKING. */
    private String vehicleType;
    /** Dùng khi requestType = PARKING. */
    private String plateNumber;
    /** Thời điểm hẹn (MOVE_IN / MOVE_OUT / REPAIR nếu có). */
    private Timestamp scheduledAt;
    /** Ghi chú chuyển nhà (MOVE_IN / MOVE_OUT). */
    private String moveNote;
    private String status;
    private String rejectReason;
    /** userId nhân viên được giao xử lý. */
    private Integer assignedTo;
    private Integer approvedBy;
    private Timestamp approvedAt;
    private Timestamp completedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    /** Mã căn hộ (JOIN hiển thị). */
    private String apartmentCode;
    /** Họ tên người tạo (JOIN hiển thị). */
    private String createdByName;
    /** Họ tên người được giao (JOIN hiển thị). */
    private String assignedToName;
}
