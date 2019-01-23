# kafka-connect-venafi-tpp
[![Build Status](https://travis-ci.com/opencredo/kafka-connect-venafi-tpp.svg?token=9Xb3AhGzVsnLVT8gQNzo&branch=master)](https://travis-ci.com/opencredo/kafka-connect-venafi-tpp)

kafka-connect-venafi-tpp is a [Kafka connector](http://kafka.apache.org/documentation.html#connect) for Venafi Trust Protection Platform security events.

# Development
To manually install the connector:
1. Build the Jar with `mvn package`
2. Find the Jar in your target folder called venafi-tpp-log-connector-<version you're building>.jar
3. Create a connect property file 
```name=venafi
connector.class=com.opencredo.connect.venafi.tpp.log.TppLogSourceConnector
tasks.max=1
type.name=kafka-connect
venafi.topic=TPP-LOGS
venafi.base.url=https://localhost:443/vedsdk
venafi.username=placeholder_username
venafi.password=placeholder_password
venafi.batch.size=100 
venafi.poll.interval=1000
```
This is filled with the default values as provided by the config definition [class](???)
Whilst all `venafi` fields are currently optional and will default to above please change `venafi.base.url`, `venafi.username` and `venafi.password`.   
4. Create a directory to place this files e.g. `<path-to-confluent>/share/kafka/plugins`.  
5. Add this to the plugin path in your Connect properties file.   
6. Then call: `bin/confluent load venafi -d venafi.properties`  
If you need to unload/reload it use: `bin/confluent unload venafi`  

For more information please look at the [Confluent instructions on manually installing connectors](https://docs.confluent.io/current/connect/managing/install.html#connect-install-connectors).
