package net.mattixtech.zk2ws

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import io.micronaut.http.HttpRequest
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.ClientWebSocket
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import net.mattixtech.zk2ws.ws.OutboundWSMessage

/**
 * @author Matt Brooks
 */
@CompileStatic
@ClientWebSocket("/")
abstract class ZKPathWebSocketClient implements AutoCloseable {
    WebSocketSession session
    HttpRequest request
    final Collection<OutboundWSMessage> values = new ArrayList<>()
    final ObjectMapper objectMapper = new ObjectMapper()

    @OnOpen
    void onOpen(WebSocketSession session, HttpRequest request) {
        this.session = session
        this.request = request
    }

    @OnMessage
    void onMessage(String message) {
        synchronized (values) {
            values.add(objectMapper.readValue(message, OutboundWSMessage))
        }
    }

    abstract void send(String message)
}
