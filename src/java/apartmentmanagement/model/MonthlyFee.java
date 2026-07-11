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
public class MonthlyFee {
    private Integer feeId;
    private Integer apartmentId;
    private Integer feeMonth;
    private Integer feeYear;
    private BigDecimal serviceFee;
    private BigDecimal waterFee;
    private BigDecimal parkingFee;
    /** computed column */
    private BigDecimal totalAmount;
    /** DRAFT | PUBLISHED | PAID | UNPAID */
    private String status;
    private String note;
    private Integer createdBy;
    private Timestamp publishedAt;
    private Timestamp paidAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // display helpers
    private String apartmentCode;
}
