package apartmentmanagement.model;

import java.sql.Date;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentResident {
    private Integer id;
    private Integer apartmentId;
    private Integer userId;
    /** OWNER | TENANT_REP */
    private String roleInApartment;
    private Boolean isCurrent;
    private Date startDate;
    private Date endDate;
    private Timestamp createdAt;

    // display helpers (JOIN, không phải nested object entity)
    private String username;
    private String fullName;
    private String apartmentCode;
}
