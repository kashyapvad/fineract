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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for EIR calculation operations.
 *
 * Contains calculation results and metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EirCalculationResponse {

    /**
     * Unique calculation ID.
     */
    private Long calculationId;

    /**
     * Loan ID for which EIR was calculated.
     */
    private Long loanId;

    /**
     * Date of the calculation.
     */
    private LocalDate calculationDate;

    /**
     * Calculated Effective Interest Rate as percentage.
     */
    private BigDecimal effectiveInterestRate;

    /**
     * Principal amount of the loan.
     */
    private BigDecimal principalAmount;

    /**
     * Net disbursement amount.
     */
    private BigDecimal netDisbursementAmount;

    /**
     * Charges due at disbursement.
     */
    private BigDecimal chargesDueAtDisbursement;

    /**
     * EMI amount.
     */
    private BigDecimal emiAmount;

    /**
     * Tenure in months.
     */
    private Integer tenureInMonths;

    /**
     * Number of installments.
     */
    private Integer numberOfInstallments;

    /**
     * Currency code.
     */
    private String currencyCode;

    /**
     * Formula used for calculation.
     */
    private String formulaUsed;

    /**
     * Calculation method used.
     */
    private String calculationMethod;

    /**
     * Calculation status (e.g., "COMPLETED", "PENDING", "FAILED").
     */
    private String calculationStatus;
}
