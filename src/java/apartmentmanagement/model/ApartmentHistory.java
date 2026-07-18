package apartmentmanagement.model;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lịch sử thay đổi trạng thái/hành động trên căn hộ (audit trail).
 * {@code actorName} là trường hiển thị từ JOIN, không map cột riêng.
 */
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
    /** Tên người thực hiện (JOIN users) — chỉ dùng hiển thị */
    private String actorName;
    private Timestamp createdAt;
}
