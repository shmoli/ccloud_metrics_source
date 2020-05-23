package com.github.shmoli.kafka.connect.source.metricsapi.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import static com.github.shmoli.kafka.connect.source.metricsapi.utils.GitProperties.*;
import static org.junit.Assert.*;

public class GitPropertiesTest {

    @Test
    public void whenGetCodeOriginCalled_resultsReturned() throws IOException {
        Map<String, String> map = GitProperties.getProperties();
        assertNotNull("null COMMIT_ID_ABBREV",  map.get(COMMIT_ID_ABBREV));
        assertNotNull("null REMOTE_ORIGIN_URL", map.get(REMOTE_ORIGIN_URL));
        assert(Pattern.matches("^[a-f0-9]{7}$",    map.get(COMMIT_ID_ABBREV).toString()));
        assert(Pattern.matches("^[a-f0-9]{7}.*",   map.get(COMMIT_ID_DESCRIBE).toString()));

    }

    @Test
    public void whenGetCodeOriginCalledTwice_resultsStillReturned() throws IOException {
        Map<String, String> map = GitProperties.getProperties();
        map = GitProperties.getProperties();
        assertEquals(true, Pattern.matches("^[a-f0-9]{7}$", map.get(COMMIT_ID_ABBREV).toString()));
    }
}