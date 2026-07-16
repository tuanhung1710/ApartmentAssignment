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
public class Apartment {
    private Integer apartmentId;
    private String apartmentCode;
    /** FK buildings.building_id — optional (null nếu tòa chưa có trong master) */
    private Integer buildingId;
    /** Mã tòa denormalized (A/B/…) — filter + generate code TV2 */
    private String building;
    private Integer floorNumber;
    private BigDecimal areaM2;
    /** OWNED | RENTED | VACANT | N/A */
    private String occupancyType;
    /** ACTIVE | INACTIVE */
    private String status;
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    /** Số thành viên hộ active (display list; không map cột DB) */
    private Integer memberCount;
}
