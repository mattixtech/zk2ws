package net.mattixtech.zk2ws

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.websocket.RxWebSocketClient
import net.mattixtech.zk2ws.ws.InboundWSMessage
import net.mattixtech.zk2ws.ws.OutboundWSMessage
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.test.TestingServer
import org.awaitility.Awaitility
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject
import java.util.concurrent.TimeUnit

/**
 * @author Matt Brooks
 */
@MicronautTest
class ZKPathWebSocketServerSpec extends Specification {
    @Inject
    EmbeddedServer embeddedServer

    @AutoCleanup
    @Inject
    @Client("/")
    RxWebSocketClient webSocketClient

    @Shared
    TestingServer testZookeeper

    @Shared
    CuratorFramework curatorFramework

    static final def objectMapper = new ObjectMapper()

    def getFreePort() {
        def port = null
        while (port == null) {
            new ServerSocket(0).withCloseable {
                port = it.getLocalPort()
            }
        }
        return port
    }

    def setupSpec() {
        // Set up the test instance of ZooKeeper
        testZookeeper = new TestingServer(getFreePort(), true)
        System.setProperty("connectionString", testZookeeper.getConnectString())
        curatorFramework = CuratorFrameworkFactory.newClient(testZookeeper.getConnectString(),
                new ExponentialBackoffRetry(1000, 100))
    }

    def 'test connection'() {
        given:
        def path = "/test"
        def payload = "Hello World!"

        // Create the initial node on ZooKeeper
        curatorFramework.start()
        curatorFramework.create().forPath(path, payload.getBytes())

        // Create the websocket connection and watch the test path
        def client = webSocketClient.connect(ZKPathWebSocketTestClient, "/")
                .blockingFirst()
        client.send(new InboundWSMessage(watchPaths: [path]))

        // Wait to see the initial value
        def originalMessage = new OutboundWSMessage(path: path, value: payload)
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until { client.values == Arrays.asList(originalMessage) }

        // Now update the value at the ZNode
        def updatedPayload = "Update"
        def updatedMessage = new OutboundWSMessage(path: path, value: updatedPayload)
        curatorFramework.setData().forPath(path, updatedPayload.getBytes())

        expect:
        // Now wait and we expect to see both the original and updated values in order
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until {
                    client.values == Arrays.asList(originalMessage, updatedMessage)
                }
    }
}
