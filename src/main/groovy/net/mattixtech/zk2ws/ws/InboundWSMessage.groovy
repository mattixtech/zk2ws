package net.mattixtech.zk2ws.ws

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.Canonical
import groovy.transform.CompileStatic

/**
 * @author Matt Brooks
 */
@CompileStatic
@Canonical
class InboundWSMessage {
    @JsonProperty(required = false, value = 'watch')
    List<String> watchPaths

    @JsonProperty(required = false, value = 'unwatch')
    List<String> unwatchPaths
}
