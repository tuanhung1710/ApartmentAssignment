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
public class FeeScope {
    private Integer scopeId;
    private Integer feeId;
    private String scopeType;
    private String building;
    private Integer floorNumber;
    private Integer apartmentId;
    private Timestamp createdAt;
    private String apartmentCode;
}
