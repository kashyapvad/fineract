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

import org.apache.fineract.extend.kfs.dto.KfsDocumentRequest;
import org.apache.fineract.extend.kfs.dto.KfsDocumentResponse;
import org.apache.fineract.extend.kfs.service.KfsDocumentReadPlatformService;
import org.apache.fineract.extend.kfs.service.KfsDocumentWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TDD Tests for KfsDocumentApiResource - RED phase These tests define the API contract that the coder agent will
 * implement
 */
@ExtendWith(MockitoExtension.class)
class KfsDocumentApiResourceTest {

    @Mock
    private KfsDocumentWritePlatformService kfsDocumentWritePlatformService;

    @Mock
    private KfsDocumentReadPlatformService kfsDocumentReadPlatformService;

    @Mock
    private PlatformSecurityContext context;

    private KfsDocumentApiResource apiResource;

    @BeforeEach
    void setUp() {
        // Will be injected by coder agent when implementing
        // apiResource = new KfsDocumentApiResource(services...);
    }

    @Test
    void testCreateKfsDocument_ServiceContract() {
        // RED: This test defines the service contract for document creation

        // GIVEN: Valid KFS document request
        KfsDocumentRequest request = createTestRequest();
        CommandProcessingResult result = CommandProcessingResult.resourceResult(1L);
        when(kfsDocumentWritePlatformService.createKfsDocument(request)).thenReturn(result);

        // WHEN: Service is called
        CommandProcessingResult actualResult = kfsDocumentWritePlatformService.createKfsDocument(request);

        // THEN: Should return correct resource ID
        assertEquals(1L, actualResult.getResourceId());
    }

    @Test
    void testGetKfsDocument_ServiceContract() {
        // RED: Contract test for document retrieval

        // GIVEN: Document exists
        Long documentId = 1L;
        KfsDocumentResponse response = createTestResponse();
        when(kfsDocumentReadPlatformService.retrieveKfsDocument(documentId)).thenReturn(response);

        // WHEN: Service is called
        KfsDocumentResponse actualResponse = kfsDocumentReadPlatformService.retrieveKfsDocument(documentId);

        // THEN: Should return document with correct data
        assertNotNull(actualResponse);
        assertEquals("KFS-2024-001", actualResponse.getDocumentReferenceNumber());
        assertEquals(1L, actualResponse.getDocumentId());
    }

    @Test
    void testGetKfsDocument_NotFound_ServiceContract() {
        // RED: Contract test for not found scenario

        // GIVEN: Document doesn't exist
        Long documentId = 999L;
        when(kfsDocumentReadPlatformService.retrieveKfsDocument(documentId)).thenReturn(null);

        // WHEN: Service is called
        KfsDocumentResponse result = kfsDocumentReadPlatformService.retrieveKfsDocument(documentId);

        // THEN: Should return null
        assertNull(result);
    }

    // Helper methods for test data
    private KfsDocumentRequest createTestRequest() {
        // Stub implementation - coder will create proper DTO
        return new KfsDocumentRequest() {

            {
                setLoanId(1L);
                setClientId(1L);
                setKfsTemplateId(1L);
                setDocumentReferenceNumber("KFS-2024-001");
            }
        };
    }

    private KfsDocumentResponse createTestResponse() {
        // Stub implementation - coder will create proper DTO
        return new KfsDocumentResponse() {

            {
                setDocumentId(1L);
                setDocumentReferenceNumber("KFS-2024-001");
                setLoanId(1L);
                setDocumentStatus("GENERATED");
            }
        };
    }
}
