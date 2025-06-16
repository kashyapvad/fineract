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
package org.apache.fineract.extend.kfs.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationRequest;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationResponse;
import org.apache.fineract.extend.kfs.service.KfsDocumentGenerationService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TDD Tests for KfsDocumentGenerationApiResource - RED phase These tests define the API contract for KFS document
 * generation
 */
@ExtendWith(MockitoExtension.class)
class KfsDocumentGenerationApiResourceTest {

    @Mock
    private KfsDocumentGenerationService kfsDocumentGenerationService;

    @Mock
    private PlatformSecurityContext context;

    private KfsDocumentGenerationApiResource apiResource;

    @BeforeEach
    void setUp() {
        // Will be injected by coder agent when implementing
        // apiResource = new KfsDocumentGenerationApiResource(services...);
    }

    @Test
    void testGenerateKfsDocument_ServiceContract() {
        // RED: Contract for generating KFS documents

        // GIVEN: Valid generation request
        KfsDocumentGenerationRequest request = createTestGenerationRequest();
        KfsDocumentGenerationResponse response = createTestGenerationResponse();
        when(kfsDocumentGenerationService.generateKfsDocument(request)).thenReturn(response);

        // WHEN: Service is called
        KfsDocumentGenerationResponse actualResponse = kfsDocumentGenerationService.generateKfsDocument(request);

        // THEN: Should return generated document info
        assertNotNull(actualResponse);
        assertEquals("KFS-2024-001", actualResponse.getDocumentReferenceNumber());
        assertEquals("GENERATED", actualResponse.getGenerationStatus());
    }

    @Test
    void testBulkGenerateKfsDocuments_ServiceContract() {
        // RED: Contract for bulk document generation

        // GIVEN: Multiple generation requests
        List<KfsDocumentGenerationRequest> requests = Arrays.asList(createTestGenerationRequest(), createTestGenerationRequest());
        List<KfsDocumentGenerationResponse> responses = Arrays.asList(createTestGenerationResponse(), createTestGenerationResponse());
        when(kfsDocumentGenerationService.bulkGenerateKfsDocuments(requests)).thenReturn(responses);

        // WHEN: Service is called
        List<KfsDocumentGenerationResponse> actualResponses = kfsDocumentGenerationService.bulkGenerateKfsDocuments(requests);

        // THEN: Should return list of generated documents
        assertNotNull(actualResponses);
        assertEquals(2, actualResponses.size());
    }

    @Test
    void testGetGenerationStatus_ServiceContract() {
        // RED: Contract for checking generation status

        // GIVEN: Generation job exists
        String jobId = "job-123";
        KfsDocumentGenerationResponse status = createTestGenerationResponse();
        when(kfsDocumentGenerationService.getGenerationStatus(jobId)).thenReturn(status);

        // WHEN: Service is called
        KfsDocumentGenerationResponse actualStatus = kfsDocumentGenerationService.getGenerationStatus(jobId);

        // THEN: Should return status info
        assertNotNull(actualStatus);
        assertEquals("GENERATED", actualStatus.getGenerationStatus());
    }

    // Helper methods for test data
    private KfsDocumentGenerationRequest createTestGenerationRequest() {
        // Use builder pattern from actual DTO
        return KfsDocumentGenerationRequest.builder().loanId(1L).templateId(1L).deliveryMethod("EMAIL").build();
    }

    private KfsDocumentGenerationResponse createTestGenerationResponse() {
        // Use builder pattern from actual DTO
        return KfsDocumentGenerationResponse.builder().documentId(1L).documentReferenceNumber("KFS-2024-001").generationStatus("GENERATED")
                .generatedFilePath("/documents/kfs/2024/kfs-001.pdf").build();
    }
}
