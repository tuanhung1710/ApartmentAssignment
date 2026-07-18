package apartmentmanagement.websocket;

import apartmentmanagement.model.User;
import apartmentmanagement.util.AppConstants;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

public class ChatHandshakeConfigurator extends ServerEndpointConfig.Configurator {

    public static final String PROP_USER = "chatUser";
    public static final String PROP_HTTP_SESSION = "httpSession";

    @Override
    public void modifyHandshake(ServerEndpointConfig sec,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        Map<String, Object> props = sec.getUserProperties();
        Object httpSessionObj = request.getHttpSession();
        if (httpSessionObj instanceof HttpSession) {
            HttpSession httpSession = (HttpSession) httpSessionObj;
            props.put(PROP_HTTP_SESSION, httpSession);
            Object userObj = httpSession.getAttribute(AppConstants.SESSION_USER);
            if (userObj instanceof User) {
                props.put(PROP_USER, userObj);
            }
        }

        if (!props.containsKey(PROP_USER)) {
            Map<String, List<String>> headers = request.getHeaders();
            if (headers != null) {
            }
        }
    }
}
