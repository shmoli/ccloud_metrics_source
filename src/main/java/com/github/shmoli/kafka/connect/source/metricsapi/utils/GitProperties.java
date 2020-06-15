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
