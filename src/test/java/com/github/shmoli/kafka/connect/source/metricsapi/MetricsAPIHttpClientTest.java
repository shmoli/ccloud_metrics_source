package com.github.shmoli.kafka.connect.source.metricsapi;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;
import static org.junit.Assert.*;

public class MetricsAPIHttpClientTest {


    MetricsAPIHttpClient c;

    @Before
    public void setUp() throws Exception {
        Map<String, String> orig = new HashMap<>();
        orig.put(MetricsAPISourceConnectorConfig.CCLOUD_CLUSTER_ID_CONFIG, "lkc-abcdef");
        orig.put(MetricsAPISourceConnectorConfig.CCLOUD_USER_ID_CONFIG, "xxx");
        orig.put(MetricsAPISourceConnectorConfig.CCLOUD_USER_PASSWORD_CONFIG, "***");
        orig.put(MetricsAPISourceConnectorConfig.KAFKA_TOPIC_NAME_CONFIG, "metrics_api");
        MetricsAPISourceConnectorConfig cfg = new MetricsAPISourceConnectorConfig(orig);
        c = new MetricsAPIHttpClient(cfg);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
   public void whenMakeRequestCalled_headersAreSet() {
        HttpRequest http = c.makeRequest("http://www.test.com", MetricsAPIHttpClient.RequestType.POST, "{}");
        assertEquals("application/json", http.getHeaders().get(CONTENT_TYPE).get(0));
        assert(http.getHeaders().containsKey(USER_AGENT));
    }
}