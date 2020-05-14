# ccloud_metrics_source

Kafka source connector for the Confluent Cloud metrics API.  This Connector reads records from the confluent cloud metrics API and pushes those into Kafka.  This is intended for use with various metrics sinks for then pushing that data into monitoring systems.

# build

mvn package

# install

cp -rf ./target/..  <connect-plugin-path>
<restart connect to pick up the new plugin>
