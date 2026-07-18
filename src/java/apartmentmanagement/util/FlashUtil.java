package apartmentmanagement.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Helper flash message theo pattern set vao session, doc mot lan roi xoa.
 * <p>
 * Dung sau redirect (PRG): controller goi {@link #success} / {@link #error},
 * request ke tiep goi {@link #moveToRequest} truoc khi forward JSP.
 */
public final class FlashUtil {

    /** Chan khoi tao — chi dung phuong thuc static. */
    private FlashUtil() {
    }

    /**
     * Ghi thong bao thanh cong vao session flash.
     *
     * @param request request hien tai (lay/tao session)
     * @param message noi dung hien thi cho nguoi dung
     */
    public static void success(HttpServletRequest request, String message) {
        HttpSession session = request.getSession();
        session.setAttribute(AppConstants.FLASH_SUCCESS, message);
    }

    /**
     * Ghi thong bao loi vao session flash.
     *
     * @param request request hien tai (lay/tao session)
     * @param message noi dung hien thi cho nguoi dung
     */
    public static void error(HttpServletRequest request, String message) {
        HttpSession session = request.getSession();
        session.setAttribute(AppConstants.FLASH_ERROR, message);
    }

    /** Alias tuong thich fee module / main. */
    public static void setFlashSuccess(HttpServletRequest request, String message) {
        success(request, message);
    }

    /** Alias tuong thich fee module / main. */
    public static void setFlashError(HttpServletRequest request, String message) {
        error(request, message);
    }

    /**
     * Chuyen flash tu session sang request attribute roi xoa session flash
     * de JSP chi thay mot lan (tranh lap sau F5).
     * <p>
     * Goi truoc khi forward sang JSP layout. Khong tao session moi neu chua co.
     *
     * @param request request se forward toi view
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
