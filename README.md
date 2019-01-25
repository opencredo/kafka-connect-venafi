# kafka-connect-venafi-tpp
[![Build Status](https://travis-ci.com/opencredo/kafka-connect-venafi-tpp.svg?token=9Xb3AhGzVsnLVT8gQNzo&branch=master)](https://travis-ci.com/opencredo/kafka-connect-venafi-tpp)

kafka-connect-venafi-tpp is a [Kafka connector](http://kafka.apache.org/documentation.html#connect) for Venafi Trust Protection Platform security events.


Description
---
This connector connects via HTTP to your instance of the Venafi Trust Protection Platform ( which shall be referred to as TPP from here on) and pulls your Log events into Kafka, allowing you to do any filtering/transforming/processing you'd like to do within a comfortable Kafka environment.  
 **N.B.** Currently the connector starts from the beginning of time (i.e. processes all past events first), a future release will allow the option of starting from now (i.e. skipping all previous events).

EventLog object produced by this source **Connector** as defined by the Venafi WebSDK
* **ClientTimestamp**: The time that the client generated the event.
* **Component**: A string that identifies a component DN. For events that occur in a subsystem other than Config, such as a Secret Store, the Distinguished Name (DN) component may be blank.
* **ComponentId**: The component ID that originated the event.
* **ComponentSubsystem**: The component subsytem that originated the event.
* **Grouping**: An integer for the component ID or Group Id that correlates to a set of events.
* **Id**: A hexadecimal value that corresponds to an Event ID.
* **Name**: The event name.
* **ServerTimestamp**: The time the Trust Protection Platform server received the event.
* **Severity**: A string value for one of these event severity: Emergency, Alert, Critical, Error, Warning, Notice, Info, or Debug
* **SourceIP**: The IP address of the host that originated the event
* **Text1**: A string variable, that contains all or part of an event message.
* **Text2**: An additional string if present, that appears as part of an event message.
* **Value1**: An integer variable, if present, that appears as part of an event message.
* **Value2**: An additional integer variable, if present, that appears as part of an event message.

---

# Development
To manually install the connector:
1. Build the JAR with `mvn package`
2. Find the JAR in your target folder called venafi-tpp-log-connector-<version you're building>.jar
3. Create a connect property file 
```
name=venafi
connector.class=com.opencredo.connect.venafi.tpp.log.TppLogSourceConnector
tasks.max=1
type.name=kafka-connect
venafi.base.url=<your_base_url>
venafi.username=<your_api_username>
venafi.password=<your_api_password>
```
This is filled with the minimum values required, any default values are provided by the [config definition class](./src/main/java/com/opencredo/connect/venafi/tpp/log/TppLogSourceConfig.java). 
This can also be looked at for more information on configuration, or look at the [wiki on the config definitions.](https://github.com/opencredo/kafka-connect-venafi-tpp/wiki/Config-Definitions-explained.)
Make sure to replace the items with the <> brackets with what's needed to connect to your Venafi TPP/VEDSDK instance.   
4. Create a directory to place the JAR file in and place it there e.g. `<path-to-confluent>/share/java/kafka-connect-venafi`.  
5. Then call: `<path-to-confluent>/bin/confluent load venafi -d venafi.properties`  

For more information please look at the [Confluent instructions on manually installing connectors](https://docs.confluent.io/current/connect/managing/install.html#connect-install-connectors).

---
If you need to unload/reload it use: `<path-to-confluent>/bin/confluent unload venafi`  
If you intend to change the JAR please stop, change the JAR, then start the cluster.  

# Useful commands while developing
```
sudo bin/confluent start  
sudo bin/confluent status
sudo bin/confluent load venafi -d ~/venafi.properties
sudo bin/confluent status venafi
sudo bin/kafka-topics --list --zookeeper localhost:2181
sudo bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic connect-offsets --from-beginning
sudo bin/kafka-avro-console-consumer --bootstrap-server localhost:9092 --topic TPP-LOGS
sudo bin/confluent log connect
``` 
---

# Config Definitions explained.

``venafi.base.url``
  URL to TPP API

  * Type: string
  * Valid Values: non-empty string and no ISO control characters
  * Importance: high

``venafi.password``
  The password to use with the api.

  * Type: string
  * Importance: high

``venafi.username``
  The username to use with the api.

  * Type: string
  * Importance: high

``venafi.topic``
  Topic to publish TPP log data to.

  * Type: string
  * Default: TPP-LOGS
  * Valid Values: non-empty string and no ISO control characters
  * Importance: high

``venafi.batch.size``
  Window of data to pull from log api.

  * Type: int
  * Default: 100
  * Valid Values: [2,...,10000]
  * Importance: low

``venafi.poll.interval``
  Poll interval in milliseconds.

  * Type: int
  * Default: 1000
  * Importance: low




