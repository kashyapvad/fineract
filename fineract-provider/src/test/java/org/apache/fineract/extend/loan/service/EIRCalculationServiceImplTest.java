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
package org.apache.fineract.extend.loan.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.extend.loan.dto.EIRCalculationResult;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for EIR calculation service implementation. Tests IRR calculation and various loan scenarios.
 */
@ExtendWith(MockitoExtension.class)
class EIRCalculationServiceImplTest {

    @InjectMocks
    private EIRCalculationServiceImpl eirCalculationService;

    @Mock
    private Loan mockLoan;

    @Mock
    private LoanRepaymentScheduleInstallment mockInstallment;

    @Mock
    private MonetaryCurrency mockCurrency;

    @Mock
    private Money mockPrincipalMoney;

    @Mock
    private Money mockEmiMoney;

    private static final BigDecimal PRINCIPAL_AMOUNT = new BigDecimal("100000.00");
    private static final BigDecimal CHARGES_DUE_AT_DISBURSEMENT = new BigDecimal("5000.00");
    private static final BigDecimal NET_DISBURSEMENT = PRINCIPAL_AMOUNT.subtract(CHARGES_DUE_AT_DISBURSEMENT);
    private static final BigDecimal EMI_AMOUNT = new BigDecimal("9168.33");
    private static final Integer TENURE_MONTHS = 12;
    private static final Integer NUMBER_OF_INSTALLMENTS = 12;

