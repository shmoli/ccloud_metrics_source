# Confluent Cloud Metrics Source

Kafka source connector for the Confluent Cloud metrics API.  This Connector reads records from the Confluent Cloud Metrics API and pushes those into a Kafka cluster for processing.  This is intended for use with various metrics sinks which will push the Confluent Cloud metrics into external monitoring systems.

For more information on Confluent Cloud and the metrics API see: <https://docs.confluent.io/current/cloud/metrics-api.html>

# Build

The projevct depends on Java 1.8 and apache maven to build.

```bash
git clone git@github.com:shmoli/ccloud_metrics_source.git
cd ccloud_metrics_source
mvn package
```

# Install

Copy the package into the connect plugin path.

```bash
cp -rf ./target/ccloud_metrics_source-1.0-package/share/java/ccloud_metrics_api  <connect-plugin-path>
```

Now restart connect cluster to pick up the new connector.

# Configure an instance

Use the following example json to post a connector instance using the REST endpoint for you connect cluster.

```json
{
    "name": "CCloudMetrics",
    "config": {
		  "name": "CCloudMetrics",
		  "connector.class": "com.github.shmoli.kafka.connect.source.metricsapi.MetricsAPISourceConnector",
		  "tasks.max": "1",
		  "key.converter": "io.confluent.connect.avro.AvroConverter",
		  "key.converter.schema.registry.url": "http://localhost:8081",
		  "value.converter": "io.confluent.connect.avro.AvroConverter",
		  "value.converter.schema.registry.url": "http://localhost:8081",
		  "errors.log.enable": true,
		  "errors.log.include.messages": true,
		  "ccloud.user.id": "<CCLoud_User_ID>",
		  "ccloud.user.password": "<CCloud_Password>",
		  "ccloud.cluster.id": "<Cluster_ID>",
		  "ccloud.topic.level.metrics": true,
		  "kafka.topic.name": "CCloudMetrics"   	
    }
}
```

# Check for data

The Connector will poll for data once every minute.  Confirm data is getting published to your cluster.

![Example Messages][messages]

[messages]: https://github.com/shmoli/ccloud_metrics_source/blob/master/resources/CCLoudMetricsScreenshot.png "Example Messages"


