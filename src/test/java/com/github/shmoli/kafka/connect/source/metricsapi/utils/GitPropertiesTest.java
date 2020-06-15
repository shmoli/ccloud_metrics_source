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