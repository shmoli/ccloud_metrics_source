package com.github.shmoli.kafka.connect.source.metricsapi;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

public class MetricsAPISchemas {

    public static String SCHEMA_KEY = "metric.key";

    public static String CCLOUD_CLUSTER_ID_FIELD = "cluster_id";   // CCloud cluster
    public static String METRIC_HOST_FIELD       = "host";         // Hostname
    public static String CCLOUD_TOPIC_FIELD      = "topic";        // CCLoud topic
    public static String METRIC_NAME_FIELD       = "name";         // Metric name
    public static String METRIC_LONG_NAME_FIELD  = "long_name";     // Long metric name
    public static String METRIC_TIMESTAMP_FIELD  = "timestamp";    // Timestamp of reading
    public static String METRIC_VALUES_FIELD     = "values";       // Value of reading
    public static String METRIC_DBLVALUE_FIELD   = "doubleValue";  // Value of reading
    public static String METRIC_DIMENSIONS_FIELD = "dimensions";   // Dimensions struct
    public static String GROUPBY_NAME_FIELD      = "groupby_name"; // Aggregation level (cluster or topic)
    public static String GROUPBY_VALUE_FIELD     = "groupby_value";// Aggregation level (cluster or topic value)

    // Key Schema
    public static Schema KEY_SCHEMA = SchemaBuilder.struct().name(SCHEMA_KEY)

            .field(CCLOUD_CLUSTER_ID_FIELD, Schema.STRING_SCHEMA)
            .field(CCLOUD_TOPIC_FIELD,      Schema.STRING_SCHEMA)
            .field(METRIC_NAME_FIELD,       Schema.STRING_SCHEMA)
            .build();

    public static String SCHEMA_DIMENSIONS_VALUE = "metrics.values.dimensions";

    public static Schema METRIC_DIMENSIONS_SCHEMA = SchemaBuilder.struct().name(SCHEMA_DIMENSIONS_VALUE)
            .field(CCLOUD_CLUSTER_ID_FIELD, Schema.STRING_SCHEMA)
            .field(CCLOUD_TOPIC_FIELD,      Schema.STRING_SCHEMA)
            .field(METRIC_HOST_FIELD,       Schema.STRING_SCHEMA)
            .build();

    public static String SCHEMA_DBL_METRIC_VALUE = "metric.values.doubleValue";

    public static Schema METRIC_DBL_VALUE_SCHEMA = SchemaBuilder.struct().name(SCHEMA_DBL_METRIC_VALUE)
            .field(METRIC_DBLVALUE_FIELD, Schema.FLOAT64_SCHEMA)
            .build();

    public static String SCHEMA_METRIC_VALUE = "metric.value";

    // Key Schema
    public static Schema METRIC_VALUE_SCHEMA = SchemaBuilder.struct().name(SCHEMA_METRIC_VALUE)

            .field(METRIC_NAME_FIELD,       Schema.STRING_SCHEMA)
            .field(METRIC_LONG_NAME_FIELD,  Schema.STRING_SCHEMA)
            .field(METRIC_TIMESTAMP_FIELD,  Schema.INT64_SCHEMA)
            .field(GROUPBY_NAME_FIELD,      Schema.STRING_SCHEMA)
            .field(GROUPBY_VALUE_FIELD,     Schema.STRING_SCHEMA)
            .field(METRIC_DIMENSIONS_FIELD, METRIC_DIMENSIONS_SCHEMA)
            .field(METRIC_VALUES_FIELD,     METRIC_DBL_VALUE_SCHEMA)
            .build();
}
