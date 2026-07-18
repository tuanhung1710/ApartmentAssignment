package apartmentmanagement.util;

/**
 * Hằng số dùng chung toàn hệ thống (TV1 platform + TV2 apartment + fee module).
 * <p>
 * Gom session keys, flash keys, role, trạng thái nghiệp vụ và giá trị
 * mặc định để tránh hard-code rải rác trong controller/DAO/JSP.
 */
public final class AppConstants {

    /** Chặn khởi tạo — chỉ dùng hằng static. */
    private AppConstants() {
    }

    /** Session key lưu user đã đăng nhập. */
    public static final String SESSION_USER = "currentUser";

    /** Session key payload OTP quên mật khẩu (TV1). */
    public static final String SESSION_FORGOT_OTP = "forgotOtpPayload";
    /** Session key thời điểm khóa nhập OTP quên mật khẩu (TV1). */
    public static final String SESSION_FORGOT_OTP_LOCK = "forgotOtpLockUntil";

    /** Flash success key (session → request qua {@code FlashUtil}). */
    public static final String FLASH_SUCCESS = "flashSuccess";
    /** Flash error key (session → request qua {@code FlashUtil}). */
    public static final String FLASH_ERROR = "flashError";

    /** Vai trò hệ thống: quản trị. */
    public static final String ROLE_ADMIN = "ADMIN";
    /** Vai trò hệ thống: quản lý. */
    public static final String ROLE_MANAGER = "MANAGER";
    /** Vai trò hệ thống: nhân viên. */
    public static final String ROLE_STAFF = "STAFF";
    /** Vai trò hệ thống: cư dân. */
    public static final String ROLE_RESIDENT = "RESIDENT";

    /** Trạng thái yêu cầu: chờ xử lý. */
    public static final String STATUS_PENDING = "PENDING";
    /** Trạng thái yêu cầu: đã duyệt. */
    public static final String STATUS_APPROVED = "APPROVED";
    /** Trạng thái yêu cầu: từ chối. */
    public static final String STATUS_REJECTED = "REJECTED";
    /** Trạng thái yêu cầu: đã gán xử lý. */
    public static final String STATUS_ASSIGNED = "ASSIGNED";
    /** Trạng thái yêu cầu: đang thực hiện. */
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    /** Trạng thái yêu cầu: hoàn thành. */
    public static final String STATUS_COMPLETED = "COMPLETED";
    /** Trạng thái yêu cầu: đã hủy. */
    public static final String STATUS_CANCELLED = "CANCELLED";

    /** Loại yêu cầu: sửa chữa. */
    public static final String TYPE_REPAIR = "REPAIR";
    /** Loại yêu cầu: gửi xe. */
    public static final String TYPE_PARKING = "PARKING";
    /** Loại yêu cầu: chuyển vào. */
    public static final String TYPE_MOVE_IN = "MOVE_IN";
    /** Loại yêu cầu: chuyển đi. */
    public static final String TYPE_MOVE_OUT = "MOVE_OUT";
    /** Loại yêu cầu: khác. */
    public static final String TYPE_OTHER = "OTHER";

    /** Trạng thái phí tháng (legacy monthly_fees): nháp. */
    public static final String FEE_DRAFT = "DRAFT";
    /** Trạng thái phí tháng (legacy monthly_fees): đã công bố. */
    public static final String FEE_PUBLISHED = "PUBLISHED";
    /** Trạng thái phí tháng (legacy monthly_fees): đã thanh toán. */
    public static final String FEE_PAID = "PAID";
    /** Trạng thái phí tháng (legacy monthly_fees): chưa thanh toán. */
    public static final String FEE_UNPAID = "UNPAID";

    /** Trạng thái đợt phí (fees): nháp. */
    public static final String FEE_STATUS_DRAFT = "DRAFT";
    /** Trạng thái đợt phí (fees): đã gán căn. */
    public static final String FEE_STATUS_ASSIGNED = "ASSIGNED";
    /** Trạng thái đợt phí (fees): đã công bố. */
    public static final String FEE_STATUS_PUBLISHED = "PUBLISHED";

    /** Trạng thái gán phí (fee_assignments): chưa thanh toán. */
    public static final String ASSIGNMENT_UNPAID = "UNPAID";
    /** Trạng thái gán phí (fee_assignments): đã thanh toán. */
    public static final String ASSIGNMENT_PAID = "PAID";

    /** Phạm vi áp phí: toàn bộ căn. */
    public static final String FEE_SCOPE_ALL = "ALL";
    /** Phạm vi áp phí: theo tòa. */
    public static final String FEE_SCOPE_BUILDING = "BUILDING";
    /** Phạm vi áp phí: theo tầng. */
    public static final String FEE_SCOPE_FLOOR = "FLOOR";
    /** Phạm vi áp phí: theo căn. */
    public static final String FEE_SCOPE_APARTMENT = "APARTMENT";

    /** Loại phí: hàng tháng. */
    public static final String FEE_TYPE_MONTHLY = "MONTHLY";
    /** Loại phí: một lần. */
    public static final String FEE_TYPE_ONE_TIME = "ONE_TIME";

    /**
     * Trạng thái lifecycle building/apartment: đang hoạt động.
     * BuildingController cũng dùng các {@code STATUS_*} này.
     */
    public static final String STATUS_ACTIVE = "ACTIVE";
    /** Trạng thái lifecycle building/apartment: ngừng hoạt động. */
    public static final String STATUS_INACTIVE = "INACTIVE";

    /** Alias trạng thái căn hộ ACTIVE (TV2). */
    public static final String APT_STATUS_ACTIVE = STATUS_ACTIVE;
    /** Alias trạng thái căn hộ INACTIVE (TV2). */
    public static final String APT_STATUS_INACTIVE = STATUS_INACTIVE;

    /** Loại hình sử dụng căn: sở hữu. */
    public static final String OCCUPANCY_OWNED = "OWNED";
    /** Loại hình sử dụng căn: cho thuê. */
    public static final String OCCUPANCY_RENTED = "RENTED";
    /** ACTIVE nhưng trống — chưa có người ở. */
    public static final String OCCUPANCY_VACANT = "VACANT";
    /** INACTIVE — chưa vận hành / không áp dụng loại hình. */
    public static final String OCCUPANCY_NA = "N/A";

    /** Số căn mặc định tối đa mỗi tầng (unit 01..06). */
    public static final int UNITS_PER_FLOOR = 6;

    /** Vai trò trong căn ({@code apartment_residents.role_in_apartment}): chủ sở hữu. */
    public static final String APT_ROLE_OWNER = "OWNER";
    /** Vai trò trong căn: đại diện thuê. */
    public static final String APT_ROLE_TENANT_REP = "TENANT_REP";
    /** Vai trò trong căn: thành viên thuê. */
    public static final String APT_ROLE_TENANT = "TENANT";

    /** Kích thước trang mặc định cho phân trang danh sách. */
    public static final int DEFAULT_PAGE_SIZE = 10;
}
