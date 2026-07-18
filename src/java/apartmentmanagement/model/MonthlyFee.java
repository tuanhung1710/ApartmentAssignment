package apartmentmanagement.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Phí hàng tháng của một căn hộ (dịch vụ, nước, gửi xe) theo kỳ tháng/năm.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFee {
    private Integer feeId;
    private Integer apartmentId;
    /** Tháng tính phí (1–12). */
    private Integer feeMonth;
    private Integer feeYear;
    private BigDecimal serviceFee;
    private BigDecimal waterFee;
    private BigDecimal parkingFee;
    /** Tổng phí (cột tính toán / derived). */
    private BigDecimal totalAmount;
    /** DRAFT | PUBLISHED | PAID | UNPAID */
    private String status;
    private String note;
    private Integer createdBy;
    private Timestamp publishedAt;
    private Timestamp paidAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    /** Mã căn hộ (JOIN hiển thị, không map cột gốc). */
    private String apartmentCode;
}
