package apartmentmanagement.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Helper flash message theo pattern “set vào session, đọc một lần rồi xóa”.
 * <p>
 * Dùng sau redirect (PRG): controller gọi {@link #success} / {@link #error},
 * request kế tiếp gọi {@link #moveToRequest} trước khi forward JSP.
 */
public final class FlashUtil {

    /** Chặn khởi tạo — chỉ dùng phương thức static. */
    private FlashUtil() {
    }

    /**
     * Ghi thông báo thành công vào session flash.
     *
     * @param request request hiện tại (lấy/tạo session)
     * @param message nội dung hiển thị cho người dùng
     */
    public static void success(HttpServletRequest request, String message) {
        HttpSession session = request.getSession();
        session.setAttribute(AppConstants.FLASH_SUCCESS, message);
    }

    /**
     * Ghi thông báo lỗi vào session flash.
     *
     * @param request request hiện tại (lấy/tạo session)
     * @param message nội dung hiển thị cho người dùng
     */
    public static void error(HttpServletRequest request, String message) {
        HttpSession session = request.getSession();
        session.setAttribute(AppConstants.FLASH_ERROR, message);
    }

    /**
     * Chuyển flash từ session sang request attribute rồi xóa session flash
     * để JSP chỉ thấy một lần (tránh lặp sau F5).
     * <p>
     * Gọi trước khi forward sang JSP layout. Không tạo session mới nếu chưa có.
     *
     * @param request request sẽ forward tới view
     */
    public static void moveToRequest(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object success = session.getAttribute(AppConstants.FLASH_SUCCESS);
        Object error = session.getAttribute(AppConstants.FLASH_ERROR);
        if (success != null) {
            request.setAttribute(AppConstants.FLASH_SUCCESS, success);
            session.removeAttribute(AppConstants.FLASH_SUCCESS);
        }
        if (error != null) {
            request.setAttribute(AppConstants.FLASH_ERROR, error);
            session.removeAttribute(AppConstants.FLASH_ERROR);
        }
    }
}
