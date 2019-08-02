package net.mattixtech.zk2ws


import groovy.transform.Canonical
import groovy.transform.CompileStatic
import io.micronaut.websocket.WebSocketBroadcaster
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket
import net.mattixtech.zk2ws.ws.InboundWSMessage
import net.mattixtech.zk2ws.ws.OutboundWSMessage
import net.mattixtech.zk2ws.zk.api.PublisherFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Flow

/**
 * @author Matt Brooks
 */
@CompileStatic
@ServerWebSocket("/")
class ZKPathWebSocketServer {
    private static final Logger LOG = LoggerFactory.getLogger(ZKPathWebSocketServer)

    private final Map<String, Flow.Publisher<byte[]>> pathToPublisher = [:]
    private final Map<String, ZNodeSubscriber> pathToSubscriber = [:]
    // TODO: Might be able to get rid of this if controller is per-connection
    private final Map<String, Set<WebSocketSession>> pathToSessions = [:]
    private final WebSocketBroadcaster broadcaster

    @Inject
    private PublisherFactory publisherFactory

    ZKPathWebSocketServer(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster
    }

    @OnOpen
    void onOpen(WebSocketSession session) {
        LOG.debug("Opened websocket connection {}", session)
    }

    /**
     * We only accept one type of message and that is a JSON object mapped via {@link InboundWSMessage}.
     *
     * We will process any watched/unwatched paths it contains.
     */
    @OnMessage
    void onMessage(InboundWSMessage message, WebSocketSession session) {
        LOG.debug("Received message {} from session {}", message, session)

        // Watch all the given paths, initializing a publisher if necessary
        message.watchPaths?.each {
            if (message.unwatchPaths?.contains(it)) {
                LOG.trace("Not watching path {} since it is being unwatched", it)
                return
            }
            LOG.trace("Processing watched path {}", it)
            initPublisher(it)
            synchronized (pathToSessions) {
                if (!pathToSessions[it]) {
                    pathToSessions.put(it, [] as HashSet)
                }
                pathToSessions[it].add(session)
            }
        }

        // Unwatch all the given paths, destroying the publisher if no one is watching the path anymore
        message.unwatchPaths?.each {
            synchronized (pathToSessions) {
                LOG.trace("Processing unwatched path {}", it)
                pathToSessions[it].remove(session)
                if (pathToSessions[it].isEmpty()) {
                    pathToSessions.remove(it)
                    destroyPublisher(it)
                }
            }
        }
    }

    @OnClose
    void onClose(WebSocketSession session) {
        LOG.debug("Session {} has been closed", session)
        // TODO: Remove session from map
    }

    private void initPublisher(String zkPath) {
        // TODO: Is there any point in running this async...?
        CompletableFuture.runAsync {
            synchronized (pathToPublisher) {
                if (!pathToPublisher[zkPath]) {
                    LOG.debug("Initializing publisher for path {}", zkPath)
                    def publisher = publisherFactory.getPublisherForPath(zkPath)
                    def subscriber = new ZNodeSubscriber(zkPath)
                    publisher.subscribe(subscriber)
                    pathToPublisher[zkPath] = publisher
                    pathToSubscriber[zkPath] = subscriber
                }
            }
        }.whenComplete { v, t ->
            if (t) {
                LOG.warn("Error while initializing publisher", t)
            }
        }
    }

    private void destroyPublisher(String zkPath) {
        synchronized (pathToPublisher) {
            if (pathToPublisher[zkPath]) {
                LOG.debug("Destroying publisher for path {}", zkPath)
                pathToPublisher.remove(zkPath)
                pathToSubscriber[zkPath].cancel()
                pathToSubscriber.remove(zkPath)
            }
        }
    }

    /**
     * @return true if the given session is watching the given path, false otherwise
     */
    private boolean shouldForward(String zkPath, WebSocketSession session) {
        return pathToSessions[zkPath]?.contains(session)
    }

    /**
     * A subscriber that converts the ZNode payload to a String and sends it via the websocket wrapped in a JSON object.
     */
    @Canonical
    private class ZNodeSubscriber implements Flow.Subscriber<byte[]> {
        private final String zkPath
        Flow.Subscription subscription

        ZNodeSubscriber(String zkPath) {
            this.zkPath = Objects.requireNonNull(zkPath)
            LOG.debug("Subscriber created for path {}", zkPath)
        }

        void cancel() {
            LOG.trace("Subscriber {} has been cancelled", this)
            subscription.cancel()
        }

        @Override
        void onSubscribe(Flow.Subscription subscription) {
            LOG.trace("Subscriber {} has been subscribed", this)
            this.subscription = subscription
            subscription.request(1)
        }

        @Override
        void onNext(byte[] item) {
            def message = new OutboundWSMessage(path: zkPath, value: new String(item))

            LOG.trace("Subscriber {} got new value {}", this, message)
            synchronized (pathToSessions) {
                broadcaster.broadcastSync(message) { session ->
                    if (shouldForward(zkPath, session)) {
                        LOG.trace("Sent message {}", message)
                        return true
                    }
                    LOG.trace("No sessions are currently interested in path {}", zkPath)
                    return false
                }
            }

            subscription.request(1)
        }

        @Override
        void onError(Throwable throwable) {
            LOG.warn("Subscriber onError", throwable)
        }

        @Override
        void onComplete() {
            LOG.debug("Subscriber {} completed", this)
        }
    }
}
