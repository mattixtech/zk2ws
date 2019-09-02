package net.mattixtech.zk2ws.zk.impl

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.Synchronized
import net.mattixtech.distributed.zk.znode2flow.ZNodePublisher
import net.mattixtech.zk2ws.zk.api.ConnectionStringFactory
import net.mattixtech.zk2ws.zk.api.PublisherFactory

import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.Flow

/**
 * @author Matt Brooks
 */
@CompileStatic
@Singleton
class ZNodePublisherFactory implements PublisherFactory {
    @Inject
    private ConnectionStringFactory connectionStringFactory

    @Override
    @Synchronized
    @Memoized
    Flow.Publisher<byte[]> getPublisherForPath(String path) {
        Objects.requireNonNull(path)

        return ZNodePublisher.withCachedCurator(connectionStringFactory.getConnectionString(), path)
    }
}
