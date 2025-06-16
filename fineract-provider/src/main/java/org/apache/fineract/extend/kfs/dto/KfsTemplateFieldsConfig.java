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
package org.apache.fineract.extend.kfs.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for KFS template fields configuration
 */
@Data
@NoArgsConstructor
public class KfsTemplateFieldsConfig {

    private List<TemplateField> fields;
    private List<String> requiredFields;
    private List<String> optionalFields;
    private Map<String, Object> fieldValidations;

    /**
     * Inner class representing a template field
     */
    @Data
    @NoArgsConstructor
    public static class TemplateField {

        private String name;
        private String type;
        private boolean required;
        private String defaultValue;

        public TemplateField(String name, String type, boolean required) {
            this.name = name;
            this.type = type;
            this.required = required;
        }
    }
}
