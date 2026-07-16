package apartmentmanagement.util;

/**
 * Hằng số dùng chung toàn hệ thống (TV1 platform + TV2 apartment).
 */
public final class AppConstants {

    private AppConstants() {
    }

    /** Session key lưu user đã đăng nhập */
    public static final String SESSION_USER = "currentUser";

    /** Forgot-password OTP (session) — TV1 */
    public static final String SESSION_FORGOT_OTP = "forgotOtpPayload";
    public static final String SESSION_FORGOT_OTP_LOCK = "forgotOtpLockUntil";

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

    /** Building / apartment lifecycle (BuildingController dùng STATUS_*) */
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    /** Apartment status aliases (TV2) */
    public static final String APT_STATUS_ACTIVE = STATUS_ACTIVE;
    public static final String APT_STATUS_INACTIVE = STATUS_INACTIVE;

    /** Apartment occupancy type (TV2) */
    public static final String OCCUPANCY_OWNED = "OWNED";
    public static final String OCCUPANCY_RENTED = "RENTED";
    /** ACTIVE + trống (chưa có người ở) */
    public static final String OCCUPANCY_VACANT = "VACANT";
    /** INACTIVE — chưa vận hành / không áp dụng loại hình */
    public static final String OCCUPANCY_NA = "N/A";

    /** Số căn mặc định tối đa mỗi tầng (unit 01..06) */
    public static final int UNITS_PER_FLOOR = 6;

    /** Role trong căn (apartment_residents.role_in_apartment) */
    public static final String APT_ROLE_OWNER = "OWNER";
    public static final String APT_ROLE_TENANT_REP = "TENANT_REP";
    public static final String APT_ROLE_TENANT = "TENANT";

    public static final int DEFAULT_PAGE_SIZE = 10;
}
