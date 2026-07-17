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
    /** FK buildings.building_id — filter theo tòa (TV2+) */
    private Integer buildingId;
    /** Mã tòa denormalized (A/B/…) — tương thích seed cũ */
    private String building;
    private Integer floorNumber;
    private BigDecimal areaM2;
    /**
     * Loại hình khi ACTIVE: OWNED | RENTED | VACANT.
     * Khi status = INACTIVE UI hiển thị N/A (không filter theo loại).
     */
    private String occupancyType;
    /** ACTIVE | INACTIVE */
    private String status;
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
