FROM openjdk:11
WORKDIR /app
COPY kafka-install/kafka_2.13-2.4.0                     ./kafka
COPY demo/connect-standalone.properties                 ./kafka/config/connect-standalone.properties
COPY demo/server.properties                             ./kafka/config/server.properties
COPY target/kafka-connect-venafi-0.9.6-SNAPSHOT-fat.jar ./venafi-connect-demo/
COPY config/source-quickstart.properties                ./venafi-connect-demo/
COPY demo/startup-script.sh                             ./venafi-connect-demo/
RUN chmod a+r+x /app/venafi-connect-demo/startup-script.sh

ENV VFURL=to-be-set
ENV VFUSERNAME=to-be-set
ENV VFPASS=to-be-set
VOLUME /app/
EXPOSE 9092:9092
EXPOSE 2181:2181

