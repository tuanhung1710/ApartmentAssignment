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
public class HouseholdMember {
    private Integer memberId;
    private Integer apartmentId;
    private String fullName;
    private String relationship;
    private String phone;
    private String idNumber;
    private Date dateOfBirth;
    private Boolean isActive;
    private Timestamp createdAt;
}
