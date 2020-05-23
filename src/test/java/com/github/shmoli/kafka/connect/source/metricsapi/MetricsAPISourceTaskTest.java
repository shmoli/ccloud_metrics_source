package com.github.shmoli.kafka.connect.source.metricsapi;

import com.github.shmoli.kafka.connect.source.metricsapi.model.Metric;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.shmoli.kafka.connect.source.metricsapi.MetricsAPISourceTask.TOPIC_LABEL;
import static org.junit.Assert.*;

public class MetricsAPISourceTaskTest {

    class MetricOverride extends Metric {
        public void setClusterId(String arg) { this.clusterId = arg; }
        public void setTimestamp(String arg) { timestamp = arg; }
        public void setName(String arg) { name = arg; }
        public void setValue(double arg) { value = arg; }
        public void setGroupbyName(String arg) { groupbyName = arg; }
        public void setGroupbyValue(String arg) { groupbyValue = arg; }
    }

    private Map<String, String> orig;
    private MetricsAPISourceTask t;
    private MetricOverride m;

    @Before
    public void setUp() throws Exception {
        t = new MetricsAPISourceTask();
        m = new MetricOverride();
        m.setName("myvar");
        m.setGroupbyName("cluster");
        orig = new HashMap<>();
        orig.put(MetricsAPISourceConnectorConfig.CCLOUD_CLUSTER_ID_CONFIG, "lkc-abcdef");
        orig.put(MetricsAPISourceConnectorConfig.CCLOUD_USER_ID_CONFIG, "xxx");
        orig.put(MetricsAPISourceConnectorConfig.CCLOUD_USER_PASSWORD_CONFIG, "***");
        orig.put(MetricsAPISourceConnectorConfig.KAFKA_TOPIC_NAME_CONFIG, "metrics_api");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testBuildRecordNameWithNoTopic() {
        t.start(orig);
        assertEquals("lkc-abcdef|myvar", t.buildRecordName(m));
    }

    @Test
    public void testBuildRecordNameNonDefaultSeparator() {

        orig.put(MetricsAPISourceConnectorConfig.NAME_SEPARATOR_CONFIG, "-->");
        t.start(orig);
        assertEquals("lkc-abcdef-->myvar", t.buildRecordName(m));
    }

    @Test
    public void testBuildRecordNameWithTopic() {
        m.setGroupbyName(TOPIC_LABEL);
        m.setGroupbyValue("topic");
        t.start(orig);
        assertEquals("lkc-abcdef|topic|myvar", t.buildRecordName(m));
    }

    @Test
    public void testBuildRecordNamePrefix() {
        orig.put(MetricsAPISourceConnectorConfig.NAME_PREFIX_CONFIG, "PrefixStr|");
        t.start(orig);
        assertEquals("PrefixStr|lkc-abcdef|myvar", t.buildRecordName(m));
    }

    @Test
    public void testBuildRecordNamePrefixTopicSep() {
        orig.put(MetricsAPISourceConnectorConfig.NAME_PREFIX_CONFIG, "Pre|");
        orig.put(MetricsAPISourceConnectorConfig.NAME_SEPARATOR_CONFIG, "/");
        m.setGroupbyName(TOPIC_LABEL);
        m.setGroupbyValue("t");
        t.start(orig);
        assertEquals("Pre|lkc-abcdef/t/myvar", t.buildRecordName(m));
    }


     @Test
     public void testTimestampParsing() {
         String time = "2020-05-12T19:40:05Z";
         assertEquals(1589312405, t.tsSecs(time));
     }

    @Test
    public void whenSleepUntilNextSampleCalled_timePasses() {
        long before = System.currentTimeMillis();
        long st = 300;
        t.sleepUntilNextSample(before, st);
        long after = System.currentTimeMillis();
        assertEquals(true, before <= after);
    }

    @Test
    public void whenGetNextSampleTimeCalled_timeIsMinuteAligned() {
        long now = 1234;
        assertEquals(60000, t.getNextSampleTime(now, 60000));
    }

    @Test
    public void whenGetNextSampleTimeCalled_timeIsValid() {
        long st = 60000;
        long now = System.currentTimeMillis();
        long next = t.getNextSampleTime(now, st);
        assertEquals(true, next > now);  // next is in the future
        assertEquals(true,  next < (now + st));  // next is sooner than now + st
        assertEquals(0,next % st);   // next is aligned to the sampletime boundary
    }


}