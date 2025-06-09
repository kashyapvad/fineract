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

/**
 * JPA converter for storing JSON data in database columns. Used for API responses and complex data structures in KYC
 * and Credit Bureau entities.
 *
 * This converter is provider-agnostic and can handle responses from different credit bureau APIs by storing them as
 * JSON.
 */
@Slf4j
@Converter
public class JsonAttributeConverter implements AttributeConverter<JsonNode, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts JsonNode to database column value (String).
     *
     * @param attribute
     *            the JsonNode to convert
     * @return JSON string representation or null
     */
    @Override
    public String convertToDatabaseColumn(JsonNode attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting JsonNode to String: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error converting JsonNode to JSON string", e);
        }
    }

    /**
     * Converts database column value (String) to JsonNode.
     *
     * @param dbData
     *            the JSON string from database
     * @return JsonNode representation or null
     */
    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readTree(dbData);
        } catch (JsonProcessingException e) {
            log.error("Error converting String to JsonNode: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error parsing JSON from database: " + dbData, e);
        }
    }
}
