package com.github.shmoli.kafka.connect.source.metricsapi;

import com.github.shmoli.kafka.connect.source.metricsapi.model.Metric;
import com.github.shmoli.kafka.connect.source.metricsapi.model.MetricType;
import com.github.shmoli.kafka.connect.source.metricsapi.utils.GitProperties;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import static com.github.shmoli.kafka.connect.source.metricsapi.utils.GitProperties.*;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;

public class MetricsAPIHttpClient {

    static final String HTTPCLIENT_URL_CLOUD_DESCRIPTORS = "https://api.telemetry.confluent.cloud/v1/metrics/cloud/descriptors";
    static final String HTTPCLIENT_URL_CLOUD_QUERY       = "https://api.telemetry.confluent.cloud/v1/metrics/cloud/query";

    enum RequestType
    {
        GET,
        POST
    }

    MetricsAPISourceConnectorConfig config;
    String userAgent;

    MetricsAPIHttpClient(MetricsAPISourceConnectorConfig config_) {
        config = config_;
        try {
            // Fess up the project/revision to CCloud in case we cause some issue
            Map<String,String> properties = GitProperties.getProperties();
            userAgent = properties.get(REMOTE_ORIGIN_URL) + " " + properties.get(COMMIT_ID_DESCRIBE);
        } catch (IOException e) {
            userAgent = "CCloud Source Connector";
        }
    }

    private static final Logger log = LoggerFactory.getLogger(MetricsAPIHttpClient.class);

    protected HttpRequest makeRequest(String URL, RequestType type){
        return makeRequest(URL, type, "");
    }

    protected  HttpRequest makeRequest(String URL, RequestType type, String body) {
        HttpRequest unirest;
        if (type == RequestType.GET) {
            unirest = Unirest.get(URL);
        } else {
             HttpRequestWithBody unirestBod = Unirest.post(URL);
             unirestBod.body(body);
             unirest = unirestBod;
        }
        setBasicAuth(unirest);
        setHeaders(unirest);
        return unirest;
    }

    protected HttpResponse<JsonNode> fetchDescriptors() throws UnirestException {
        HttpRequest unirest = makeRequest(HTTPCLIENT_URL_CLOUD_DESCRIPTORS, RequestType.GET);
        return unirest.asJson();
    }

    protected HttpResponse<JsonNode> fetchMetric(String body) throws UnirestException {
        log.debug("fetchMetric: " +HTTPCLIENT_URL_CLOUD_QUERY + " : ==" + body + "==");
        HttpRequest unirest = makeRequest(HTTPCLIENT_URL_CLOUD_QUERY, RequestType.POST, body);
        return unirest.asJson();
    }


    protected ArrayList<MetricType> parseDescriptorBody(JSONArray array) {
        ArrayList<MetricType> metrics = new ArrayList<>();
        JSONArray jsonData = array.getJSONObject(0).getJSONArray("data");
        for (int i = 0; i < jsonData.length(); i++) {

            JSONObject record = jsonData.getJSONObject(i);
            log.info(record.toString());
            metrics.add(new MetricType (record));
        }
        log.debug("parseDescriptorBody return " + metrics.size() + " decriptors" );
        return metrics;
    }

    //String metricName, String clusterId, String groupbyName
    protected ArrayList<Metric> parseMetricsBody(JSONArray array, MetricType metricType) {
        ArrayList<Metric> metrics = new ArrayList<>();
        JSONArray jsonData = array.getJSONObject(0).getJSONArray("data");
        for (int i = 0; i < jsonData.length(); i++) {

            JSONObject record = jsonData.getJSONObject(i);
            log.debug(record.toString());
            metrics.add(new Metric (record, metricType.getName(), config.getCcloudClusterId(), getGroupByName(metricType)));
        }
        log.debug("parseMetricsBody return " + metrics.size() + " metrics for " + metricType.getName());
        return metrics;
    }


