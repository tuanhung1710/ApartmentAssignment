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
public class FeeCategory {
    private Integer categoryId;
    private String name;
    private String description;
    private Boolean isActive;
    private Timestamp createdAt;
}
