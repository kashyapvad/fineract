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
package org.apache.fineract.extend.kfs.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.fineract.extend.converter.PostgresJsonbConverter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

/**
 * JPA Entity representing KFS template configuration. Used for storing template information for KFS document
 * generation.
 *
 * Extends AbstractAuditableWithUTCDateTimeCustom for audit fields: - created_by, created_on_utc, last_modified_by,
 * last_modified_on_utc
 */
@Entity
@Table(name = "m_extend_kfs_template", uniqueConstraints = @UniqueConstraint(columnNames = { "template_name", "template_version" }))
public class KfsTemplate extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Template name cannot be blank")
    @Size(max = 200, message = "Template name cannot exceed 200 characters")
    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;

    @NotBlank(message = "Template version cannot be blank")
    @Size(max = 50, message = "Template version cannot exceed 50 characters")
    @Column(name = "template_version", nullable = false, length = 50)
    private String templateVersion;

    @Size(max = 1000, message = "Template description cannot exceed 1000 characters")
    @Column(name = "template_description", length = 1000)
    private String templateDescription;

    @NotBlank(message = "Template file path cannot be blank")
    @Size(max = 500, message = "Template file path cannot exceed 500 characters")
    @Column(name = "template_file_path", nullable = false, length = 500)
    private String templateFilePath;

    @Convert(converter = PostgresJsonbConverter.class)
    @Column(name = "template_fields_config", columnDefinition = "JSONB")
    private JsonNode templateFieldsConfig;

    @Lob
    @Column(name = "template_content")
    private String templateContent;

    @NotNull(message = "Active version flag cannot be null")
    @Column(name = "is_active_version", nullable = false)
    private Boolean isActiveVersion;

    @Size(max = 100, message = "Template type cannot exceed 100 characters")
    @Column(name = "template_type", length = 100)
    private String templateType;

    @Column(name = "parent_template_id")
    private Long parentTemplateId;

    @Size(max = 1000, message = "Supported loan products cannot exceed 1000 characters")
    @Column(name = "supported_loan_products", columnDefinition = "TEXT")
    private String supportedLoanProducts;

    @Size(max = 100, message = "Regulatory compliance cannot exceed 100 characters")
    @Column(name = "regulatory_compliance", length = 100)
    private String regulatoryCompliance;

    @Size(max = 20, message = "Template format cannot exceed 20 characters")
    @Column(name = "template_format", length = 20)
    private String templateFormat; // "DOCX", "HTML", "PDF"

    // Default constructor for JPA
    protected KfsTemplate() {
        // JPA requires default constructor
    }

    // Constructor with required fields
    public KfsTemplate(String templateName, String templateVersion, String templateFilePath, Boolean isActiveVersion) {
        this.templateName = templateName;
        this.templateVersion = templateVersion;
        this.templateFilePath = templateFilePath;
        this.isActiveVersion = isActiveVersion;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTemplateDescription() {
        return templateDescription;
    }

    public void setTemplateDescription(String templateDescription) {
        this.templateDescription = templateDescription;
    }

    public String getTemplateFilePath() {
        return templateFilePath;
    }

    public void setTemplateFilePath(String templateFilePath) {
        this.templateFilePath = templateFilePath;
    }

    public JsonNode getTemplateFieldsConfig() {
        return templateFieldsConfig;
    }

    public void setTemplateFieldsConfig(JsonNode templateFieldsConfig) {
        this.templateFieldsConfig = templateFieldsConfig;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    public Boolean getIsActiveVersion() {
        return isActiveVersion;
    }

    public void setIsActiveVersion(Boolean isActiveVersion) {
        this.isActiveVersion = isActiveVersion;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public Long getParentTemplateId() {
        return parentTemplateId;
    }

    public void setParentTemplateId(Long parentTemplateId) {
        this.parentTemplateId = parentTemplateId;
    }

    public String getSupportedLoanProducts() {
        return supportedLoanProducts;
    }

    public void setSupportedLoanProducts(String supportedLoanProducts) {
        this.supportedLoanProducts = supportedLoanProducts;
    }

    public String getRegulatoryCompliance() {
        return regulatoryCompliance;
    }

    public void setRegulatoryCompliance(String regulatoryCompliance) {
        this.regulatoryCompliance = regulatoryCompliance;
    }

    public String getTemplateFormat() {
        return templateFormat;
    }

    public void setTemplateFormat(String templateFormat) {
        this.templateFormat = templateFormat;
    }

    /**
     * Check if this template format yields better results than HTML templates
     *
     * @return true if DOCX template, false otherwise
     */
    public boolean yieldsHighQualityResults() {
        return "DOCX".equalsIgnoreCase(this.templateFormat);
    }

    /**
     * Check if this is a DOCX template
     *
     * @return true if DOCX template
     */
    public boolean isDocxTemplate() {
        return "DOCX".equalsIgnoreCase(this.templateFormat);
    }

    /**
     * Check if this is an HTML template
     *
     * @return true if HTML template
     */
    public boolean isHtmlTemplate() {
        return "HTML".equalsIgnoreCase(this.templateFormat);
    }

    // equals() and hashCode() based on ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KfsTemplate that = (KfsTemplate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString() method
    @Override
    public String toString() {
        return "KfsTemplate{" + "id=" + id + ", templateName='" + templateName + '\'' + ", templateVersion='" + templateVersion + '\''
                + ", templateDescription='" + templateDescription + '\'' + ", templateFilePath='" + templateFilePath + '\''
                + ", isActiveVersion=" + isActiveVersion + ", templateType='" + templateType + '\'' + ", parentTemplateId="
                + parentTemplateId + '}';
    }
}
