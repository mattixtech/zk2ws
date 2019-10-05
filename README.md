# zk2ws
zk2ws is a microservice for serving the contents of a ZooKeeper ZNode in a streaming manner via a websocket. 

## Building
### Prerequisites
Building zk2ws depends on the ZNode2Flow library. See https://github.com/mattixtech/znode2flow

### Build
`./gradlew build`

#### Build docker image
From the root directory of this project run the docker build command as follows:

`docker build -t zk2ws .`

## Testing
`./gradlew test`

## Running
### Running the jar
`java -DconnectionString="zookeeperIp:Port" -jar build/libs/zk2ws-0.1.jar`
### Running via docker
`docker run --name zk2ws -d -p 8080:8080 -e "JAVA_OPTS=-DconnectionString=<ZooKeeper host>:<ZooKeeper port>" zk2ws`

## Websocket API
Once running, the server accepts websocket connections on "/" port 8080.

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
### Set up docker network
`sudo docker network create --driver bridge zk2ws_network`
### Start a ZooKeeper docker instance
`docker run --name zookeeper --network zk2ws_network -e ZOOKEEPER_IP="127.0.0.1" -d zookeeper`
### Start a zk2ws docker instance
`docker run --name zk2ws --network zk2ws_network -d -p 8080:8080 -e "JAVA_OPTS=-DconnectionString=zookeeper:2181" zk2ws`
### Open a websocket connection and watch a node
Initiate a websocket connection to the service via `wscat` and send a `watch` payload.
```
# wscat -c localhost:8080
> {"watch": "/test"}
```
### Create the ZK node and view updates
```
# sudo docker exec -it zookeeper zkCli.sh
[zk: localhost:2181(CONNECTED) 1] create /test "hello world"
```

You should now see the node via the websocket connection that was previously opened:
```
< {"path":"/test","value":"hello world"}
```

You can now update the ZK node value and observe the updates over the websocket connection.
