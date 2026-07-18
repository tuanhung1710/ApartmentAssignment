package apartmentmanagement.model;

import java.sql.Date;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thành viên hộ gia đình gắn với một căn hộ (không nhất thiết có tài khoản user).
 * apartmentCode/building/floorNumber chỉ phục vụ hiển thị sau JOIN.
 */
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

    /** Display helpers (JOIN) — không phải nested entity */
    private String apartmentCode;
    private String building;
    private Integer floorNumber;
}
