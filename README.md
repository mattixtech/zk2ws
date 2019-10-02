# zk2ws
zk2ws is a microservice for serving the contents of a ZooKeeper ZNode in a streaming manner via a websocket. 

## Building
### Prerequisites
zk2ws depends on the ZNode2Flow library. See https://github.com/mattixtech/znode2flow

### Build
`./gradlew build`

## Testing
`./gradlew test`

## Running
### Running the jar
`java -DconnectionString="zookeeperIp:Port" -jar build/libs/zk2ws-0.1.jar`
### Running via docker
TODO

## Websocket API
Once running, the server accepts websocket connections on "/".

Messages sent to the server should be a JSON encoded object containing either or both of the following fields:
* `watch`: A list of ZooKeeper paths to watch.
* `unwatch`: A list of ZooKeeper paths to stop watching.

ex:
```
{
    "watch": ["/test/next"],
    "unwatch": ["/test/prev"]
}
```

## Demo
Prerequisites:
* docker
* wscat (install via npm)
* this project's docker image built
### Start a ZooKeeper docker instance
`docker run --name zookeeper -e ZOOKEEPER_IP="127.0.0.1" -p 2181:2181 -d zookeeper`
### Start a zk2ws docker instance
TODO
### Open a websocket connection and watch a node
Initiate a websocket connection to the service via `wscat` and send a `watch` payload.
```
# wscat -c localhost:8080
> {"watch": "/test"}
```
### Create the ZK node and view updates
```
# sudo docker exec -it zookeeper bin/zkCli.sh
[zk: localhost:2181(CONNECTED) 1] create /test "hello world"
```

You should now see the node via the websocket connection that was previously opened:
```
< {"path":"/test","value":"hello world"}
```

You can now update the ZK node value and observe the updates over the websocket connection.
