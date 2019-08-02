package net.mattixtech.zk2ws.ws

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.Canonical
import groovy.transform.CompileStatic

/**
 * @author Matt Brooks
 */
@CompileStatic
@Canonical
class OutboundWSMessage {
    @JsonProperty(required = true, value = 'path')
    String path

    @JsonProperty(required = true, value = 'value')
    String value
}
