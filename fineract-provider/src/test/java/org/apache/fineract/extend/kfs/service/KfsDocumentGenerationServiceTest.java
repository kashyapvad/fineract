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
package org.apache.fineract.extend.kfs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.extend.kfs.domain.KfsDocument;
import org.apache.fineract.extend.kfs.domain.KfsDocumentRepository;
import org.apache.fineract.extend.kfs.domain.KfsTemplate;
import org.apache.fineract.extend.kfs.domain.KfsTemplateRepository;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationRequest;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationResponse;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationResult;
import org.apache.fineract.extend.kfs.dto.RepaymentScheduleData;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for KfsDocumentGenerationService. Tests template loading, data transformation, and document generation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KfsDocumentGenerationServiceTest {

    @InjectMocks
    private KfsDocumentGenerationServiceImpl kfsDocumentGenerationService;

    @Mock
    private KfsTemplateRepository kfsTemplateRepository;

    @Mock
    private KfsDocumentRepository kfsDocumentRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private PlatformSecurityContext context;

    @Mock
    private KfsDocumentWritePlatformService kfsDocumentWritePlatformService;

    @Mock
    private KfsTemplate mockTemplate;

    @Mock
    private KfsDocument mockDocument;

    @Mock
    private Loan mockLoan;

    @Mock
    private AppUser mockUser;

    @Mock
    private KfsDocx4jGenerationService docx4jGenerationService;

    // Test data constants based on handoff document
    private static final Long LOAN_ID = 1L;
    private static final String CUSTOMER_NAME = "Rumi Thakuria";
    private static final String CUSTOMER_MOBILE = "9854089853";
    private static final String CUSTOMER_EMAIL = "rumithakuriags@gmail.com";
    private static final BigDecimal PRINCIPAL_AMOUNT = new BigDecimal("100000.00");
    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("94982.00");
    private static final BigDecimal CHARGES_AT_DISBURSEMENT = new BigDecimal("5018.00");
    private static final BigDecimal EMI_AMOUNT = new BigDecimal("5000.00");
    private static final BigDecimal FINAL_EMI_AMOUNT = new BigDecimal("1082.00");
    private static final Integer TENURE_MONTHS = 24;
    private static final String TEMPLATE_NAME = "KFS_STANDARD_TEMPLATE";
    private static final String TEMPLATE_VERSION = "1.0";

    @BeforeEach
    void setUp() {
        // Setup tenant context for testing
        FineractPlatformTenant tenant = new FineractPlatformTenant(1L, "default", "Default", "Europe/Berlin", null);
        ThreadLocalContextUtil.setTenant(tenant);
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);

        // Setup common mock behavior - using lenient() to avoid unnecessary stubbing errors
        lenient().when(context.authenticatedUser()).thenReturn(mockUser);
        lenient().when(mockUser.getId()).thenReturn(1L);
        lenient().when(mockUser.getUsername()).thenReturn("testuser");
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void testGenerateKfsDocument_SuccessfulGeneration() {
        // Given: Valid loan and template
        KfsDocumentGenerationRequest request = createGenerationRequest();
        List<RepaymentScheduleData> scheduleData = createRepaymentScheduleData();

        // Mock the docx4j service response
        KfsDocumentGenerationResult expectedResult = new KfsDocumentGenerationResult();
        expectedResult.setStatus("SUCCESS");
        expectedResult.setDocumentId(1L);
        expectedResult.setMessage("KFS document generated successfully");

        when(docx4jGenerationService.generateKfsDocument(request)).thenReturn(expectedResult);

        // When: Generate KFS document
        KfsDocumentGenerationResult result = kfsDocumentGenerationService.generateKfsDocument(request, scheduleData);

        // Then: Should successfully generate document
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(1L, result.getDocumentId());

        verify(docx4jGenerationService).generateKfsDocument(request);
    }

    @Test
    void testGenerateKfsDocument_LoanNotFound() {
        // Given: Non-existent loan ID
        KfsDocumentGenerationRequest request = createGenerationRequest();
        List<RepaymentScheduleData> scheduleData = createRepaymentScheduleData();

        // Mock the docx4j service to return failure
        KfsDocumentGenerationResult expectedResult = new KfsDocumentGenerationResult();
        expectedResult.setStatus("FAILED");
        expectedResult.setMessage("Loan with ID 1 not found");

        when(docx4jGenerationService.generateKfsDocument(request)).thenReturn(expectedResult);

        // When: Generate KFS document
        KfsDocumentGenerationResult result = kfsDocumentGenerationService.generateKfsDocument(request, scheduleData);

        // Then: Should return failed result
        assertNotNull(result);
        assertEquals("FAILED", result.getStatus());
        assertTrue(result.getMessage().contains("Loan with ID 1 not found"));

        verify(docx4jGenerationService).generateKfsDocument(request);
    }

    @Test
    void testGenerateKfsDocument_TemplateNotFound() {
        // Given: Valid loan but non-existent template ID
        KfsDocumentGenerationRequest request = createGenerationRequest();
        List<RepaymentScheduleData> scheduleData = createRepaymentScheduleData();

        // Mock the docx4j service to return failure
        KfsDocumentGenerationResult expectedResult = new KfsDocumentGenerationResult();
        expectedResult.setStatus("FAILED");
        expectedResult.setMessage("Template with ID 1 not found");

        when(docx4jGenerationService.generateKfsDocument(request)).thenReturn(expectedResult);

        // When: Generate KFS document
        KfsDocumentGenerationResult result = kfsDocumentGenerationService.generateKfsDocument(request, scheduleData);

        // Then: Should return failed result
        assertNotNull(result);
        assertEquals("FAILED", result.getStatus());
        assertTrue(result.getMessage().contains("Template"));

        verify(docx4jGenerationService).generateKfsDocument(request);
    }

    @Test
    void testGenerateKfsDocument_TemplateNotActive() {
        // Given: Valid loan and template exists but template is INACTIVE (MISSING test case!)
        KfsDocumentGenerationRequest request = createGenerationRequest();
        List<RepaymentScheduleData> scheduleData = createRepaymentScheduleData();

        when(loanRepository.existsById(LOAN_ID)).thenReturn(true);
        when(kfsTemplateRepository.findById(request.getTemplateId())).thenReturn(Optional.of(mockTemplate));
        when(mockTemplate.getIsActiveVersion()).thenReturn(false); // Template exists but INACTIVE

        // When: Generate KFS document
        KfsDocumentGenerationResult result = kfsDocumentGenerationService.generateKfsDocument(request, scheduleData);

        // Then: Should return failed result with "not active" message
        assertNotNull(result);
        assertEquals("FAILED", result.getStatus());
        assertTrue(result.getMessage().contains("is not active"));

        verify(loanRepository).existsById(LOAN_ID);
        verify(kfsTemplateRepository).findById(request.getTemplateId());
        verify(mockTemplate).getIsActiveVersion();
        // Should not proceed to document creation if template is inactive
        verifyNoInteractions(kfsDocumentWritePlatformService);
    }

    @Test
    void testGenerateKfsDocument_EmptyRepaymentSchedule() {
        // Given: Empty repayment schedule data
        KfsDocumentGenerationRequest request = createGenerationRequest();
        List<RepaymentScheduleData> emptyScheduleData = new ArrayList<>();

        // Mock the docx4j service response for empty schedule
        KfsDocumentGenerationResult expectedResult = new KfsDocumentGenerationResult();
        expectedResult.setStatus("SUCCESS");
        expectedResult.setDocumentId(1L);
        expectedResult.setMessage("KFS document generated successfully");

        when(docx4jGenerationService.generateKfsDocument(request)).thenReturn(expectedResult);

        // When: Generate KFS document with empty schedule
        KfsDocumentGenerationResult result = kfsDocumentGenerationService.generateKfsDocument(request, emptyScheduleData);

        // Then: Should handle empty schedule gracefully
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());

        verify(docx4jGenerationService).generateKfsDocument(request);
    }

    @Test
    void testBulkDocumentGeneration_MultipleLoans() {
        // Given: Multiple loan IDs for bulk generation
        List<Long> loanIds = List.of(1L, 2L, 3L);
        KfsDocumentGenerationRequest baseRequest = createGenerationRequest();

        // Mock responses for each loan
        KfsDocumentGenerationResponse response1 = new KfsDocumentGenerationResponse();
        response1.setGenerationStatus("SUCCESS");
        response1.setLoanId(1L);
        response1.setSuccess(true);

        KfsDocumentGenerationResponse response2 = new KfsDocumentGenerationResponse();
        response2.setGenerationStatus("SUCCESS");
        response2.setLoanId(2L);
        response2.setSuccess(true);

        KfsDocumentGenerationResponse response3 = new KfsDocumentGenerationResponse();
        response3.setGenerationStatus("SUCCESS");
        response3.setLoanId(3L);
        response3.setSuccess(true);

        when(docx4jGenerationService.generateKfsDocument(any(KfsDocumentGenerationRequest.class))).thenReturn(createSuccessResult(1L))
                .thenReturn(createSuccessResult(2L)).thenReturn(createSuccessResult(3L));

        // When: Generate documents using bulk method
        List<KfsDocumentGenerationRequest> requests = loanIds.stream().map(loanId -> {
            KfsDocumentGenerationRequest req = new KfsDocumentGenerationRequest();
            req.setLoanId(loanId);
            req.setTemplateId(baseRequest.getTemplateId());
            req.setDeliveryMethod(baseRequest.getDeliveryMethod());
            req.setPreview(baseRequest.getPreview());
            return req;
        }).toList();

        List<KfsDocumentGenerationResponse> results = kfsDocumentGenerationService.bulkGenerateKfsDocuments(requests);

        // Then: Should generate all documents
        assertNotNull(results);
        assertEquals(3, results.size());

        verify(docx4jGenerationService, times(3)).generateKfsDocument(any(KfsDocumentGenerationRequest.class));
    }

    private KfsDocumentGenerationResult createSuccessResult(Long loanId) {
        KfsDocumentGenerationResult result = new KfsDocumentGenerationResult();
        result.setStatus("SUCCESS");
        result.setDocumentId(loanId);
        result.setMessage("KFS document generated successfully");
        return result;
    }

    @Test
    void testPerformanceTest_LargeRepaymentSchedule() {
        // Given: Large repayment schedule (360 months)
        List<RepaymentScheduleData> largeSchedule = createLargeRepaymentSchedule(360);
        KfsDocumentGenerationRequest request = createGenerationRequest();

        // Mock the docx4j service response
        KfsDocumentGenerationResult expectedResult = new KfsDocumentGenerationResult();
        expectedResult.setStatus("SUCCESS");
        expectedResult.setDocumentId(1L);
        expectedResult.setMessage("KFS document generated successfully");

        when(docx4jGenerationService.generateKfsDocument(request)).thenReturn(expectedResult);

        // When: Generate document with large schedule
        long startTime = System.currentTimeMillis();
        KfsDocumentGenerationResult result = kfsDocumentGenerationService.generateKfsDocument(request, largeSchedule);
        long endTime = System.currentTimeMillis();

        // Then: Should complete within reasonable time (less than 5 seconds)
        long processingTime = endTime - startTime;
        assertTrue(processingTime < 5000, "Processing should complete within 5 seconds");

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());

        verify(docx4jGenerationService).generateKfsDocument(request);
    }

    // Helper methods for test data creation

    private KfsDocumentGenerationRequest createGenerationRequest() {
        KfsDocumentGenerationRequest request = new KfsDocumentGenerationRequest();
        request.setLoanId(LOAN_ID);
        request.setTemplateId(1L);
        request.setDeliveryMethod("DOWNLOAD");
        request.setPreview(false);
        return request;
    }

    private List<RepaymentScheduleData> createRepaymentScheduleData() {
        List<RepaymentScheduleData> schedule = new ArrayList<>();
        BigDecimal outstanding = new BigDecimal("96438.00");

        // Create 23 regular installments
        for (int i = 1; i <= 23; i++) {
            schedule.add(RepaymentScheduleData.builder().installmentNumber(i).dueDate(LocalDate.now().plusMonths(i)).totalAmount(EMI_AMOUNT)
                    .outstandingBalance(outstanding).formattedEmiAmount("5,000.00").formattedOutstandingBalance(formatAmount(outstanding))
                    .build());
            outstanding = outstanding.subtract(new BigDecimal("3811.00")); // Approximate principal reduction
        }

        // Create final installment
        schedule.add(
                RepaymentScheduleData.builder().installmentNumber(24).dueDate(LocalDate.now().plusMonths(24)).totalAmount(FINAL_EMI_AMOUNT)
                        .outstandingBalance(BigDecimal.ZERO).formattedEmiAmount("1,082.00").formattedOutstandingBalance("0.00").build());

        return schedule;
    }

    private List<RepaymentScheduleData> createLargeRepaymentSchedule(int months) {
        List<RepaymentScheduleData> schedule = new ArrayList<>();
        for (int i = 1; i <= months; i++) {
            schedule.add(RepaymentScheduleData.builder().installmentNumber(i).dueDate(LocalDate.now().plusMonths(i)).totalAmount(EMI_AMOUNT)
                    .outstandingBalance(new BigDecimal("50000.00")).formattedEmiAmount("5,000.00").formattedOutstandingBalance("50,000.00")
                    .build());
        }
        return schedule;
    }

    private String formatAmount(BigDecimal amount) {
        return String.format("%,.2f", amount);
    }
}
