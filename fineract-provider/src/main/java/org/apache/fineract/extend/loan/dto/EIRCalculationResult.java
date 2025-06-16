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
package org.apache.fineract.extend.loan.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Data Transfer Object containing EIR calculation results and supporting data.
 *
 * Captures all calculation parameters for audit trail and transparency.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class EIRCalculationResult {

    /**
     * Calculated Effective Interest Rate as percentage (e.g., 12.50 for 12.5%).
     */
    private final BigDecimal effectiveInterestRate;

    /**
     * Principal amount of the loan.
     */
    private final BigDecimal principalAmount;

    /**
     * Net disbursement amount (Principal - Charges due at disbursement).
     */
    private final BigDecimal netDisbursementAmount;

    /**
     * Total charges due at disbursement.
     */
    private final BigDecimal chargesDueAtDisbursement;

    /**
     * EMI amount for the loan.
     */
    private final BigDecimal emiAmount;

    /**
     * Loan tenure in months.
     */
    private final Integer tenureInMonths;

    /**
     * Number of installments.
     */
    private final Integer numberOfInstallments;

    /**
     * Calculation date.
     */
    private final LocalDate calculationDate;

    /**
     * Currency code for the loan.
     */
    private final String currencyCode;

    /**
     * IRR formula used for calculation.
     */
    private final String formulaUsed;

    /**
     * Check if the calculation is valid.
     *
     * @return true if all required parameters are present and calculation is valid
     */
    public boolean isValid() {
        return effectiveInterestRate != null && effectiveInterestRate.compareTo(BigDecimal.ZERO) >= 0 && netDisbursementAmount != null
                && netDisbursementAmount.compareTo(BigDecimal.ZERO) > 0 && emiAmount != null && emiAmount.compareTo(BigDecimal.ZERO) > 0
                && tenureInMonths != null && tenureInMonths > 0;
    }
}
