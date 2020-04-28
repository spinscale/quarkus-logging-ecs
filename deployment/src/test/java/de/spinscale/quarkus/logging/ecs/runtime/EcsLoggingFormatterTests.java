/*
 * Copyright [2020] [Alexander Reelsen]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package de.spinscale.quarkus.logging.ecs.runtime;

import org.jboss.logmanager.ExtLogRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
import java.io.StringReader;
import java.util.HashMap;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;

class EcsLoggingFormatterTests {

    private final ExtLogRecord record = new ExtLogRecord(Level.INFO, "my message", "logger_class_name");
    private final EcsLoggingConfig config = new EcsLoggingConfig();

    @BeforeEach
    public void setup() {
        config.enable = true;
        config.serviceName = "my_favourite_service";
        record.setLoggerName("my_logger_name");
    }

    @Test
    public void testEnabled() {
        JsonObject root = getLoggingJson(record);
        assertThat(root).containsKeys("@timestamp", "log.level", "message", "service.name", "process.thread.name", "log.logger");
        assertThat(root.getString("log.level")).isEqualTo("INFO");
        assertThat(root.getString("log.logger")).isEqualTo("my_logger_name");
        assertThat(root).doesNotContainKey("log.origin");
        assertThat(root.getString("service.name")).isEqualTo("my_favourite_service");
    }

    @Test
    public void testStackTraceAsString() {
        record.setThrown(new RuntimeException("blabla"));

        JsonObject root = getLoggingJson(record);
        assertThat(root).containsKeys("@timestamp", "log.level", "message", "service.name", "process.thread.name", "log.logger");
        assertThat(root.getString("error.message")).isEqualTo("blabla");
        assertThat(root.getString("error.stack_trace").length()).isGreaterThan(1000);
    }

    @Test
    public void testStackTracesAsArray() {
        config.stackTraceAsArray = true;
        record.setThrown(new RuntimeException("blabla"));

        JsonObject root = getLoggingJson(record);
        assertThat(root).containsKeys("@timestamp", "log.level", "message", "service.name", "process.thread.name", "log.logger");
        assertThat(root.getString("error.message")).isEqualTo("blabla");
        final JsonArray array = root.getJsonArray("error.stack_trace");
        assertThat(array.size()).isGreaterThan(10);
    }

    @Test
    public void testIncludeOrigin() {
        config.includeOrigin = true;
        record.setSourceFileName("MySource.java");
        record.setSourceClassName("MySource");
        record.setSourceLineNumber(123);
        record.setSourceMethodName("method_name");

        JsonObject root = getLoggingJson(record);
        assertThat(root).containsKey("log.origin");
        final JsonObject logOrigin = root.get("log.origin").asJsonObject();
        assertThat(logOrigin).containsKeys("file.line", "file.name", "function");
        assertThat(logOrigin.getInt("file.line")).isEqualTo(123);
        assertThat(logOrigin.getString("file.name")).isEqualTo("MySource.java");
        assertThat(logOrigin.getString("function")).isEqualTo("method_name");
    }

    @Test
    public void testAdditionalFields() {
        config.additionalFields = new HashMap<>();
        config.additionalFields.put("foo", "bar");
        config.additionalFields.put("spam", "eggs");

        JsonObject root = getLoggingJson(record);
        assertThat(root).containsKeys("spam", "foo");
        assertThat(root.getString("spam")).isEqualTo("eggs");
        assertThat(root.getString("foo")).isEqualTo("bar");
    }

    @Test
    public void testMdc() {
        record.putMdc("mdc", "hell yeah");
        JsonObject root = getLoggingJson(record);
        assertThat(root).containsKey("mdc");
        assertThat(root.getString("mdc")).isEqualTo("hell yeah");
    }

    private JsonObject getLoggingJson(ExtLogRecord record) {
        EcsLoggingFormatter formatter = new EcsLoggingFormatter(config);
        final String json = formatter.format(record);

        try (final JsonParser parser = Json.createParser(new StringReader(json))) {
            parser.next();
            return parser.getObject();
        }
    }
}