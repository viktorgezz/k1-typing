package testconfig;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

public abstract class AbstractWebSocketE2ETest extends AbstractIntegrationRedisTest {

    @LocalServerPort
    protected int port;

    protected WebSocketStompClient stompClient;
    protected static final int TIMEOUT_SECONDS = 10;

    protected void setupStompClient() {
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    protected StompSession connectToWebSocket(String token) throws Exception {
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        String wsUrl = String.format("ws://localhost:%d/ws/contest", port);

        return stompClient.connectAsync(
                wsUrl,
                new org.springframework.web.socket.WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {}
        ).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    protected <T> BlockingQueue<T> subscribeToTopic(StompSession session, String topic, Class<T> messageType) {
        BlockingQueue<T> messageQueue = new LinkedBlockingQueue<>();

        session.subscribe(topic, new StompFrameHandler() {
            @Override
            public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                return messageType;
            }

            @Override
            public void handleFrame(@NotNull StompHeaders headers, Object payload) {
                messageQueue.offer(messageType.cast(payload));
            }
        });

        return messageQueue;
    }
}
