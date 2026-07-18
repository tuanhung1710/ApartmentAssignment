package apartmentmanagement.model;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /** Không map DB – dùng hiển thị list/detail (số căn thuộc tòa) */
    private Integer apartmentCount;
}
