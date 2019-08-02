# zk2ws
zk2ws is a microservice for serving the contents of a ZooKeeper ZNode in a streaming manner via a websocket. 

# WIP
This project is a work in progress.

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

## Connecting via websocket
TODO