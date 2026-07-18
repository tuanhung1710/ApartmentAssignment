package apartmentmanagement.websocket;

import apartmentmanagement.dao.RequestDAO;
import apartmentmanagement.dao.RequestHistoryDAO;
import apartmentmanagement.model.Request;
import apartmentmanagement.model.RequestHistory;
import apartmentmanagement.model.User;
import apartmentmanagement.util.AppConstants;
import apartmentmanagement.util.DateTimeUtil;
import apartmentmanagement.util.HtmlSanitizer;
import java.sql.Timestamp;
import java.util.List;


public class RequestChatService {

    public static final int MAX_CONTENT = 4000;

    private final RequestHistoryDAO historyDAO = new RequestHistoryDAO();
    private final RequestDAO requestDAO = new RequestDAO();

    public static final class ChatResult {
        public final boolean ok;
        public final String message;
        public final RequestHistory comment;
        public final List<RequestHistory> comments;

        private ChatResult(boolean ok, String message, RequestHistory comment, List<RequestHistory> comments) {
            this.ok = ok;
            this.message = message;
            this.comment = comment;
            this.comments = comments;
        }

        public static ChatResult error(String message) {
            return new ChatResult(false, message, null, null);
        }

        public static ChatResult okComment(RequestHistory comment) {
            return new ChatResult(true, null, comment, null);
        }

        public static ChatResult okList(List<RequestHistory> comments) {
            return new ChatResult(true, null, null, comments);
        }
    }

    
    public boolean canAccess(Request r, User u) {
        if (r == null || u == null || u.getUserId() == null) {
            return false;
        }
        String role = u.getRole();
        if (AppConstants.ROLE_MANAGER.equals(role) || AppConstants.ROLE_ADMIN.equals(role)) {
            return true;
        }
        Integer uid = u.getUserId();
        return uid.equals(r.getCreatedBy())
                || uid.equals(r.getAssignedTo())
                || uid.equals(r.getApprovedBy());
    }

    public Request findRequest(int requestId) {
        return requestDAO.findById(requestId);
    }

    public ChatResult listComments(int requestId, int afterId, User user) {
        Request entity = requestDAO.findById(requestId);
        if (!canAccess(entity, user)) {
            return ChatResult.error("Bạn không có quyền xem trao đổi này.");
        }
        List<RequestHistory> comments = afterId > 0
                ? historyDAO.findCommentsAfter(requestId, afterId)
                : historyDAO.findCommentsByRequestId(requestId);
        return ChatResult.okList(comments);
    }

    
    public ChatResult addComment(int requestId, String rawContent, User user) {
        if (user == null || user.getUserId() == null) {
            return ChatResult.error("Chưa đăng nhập.");
        }
        Request entity = requestDAO.findById(requestId);
        if (!canAccess(entity, user)) {
            return ChatResult.error("Bạn không có quyền gửi tin trên yêu cầu này.");
        }

        String content = HtmlSanitizer.sanitize(rawContent);
        if (content == null || HtmlSanitizer.isBlankHtml(content)) {
            return ChatResult.error("Nội dung không được để trống.");
        }
        if (content.length() > MAX_CONTENT) {
            return ChatResult.error("Nội dung tối đa " + MAX_CONTENT + " ký tự.");
        }

        String status = entity.getStatus() == null ? AppConstants.STATUS_PENDING : entity.getStatus();
        RequestHistory toInsert = RequestHistory.builder()
                .requestId(requestId)
                .changedBy(user.getUserId())
                .oldStatus(status)
                .newStatus(status)
                .note(content)
                .build();

        
        Timestamp sentAt = DateTimeUtil.nowTimestamp();
        toInsert.setCreatedAt(sentAt);

        int newId = historyDAO.insert(toInsert);
        if (newId <= 0) {
            return ChatResult.error("Gửi tin thất bại.");
        }

        RequestHistory saved = historyDAO.findById(newId);
        if (saved == null) {
            saved = RequestHistory.builder()
                    .historyId(newId)
                    .requestId(requestId)
                    .changedBy(user.getUserId())
                    .oldStatus(status)
                    .newStatus(status)
                    .note(content)
                    .changedByName(user.getFullName())
                    .changedByRole(user.getRole())
                    .createdAt(sentAt)
                    .build();
        } else {
            saved.setCreatedAt(sentAt);
            if (saved.getChangedByName() == null || saved.getChangedByName().isEmpty()) {
                saved.setChangedByName(user.getFullName());
            }
            if (saved.getChangedByRole() == null || saved.getChangedByRole().isEmpty()) {
                saved.setChangedByRole(user.getRole());
            }
        }

        String payload = buildNewCommentEvent(saved);
        RequestChatHub.getInstance().broadcast(requestId, payload);

        return ChatResult.okComment(saved);
    }

    public String buildNewCommentEvent(RequestHistory h) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("{\"type\":\"NEW_COMMENT\",\"comment\":");
        appendCommentJson(sb, h, null);
        sb.append('}');
        return sb.toString();
    }

    public String buildCommentJson(RequestHistory h, Integer currentUserId) {
        StringBuilder sb = new StringBuilder(256);
        appendCommentJson(sb, h, currentUserId);
        return sb.toString();
    }

    public String buildListJson(List<RequestHistory> comments, Integer currentUserId) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("{\"ok\":true,\"comments\":[");
        if (comments != null) {
            for (int i = 0; i < comments.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                appendCommentJson(sb, comments.get(i), currentUserId);
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    public String buildOkCommentJson(RequestHistory h, Integer currentUserId) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("{\"ok\":true,\"comment\":");
        appendCommentJson(sb, h, currentUserId);
        sb.append('}');
        return sb.toString();
    }

    public String buildErrorJson(String message) {
        return "{\"ok\":false,\"message\":\"" + escapeJson(message) + "\"}";
    }

    public String buildWsError(String message) {
        return "{\"type\":\"ERROR\",\"message\":\"" + escapeJson(message) + "\"}";
    }

    public String buildWsJoined(int requestId) {
        return "{\"type\":\"JOINED\",\"requestId\":" + requestId + "}";
    }

    private void appendCommentJson(StringBuilder sb, RequestHistory h, Integer currentUserId) {
        boolean mine = currentUserId != null && currentUserId.equals(h.getChangedBy());
        sb.append('{');
        sb.append("\"commentId\":").append(h.getHistoryId() == null ? 0 : h.getHistoryId());
        sb.append(",\"requestId\":").append(h.getRequestId() == null ? 0 : h.getRequestId());
        sb.append(",\"userId\":").append(h.getChangedBy() == null ? 0 : h.getChangedBy());
        sb.append(",\"userFullName\":\"").append(escapeJson(h.getChangedByName())).append('"');
        sb.append(",\"userRole\":\"").append(escapeJson(h.getChangedByRole())).append('"');
        sb.append(",\"content\":\"").append(escapeJson(h.getNote())).append('"');
        sb.append(",\"createdAt\":\"").append(escapeJson(formatTs(h.getCreatedAt()))).append('"');
        if (currentUserId != null) {
            sb.append(",\"mine\":").append(mine ? "true" : "false");
        }
        sb.append('}');
    }

    private String formatTs(Timestamp ts) {
        return DateTimeUtil.formatRealtime(ts);
    }

    public static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }
}
