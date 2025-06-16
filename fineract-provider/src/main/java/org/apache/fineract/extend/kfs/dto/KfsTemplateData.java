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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for KFS Template Data Provides template information and configuration for KFS document generation
 */
public class KfsTemplateData {

    @JsonProperty("templateId")
    private Long templateId;

    @JsonProperty("templateName")
    private String templateName;

    @JsonProperty("templateVersion")
    private String templateVersion;

    @JsonProperty("description")
    private String description;

    @JsonProperty("templateType")
    private String templateType;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("templateContent")
    private String templateContent;

    @JsonProperty("supportedFields")
    private List<String> supportedFields;

    @JsonProperty("createdDate")
    private LocalDateTime createdDate;

    @JsonProperty("lastModifiedDate")
    private LocalDateTime lastModifiedDate;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("lastModifiedBy")
    private String lastModifiedBy;

    // Constructors
    public KfsTemplateData() {}

    public KfsTemplateData(Long templateId, String templateName, String templateVersion, String description, String templateType,
            Boolean isActive, String templateContent, List<String> supportedFields, LocalDateTime createdDate,
            LocalDateTime lastModifiedDate, String createdBy, String lastModifiedBy) {
        this.templateId = templateId;
        this.templateName = templateName;
        this.templateVersion = templateVersion;
        this.description = description;
        this.templateType = templateType;
        this.isActive = isActive;
        this.templateContent = templateContent;
        this.supportedFields = supportedFields;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
    }

    // Getters and Setters
    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    public List<String> getSupportedFields() {
        return supportedFields;
    }

    public void setSupportedFields(List<String> supportedFields) {
        this.supportedFields = supportedFields;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}
