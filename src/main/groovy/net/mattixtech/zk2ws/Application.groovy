package net.mattixtech.zk2ws

import groovy.transform.CompileStatic
import io.micronaut.runtime.Micronaut

/**
 * @author Matt Brooks
 */
@CompileStatic
class Application {
    static void main(String[] args) {
        Micronaut.run(Application)
    }
}