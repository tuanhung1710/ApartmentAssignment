package apartmentmanagement.model;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tòa nhà (building) trong master data chung cư.
 * {@code apartmentCount} chỉ dùng hiển thị list/detail, không map cột DB.
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

    /** Số căn thuộc tòa (display; không map cột DB) */
    private Integer apartmentCount;
}
