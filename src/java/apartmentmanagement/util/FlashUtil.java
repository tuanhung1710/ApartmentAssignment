package apartmentmanagement.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Flash message helper – set vào session, đọc 1 lần rồi xóa.
 */
public final class FlashUtil {

    private FlashUtil() {
    }

    public static void success(HttpServletRequest request, String message) {
        HttpSession session = request.getSession();
        session.setAttribute(AppConstants.FLASH_SUCCESS, message);
    }

    public static void error(HttpServletRequest request, String message) {
        HttpSession session = request.getSession();
        session.setAttribute(AppConstants.FLASH_ERROR, message);
    }

    /**
     * Chuyển flash từ session sang request attribute rồi xóa session flash.
     * Gọi trước khi forward JSP layout.
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
