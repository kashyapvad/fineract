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

import java.math.BigDecimal;
import org.apache.fineract.extend.loan.dto.EIRCalculationResult;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;

/**
 * Service interface for Effective Interest Rate (EIR) calculations using IRR methodology.
 *
 * IRR Formula: IRR = (NET DISBURSE AMOUNT : EMI INFLOW) * EMI_COUNT/TENURE_IN_MONTHS * 12 Note: EMI_COUNT and
 * TENURE_IN_MONTHS are dynamically populated with actual loan values
 *
 * @see <a href="https://en.wikipedia.org/wiki/Internal_rate_of_return">IRR Calculation Methods</a>
 * @author fineract
 */
public interface EIRCalculationService {

    /**
     * Calculate Effective Interest Rate for a loan using IRR formula.
     *
     * @param loan
     *            The loan entity for which to calculate EIR
     * @return EIRCalculationResult containing calculated EIR and supporting data
     * @throws IllegalArgumentException
     *             if loan is null or not in a valid state for EIR calculation
     */
    EIRCalculationResult calculateEIR(Loan loan);

    /**
     * Calculate net disbursement amount (Principal - Charges due at disbursement).
     *
     * @param loan
     *            The loan entity
     * @return Net disbursement amount
     */
    BigDecimal calculateNetDisbursementAmount(Loan loan);

    /**
     * Validate if a loan is eligible for EIR calculation.
     *
     * @param loan
     *            The loan entity to validate
     * @return true if loan is eligible for EIR calculation
     */
    boolean isEligibleForEIRCalculation(Loan loan);
}
