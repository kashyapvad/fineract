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
package org.apache.fineract.extend.loan.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.apache.fineract.extend.loan.dto.EirCalculationRequest;
import org.apache.fineract.extend.loan.dto.EirCalculationResponse;
import org.apache.fineract.extend.loan.service.EirCalculationReadPlatformService;
import org.apache.fineract.extend.loan.service.EirCalculationWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TDD Tests for EirCalculationApiResource - RED phase These tests define the complete API contract for EIR calculations
 *
 * This API doesn't exist yet - coder agent will implement based on these contracts
 */
@ExtendWith(MockitoExtension.class)
class EirCalculationApiResourceTest {

    @Mock
    private EirCalculationWritePlatformService eirCalculationWritePlatformService;

    @Mock
    private EirCalculationReadPlatformService eirCalculationReadPlatformService;

    @Mock
    private PlatformSecurityContext context;

    // This will be implemented by coder agent
    private EirCalculationApiResource apiResource;

    @BeforeEach
    void setUp() {
        // Coder agent will inject this:
        // apiResource = new EirCalculationApiResource(services...);
    }

    @Test
    void testCreateEirCalculation_ServiceContract() {
        // RED: Contract for creating EIR calculations

        // GIVEN: Valid EIR calculation request
        EirCalculationRequest request = createTestRequest();
        CommandProcessingResult result = CommandProcessingResult.resourceResult(1L);
        when(eirCalculationWritePlatformService.createEirCalculation(request)).thenReturn(result);

        // WHEN: Service is called
        CommandProcessingResult actualResult = eirCalculationWritePlatformService.createEirCalculation(request);

        // THEN: Should return correct resource ID
        assertEquals(1L, actualResult.getResourceId());
    }

    @Test
    void testGetEirCalculation_ServiceContract() {
        // RED: Contract for retrieving single EIR calculation

        // GIVEN: EIR calculation exists
        Long calculationId = 1L;
        EirCalculationResponse response = createTestResponse();
        when(eirCalculationReadPlatformService.retrieveEirCalculation(calculationId)).thenReturn(response);

        // WHEN: Service is called
        EirCalculationResponse actualResponse = eirCalculationReadPlatformService.retrieveEirCalculation(calculationId);

        // THEN: Should return calculation with correct data
        assertNotNull(actualResponse);
        assertEquals(1L, actualResponse.getCalculationId());
        assertEquals(new BigDecimal("15.50"), actualResponse.getEffectiveInterestRate());
        assertEquals(1L, actualResponse.getLoanId());
    }

    @Test
    void testGetEirCalculationsByLoanId_ServiceContract() {
        // RED: Contract for retrieving EIR calculations by loan

        // GIVEN: Multiple calculations exist for loan
        Long loanId = 1L;
        List<EirCalculationResponse> calculations = Arrays.asList(createTestResponse(), createTestResponse());
        when(eirCalculationReadPlatformService.retrieveEirCalculationsByLoanId(loanId)).thenReturn(calculations);

        // WHEN: Service is called
        List<EirCalculationResponse> actualCalculations = eirCalculationReadPlatformService.retrieveEirCalculationsByLoanId(loanId);

        // THEN: Should return list of calculations
        assertNotNull(actualCalculations);
        assertEquals(2, actualCalculations.size());
    }

    @Test
    void testCalculateEir_ServiceContract() {
        // RED: Contract for performing EIR calculations

        // GIVEN: Loan data for calculation
        Long loanId = 1L;
        EirCalculationResponse calculationResult = createTestResponse();
        when(eirCalculationWritePlatformService.calculateEir(loanId)).thenReturn(calculationResult);

        // WHEN: Service is called
        EirCalculationResponse actualResult = eirCalculationWritePlatformService.calculateEir(loanId);

        // THEN: Should return calculated EIR
        assertNotNull(actualResult);
        assertEquals(new BigDecimal("15.50"), actualResult.getEffectiveInterestRate());
    }

    @Test
    void testUpdateEirCalculation_ServiceContract() {
        // RED: Contract for updating EIR calculations

        // GIVEN: Valid update request
        Long calculationId = 1L;
        EirCalculationRequest updateRequest = createTestRequest();
        CommandProcessingResult result = CommandProcessingResult.resourceResult(calculationId);
        when(eirCalculationWritePlatformService.updateEirCalculation(calculationId, updateRequest)).thenReturn(result);

        // WHEN: Service is called
        CommandProcessingResult actualResult = eirCalculationWritePlatformService.updateEirCalculation(calculationId, updateRequest);

        // THEN: Should return updated resource ID
        assertEquals(calculationId, actualResult.getResourceId());
    }

    @Test
    void testDeleteEirCalculation_ServiceContract() {
        // RED: Contract for deleting EIR calculations

        // GIVEN: Calculation exists
        Long calculationId = 1L;
        CommandProcessingResult result = CommandProcessingResult.resourceResult(calculationId);
        when(eirCalculationWritePlatformService.deleteEirCalculation(calculationId)).thenReturn(result);

        // WHEN: Service is called
        CommandProcessingResult actualResult = eirCalculationWritePlatformService.deleteEirCalculation(calculationId);

        // THEN: Should return deleted resource ID
        assertEquals(calculationId, actualResult.getResourceId());
    }

    @Test
    void testGetEirCalculationHistory_ServiceContract() {
        // RED: Contract for calculation history

        // GIVEN: Historical calculations exist
        Long loanId = 1L;
        List<EirCalculationResponse> history = Arrays.asList(createTestResponse());
        when(eirCalculationReadPlatformService.retrieveEirCalculationHistory(loanId)).thenReturn(history);

        // WHEN: Service is called
        List<EirCalculationResponse> actualHistory = eirCalculationReadPlatformService.retrieveEirCalculationHistory(loanId);

        // THEN: Should return calculation history
        assertNotNull(actualHistory);
        assertEquals(1, actualHistory.size());
    }

    @Test
    void testGetEirCalculation_NotFound_ServiceContract() {
        // RED: Contract for not found scenario

        // GIVEN: Calculation doesn't exist
        Long calculationId = 999L;
        when(eirCalculationReadPlatformService.retrieveEirCalculation(calculationId)).thenReturn(null);

        // WHEN: Service is called
        EirCalculationResponse result = eirCalculationReadPlatformService.retrieveEirCalculation(calculationId);

        // THEN: Should return null
        assertNull(result);
    }

    // Helper methods for test data - coder will implement actual DTOs
    private EirCalculationRequest createTestRequest() {
        // Stub implementation - coder will create proper DTO
        return new EirCalculationRequest() {

            {
                setLoanId(1L);
                setCalculationDate(LocalDate.of(2024, 12, 19));
                setEffectiveInterestRate(new BigDecimal("15.50"));
                setPrincipalAmount(new BigDecimal("100000.00"));
                setCalculationMethod("IRR_METHOD");
            }
        };
    }

    private EirCalculationResponse createTestResponse() {
        // Stub implementation - coder will create proper DTO
        return new EirCalculationResponse() {

            {
                setCalculationId(1L);
                setLoanId(1L);
                setCalculationDate(LocalDate.of(2024, 12, 19));
                setEffectiveInterestRate(new BigDecimal("15.50"));
                setPrincipalAmount(new BigDecimal("100000.00"));
                setCalculationMethod("IRR_METHOD");
                setCalculationStatus("COMPLETED");
            }
        };
    }
}
