package com.github.shmoli.kafka.connect.source.metricsapi;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.types.Password;

import java.util.Map;



public class MetricsAPISourceConnectorConfig extends AbstractConfig {

  public static final String CCLOUD_USER_ID_CONFIG = "ccloud.user.id";
  private static final String CCLOUD_USER_ID_DOC = "The user account to connect to Confluent Cloud\n"+
                                                "This is the email address you would use to log into Confluent Cloud";

  public static final String CCLOUD_USER_PASSWORD_CONFIG = "ccloud.user.password";
  private static final String CCLOUD_USER_PASSWORD_DOC = "The Password to log into Confluent Cloud\n";

  public static final String CCLOUD_CLUSTER_ID_CONFIG = "ccloud.cluster.id";
  private static final String CCLOUD_CLUSTER_ID_DOC = "The Cluster id to monitor, usually in the format lkc-XXXXX\n";

  public static final String TOPIC_LEVEL_METRICS_CONFIG = "ccloud.topic.level.metrics";
  private static final String TOPIC_LEVEL_METRICS_DOC = "If true then return metrics aggregated per topic\n" +
                                                        "If false then return metrics aggregated per cluster";

  public static final String KAFKA_TOPIC_NAME_CONFIG = "kafka.topic.name";
  private static final String KAFKA_TOPIC_NAME_DOC = "Name of the topic to produce to";

  public static final String NAME_SEPARATOR_CONFIG = "name.separator";
  private static final String NAME_SEPARATOR_DOC = "Separator character for the metric name\n" +
                                                    "defaults to |\n" +
                                                    "name is comprised of: [prefix]clusterId|<topic|>metric";

  public static final String NAME_PREFIX_CONFIG = "name.prefix";
  private static final String NAME_PREFIX_DOC = "Prefix string for the metric name\n" +
                                                    "name is comprised of: [prefix]clusterId|<topic|>metric";

  public String getCcloudUserId() {
    return ccloudUserId;
  }

  public String getCcloudUserPassword() {
    return ccloudUserPassword.value();
  }

  public String getCcloudClusterId() {
    return ccloudClusterId;
  }

  public String getTopic() { return topic; }

  public boolean isTopicLevelMetrics() {
    return topicLevelMetrics;
  }

  public String getSeparator() {return separator; }

  public String getPrefix() { return prefix; }

  private String ccloudUserId;
  private Password ccloudUserPassword;
  private String ccloudClusterId;
  private boolean topicLevelMetrics;
  private String topic;
  private String separator;
  private String prefix;



  public MetricsAPISourceConnectorConfig(Map<String, String> originals) {
    super(config(), originals);
    this.ccloudUserId = this.getString(CCLOUD_USER_ID_CONFIG);
    this.ccloudUserPassword = this.getPassword(CCLOUD_USER_PASSWORD_CONFIG);
    this.ccloudClusterId = this.getString(CCLOUD_CLUSTER_ID_CONFIG);
    this.topicLevelMetrics = this.getBoolean(TOPIC_LEVEL_METRICS_CONFIG);
    this.topic = this.getString(KAFKA_TOPIC_NAME_CONFIG);
    this.separator = this.getString(NAME_SEPARATOR_CONFIG);
    this.prefix = this.getString(NAME_PREFIX_CONFIG);
  }

  public static ConfigDef config() {
    return new ConfigDef()
        .define(CCLOUD_USER_ID_CONFIG,       Type.STRING,   Importance.HIGH,   CCLOUD_USER_ID_DOC)
        .define(CCLOUD_USER_PASSWORD_CONFIG, Type.PASSWORD, Importance.HIGH,   CCLOUD_USER_PASSWORD_DOC)
        .define(CCLOUD_CLUSTER_ID_CONFIG,    Type.STRING,   Importance.HIGH,   CCLOUD_CLUSTER_ID_DOC)
        .define(TOPIC_LEVEL_METRICS_CONFIG,  Type.BOOLEAN,  true,  Importance.MEDIUM, TOPIC_LEVEL_METRICS_DOC)
        .define(KAFKA_TOPIC_NAME_CONFIG,     Type.STRING,   Importance.HIGH,   KAFKA_TOPIC_NAME_DOC)
        .define(NAME_SEPARATOR_CONFIG,       Type.STRING,   "|",   Importance.MEDIUM, NAME_SEPARATOR_DOC)
        .define(NAME_PREFIX_CONFIG,          Type.STRING,   "",    Importance.MEDIUM, NAME_PREFIX_CONFIG);
  }
}
