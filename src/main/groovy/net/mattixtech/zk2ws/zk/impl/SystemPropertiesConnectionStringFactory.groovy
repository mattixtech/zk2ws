package net.mattixtech.zk2ws.zk.impl

import groovy.transform.CompileStatic
import net.mattixtech.zk2ws.zk.api.ConnectionStringFactory

import javax.inject.Singleton

/**
 * @author Matt Brooks
 */
@CompileStatic
@Singleton
class SystemPropertiesConnectionStringFactory implements ConnectionStringFactory {
    @Override
    String getConnectionString() {
        Objects.requireNonNull(System.getProperty("connectionString"))
    }
}