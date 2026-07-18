package apartmentmanagement.websocket;

import apartmentmanagement.model.Request;
import apartmentmanagement.model.User;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@ServerEndpoint(
        value = "/ws/request-chat/{requestId}",
        configurator = ChatHandshakeConfigurator.class
)
public class RequestChatEndpoint {

    private static final Pattern CONTENT_PATTERN = Pattern.compile(
            "\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final Pattern TYPE_PATTERN = Pattern.compile(
            "\"type\"\\s*:\\s*\"([A-Za-z0-9_]+)\"");

    private final RequestChatService chatService = new RequestChatService();
    private final RequestChatHub hub = RequestChatHub.getInstance();

    @OnOpen
    public void onOpen(Session session,
                       EndpointConfig config,
                       @PathParam("requestId") String requestIdRaw) throws IOException {
        User user = (User) session.getUserProperties().get(ChatHandshakeConfigurator.PROP_USER);
        if (user == null && config != null) {
            user = (User) config.getUserProperties().get(ChatHandshakeConfigurator.PROP_USER);
            if (user != null) {
                session.getUserProperties().put(ChatHandshakeConfigurator.PROP_USER, user);
            }
        }

        Integer requestId = parsePositiveInt(requestIdRaw);
        if (user == null || requestId == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            return;
        }

        Request entity = chatService.findRequest(requestId);
        if (!chatService.canAccess(entity, user)) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Forbidden"));
            return;
        }

        session.getUserProperties().put("requestId", requestId);
        session.getUserProperties().put("userId", user.getUserId());
        hub.join(requestId, session);

        sendText(session, chatService.buildWsJoined(requestId));
        System.out.println("RequestChatEndpoint open: request#" + requestId
                + " user=" + user.getUsername()
                + " roomSize=" + hub.roomSize(requestId));
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        User user = (User) session.getUserProperties().get(ChatHandshakeConfigurator.PROP_USER);
        Integer requestId = (Integer) session.getUserProperties().get("requestId");
        if (user == null || requestId == null) {
            sendText(session, chatService.buildWsError("Phiên không hợp lệ."));
            return;
        }
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        String type = extractType(message);
        if (type == null) {
            type = "SEND";
        }

        switch (type) {
            case "SEND":
            case "CHAT":
            case "MESSAGE":
                handleSend(session, requestId, user, message);
                break;
            case "PING":
                sendText(session, "{\"type\":\"PONG\"}");
                break;
            default:
                sendText(session, chatService.buildWsError("Type không hỗ trợ: " + type));
                break;
        }
    }

    private void handleSend(Session session, int requestId, User user, String rawMessage)
            throws IOException {
        String content = extractContent(rawMessage);
        if (content == null) {
            sendText(session, chatService.buildWsError("Thiếu content."));
            return;
        }
        // Unescape JSON string
        content = unescapeJson(content);

        RequestChatService.ChatResult result = chatService.addComment(requestId, content, user);
        if (!result.ok) {
            sendText(session, chatService.buildWsError(result.message));
            return;
        }


        sendText(session, "{\"type\":\"ACK\",\"commentId\":"
                + (result.comment.getHistoryId() == null ? 0 : result.comment.getHistoryId())
                + "}");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Integer requestId = (Integer) session.getUserProperties().get("requestId");
        if (requestId != null) {
            hub.leave(requestId, session);
        } else {
            hub.leaveAll(session);
        }
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        System.out.println("RequestChatEndpoint error: "
                + (thr == null ? "unknown" : thr.getMessage()));
        if (session != null) {
            Integer requestId = (Integer) session.getUserProperties().get("requestId");
            if (requestId != null) {
                hub.leave(requestId, session);
            } else {
                hub.leaveAll(session);
            }
        }
    }

    private void sendText(Session session, String text) throws IOException {
        if (session != null && session.isOpen() && text != null) {
            synchronized (session) {
                session.getBasicRemote().sendText(text);
            }
        }
    }

    private Integer parsePositiveInt(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            int v = Integer.parseInt(raw.trim());
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractType(String json) {
        Matcher m = TYPE_PATTERN.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private String extractContent(String json) {
        Matcher m = CONTENT_PATTERN.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private String unescapeJson(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                switch (n) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u':
                        if (i + 4 < s.length()) {
                            String hex = s.substring(i + 1, i + 5);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append('u');
                            }
                        } else {
                            sb.append('u');
                        }
                        break;
                    default:
                        sb.append(n);
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
