package apartmentmanagement.websocket;

import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


public final class RequestChatHub {

    private static final RequestChatHub INSTANCE = new RequestChatHub();


    private final Map<Integer, Set<Session>> rooms = new ConcurrentHashMap<>();

    private RequestChatHub() {
    }

    public static RequestChatHub getInstance() {
        return INSTANCE;
    }

    public void join(int requestId, Session session) {
        rooms.computeIfAbsent(requestId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void leave(int requestId, Session session) {
        Set<Session> set = rooms.get(requestId);
        if (set == null) {
            return;
        }
        set.remove(session);
        if (set.isEmpty()) {
            rooms.remove(requestId, set);
        }
    }

    public void leaveAll(Session session) {
        for (Map.Entry<Integer, Set<Session>> e : rooms.entrySet()) {
            e.getValue().remove(session);
            if (e.getValue().isEmpty()) {
                rooms.remove(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Gửi payload JSON tới mọi session trong room (trừ exclude nếu có).
     */
    public void broadcast(int requestId, String jsonPayload, Session exclude) {
        Set<Session> set = rooms.get(requestId);
        if (set == null || set.isEmpty() || jsonPayload == null) {
            return;
        }
        for (Session s : set) {
            if (s == null || !s.isOpen()) {
                continue;
            }
            if (exclude != null && s.getId().equals(exclude.getId())) {
                continue;
            }
            try {
                synchronized (s) {
                    s.getBasicRemote().sendText(jsonPayload);
                }
            } catch (IOException ex) {
                System.out.println("RequestChatHub.broadcast error: " + ex.getMessage());
            }
        }
    }

    public void broadcast(int requestId, String jsonPayload) {
        broadcast(requestId, jsonPayload, null);
    }

    public int roomSize(int requestId) {
        Set<Session> set = rooms.get(requestId);
        return set == null ? 0 : set.size();
    }
}
