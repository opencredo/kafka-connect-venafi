#! /bin/bash

set -e

if [ "${VFURL}" == "" ] || [ "${VFURL}" == "to-be-set" ] || [ "${VFPASS}" == "" ] ||
   [ "${VFPASS}" == "to-be-set" ] || [ "${VFUSERNAME}" == "" ] || [ "${VFUSERNAME}" ==  "to-be-set" ] ; then
    printf "
     Environment vars :
     VFURL     : [%s]
     VFPASS    : [%s]
     VFUSERNAME: [%s]

     MUST BE SET before running this script, exiting (255)
     *****************************************************
     
"   ${VFURL} ${VFPASS} ${VFUSERNAME}

    exit 255;
else
  printf "
    Current environment vars :
    VFURL     : [%s]
    VFPASS    : [%s]
    VFUSERNAME: [%s]

    Inserting values into source-quickstart.properties...

"   ${VFURL} ${VFPASS} ${VFUSERNAME}


  sed -i ""s/venafi.password=.*/venafi.password=${VFPASS}/g"" /app/venafi-connect-demo/source-quickstart.properties
  sed -i ""s/venafi.username=.*/venafi.username=${VFUSERNAME}/g"" /app/venafi-connect-demo/source-quickstart.properties
  # Use hash as sed command delimiter because otherwise urls need to be escaped
  sed -i ""s#venafi.base.url=.*#venafi.base.url=${VFURL}#g"" /app/venafi-connect-demo/source-quickstart.properties

  echo "Starting Zookeeper (wait 30s) ..."
  /app/kafka/bin/zookeeper-server-start.sh /app/kafka/config/zookeeper.properties > /app/kafka/logs/zookeeper_stdout.log 2> /app/kafka/logs/zookeeper_stderr.log &
  sleep 30
  echo "Starting Kafka ..."
  /app/kafka/bin/kafka-server-start.sh /app/kafka/config/server.properties > /app/kafka/logs/kafka_stdout.log 2> /app/kafka/logs/kafka_stderr.log &
  echo "Starting Kafka-Connect (wait 60s)..."
  /app/kafka/bin/connect-standalone.sh /app/kafka/config/connect-standalone.properties /app/venafi-connect-demo/source-quickstart.properties  > /app/kafka/logs/connect_stdout.log 2> /app/kafka/logs/connect_stderr.log &
  sleep 60
  echo "Starting console consumer..."
  /app/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic VENAFI-LOGS
fi
