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
public class User {
    private Integer userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    /** ADMIN | MANAGER | STAFF | RESIDENT */
    private String role;
    private String department;
    private Boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
