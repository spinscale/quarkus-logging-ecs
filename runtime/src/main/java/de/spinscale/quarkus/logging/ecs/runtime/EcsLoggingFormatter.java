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

import co.elastic.logging.EcsJsonSerializer;
import co.elastic.logging.JsonUtils;
import org.jboss.logmanager.ExtFormatter;
import org.jboss.logmanager.ExtLogRecord;

import java.util.Map;

public class EcsLoggingFormatter extends ExtFormatter {

    private final boolean includeOrigin;
    private final boolean stackTraceAsArray;
    private final String serviceName;
    private final String serializedAdditionalFields;

    public EcsLoggingFormatter(final EcsLoggingConfig config) {
        this.includeOrigin = config.includeOrigin;
        this.stackTraceAsArray = config.stackTraceAsArray;
        this.serviceName = "default".equals(config.serviceName) ? null : config.serviceName;
        this.serializedAdditionalFields = serializeAdditionalFields(config.additionalFields);
    }

    @Override
    public String format(ExtLogRecord record) {
        StringBuilder builder = new StringBuilder();

        EcsJsonSerializer.serializeObjectStart(builder, record.getMillis());
        EcsJsonSerializer.serializeLogLevel(builder, record.getLevel().getName());
        EcsJsonSerializer.serializeFormattedMessage(builder, this.formatMessage(record));
        EcsJsonSerializer.serializeServiceName(builder, serviceName);
        EcsJsonSerializer.serializeThreadName(builder, record.getThreadName());
        EcsJsonSerializer.serializeLoggerName(builder, record.getLoggerName());
        EcsJsonSerializer.serializeMDC(builder, record.getMdcCopy());

        if (includeOrigin && record.getSourceFileName() != null && record.getSourceMethodName() != null) {
            EcsJsonSerializer.serializeOrigin(builder, record.getSourceFileName(), record.getSourceMethodName(), record.getSourceLineNumber());
        }

        if (!serializedAdditionalFields.isEmpty()) {
            builder.append(serializedAdditionalFields);
        }
        EcsJsonSerializer.serializeException(builder, record.getThrown(), stackTraceAsArray);
        EcsJsonSerializer.serializeObjectEnd(builder);

        // also serialize NDC?

        return builder.toString();
    }

    private String serializeAdditionalFields(Map<String, String> additionalFields) {
        if (additionalFields == null || additionalFields.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : additionalFields.entrySet()) {
            builder.append('\"');
            JsonUtils.quoteAsString(entry.getKey(), builder);
            builder.append("\":\"");
            JsonUtils.quoteAsString(entry.getValue(), builder);
            builder.append("\",");
        }

        return builder.toString();
    }
}
