package com.github.shmoli.kafka.connect.source.metricsapi.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MetricType {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ValueType getType() {
        return type;
    }

    public void setType(ValueType type) {
        this.type = type;
    }

    public ValueUnit getUnit() {
        return unit;
    }

    public void setUnit(ValueUnit unit) {
        this.unit = unit;
    }

    public MetricType() {
    }

    public MetricType(JSONObject record){

        this.name =        record.getString("name");
        this.description = record.getString("description");
        this.type =        record.getString("type").equals("COUNTER_INT64") ? MetricType.ValueType.COUNTER_INT64 : MetricType.ValueType.GAUGE_INT64;
        this.unit =        record.getString("unit").equals("By") ? MetricType.ValueUnit.By : MetricType.ValueUnit.One;
        this.labels = new ArrayList<String>();
        JSONArray jsonlabels = record.getJSONArray("labels");
        for (int nn=0; nn<jsonlabels.length(); nn++) {
            labels.add(jsonlabels.getJSONObject(nn).getString("key"));
        }
    }

    private String name;
    private String description;
    enum ValueType {
        COUNTER_INT64,
        GAUGE_INT64
    };
    private ValueType type;
    enum ValueUnit {
        By,
        One
    }
    private ValueUnit unit;

    public ArrayList<String> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }

    private ArrayList<String> labels = new ArrayList<String>();
}
