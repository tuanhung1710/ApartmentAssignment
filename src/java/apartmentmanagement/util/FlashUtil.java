package apartmentmanagement.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Flash message helper – lưu session, đọc 1 lần rồi xóa (Post/Redirect/Get).
 *
 * Cách dùng:
 *   FlashUtil.success(request, "Lưu thành công");
 *   response.sendRedirect(...);
 *   // ở doGet trước khi forward layout:
 *   FlashUtil.moveToRequest(request);
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

    /** Alias tên theo brief assignment */
    public static void setFlashSuccess(HttpServletRequest request, String message) {
        success(request, message);
    }

    public static void setFlashError(HttpServletRequest request, String message) {
        error(request, message);
    }

    /**
     * Chuyển flash từ session sang request attribute rồi xóa session flash.
     * Gọi trước khi forward JSP layout (flash.jsp đọc từ request).
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
