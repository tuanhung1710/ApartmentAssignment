package apartmentmanagement.model;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lịch sử thay đổi trạng thái của một {@link Request}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestHistory {
    private Integer historyId;
    private Integer requestId;
    /** userId người thực hiện thay đổi. */
    private Integer changedBy;
    private String oldStatus;
    private String newStatus;
    private String note;
    private Timestamp createdAt;

    /** Họ tên người thay đổi (JOIN hiển thị). */
    private String changedByName;
    /** Vai trò người thay đổi (JOIN hiển thị / chat). */
    private String changedByRole;

    /** Comment/chat: oldStatus == newStatus (không đổi trạng thái ticket). */
    public boolean isComment() {
        return oldStatus != null && oldStatus.equals(newStatus);
    }
}
