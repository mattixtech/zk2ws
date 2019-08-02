package net.mattixtech.zk2ws.zk.api

import groovy.transform.CompileStatic

import java.util.concurrent.Flow

/**
 * @author Matt Brooks
 */
@CompileStatic
interface PublisherFactory {
    Flow.Publisher<byte[]> getPublisherForPath(String path)
}