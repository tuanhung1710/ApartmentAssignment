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
public class Payment {
    private Integer paymentId;
    private Integer assignmentId;
    private BigDecimal amount;
    private Timestamp paidAt;
    private String note;
    private Integer recordedBy;
}
