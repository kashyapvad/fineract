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
package org.apache.fineract.portfolio.loanaccount.guarantor.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.springframework.stereotype.Component;

/**
 * Post-processor that fixes the guarantors field in loan JSON response. This works around Fineract's
 * ParameterListInclusionStrategy that causes nested GuarantorData objects to serialize as empty {}.
 */
@Slf4j
@Component
public class GuarantorJsonPostProcessor {

    private final Gson gson;

    public GuarantorJsonPostProcessor() {
        // Use Fineract's configured Gson with proper TypeAdapters
        this.gson = GoogleGsonSerializerHelper.createSimpleGson();
    }

    /**
     * Fixes the guarantors field in the JSON response by re-serializing GuarantorData objects with proper field values.
     */
    public String fixGuarantorsInJson(String originalJson, LoanAccountData loanData) {
        try {
            // Parse the original JSON
            JsonObject jsonObject = JsonParser.parseString(originalJson).getAsJsonObject();

            // Check if guarantors field exists and loan has guarantor data
            if (jsonObject.has("guarantors") && loanData.getGuarantors() != null) {
                // Re-serialize guarantors with proper Gson configuration
                JsonElement guarantorsJson = gson.toJsonTree(loanData.getGuarantors());

                // Replace the empty guarantors array with properly serialized data
                jsonObject.add("guarantors", guarantorsJson);

                // Convert back to JSON string
                return gson.toJson(jsonObject);
            }

            return originalJson;

        } catch (Exception e) {
            // Log the error and return original JSON as fallback
            log.error("GuarantorJsonPostProcessor error", e);
            return originalJson;
        }
    }
}
