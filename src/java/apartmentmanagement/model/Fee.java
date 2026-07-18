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
public class Fee {
    private Integer feeId;
    private Integer categoryId;
    private String title;
    private BigDecimal amount;
    private Integer feeMonth;
    private Integer feeYear;
    private String feeType;
    private String status;
    private String note;
    private Integer createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private String categoryName;
    private String scopeType;
    private String scopeBuilding;
    private Integer scopeFloor;
    private Integer scopeApartmentId;
    private String scopeApartmentCode;
    private Integer assignmentCount;
    private Integer paidCount;
    private Integer unpaidCount;
    private BigDecimal totalReceivable;
    private BigDecimal totalPaid;
    private BigDecimal totalUnpaid;
}
