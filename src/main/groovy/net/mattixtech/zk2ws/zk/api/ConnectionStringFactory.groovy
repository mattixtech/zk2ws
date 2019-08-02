package net.mattixtech.zk2ws.zk.api

import groovy.transform.CompileStatic

/**
 * @author Matt Brooks
 */
@CompileStatic
interface ConnectionStringFactory {
    String getConnectionString()
}