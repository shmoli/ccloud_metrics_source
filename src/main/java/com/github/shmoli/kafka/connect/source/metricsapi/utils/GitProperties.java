package com.github.shmoli.kafka.connect.source.metricsapi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class GitProperties {

    public static final String COMMIT_ID_ABBREV    = "git.commit.id.abbrev";
    public static final String COMMIT_ID_DESCRIBE  = "git.commit.id.describe";
    public static final String REMOTE_ORIGIN_URL   = "git.remote.origin.url";

    private static final Logger log = LoggerFactory.getLogger(GitProperties.class);

    public static Map<String, String> getProperties() throws IOException {

        ClassLoader classLoader = GitProperties.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("git.properties");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> jsonMap  = mapper.readValue(inputStream, Map.class);

        log.info("Retrieved GIT Properties: " + jsonMap.toString());

        return jsonMap;
    }
}
