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