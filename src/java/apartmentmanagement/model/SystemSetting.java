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
public class SystemSetting {
    private String settingKey;
    private String settingValue;
    private String description;
    private Integer updatedBy;
    private Timestamp updatedAt;
}
