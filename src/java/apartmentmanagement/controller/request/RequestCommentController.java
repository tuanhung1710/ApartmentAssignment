package apartmentmanagement.controller.request;

import apartmentmanagement.model.Request;
import apartmentmanagement.model.User;
import apartmentmanagement.util.FlashUtil;
import apartmentmanagement.websocket.RequestChatService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet(name = "RequestCommentController", urlPatterns = {"/request-comment"})
public class RequestCommentController extends HttpServlet {

    private final RequestChatService chatService = new RequestChatService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list";
        }
        switch (action) {
            case "list":
                handleList(request, response);
                break;
            default:
                writeRaw(response, HttpServletResponse.SC_NOT_FOUND,
                        chatService.buildErrorJson("Action không hợp lệ."));
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "add";
        }
        switch (action) {
            case "add":
                handleAdd(request, response);
                break;
            default:
                writeRaw(response, HttpServletResponse.SC_NOT_FOUND,
                        chatService.buildErrorJson("Action không hợp lệ."));
                break;
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }

        Integer requestId = parseId(request.getParameter("requestId"));
        if (requestId == null) {
            writeRaw(response, HttpServletResponse.SC_BAD_REQUEST,
                    chatService.buildErrorJson("Thiếu requestId."));
            return;
        }

        int afterId = parseAfterId(request.getParameter("afterId"));
        RequestChatService.ChatResult result = chatService.listComments(requestId, afterId, user);
        if (!result.ok) {
            writeRaw(response, HttpServletResponse.SC_FORBIDDEN, chatService.buildErrorJson(result.message));
            return;
        }
        writeRaw(response, HttpServletResponse.SC_OK,
                chatService.buildListJson(result.comments, user.getUserId()));
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }

        boolean wantsJson = wantsJson(request);
        Integer requestId = parseId(request.getParameter("requestId"));
        String rawContent = trim(request.getParameter("content"));

        if (requestId == null) {
            fail(request, response, wantsJson, HttpServletResponse.SC_BAD_REQUEST,
                    "Thiếu requestId.", "Thiếu mã yêu cầu.",
                    request.getContextPath() + "/request?action=my");
            return;
        }

        RequestChatService.ChatResult result = chatService.addComment(requestId, rawContent, user);
        if (!result.ok) {
            int status = result.message != null && result.message.contains("quyền")
                    ? HttpServletResponse.SC_FORBIDDEN
                    : HttpServletResponse.SC_BAD_REQUEST;
            if (result.message != null && result.message.contains("thất bại")) {
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
            fail(request, response, wantsJson, status,
                    result.message, result.message,
                    request.getContextPath() + "/request?action=detail&id=" + requestId);
            return;
        }

        if (wantsJson) {
            writeRaw(response, HttpServletResponse.SC_OK,
                    chatService.buildOkCommentJson(result.comment, user.getUserId()));
        } else {
            FlashUtil.success(request, "Đã gửi tin nhắn.");
            response.sendRedirect(request.getContextPath() + "/request?action=detail&id=" + requestId);
        }
    }

    public static boolean canAccess(Request r, User u) {
        return new RequestChatService().canAccess(r, u);
    }

    private User requireUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(
                apartmentmanagement.util.AppConstants.SESSION_USER);
        if (user == null) {
            writeRaw(response, HttpServletResponse.SC_UNAUTHORIZED,
                    chatService.buildErrorJson("Chưa đăng nhập."));
            return null;
        }
        return user;
    }

    private void fail(HttpServletRequest request, HttpServletResponse response, boolean wantsJson,
            int status, String jsonMsg, String flashMsg, String redirect) throws IOException {
        if (wantsJson) {
            writeRaw(response, status, chatService.buildErrorJson(jsonMsg));
        } else {
            FlashUtil.error(request, flashMsg);
            response.sendRedirect(redirect);
        }
    }

    private boolean wantsJson(HttpServletRequest request) {
        String xhr = request.getHeader("X-Requested-With");
        if (xhr != null && "XMLHttpRequest".equalsIgnoreCase(xhr)) {
            return true;
        }
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

    private Integer parseId(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private int parseAfterId(String raw) {
        try {
            if (raw == null || raw.isEmpty()) {
                return 0;
            }
            int v = Integer.parseInt(raw);
            return v < 0 ? 0 : v;
        } catch (Exception e) {
            return 0;
        }
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    private void writeRaw(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write(json == null ? "{}" : json);
    }
}
