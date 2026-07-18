package apartmentmanagement.util;

public final class AppConstants {

    private AppConstants() {
    }

    public static final String SESSION_USER = "currentUser";

    /** Flash message keys (session) */
    public static final String FLASH_SUCCESS = "flashSuccess";
    public static final String FLASH_ERROR = "flashError";

    /** Roles */
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_STAFF = "STAFF";
    public static final String ROLE_RESIDENT = "RESIDENT";

    /** Request status */
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    /** Request types */
    public static final String TYPE_REPAIR = "REPAIR";
    public static final String TYPE_PARKING = "PARKING";
    public static final String TYPE_MOVE_IN = "MOVE_IN";
    public static final String TYPE_MOVE_OUT = "MOVE_OUT";
    public static final String TYPE_OTHER = "OTHER";

    /** Fee status */
    public static final String FEE_DRAFT = "DRAFT";
    public static final String FEE_PUBLISHED = "PUBLISHED";
    public static final String FEE_PAID = "PAID";
    public static final String FEE_UNPAID = "UNPAID";

    public static final int DEFAULT_PAGE_SIZE = 10;

    public static final String DEFAULT_PASSWORD = "123456";
}
