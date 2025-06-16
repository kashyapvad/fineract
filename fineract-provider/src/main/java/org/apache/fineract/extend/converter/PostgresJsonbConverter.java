/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.extend.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;

/**
 * JPA converter specifically for PostgreSQL JSONB columns.
 * 
 * This converter handles PostgreSQL's PGobject type which is returned by the PostgreSQL JDBC driver
 * for JSONB columns. It converts between JsonNode and PGobject.
 */
@Slf4j
@Converter(autoApply = false)
public class PostgresJsonbConverter implements AttributeConverter<JsonNode, Object> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object convertToDatabaseColumn(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        try {
            // Convert JsonNode to String - PostgreSQL will cast this to JSONB
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            log.error("Error converting JsonNode to String for PostgreSQL JSONB", e);
            throw new IllegalArgumentException("Cannot convert JsonNode to PostgreSQL JSONB", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        
        try {
            String jsonString;
            if (dbData instanceof PGobject pgObject) {
                // PostgreSQL returns JSONB data as PGobject
                jsonString = pgObject.getValue();
            } else if (dbData instanceof String stringData) {
                // Handle String data (should not happen in PostgreSQL but defensive coding)
                jsonString = stringData;
            } else {
                throw new IllegalArgumentException("Unsupported database type for JSONB conversion: " + dbData.getClass());
            }
            
            if (jsonString == null || jsonString.trim().isEmpty()) {
                return null;
            }
            
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            log.error("Error converting database data to JsonNode from PostgreSQL JSONB: {}", dbData, e);
            throw new IllegalArgumentException("Cannot convert PostgreSQL JSONB to JsonNode", e);
        }
    }
} 