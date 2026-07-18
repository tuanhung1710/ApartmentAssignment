package apartmentmanagement.model;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cấu hình hệ thống dạng key–value (đơn giá, tham số vận hành, v.v.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSetting {
    private String settingKey;
    private String settingValue;
    private String description;
    /** userId người cập nhật gần nhất. */
    private Integer updatedBy;
    private Timestamp updatedAt;
}
