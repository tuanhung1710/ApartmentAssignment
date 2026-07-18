package apartmentmanagement.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Can ho trong he thong quan ly chung cu.
 * Mot so truong (memberCount) chi phuc vu hien thi, khong map cot DB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Apartment {
    private Integer apartmentId;
    private String apartmentCode;
    /** FK buildings.building_id — null neu toa chua co trong master */
    private Integer buildingId;
    /** Ma toa denormalized (A/B/…) — dung filter va generate code TV2 */
    private String building;
    private Integer floorNumber;
    private BigDecimal areaM2;
    /**
     * Loai hinh khi ACTIVE: OWNED | RENTED | VACANT.
     * Khi status = INACTIVE UI hien thi N/A (khong filter theo loai).
     */
    private String occupancyType;
    /** ACTIVE | INACTIVE */
    private String status;
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    /** So thanh vien ho active (display list; khong map cot DB) */
    private Integer memberCount;
}
