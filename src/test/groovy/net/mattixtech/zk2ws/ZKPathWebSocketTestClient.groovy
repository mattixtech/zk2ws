package net.mattixtech.zk2ws

import groovy.transform.CompileStatic
import io.micronaut.websocket.annotation.ClientWebSocket
import io.micronaut.websocket.annotation.OnMessage
import net.mattixtech.zk2ws.ws.InboundWSMessage
import net.mattixtech.zk2ws.ws.OutboundWSMessage

import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Matt Brooks
 */
@CompileStatic
@ClientWebSocket("/")
abstract class ZKPathWebSocketTestClient implements AutoCloseable {
    final Collection<OutboundWSMessage> values = [] as CopyOnWriteArrayList

    @OnMessage
    void onMessage(OutboundWSMessage message) {
        values.add(message)
    }

    abstract void send(InboundWSMessage message)
}
