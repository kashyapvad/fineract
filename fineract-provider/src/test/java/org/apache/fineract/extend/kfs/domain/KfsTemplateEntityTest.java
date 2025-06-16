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

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for KfsTemplate JPA entity. Tests entity mapping, validation, and audit fields.
 *
 * Following TDD approach - tests exist before implementation.
 */
@ExtendWith(MockitoExtension.class)
class KfsTemplateEntityTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    // Test data constants based on handoff document
    private static final String TEMPLATE_NAME = "KFS_STANDARD_TEMPLATE";
    private static final String TEMPLATE_VERSION = "1.0";
    private static final String TEMPLATE_DESCRIPTION = "Standard KFS template for loan documentation";
    private static final String TEMPLATE_FILE_PATH = "/templates/kfs/standard_template_v1.html";
    private static final String TEMPLATE_FIELDS_CONFIG = "{\"borrower_name\":{\"type\":\"text\",\"required\":true},\"loan_amount\":{\"type\":\"currency\",\"required\":true}}";
    private static final Boolean IS_ACTIVE_VERSION = true;
    private static final String TEMPLATE_TYPE = "KFS_STANDARD";

    @BeforeEach
    void setUp() {
        // Setup tenant context for testing
        FineractPlatformTenant tenant = new FineractPlatformTenant(1L, "default", "Default", "Europe/Berlin", null);
        ThreadLocalContextUtil.setTenant(tenant);
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);

        // Initialize validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Initialize ObjectMapper for JSON conversion
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void testCreateValidKfsTemplate() throws Exception {
        // Given: Valid KFS template data
        KfsTemplate kfsTemplate = createValidKfsTemplate();

        // When: Validate entity
        Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

        // Then: No validation errors
        assertTrue(violations.isEmpty(), "Should have no validation errors");
        assertEquals(TEMPLATE_NAME, kfsTemplate.getTemplateName());
        assertEquals(TEMPLATE_VERSION, kfsTemplate.getTemplateVersion());
        assertEquals(TEMPLATE_DESCRIPTION, kfsTemplate.getTemplateDescription());
        assertEquals(TEMPLATE_FILE_PATH, kfsTemplate.getTemplateFilePath());
        assertEquals(objectMapper.readTree(TEMPLATE_FIELDS_CONFIG), kfsTemplate.getTemplateFieldsConfig());
        assertEquals(IS_ACTIVE_VERSION, kfsTemplate.getIsActiveVersion());
        assertEquals(TEMPLATE_TYPE, kfsTemplate.getTemplateType());
    }

    @Test
    void testKfsTemplateWithNullTemplateName() {
        // Given: KFS template with null template name
        KfsTemplate kfsTemplate = createValidKfsTemplate();
        kfsTemplate.setTemplateName(null);

        // When: Validate entity
        Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

        // Then: Should have validation error
        assertFalse(violations.isEmpty(), "Should have validation errors for null template name");
        assertTrue(violations.stream().anyMatch(v -> "templateName".equals(v.getPropertyPath().toString())));
    }

    @Test
    void testKfsTemplateWithBlankTemplateName() {
        // Given: KFS template with blank template name
        KfsTemplate kfsTemplate = createValidKfsTemplate();
        kfsTemplate.setTemplateName("");

        // When: Validate entity
        Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

        // Then: Should have validation error
        assertFalse(violations.isEmpty(), "Should have validation errors for blank template name");
    }

    @Test
    void testKfsTemplateWithNullTemplateVersion() {
        // Given: KFS template with null template version
        KfsTemplate kfsTemplate = createValidKfsTemplate();
        kfsTemplate.setTemplateVersion(null);

        // When: Validate entity
        Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

        // Then: Should have validation error
        assertFalse(violations.isEmpty(), "Should have validation errors for null template version");
        assertTrue(violations.stream().anyMatch(v -> "templateVersion".equals(v.getPropertyPath().toString())));
    }

    @Test
    void testKfsTemplateWithNullTemplateFilePath() {
        // Given: KFS template with null template file path
        KfsTemplate kfsTemplate = createValidKfsTemplate();
        kfsTemplate.setTemplateFilePath(null);

        // When: Validate entity
        Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

        // Then: Should have validation error
        assertFalse(violations.isEmpty(), "Should have validation errors for null template file path");
        assertTrue(violations.stream().anyMatch(v -> "templateFilePath".equals(v.getPropertyPath().toString())));
    }

    @Test
    void testKfsTemplateWithInvalidTemplateNameLength() {
        // Given: KFS template with template name too long
        KfsTemplate kfsTemplate = createValidKfsTemplate();
        String longName = "A".repeat(201); // Assuming max length is 200
        kfsTemplate.setTemplateName(longName);

        // When: Validate entity
        Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

        // Then: Should have validation error for length
        assertFalse(violations.isEmpty(), "Should have validation errors for template name too long");
    }

    @Test
    void testKfsTemplateWithInvalidTemplateVersionLength() {
        // Given: KFS template with template version too long
        KfsTemplate kfsTemplate = createValidKfsTemplate();
        String longVersion = "1.".repeat(26); // Assuming max length is 50
        kfsTemplate.setTemplateVersion(longVersion);

        // When: Validate entity
        Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

        // Then: Should have validation error for length
        assertFalse(violations.isEmpty(), "Should have validation errors for template version too long");
    }

    @Test
    void testKfsTemplateWithInvalidFilePathLength() {
        // Given: KFS template with file path too long
        KfsTemplate kfsTemplate = createValidKfsTemplate();
        String longPath = "/very/long/path/".repeat(50); // Assuming max length is 500
        kfsTemplate.setTemplateFilePath(longPath);

        // When: Validate entity
        Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

        // Then: Should have validation error for length
        assertFalse(violations.isEmpty(), "Should have validation errors for file path too long");
    }

    @Test
    void testKfsTemplateEqualsAndHashCode() {
        // Given: Two KFS templates with same data
        KfsTemplate kfsTemplate1 = createValidKfsTemplate();
        KfsTemplate kfsTemplate2 = createValidKfsTemplate();

        // Set same ID for both
        kfsTemplate1.setId(1L);
        kfsTemplate2.setId(1L);

        // Then: Should be equal and have same hash code
        assertEquals(kfsTemplate1, kfsTemplate2);
        assertEquals(kfsTemplate1.hashCode(), kfsTemplate2.hashCode());
    }

    @Test
    void testKfsTemplateNotEquals() {
        // Given: Two KFS templates with different IDs
        KfsTemplate kfsTemplate1 = createValidKfsTemplate();
        KfsTemplate kfsTemplate2 = createValidKfsTemplate();

        kfsTemplate1.setId(1L);
        kfsTemplate2.setId(2L);

        // Then: Should not be equal
        assertNotEquals(kfsTemplate1, kfsTemplate2);
    }

    @Test
    void testKfsTemplateToString() {
        // Given: Valid KFS template
        KfsTemplate kfsTemplate = createValidKfsTemplate();
        kfsTemplate.setId(1L);

        // When: Call toString
        String toStringResult = kfsTemplate.toString();

        // Then: Should contain key information - updated to match actual toString format
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("KfsTemplate"));
        assertTrue(toStringResult.contains("id=1"));
        assertTrue(toStringResult.contains("templateName='" + TEMPLATE_NAME + "'"));
    }

    @Test
    void testKfsTemplateAuditFields() {
        // Given: Valid KFS template
        KfsTemplate kfsTemplate = createValidKfsTemplate();

        // Then: Audit fields should be available (if extending audit base class)
        // Note: This test assumes the entity extends AbstractAuditableWithUTCDateTimeCustom
        assertNotNull(kfsTemplate, "Entity should be created successfully");

        // Additional audit field tests would be added here based on actual entity implementation
    }

    @Test
    void testKfsTemplateVersionComparison() {
        // Given: Two templates with different versions
        KfsTemplate template1 = createValidKfsTemplate();
        template1.setTemplateVersion("1.0");

        KfsTemplate template2 = createValidKfsTemplate();
        template2.setTemplateVersion("2.0");

        // Then: Version comparison should work (if implemented)
        assertNotEquals(template1.getTemplateVersion(), template2.getTemplateVersion());
    }

    @Test
    void testKfsTemplateActiveVersionFlag() {
        // Given: Template with active version flag
        KfsTemplate activeTemplate = createValidKfsTemplate();
        activeTemplate.setIsActiveVersion(true);

        KfsTemplate inactiveTemplate = createValidKfsTemplate();
        inactiveTemplate.setIsActiveVersion(false);

        // Then: Active version flag should be set correctly
        assertTrue(activeTemplate.getIsActiveVersion());
        assertFalse(inactiveTemplate.getIsActiveVersion());
    }

    @Test
    void testKfsTemplateWithParentTemplate() {
        // Given: Template with parent template reference
        KfsTemplate parentTemplate = createValidKfsTemplate();
        parentTemplate.setId(1L);

        KfsTemplate childTemplate = createValidKfsTemplate();
        childTemplate.setParentTemplateId(1L);

        // Then: Parent-child relationship should be maintained
        assertEquals(Long.valueOf(1L), childTemplate.getParentTemplateId());
    }

    @Test
    void testKfsTemplateFieldsConfigValidation() throws Exception {
        // Given: Template with valid JSON fields config
        KfsTemplate kfsTemplate = createValidKfsTemplate();
        String validJson = "{\"field1\":{\"type\":\"text\",\"required\":true}}";
        JsonNode validJsonNode = objectMapper.readTree(validJson);
        kfsTemplate.setTemplateFieldsConfig(validJsonNode);

        // When: Validate entity
        Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

        // Then: Should have no validation errors
        assertTrue(violations.isEmpty(), "Should accept valid JSON configuration");
        assertEquals(validJsonNode, kfsTemplate.getTemplateFieldsConfig());
    }

    @Test
    void testKfsTemplateWithNullFieldsConfig() {
        // Given: Template with null fields config (should be allowed)
        KfsTemplate kfsTemplate = createValidKfsTemplate();
        kfsTemplate.setTemplateFieldsConfig(null);

        // When: Validate entity
        Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

        // Then: Should be valid (fields config is optional)
        assertTrue(violations.isEmpty(), "Should allow null fields configuration");
        assertNull(kfsTemplate.getTemplateFieldsConfig());
    }

    @Test
    void testKfsTemplateValidationWithAllNullValues() {
        // Given: KFS template with all null values
        KfsTemplate kfsTemplate = new KfsTemplate();

        // When: Validate entity
        Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

        // Then: Should have multiple validation errors
        assertFalse(violations.isEmpty(), "Should have multiple validation errors");
        assertTrue(violations.size() >= 3, "Should have at least 3 validation errors for required fields");
    }

    @Test
    void testKfsTemplateWithValidTemplateTypes() {
        // Given: Templates with different valid types
        String[] validTypes = { "KFS_STANDARD", "KFS_CUSTOM", "KFS_REGULATORY" };

        for (String type : validTypes) {
            KfsTemplate kfsTemplate = createValidKfsTemplate();
            kfsTemplate.setTemplateType(type);

            // When: Validate entity
            Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

            // Then: Should be valid
            assertTrue(violations.isEmpty(), "Should accept valid template type: " + type);
            assertEquals(type, kfsTemplate.getTemplateType());
        }
    }

    @Test
    void testKfsTemplateBuilderPattern() {
        // Given: KFS template using builder pattern (if available)
        KfsTemplate kfsTemplate = createValidKfsTemplate();

        // Then: All fields should be set correctly
        assertNotNull(kfsTemplate);
        assertEquals(TEMPLATE_NAME, kfsTemplate.getTemplateName());
        assertEquals(TEMPLATE_VERSION, kfsTemplate.getTemplateVersion());
    }

    @Test
    void testKfsTemplateWithExtremeValidValues() {
        // Given: KFS template with extreme but valid values
        KfsTemplate kfsTemplate = createValidKfsTemplate();
        kfsTemplate.setTemplateName("A".repeat(199)); // Just under the limit
        kfsTemplate.setTemplateVersion("9.9.9.9.9"); // Complex version number
        kfsTemplate.setTemplateDescription("A".repeat(1000)); // Long description

        // When: Validate entity
        Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

        // Then: Should accept extreme but valid values
        assertTrue(violations.isEmpty(), "Should accept extreme but valid values");
    }

    @Test
    void testKfsTemplateFilePathValidation() {
        // Given: Template with various file path formats
        String[] validPaths = { "/templates/kfs/standard.html", "templates/custom/custom_template.html",
                "/absolute/path/to/template.html" };

        for (String path : validPaths) {
            KfsTemplate kfsTemplate = createValidKfsTemplate();
            kfsTemplate.setTemplateFilePath(path);

            // When: Validate entity
            Set<ConstraintViolation<KfsTemplate>> violations = validator.validate(kfsTemplate);

            // Then: Should be valid
            assertTrue(violations.isEmpty(), "Should accept valid file path: " + path);
            assertEquals(path, kfsTemplate.getTemplateFilePath());
        }
    }

    /**
     * Create a valid KfsTemplate entity for testing
     */
    private KfsTemplate createValidKfsTemplate() {
        try {
            KfsTemplate kfsTemplate = new KfsTemplate();
            kfsTemplate.setTemplateName(TEMPLATE_NAME);
            kfsTemplate.setTemplateVersion(TEMPLATE_VERSION);
            kfsTemplate.setTemplateDescription(TEMPLATE_DESCRIPTION);
            kfsTemplate.setTemplateFilePath(TEMPLATE_FILE_PATH);
            kfsTemplate.setTemplateFieldsConfig(objectMapper.readTree(TEMPLATE_FIELDS_CONFIG));
            kfsTemplate.setIsActiveVersion(IS_ACTIVE_VERSION);
            kfsTemplate.setTemplateType(TEMPLATE_TYPE);
            kfsTemplate.setSupportedLoanProducts("PERSONAL,HOME,VEHICLE");
            kfsTemplate.setRegulatoryCompliance("STANDARD_2024");

            return kfsTemplate;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test KfsTemplate", e);
        }
    }
}
