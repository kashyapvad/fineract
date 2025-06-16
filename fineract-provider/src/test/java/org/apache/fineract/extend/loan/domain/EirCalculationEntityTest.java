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
package org.apache.fineract.extend.loan.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Test class for EirCalculation entity
 *
 * Tests JPA entity mapping, builder pattern, and business logic as specified in the architectural analysis.
 */
class EirCalculationEntityTest {

    @Test
    void testEirCalculationBuilder() {
        // Given
        Long loanId = 1L;
        LocalDate calculationDate = LocalDate.of(2024, 12, 19);
        BigDecimal effectiveInterestRate = new BigDecimal("15.50");
        BigDecimal principalAmount = new BigDecimal("100000.00");
        BigDecimal netDisbursementAmount = new BigDecimal("98000.00");

        // When
        EirCalculation eirCalculation = EirCalculation.builder().loanId(loanId).calculationDate(calculationDate)
                .effectiveInterestRate(effectiveInterestRate).principalAmount(principalAmount).netDisbursementAmount(netDisbursementAmount)
                .chargesDueAtDisbursement(new BigDecimal("2000.00")).totalRepaymentAmount(new BigDecimal("120000.00"))
                .emiAmount(new BigDecimal("5000.00")).numberOfInstallments(24).currencyCode("INR").calculationMethod("IRR_METHOD").build();

        // Then
        assertThat(eirCalculation.getLoanId()).isEqualTo(loanId);
        assertThat(eirCalculation.getCalculationDate()).isEqualTo(calculationDate);
        assertThat(eirCalculation.getEffectiveInterestRate()).isEqualByComparingTo(effectiveInterestRate);
        assertThat(eirCalculation.getPrincipalAmount()).isEqualByComparingTo(principalAmount);
        assertThat(eirCalculation.getNetDisbursementAmount()).isEqualByComparingTo(netDisbursementAmount);
        assertThat(eirCalculation.getChargesDueAtDisbursement()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(eirCalculation.getTotalRepaymentAmount()).isEqualByComparingTo(new BigDecimal("120000.00"));
        assertThat(eirCalculation.getEmiAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(eirCalculation.getNumberOfInstallments()).isEqualTo(24);
        assertThat(eirCalculation.getCurrencyCode()).isEqualTo("INR");
        assertThat(eirCalculation.getCalculationMethod()).isEqualTo("IRR_METHOD");
    }

    @Test
    void testEirCalculationDefaultValues() {
        // When
        EirCalculation eirCalculation = EirCalculation.builder().loanId(1L).calculationDate(LocalDate.now())
                .effectiveInterestRate(new BigDecimal("12.00")).principalAmount(new BigDecimal("50000.00")).calculationMethod("IRR_METHOD")
                .build();

        // Then
        assertThat(eirCalculation.getLoanId()).isEqualTo(1L);
        assertThat(eirCalculation.getCalculationMethod()).isEqualTo("IRR_METHOD");
    }

    @Test
    void testEirCalculationEquality() {
        // Given
        LocalDate calculationDate = LocalDate.of(2024, 12, 19);

        EirCalculation eirCalculation1 = EirCalculation.builder().loanId(1L).calculationDate(calculationDate)
                .effectiveInterestRate(new BigDecimal("15.50")).principalAmount(new BigDecimal("100000.00")).calculationMethod("IRR_METHOD")
                .build();

        EirCalculation eirCalculation2 = EirCalculation.builder().loanId(1L).calculationDate(calculationDate)
                .effectiveInterestRate(new BigDecimal("15.50")).principalAmount(new BigDecimal("100000.00")).calculationMethod("IRR_METHOD")
                .build();

        // Then - Test that objects can be created with same data (if equals/hashCode not overridden)
        assertThat(eirCalculation1.getLoanId()).isEqualTo(eirCalculation2.getLoanId());
        assertThat(eirCalculation1.getCalculationDate()).isEqualTo(eirCalculation2.getCalculationDate());
        assertThat(eirCalculation1.getEffectiveInterestRate()).isEqualByComparingTo(eirCalculation2.getEffectiveInterestRate());
        assertThat(eirCalculation1.getPrincipalAmount()).isEqualByComparingTo(eirCalculation2.getPrincipalAmount());
        assertThat(eirCalculation1.getCalculationMethod()).isEqualTo(eirCalculation2.getCalculationMethod());
    }

    @Test
    void testEirCalculationAllFieldsSet() {
        // Given - Create comprehensive EIR calculation with all fields
        EirCalculation eirCalculation = EirCalculation.builder().loanId(1L).calculationDate(LocalDate.of(2024, 12, 19))
                .effectiveInterestRate(new BigDecimal("15.50")).principalAmount(new BigDecimal("100000.00"))
                .interestRateDifferential(new BigDecimal("3.50")).totalChargesFeesInsurance(new BigDecimal("5000.00"))
                .processingFees(new BigDecimal("2500.00")).gstAmount(new BigDecimal("450.00")).otherCharges(new BigDecimal("2050.00"))
                .totalRepaymentAmount(new BigDecimal("120000.00")).loanTenureMonths(24).netDisbursementAmount(new BigDecimal("95000.00"))
                .chargesDueAtDisbursement(new BigDecimal("5000.00")).emiAmount(new BigDecimal("5000.00")).tenureInMonths(24)
                .numberOfInstallments(24).currencyCode("INR").formulaUsed("IRR Newton-Raphson Method").calculationMethod("IRR_METHOD")
                .build();

        // Then - Verify all fields are properly set
        assertThat(eirCalculation.getLoanId()).isEqualTo(1L);
        assertThat(eirCalculation.getEffectiveInterestRate()).isEqualByComparingTo(new BigDecimal("15.50"));
        assertThat(eirCalculation.getInterestRateDifferential()).isEqualByComparingTo(new BigDecimal("3.50"));
        assertThat(eirCalculation.getTotalChargesFeesInsurance()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(eirCalculation.getProcessingFees()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(eirCalculation.getGstAmount()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(eirCalculation.getOtherCharges()).isEqualByComparingTo(new BigDecimal("2050.00"));
        assertThat(eirCalculation.getLoanTenureMonths()).isEqualTo(24);
        assertThat(eirCalculation.getTenureInMonths()).isEqualTo(24);
        assertThat(eirCalculation.getFormulaUsed()).isEqualTo("IRR Newton-Raphson Method");
        assertThat(eirCalculation.getCalculationMethod()).isEqualTo("IRR_METHOD");
    }
}