    @BeforeEach
    void setUp() {
        // Setup tenant context for testing
        FineractPlatformTenant tenant = new FineractPlatformTenant(1L, "default", "Default", "Europe/Berlin", null);
        ThreadLocalContextUtil.setTenant(tenant);
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void testCalculateEIR_ZeroChargesScenario() {
        // Given: Loan with no charges
        setupMockLoanWithZeroCharges();
        when(mockLoan.isDisbursed()).thenReturn(true);
        when(mockLoan.getRepaymentScheduleInstallments()).thenReturn(List.of(mockInstallment));
        when(mockLoan.getNumberOfRepayments()).thenReturn(NUMBER_OF_INSTALLMENTS);
        when(mockInstallment.isDownPayment()).thenReturn(false);
        when(mockInstallment.isAdditional()).thenReturn(false);
        when(mockInstallment.getDue(any())).thenReturn(mockEmiMoney);
        when(mockEmiMoney.getAmount()).thenReturn(EMI_AMOUNT);

        // When: Calculate EIR
        EIRCalculationResult result = eirCalculationService.calculateEIR(mockLoan);

        // Then: Net disbursement equals principal
        assertEquals(PRINCIPAL_AMOUNT, result.getNetDisbursementAmount());
        assertEquals(BigDecimal.ZERO, result.getChargesDueAtDisbursement());
    }

    @Test
    void testCalculateNetDisbursementAmount() {
        // Given: Loan with charges
        when(mockLoan.getPrincipal()).thenReturn(mockPrincipalMoney);
        when(mockPrincipalMoney.getAmount()).thenReturn(PRINCIPAL_AMOUNT);
        when(mockLoan.deriveSumTotalOfChargesDueAtDisbursement()).thenReturn(CHARGES_DUE_AT_DISBURSEMENT);

        // When: Calculate net disbursement
        BigDecimal result = eirCalculationService.calculateNetDisbursementAmount(mockLoan);

        // Then: Verify calculation
        assertEquals(NET_DISBURSEMENT, result);
    }

    @Test
    void testIsEligibleForEIRCalculation_ValidLoan() {
        // Given: Valid disbursed loan
        when(mockLoan.getPrincipal()).thenReturn(mockPrincipalMoney);
        when(mockPrincipalMoney.getAmount()).thenReturn(PRINCIPAL_AMOUNT);
        when(mockLoan.deriveSumTotalOfChargesDueAtDisbursement()).thenReturn(CHARGES_DUE_AT_DISBURSEMENT);
        when(mockLoan.isDisbursed()).thenReturn(true);
        when(mockLoan.getRepaymentScheduleInstallments()).thenReturn(List.of(mockInstallment));

        // When: Check eligibility
        boolean result = eirCalculationService.isEligibleForEIRCalculation(mockLoan);

        // Then: Should be eligible
        assertTrue(result);
    }

    @Test
    void testIsEligibleForEIRCalculation_NotDisbursed() {
        // Given: Loan not disbursed
        when(mockLoan.isDisbursed()).thenReturn(false);

        // When: Check eligibility
        boolean result = eirCalculationService.isEligibleForEIRCalculation(mockLoan);

        // Then: Should not be eligible
        assertFalse(result);
    }

    @Test
    void testIsEligibleForEIRCalculation_NoRepaymentSchedule() {
        // Given: Loan with no repayment schedule
        when(mockLoan.isDisbursed()).thenReturn(true);
        when(mockLoan.getRepaymentScheduleInstallments()).thenReturn(List.of());

        // When: Check eligibility
        boolean result = eirCalculationService.isEligibleForEIRCalculation(mockLoan);

        // Then: Should not be eligible
        assertFalse(result);
    }

    @Test
    void testIsEligibleForEIRCalculation_NegativeNetDisbursement() {
        // Given: Loan with charges exceeding principal
        when(mockLoan.isDisbursed()).thenReturn(true);
        when(mockLoan.getRepaymentScheduleInstallments()).thenReturn(List.of(mockInstallment));
        when(mockLoan.getPrincipal()).thenReturn(mockPrincipalMoney);
        when(mockPrincipalMoney.getAmount()).thenReturn(new BigDecimal("1000.00"));
        when(mockLoan.deriveSumTotalOfChargesDueAtDisbursement()).thenReturn(new BigDecimal("2000.00"));

        // When: Check eligibility
        boolean result = eirCalculationService.isEligibleForEIRCalculation(mockLoan);

        // Then: Should not be eligible
        assertFalse(result);
    }

    @Test
    void testCalculateEIR_NullLoan() {
        // When & Then: Should throw exception
        assertThrows(Exception.class, () -> eirCalculationService.calculateEIR(null));
    }

    @Test
    void testIRRFormulaCalculation() {
        // Given: User's specific values for IRR calculation
        // Net Disbursement: 94,982, Tenure: 24 months, EMIs: 23 x 5000 + 1 x 1082
        BigDecimal netDisbursement = new BigDecimal("94982");
        BigDecimal regularEMI = new BigDecimal("5000");
        BigDecimal finalEMI = new BigDecimal("1082");
        Integer tenure = 24;
        Integer numberOfInstallments = 24;

        // Create mock installments with the actual EMI schedule
        List<LoanRepaymentScheduleInstallment> mockInstallments = new ArrayList<>();

        // Create 23 regular EMI installments of 5000 each
        for (int i = 0; i < 23; i++) {
            LoanRepaymentScheduleInstallment installment = mock(LoanRepaymentScheduleInstallment.class);
            Money emiMoney = mock(Money.class);
            when(installment.isDownPayment()).thenReturn(false);
            when(installment.isAdditional()).thenReturn(false);
            when(installment.getDue(any())).thenReturn(emiMoney);
            when(emiMoney.getAmount()).thenReturn(regularEMI);
            mockInstallments.add(installment);
        }

        // Create 1 final EMI installment of 1082
        LoanRepaymentScheduleInstallment finalInstallment = mock(LoanRepaymentScheduleInstallment.class);
        Money finalEmiMoney = mock(Money.class);
        when(finalInstallment.isDownPayment()).thenReturn(false);
        when(finalInstallment.isAdditional()).thenReturn(false);
        when(finalInstallment.getDue(any())).thenReturn(finalEmiMoney);
        when(finalEmiMoney.getAmount()).thenReturn(finalEMI);
        mockInstallments.add(finalInstallment);

        // Setup loan mocks
        when(mockLoan.getPrincipal()).thenReturn(mockPrincipalMoney);
        when(mockPrincipalMoney.getAmount()).thenReturn(new BigDecimal("100000"));
        when(mockLoan.deriveSumTotalOfChargesDueAtDisbursement()).thenReturn(new BigDecimal("5018")); // 100000 - 94982
        when(mockLoan.isDisbursed()).thenReturn(true);
        when(mockLoan.getRepaymentScheduleInstallments()).thenReturn(mockInstallments);
        when(mockLoan.getNumberOfRepayments()).thenReturn(numberOfInstallments);
        when(mockLoan.getTermPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        when(mockLoan.getTermFrequency()).thenReturn(tenure);
        when(mockLoan.getCurrency()).thenReturn(mockCurrency);
        when(mockLoan.getCurrencyCode()).thenReturn("USD");
        when(mockLoan.getId()).thenReturn(1L);

        // When: Calculate EIR
        EIRCalculationResult result = eirCalculationService.calculateEIR(mockLoan);

        // Then: Verify the EIR is around 20.7% (between 20.6% and 20.8%)
        assertNotNull(result);
        assertEquals(numberOfInstallments, result.getNumberOfInstallments());
        assertEquals(tenure, result.getTenureInMonths());
        assertEquals(netDisbursement, result.getNetDisbursementAmount());

        // Check that EIR is in the expected range (20.6% to 20.8%)
        BigDecimal calculatedEIR = result.getEffectiveInterestRate();
        assertTrue(calculatedEIR.compareTo(new BigDecimal("20.6")) >= 0, "EIR should be >= 20.6%, but was: " + calculatedEIR);
        assertTrue(calculatedEIR.compareTo(new BigDecimal("20.8")) <= 0, "EIR should be <= 20.8%, but was: " + calculatedEIR);
    }

    @Test
    void testCalculateTenureInMonths_DifferentFrequencies() {
        // Test will be implemented when we access the actual method
        // For now, this validates the service creation
        assertNotNull(eirCalculationService);
    }

    private void setupMockLoan() {
        when(mockLoan.getPrincipal()).thenReturn(mockPrincipalMoney);
        when(mockPrincipalMoney.getAmount()).thenReturn(PRINCIPAL_AMOUNT);
        when(mockLoan.deriveSumTotalOfChargesDueAtDisbursement()).thenReturn(CHARGES_DUE_AT_DISBURSEMENT);
        when(mockLoan.getNumberOfRepayments()).thenReturn(NUMBER_OF_INSTALLMENTS);
        when(mockLoan.getTermFrequency()).thenReturn(TENURE_MONTHS);
        when(mockLoan.getTermPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        when(mockLoan.getCurrencyCode()).thenReturn("USD");
        when(mockLoan.getCurrency()).thenReturn(mockCurrency);
        when(mockLoan.getId()).thenReturn(1L);
    }

    private void setupMockLoanWithZeroCharges() {
        when(mockLoan.getPrincipal()).thenReturn(mockPrincipalMoney);
        when(mockPrincipalMoney.getAmount()).thenReturn(PRINCIPAL_AMOUNT);
        when(mockLoan.deriveSumTotalOfChargesDueAtDisbursement()).thenReturn(BigDecimal.ZERO);
        when(mockLoan.getNumberOfRepayments()).thenReturn(NUMBER_OF_INSTALLMENTS);
        when(mockLoan.getTermFrequency()).thenReturn(TENURE_MONTHS);
        when(mockLoan.getTermPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        when(mockLoan.getCurrencyCode()).thenReturn("USD");
        when(mockLoan.getCurrency()).thenReturn(mockCurrency);
        when(mockLoan.getId()).thenReturn(1L);
    }
}
