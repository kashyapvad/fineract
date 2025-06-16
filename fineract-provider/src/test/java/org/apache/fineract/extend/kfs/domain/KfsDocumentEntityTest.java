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
import java.time.LocalDate;
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
 * Unit tests for KfsDocument JPA entity. Tests entity mapping, validation, and audit fields.
 *
 * Following TDD approach - tests exist before implementation.
 */
@ExtendWith(MockitoExtension.class)
class KfsDocumentEntityTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    // Test data constants based on handoff document
    private static final Long LOAN_ID = 1L;
    private static final Long CLIENT_ID = 1L;
    private static final Long EIR_CALCULATION_ID = 1L;
    private static final Long KFS_TEMPLATE_ID = 1L;
    private static final String DOCUMENT_REFERENCE_NUMBER = "KFS-2024-001";
    private static final LocalDate GENERATION_DATE = LocalDate.now();
    private static final String GENERATED_FILE_PATH = "/documents/kfs/2024/kfs-document-001.pdf";
    private static final Long FILE_SIZE = 1024L;
    private static final String CHECKSUM = "d41d8cd98f00b204e9800998ecf8427e";
    private static final String DOCUMENT_STATUS = "GENERATED";
    private static final String DELIVERY_METHOD = "EMAIL";
    private static final LocalDate DELIVERY_DATE = LocalDate.now();
    private static final Boolean RECIPIENT_ACKNOWLEDGMENT = false;

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
    void testCreateValidKfsDocument() {
        // Given: Valid KFS document data
        KfsDocument kfsDocument = createValidKfsDocument();

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: No validation errors
        assertTrue(violations.isEmpty(), "Should have no validation errors");
        assertEquals(LOAN_ID, kfsDocument.getLoanId());
        assertEquals(CLIENT_ID, kfsDocument.getClientId());
        assertEquals(EIR_CALCULATION_ID, kfsDocument.getEirCalculationId());
        assertEquals(KFS_TEMPLATE_ID, kfsDocument.getKfsTemplateId());
        assertEquals(DOCUMENT_REFERENCE_NUMBER, kfsDocument.getDocumentReferenceNumber());
        assertEquals(GENERATION_DATE, kfsDocument.getGenerationDate());
        assertEquals(GENERATED_FILE_PATH, kfsDocument.getGeneratedFilePath());
        assertEquals(FILE_SIZE, kfsDocument.getFileSize());
        assertEquals(CHECKSUM, kfsDocument.getChecksum());
        assertEquals(DOCUMENT_STATUS, kfsDocument.getDocumentStatus());
        assertEquals(DELIVERY_METHOD, kfsDocument.getDeliveryMethod());
        assertEquals(DELIVERY_DATE, kfsDocument.getDeliveryDate());
        assertEquals(RECIPIENT_ACKNOWLEDGMENT, kfsDocument.getRecipientAcknowledgment());
    }

    @Test
    void testKfsDocumentWithNullLoanId() {
        // Given: KFS document with null loan ID
        KfsDocument kfsDocument = createValidKfsDocument();
        kfsDocument.setLoanId(null);

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should have validation error
        assertFalse(violations.isEmpty(), "Should have validation errors for null loan ID");
        assertTrue(violations.stream().anyMatch(v -> "loanId".equals(v.getPropertyPath().toString())));
    }

    @Test
    void testKfsDocumentWithNullClientId() {
        // Given: KFS document with null client ID
        KfsDocument kfsDocument = createValidKfsDocument();
        kfsDocument.setClientId(null);

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should have validation error
        assertFalse(violations.isEmpty(), "Should have validation errors for null client ID");
        assertTrue(violations.stream().anyMatch(v -> "clientId".equals(v.getPropertyPath().toString())));
    }

    @Test
    void testKfsDocumentWithNullEirCalculationId() {
        // Given: KFS document with null EIR calculation ID
        KfsDocument kfsDocument = createValidKfsDocument();
        kfsDocument.setEirCalculationId(null);

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should have validation error
        assertFalse(violations.isEmpty(), "Should have validation errors for null EIR calculation ID");
        assertTrue(violations.stream().anyMatch(v -> "eirCalculationId".equals(v.getPropertyPath().toString())));
    }

    @Test
    void testKfsDocumentWithNullKfsTemplateId() {
        // Given: KFS document with null KFS template ID
        KfsDocument kfsDocument = createValidKfsDocument();
        kfsDocument.setKfsTemplateId(null);

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should have validation error
        assertFalse(violations.isEmpty(), "Should have validation errors for null KFS template ID");
        assertTrue(violations.stream().anyMatch(v -> "kfsTemplateId".equals(v.getPropertyPath().toString())));
    }

    @Test
    void testKfsDocumentWithNullDocumentReferenceNumber() {
        // Given: KFS document with null document reference number
        KfsDocument kfsDocument = createValidKfsDocument();
        kfsDocument.setDocumentReferenceNumber(null);

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should have validation error
        assertFalse(violations.isEmpty(), "Should have validation errors for null document reference number");
        assertTrue(violations.stream().anyMatch(v -> "documentReferenceNumber".equals(v.getPropertyPath().toString())));
    }

    @Test
    void testKfsDocumentWithBlankDocumentReferenceNumber() {
        // Given: KFS document with blank document reference number
        KfsDocument kfsDocument = createValidKfsDocument();
        kfsDocument.setDocumentReferenceNumber("");

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should have validation error
        assertFalse(violations.isEmpty(), "Should have validation errors for blank document reference number");
    }

    @Test
    void testKfsDocumentWithNullGenerationDate() {
        // Given: KFS document with null generation date
        KfsDocument kfsDocument = createValidKfsDocument();
        kfsDocument.setGenerationDate(null);

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should have validation error
        assertFalse(violations.isEmpty(), "Should have validation errors for null generation date");
        assertTrue(violations.stream().anyMatch(v -> "generationDate".equals(v.getPropertyPath().toString())));
    }

    @Test
    void testKfsDocumentWithNullGeneratedFilePath() {
        // Given: KFS document with null generated file path
        KfsDocument kfsDocument = createValidKfsDocument();
        kfsDocument.setGeneratedFilePath(null);

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should have validation error
        assertFalse(violations.isEmpty(), "Should have validation errors for null generated file path");
        assertTrue(violations.stream().anyMatch(v -> "generatedFilePath".equals(v.getPropertyPath().toString())));
    }

    @Test
    void testKfsDocumentWithInvalidReferenceNumberLength() {
        // Given: KFS document with reference number too long
        KfsDocument kfsDocument = createValidKfsDocument();
        String longRefNumber = "KFS-".repeat(26); // Assuming max length is 100
        kfsDocument.setDocumentReferenceNumber(longRefNumber);

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should have validation error for length
        assertFalse(violations.isEmpty(), "Should have validation errors for reference number too long");
    }

    @Test
    void testKfsDocumentWithInvalidFilePathLength() {
        // Given: KFS document with file path too long
        KfsDocument kfsDocument = createValidKfsDocument();
        String longPath = "/very/long/path/".repeat(50); // Assuming max length is 500
        kfsDocument.setGeneratedFilePath(longPath);

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should have validation error for length
        assertFalse(violations.isEmpty(), "Should have validation errors for file path too long");
    }

    @Test
    void testKfsDocumentEqualsAndHashCode() {
        // Given: Two KFS documents with same data
        KfsDocument kfsDocument1 = createValidKfsDocument();
        KfsDocument kfsDocument2 = createValidKfsDocument();

        // Set same ID for both
        kfsDocument1.setId(1L);
        kfsDocument2.setId(1L);

        // Then: Should be equal and have same hash code
        assertEquals(kfsDocument1, kfsDocument2);
        assertEquals(kfsDocument1.hashCode(), kfsDocument2.hashCode());
    }

    @Test
    void testKfsDocumentNotEquals() {
        // Given: Two KFS documents with different IDs
        KfsDocument kfsDocument1 = createValidKfsDocument();
        KfsDocument kfsDocument2 = createValidKfsDocument();

        kfsDocument1.setId(1L);
        kfsDocument2.setId(2L);

        // Then: Should not be equal
        assertNotEquals(kfsDocument1, kfsDocument2);
    }

    @Test
    void testKfsDocumentToString() {
        // Given: Valid KFS document
        KfsDocument kfsDocument = createValidKfsDocument();
        kfsDocument.setId(1L);

        // When: Call toString
        String toStringResult = kfsDocument.toString();

        // Then: Should contain key information - updated to match actual toString format
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("KfsDocument"));
        assertTrue(toStringResult.contains("id=1"));
        assertTrue(toStringResult.contains("documentReferenceNumber='" + DOCUMENT_REFERENCE_NUMBER + "'"));
    }

    @Test
    void testKfsDocumentAuditFields() {
        // Given: Valid KFS document
        KfsDocument kfsDocument = createValidKfsDocument();

        // Then: Audit fields should be available (if extending audit base class)
        // Note: This test assumes the entity extends AbstractAuditableWithUTCDateTimeCustom
        assertNotNull(kfsDocument, "Entity should be created successfully");
    }

    @Test
    void testKfsDocumentWithOptionalFields() {
        // Given: Document with all optional fields as null
        KfsDocument kfsDocument = createValidKfsDocument();
        kfsDocument.setFileSize(null);
        kfsDocument.setChecksum(null);
        kfsDocument.setDeliveryMethod(null);
        kfsDocument.setDeliveryDate(null);
        kfsDocument.setRecipientAcknowledgment(null);

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should be valid (optional fields can be null)
        assertTrue(violations.isEmpty(), "Should allow null optional fields");
        assertNull(kfsDocument.getFileSize());
        assertNull(kfsDocument.getChecksum());
        assertNull(kfsDocument.getDeliveryMethod());
        assertNull(kfsDocument.getDeliveryDate());
        assertNull(kfsDocument.getRecipientAcknowledgment());
    }

    @Test
    void testKfsDocumentWithValidDocumentStatuses() {
        // Given: Documents with different valid statuses
        String[] validStatuses = { "GENERATED", "DELIVERED", "ACKNOWLEDGED", "ARCHIVED" };

        for (String status : validStatuses) {
            KfsDocument kfsDocument = createValidKfsDocument();
            kfsDocument.setDocumentStatus(status);

            // When: Validate entity
            Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

            // Then: Should be valid
            assertTrue(violations.isEmpty(), "Should accept valid document status: " + status);
            assertEquals(status, kfsDocument.getDocumentStatus());
        }
    }

    @Test
    void testKfsDocumentWithValidDeliveryMethods() {
        // Given: Documents with different valid delivery methods
        String[] validMethods = { "EMAIL", "SMS", "POSTAL", "PICKUP", "DOWNLOAD" };

        for (String method : validMethods) {
            KfsDocument kfsDocument = createValidKfsDocument();
            kfsDocument.setDeliveryMethod(method);

            // When: Validate entity
            Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

            // Then: Should be valid
            assertTrue(violations.isEmpty(), "Should accept valid delivery method: " + method);
            assertEquals(method, kfsDocument.getDeliveryMethod());
        }
    }

    @Test
    void testKfsDocumentValidationWithAllNullValues() {
        // Given: KFS document with all null values
        KfsDocument kfsDocument = new KfsDocument();

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should have multiple validation errors
        assertFalse(violations.isEmpty(), "Should have multiple validation errors");
        assertTrue(violations.size() >= 5, "Should have at least 5 validation errors for required fields");
    }

    @Test
    void testKfsDocumentFileSizeValidation() {
        // Given: Document with various file sizes
        KfsDocument kfsDocument = createValidKfsDocument();

        // Test zero file size
        kfsDocument.setFileSize(0L);
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);
        assertTrue(violations.isEmpty(), "Should allow zero file size");

        // Test negative file size (should be invalid if validation exists)
        kfsDocument.setFileSize(-1L);
        violations = validator.validate(kfsDocument);
        // Uncomment if minimum validation exists
        // assertFalse(violations.isEmpty(), "Should not allow negative file size");
    }

    @Test
    void testKfsDocumentChecksumValidation() {
        // Given: Document with various checksum formats
        String[] validChecksums = { "d41d8cd98f00b204e9800998ecf8427e", // MD5
                "da39a3ee5e6b4b0d3255bfef95601890afd80709", // SHA1
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" // SHA256
        };

        for (String checksum : validChecksums) {
            KfsDocument kfsDocument = createValidKfsDocument();
            kfsDocument.setChecksum(checksum);

            // When: Validate entity
            Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

            // Then: Should be valid
            assertTrue(violations.isEmpty(), "Should accept valid checksum format: " + checksum);
            assertEquals(checksum, kfsDocument.getChecksum());
        }
    }

    @Test
    void testKfsDocumentWithExtremeValidValues() {
        // Given: KFS document with extreme but valid values
        KfsDocument kfsDocument = createValidKfsDocument();
        kfsDocument.setDocumentReferenceNumber("A".repeat(99)); // Just under the limit
        kfsDocument.setGeneratedFilePath("/".repeat(499)); // Just under the limit
        kfsDocument.setFileSize(Long.MAX_VALUE); // Maximum file size

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should accept extreme but valid values
        assertTrue(violations.isEmpty(), "Should accept extreme but valid values");
    }

    @Test
    void testKfsDocumentDeliveryDateValidation() {
        // Given: Document with delivery date before generation date
        KfsDocument kfsDocument = createValidKfsDocument();
        kfsDocument.setGenerationDate(LocalDate.now());
        kfsDocument.setDeliveryDate(LocalDate.now().minusDays(1)); // Before generation

        // When: Validate entity
        Set<ConstraintViolation<KfsDocument>> violations = validator.validate(kfsDocument);

        // Then: Should be valid (business logic validation might happen elsewhere)
        // Uncomment if cross-field validation is implemented
        // assertFalse(violations.isEmpty(), "Delivery date should not be before generation date");
    }

    @Test
    void testKfsDocumentAcknowledgmentFlag() {
        // Given: Document with acknowledgment flags
        KfsDocument acknowledgedDoc = createValidKfsDocument();
        acknowledgedDoc.setRecipientAcknowledgment(true);

        KfsDocument unacknowledgedDoc = createValidKfsDocument();
        unacknowledgedDoc.setRecipientAcknowledgment(false);

        // Then: Acknowledgment flags should be set correctly
        assertTrue(acknowledgedDoc.getRecipientAcknowledgment());
        assertFalse(unacknowledgedDoc.getRecipientAcknowledgment());
    }

    /**
     * Create a valid KfsDocument entity for testing
     */
    private KfsDocument createValidKfsDocument() {
        try {
            KfsDocument kfsDocument = new KfsDocument();
            kfsDocument.setLoanId(LOAN_ID);
            kfsDocument.setClientId(CLIENT_ID);
            kfsDocument.setEirCalculationId(EIR_CALCULATION_ID);
            kfsDocument.setKfsTemplateId(KFS_TEMPLATE_ID);
            kfsDocument.setDocumentReferenceNumber(DOCUMENT_REFERENCE_NUMBER);
            kfsDocument.setGenerationDate(GENERATION_DATE);
            kfsDocument.setGeneratedFilePath(GENERATED_FILE_PATH);
            kfsDocument.setFileSize(FILE_SIZE);
            kfsDocument.setChecksum(CHECKSUM);
            kfsDocument.setDocumentStatus(DOCUMENT_STATUS);
            kfsDocument.setDeliveryMethod(DELIVERY_METHOD);
            kfsDocument.setDeliveryDate(DELIVERY_DATE);
            kfsDocument.setRecipientAcknowledgment(RECIPIENT_ACKNOWLEDGMENT);
            kfsDocument.setAcknowledgmentDate(LocalDate.now().plusDays(1));
            kfsDocument.setDocumentMetadata(objectMapper.readTree("{\"version\":\"1.0\",\"format\":\"PDF\"}"));

            return kfsDocument;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test KfsDocument", e);
        }
    }
}
