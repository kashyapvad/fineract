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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

/**
 * Entity representing Effective Interest Rate (EIR) calculations for loans.
 *
 * Stores Effective Interest Rate calculations for loans using IRR methodology. This entity is used for calculating and
 * storing EIR values for loan products to provide transparency in loan pricing.
 *
 * Example usage:
 *
 * <pre>
 *
 * EirCalculation calculation = EirCalculation.builder().loanId(1L).effectiveInterestRate(new BigDecimal("15.50"))
 *         .principalAmount(new BigDecimal("100000.00")).calculationMethod("IRR_METHOD").build();
 * </pre>
 *
 * @author fineract
 */
@Entity
@Table(name = "m_extend_eir_calculation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EirCalculation extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "loan_id", nullable = false)
    private Long loanId;

    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;

    @Column(name = "effective_interest_rate", precision = 19, scale = 6, nullable = false)
    private BigDecimal effectiveInterestRate;

    @Column(name = "principal_amount", precision = 19, scale = 6, nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate_differential", precision = 19, scale = 6)
    private BigDecimal interestRateDifferential;

    @Column(name = "total_charges_fees_insurance", precision = 19, scale = 6)
    private BigDecimal totalChargesFeesInsurance;

    @Column(name = "processing_fees", precision = 19, scale = 6)
    private BigDecimal processingFees;

    @Column(name = "gst_amount", precision = 19, scale = 6)
    private BigDecimal gstAmount;

    @Column(name = "other_charges", precision = 19, scale = 6)
    private BigDecimal otherCharges;

    @Column(name = "total_repayment_amount", precision = 19, scale = 6)
    private BigDecimal totalRepaymentAmount;

    @Column(name = "loan_tenure_months")
    private Integer loanTenureMonths;

    @Column(name = "net_disbursement_amount", precision = 19, scale = 6)
    private BigDecimal netDisbursementAmount;

    @Column(name = "charges_due_at_disbursement", precision = 19, scale = 6)
    private BigDecimal chargesDueAtDisbursement;

    @Column(name = "emi_amount", precision = 19, scale = 6)
    private BigDecimal emiAmount;

    @Column(name = "tenure_in_months")
    private Integer tenureInMonths;

    @Column(name = "number_of_installments")
    private Integer numberOfInstallments;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "formula_used")
    private String formulaUsed;

    @Column(name = "calculation_method", nullable = false, length = 100)
    private String calculationMethod;

    /**
     * Static factory method for creating EIR calculation with default values
     */
    public static EirCalculation createDefault(Long loanId, BigDecimal effectiveInterestRate, BigDecimal principalAmount) {
        return EirCalculation.builder().loanId(loanId).calculationDate(LocalDate.now()).effectiveInterestRate(effectiveInterestRate)
                .principalAmount(principalAmount).calculationMethod("IRR_METHOD").build();
    }
}
