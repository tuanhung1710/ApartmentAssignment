package apartmentmanagement.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeAssignment {
    private Integer assignmentId;
    private Integer feeId;
    private Integer apartmentId;
    private BigDecimal amount;
    private String status;
    private Timestamp assignedAt;
    private Timestamp paidAt;
    private String apartmentCode;
    private String building;
    private Integer floorNumber;
    private String feeTitle;
    private String categoryName;
    private Integer feeMonth;
    private Integer feeYear;
    private String feeType;
}
