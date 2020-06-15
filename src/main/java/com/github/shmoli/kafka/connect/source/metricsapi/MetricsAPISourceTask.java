/**
 Copyright 2020 Oli Watson

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */


package com.github.shmoli.kafka.connect.source.metricsapi;

import com.github.shmoli.kafka.connect.source.metricsapi.model.Metric;
import com.github.shmoli.kafka.connect.source.metricsapi.model.MetricType;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;

import static com.github.shmoli.kafka.connect.source.metricsapi.MetricsAPISchemas.*;

public class MetricsAPISourceTask extends SourceTask {

  static final Logger log = LoggerFactory.getLogger(MetricsAPISourceTask.class);
  private MetricsAPISourceConnectorConfig config;
  private MetricsAPIHttpClient httpClient;

  private ArrayList<MetricType> metricsTypes;
  private ArrayList<Metric> metrics;

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> map) {
    // Do things here that are required to start your task. This could be open a connection to a database, etc.
    config = new MetricsAPISourceConnectorConfig(map);
    httpClient = new MetricsAPIHttpClient(config);
  }


  @Override
  public List<SourceRecord> poll() throws InterruptedException {
    //Create SourceRecord objects that will be sent the kafka cluster.
    long nowMillis = System.currentTimeMillis();
    long nowSecs   = nowMillis / 1000;
    final ArrayList<SourceRecord> records = new ArrayList<>();

    // Sample every 60 seconds
    sleepUntilNextSample(nowMillis, 60000);

    log.debug("Fetching records ...");

    metricsTypes = httpClient.getDescriptors();
    metrics = httpClient.getMetrics(metricsTypes);

    for (Metric metric : metrics) {
      records.add(generateSourceRecord(metric, nowSecs));
    }

    log.debug("returning " + records.size() + " entries" );

    return records;
  }

  protected void sleepUntilNextSample(long timeNowMillis, long nSampleIntervalMillis) {
    long next = getNextSampleTime(timeNowMillis, nSampleIntervalMillis);
    log.info("Sleeping until " + Instant.ofEpochMilli(next).toString());
    try {
      Thread.sleep(next - timeNowMillis);
      log.info("Sampling at " + Instant.ofEpochMilli(System.currentTimeMillis()).toString());
    } catch (InterruptedException e) {
      log.error("sleepUntilNextSample interrupted " + e.toString());
    }
  }

  protected long getNextSampleTime(long timeNowMillis, long nSampleIntervalMillis) {
      return (timeNowMillis + nSampleIntervalMillis) - (timeNowMillis % nSampleIntervalMillis);
  }


  private SourceRecord generateSourceRecord(Metric metric, long nowSecs) {

    Map sourcePartition = new HashMap<String,String>();
    sourcePartition.put("SourceCluster", config.getCcloudClusterId());

    Map sourceOffset = new HashMap<String,Long>();
    sourceOffset.put("ts", new Long(nowSecs));

    return new SourceRecord (
            sourcePartition,
            sourceOffset,
            config.getTopic(),
            null, // partition will be inferred by the framework
            KEY_SCHEMA,
            buildRecordKey(metric),
            METRIC_VALUE_SCHEMA,
            buildRecordValue(metric),
            nowSecs * 1000);
  }

  private Struct buildRecordKey(Metric metric){
    // Key Schema
    Struct key = new Struct(KEY_SCHEMA)
            .put(CCLOUD_CLUSTER_ID_FIELD, config.getCcloudClusterId())
            .put(METRIC_NAME_FIELD, metric.getName());

    if(config.isTopicLevelMetrics())
      key.put(CCLOUD_TOPIC_FIELD, metric.getGroupbyValue());
    else
      key.put(CCLOUD_TOPIC_FIELD, "");

    return key;
  }

  final static String TOPIC_LABEL = "metric.label.topic";

  public static long tsMillis(String timestamp) {
    return tsSecs(timestamp) * 1000;
  }

  public static long tsSecs(String timestamp) {
    return Instant.parse(timestamp).getEpochSecond();
  }

  protected String buildRecordName(Metric metric) {

    String NAME_PRE = config.getPrefix();
    String NAME_SEP = config.getSeparator();

   if(metric.getGroupbyName().equals(TOPIC_LABEL)) {
      return NAME_PRE + config.getCcloudClusterId() + NAME_SEP + metric.getGroupbyValue() + NAME_SEP + metric.getName();
    } else {
      return NAME_PRE + config.getCcloudClusterId() + NAME_SEP  + metric.getName();
    }
}

  private Struct buildRecordValue(Metric metric) {
    // Issue top level fields

    Struct dimensionStruct = new Struct(METRIC_DIMENSIONS_SCHEMA)
            .put(CCLOUD_CLUSTER_ID_FIELD, config.getCcloudClusterId())
            .put(METRIC_HOST_FIELD, config.getCcloudClusterId()); // Get rid of forward slash for ddog

    if (metric.getGroupbyName().equals(TOPIC_LABEL)) {
      dimensionStruct.put(CCLOUD_TOPIC_FIELD, metric.getGroupbyValue());
    } else {
      dimensionStruct.put(CCLOUD_TOPIC_FIELD, "");
    }

    String longname = buildRecordName(metric);

    Struct dblValueStruct = new Struct(METRIC_DBL_VALUE_SCHEMA)
            .put(METRIC_DBLVALUE_FIELD, metric.getValue());

    Struct valuesStruct = new Struct(METRIC_VALUE_SCHEMA)
            .put(METRIC_NAME_FIELD, metric.getName())
            .put(METRIC_LONG_NAME_FIELD, buildRecordName(metric))
            .put(METRIC_TIMESTAMP_FIELD, tsSecs(metric.getTimestamp()))
            .put(GROUPBY_NAME_FIELD, metric.getGroupbyName())
            .put(GROUPBY_VALUE_FIELD, metric.getGroupbyValue())
            .put(METRIC_DIMENSIONS_FIELD, dimensionStruct)
            .put(METRIC_VALUES_FIELD, dblValueStruct);

    return valuesStruct;
  }


  @Override
  public void stop() {
    //Do whatever is required to stop your task.
  }
}