    HttpResponse<JsonNode> checkResponseCodes(HttpResponse<JsonNode> jsonResponse) throws UnirestException, AuthenticationException {
        log.debug("checkResponseCodes: " + jsonResponse.getStatus() + " : " + jsonResponse.getStatusText());
        switch (jsonResponse.getStatus()) {
            case 200:
                return jsonResponse;
            case 401:
                throw new AuthenticationException("Bad credentials provided, please edit your config");
            default:
                throw new UnirestException(String.format("Unexpected Unirest error %d, %s %s",
                        jsonResponse.getStatus(),
                        jsonResponse.getStatusText().toString(),
                        jsonResponse.getBody().toString()));
        }
    }


    public ArrayList<MetricType> getDescriptors() {

        try {
            HttpResponse<JsonNode> jsonResponse = fetchDescriptors();
            checkResponseCodes(jsonResponse);
            return parseDescriptorBody(jsonResponse.getBody().getArray());
        } catch (Exception e) {
            log.error(String.format("Failed to query metric descriptors '%s'", e.getMessage()));
            return new ArrayList<MetricType>();
        }

    }

    public ArrayList<Metric> getMetrics(ArrayList<MetricType> metricTypes) {

        String interval = getInterval();
        ArrayList<HttpResponse<JsonNode>> ret = new ArrayList<HttpResponse<JsonNode>>();
        ArrayList<Metric> metrics = new ArrayList<Metric>();

        for (int nn=0; nn<metricTypes.size(); nn++) {
            MetricType metricType = metricTypes.get(nn);
            try {
                HttpResponse<JsonNode> metricValue = getMetric(metricType, interval);
                log.debug(metricType.getName() + "getMetric returned :==" + metricValue.getBody().toString() + "==");
                checkResponseCodes(metricValue);
                metrics.addAll(parseMetricsBody(metricValue.getBody().getArray(), metricType));
                log.debug("getMetrics return " + metrics.size() + " metrics" );
            } catch (Exception e) {
                // If there's an exception the skip this record and try the next ones.
                log.error(String.format("Failed to query metric '%s', '%s'", metricType.getName(), e.getMessage()));
            }
        }
        return metrics;
    }

    private String getInterval() {
        OffsetDateTime timeNow = OffsetDateTime.now().withNano(0).withSecond(0);
        // Grab data from -2 mins to -1 mins
        String start =  DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(timeNow.minusMinutes(2));
        String end =  DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(timeNow.minusMinutes(1));

        return start + "/" + end;
    }

    private HttpResponse<JsonNode> getMetric(MetricType metricType, String interval) throws UnirestException, AuthenticationException {
        String query = makeQuery(metricType, interval);
        HttpResponse<JsonNode> jsonResponse = fetchMetric(query);
        return checkResponseCodes(jsonResponse);
    }

    void setBasicAuth(HttpRequest request) {
        request.basicAuth(config.getCcloudUserId(), config.getCcloudUserPassword());
    }

    void setHeaders(HttpRequest request){
        request.header(CONTENT_TYPE, "application/json");
        request.header(USER_AGENT, userAgent);
    }

    protected String getGroupByName(MetricType metric) {

        if(config.isTopicLevelMetrics() && metric.getLabels().contains("topic"))
            return "metric.label.topic";
        else
            return "metric.label.cluster_id";

    }

    protected String makeQuery(MetricType metric, String timeInterval) {

        String query = String.format("{\n" +
                "    \"aggregations\": [\n" +
                "        {\n" +
                "            \"agg\": \"SUM\",\n" +
                "            \"metric\": \"%s\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"filter\": {\n" +
                "        \"filters\": [\n" +
                "            {\n" +
                "                \"field\": \"metric.label.cluster_id\",\n" +
                "                \"op\": \"EQ\",\n" +
                "                \"value\": \"%s\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"op\": \"AND\"\n" +
                "    },\n" +
                "    \"granularity\": \"PT1M\",\n" +
                "    \"group_by\": [\n" +
                "        \"%s\"\n" +
                "    ],\n" +
                "    \"intervals\": [\n" +
                "        \"%s\"\n" +
                "    ],\n" +
                "    \"limit\": 1000\n" +
                "}", metric.getName(), config.getCcloudClusterId(), getGroupByName(metric), timeInterval);
        return query;
    }

}
