package apartmentmanagement.model;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Toa nha (building) trong master data chung cu.
 * {@code apartmentCount} chi dung hien thi list/detail, khong map cot DB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Building {
    private Integer buildingId;
    private String buildingCode;
    private String buildingName;
    private String address;
    private Integer totalFloors;
    private String description;
    /** ACTIVE | INACTIVE */
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    /** So can thuoc toa (display; khong map cot DB) */
    private Integer apartmentCount;
}
