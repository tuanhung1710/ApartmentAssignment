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
    private String building;
    private Integer floorNumber;
    private BigDecimal areaM2;
    /** OWNED | RENTED */
    private String occupancyType;
    /** ACTIVE | INACTIVE */
    private String status;
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
