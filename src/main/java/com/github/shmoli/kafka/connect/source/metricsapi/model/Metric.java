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

package com.github.shmoli.kafka.connect.source.metricsapi.model;

import org.json.JSONObject;

public class Metric {

    public String getClusterId() {
        return clusterId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public String getGroupbyName() {
        return groupbyName;
    }

    public String getGroupbyValue() {
        return groupbyValue;
    }

    protected Metric() {}

    public Metric(JSONObject record, String metricName, String clusterId, String groupbyName) {

        this.clusterId = clusterId;
        this.timestamp = record.getString(TIMESTAMP_KEY);
        this.name = metricName;
        this.value = record.getBigDecimal(VALUE_KEY).doubleValue();
        this.groupbyName = groupbyName;
        this.groupbyValue = record.getString(groupbyName);
    }

    private final String TIMESTAMP_KEY = "timestamp";
    private final String VALUE_KEY = "value";

    protected String clusterId;
    protected String timestamp;
    protected String name;
    protected double value;
    protected String groupbyName;
    protected String groupbyValue;
}
