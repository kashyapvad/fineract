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
package org.apache.fineract.portfolio.loanaccount.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;

/**
 * Encapsulates all the summary details of a {@link Loan}.
 *
 * {@link LoanSummary} fields are updated through a scheduled job. see -
 *
 */
@Embeddable
@Getter
public class LoanSummary {

    // derived totals fields
    @Column(name = "total_principal_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipal;

    @Column(name = "capitalized_income_derived", scale = 6, precision = 19)
    private BigDecimal totalCapitalizedIncome;

    @Column(name = "capitalized_income_adjustment_derived", scale = 6, precision = 19)
    private BigDecimal totalCapitalizedIncomeAdjustment;

    @Column(name = "principal_disbursed_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalDisbursed;

    @Column(name = "principal_adjustments_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalAdjustments;

    @Column(name = "principal_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalRepaid;

    @Column(name = "principal_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalWrittenOff;

    @Column(name = "principal_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalOutstanding;

    @Column(name = "interest_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestCharged;

    @Column(name = "interest_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestRepaid;

    @Column(name = "interest_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestWaived;

    @Column(name = "interest_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestWrittenOff;

    @Column(name = "interest_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestOutstanding;

    @Column(name = "fee_charges_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesCharged;

    @Column(name = "total_charges_due_at_disbursement_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesDueAtDisbursement;

    @Column(name = "fee_adjustments_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeAdjustments;

    @Column(name = "fee_charges_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesRepaid;

    @Column(name = "fee_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesWaived;

    @Column(name = "fee_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesWrittenOff;

    @Column(name = "fee_charges_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesOutstanding;

    @Column(name = "penalty_charges_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesCharged;

    @Column(name = "penalty_adjustments_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyAdjustments;

    @Column(name = "penalty_charges_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesRepaid;

    @Column(name = "penalty_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesWaived;

    @Column(name = "penalty_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesWrittenOff;

    @Column(name = "penalty_charges_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesOutstanding;

    @Column(name = "total_expected_repayment_derived", scale = 6, precision = 19)
    private BigDecimal totalExpectedRepayment;

    @Column(name = "total_repayment_derived", scale = 6, precision = 19)
    private BigDecimal totalRepayment;

    @Column(name = "total_expected_costofloan_derived", scale = 6, precision = 19)
    private BigDecimal totalExpectedCostOfLoan;

    @Column(name = "total_costofloan_derived", scale = 6, precision = 19)
    private BigDecimal totalCostOfLoan;

    @Column(name = "total_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalWaived;

    @Column(name = "total_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalWrittenOff;

    @Column(name = "total_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalOutstanding;

    public static LoanSummary create(final BigDecimal totalFeeChargesDueAtDisbursement) {
        return new LoanSummary(totalFeeChargesDueAtDisbursement);
    }

    protected LoanSummary() {
        //
    }

    private LoanSummary(final BigDecimal totalFeeChargesDueAtDisbursement) {
        this.totalFeeChargesDueAtDisbursement = totalFeeChargesDueAtDisbursement;
    }

    /**
     * All fields but <code>totalFeeChargesDueAtDisbursement</code> should be reset.
     */
    public void zeroFields() {
        this.totalCostOfLoan = BigDecimal.ZERO;
        this.totalExpectedCostOfLoan = BigDecimal.ZERO;
        this.totalExpectedRepayment = BigDecimal.ZERO;
        this.totalFeeAdjustments = BigDecimal.ZERO;
        this.totalFeeChargesCharged = BigDecimal.ZERO;
        this.totalFeeChargesOutstanding = BigDecimal.ZERO;
        this.totalFeeChargesRepaid = BigDecimal.ZERO;
        this.totalFeeChargesWaived = BigDecimal.ZERO;
        this.totalFeeChargesWrittenOff = BigDecimal.ZERO;
        this.totalInterestCharged = BigDecimal.ZERO;
        this.totalInterestOutstanding = BigDecimal.ZERO;
        this.totalInterestRepaid = BigDecimal.ZERO;
        this.totalInterestWaived = BigDecimal.ZERO;
        this.totalInterestWrittenOff = BigDecimal.ZERO;
        this.totalOutstanding = BigDecimal.ZERO;
        this.totalPenaltyAdjustments = BigDecimal.ZERO;
        this.totalPenaltyChargesCharged = BigDecimal.ZERO;
        this.totalPenaltyChargesOutstanding = BigDecimal.ZERO;
        this.totalPenaltyChargesRepaid = BigDecimal.ZERO;
        this.totalPenaltyChargesWaived = BigDecimal.ZERO;
        this.totalPenaltyChargesWrittenOff = BigDecimal.ZERO;
        this.totalPrincipalAdjustments = BigDecimal.ZERO;
        this.totalPrincipal = BigDecimal.ZERO;
        this.totalCapitalizedIncome = BigDecimal.ZERO;
        this.totalCapitalizedIncomeAdjustment = BigDecimal.ZERO;
        this.totalPrincipalDisbursed = BigDecimal.ZERO;
        this.totalPrincipalOutstanding = BigDecimal.ZERO;
        this.totalPrincipalRepaid = BigDecimal.ZERO;
        this.totalPrincipalWrittenOff = BigDecimal.ZERO;
        this.totalRepayment = BigDecimal.ZERO;
        this.totalWaived = BigDecimal.ZERO;
        this.totalWrittenOff = BigDecimal.ZERO;
    }

    public void updateSummary(final MonetaryCurrency currency, final Money principal,
            final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Set<LoanCharge> charges, Money capitalizedIncome,
            Money capitalizedIncomeAdjustment) {
        this.totalPrincipalDisbursed = principal.getAmount();
        this.totalCapitalizedIncome = capitalizedIncome.getAmount();
        this.totalCapitalizedIncomeAdjustment = capitalizedIncomeAdjustment.getAmount();
        this.totalPrincipal = principal.plus(capitalizedIncome).getAmount();
        this.totalPrincipalAdjustments = calculateTotalPrincipalAdjusted(repaymentScheduleInstallments, currency).getAmount();
        this.totalFeeAdjustments = calculateTotalFeeAdjusted(repaymentScheduleInstallments, currency).getAmount();
        this.totalPenaltyAdjustments = calculateTotalPenaltyAdjusted(repaymentScheduleInstallments, currency).getAmount();
        this.totalPrincipalRepaid = calculateTotalPrincipalRepaid(repaymentScheduleInstallments, currency).getAmount();
        this.totalPrincipalWrittenOff = calculateTotalPrincipalWrittenOff(repaymentScheduleInstallments, currency).getAmount();

        this.totalPrincipalOutstanding = principal.plus(capitalizedIncome).plus(this.totalPrincipalAdjustments)
                .minus(this.totalPrincipalRepaid).minus(this.totalPrincipalWrittenOff).getAmount();

        final Money totalInterestCharged = calculateTotalInterestCharged(repaymentScheduleInstallments, currency);
        this.totalInterestCharged = totalInterestCharged.getAmount();
        this.totalInterestRepaid = calculateTotalInterestRepaid(repaymentScheduleInstallments, currency).getAmount();
        this.totalInterestWaived = calculateTotalInterestWaived(repaymentScheduleInstallments, currency).getAmount();
        this.totalInterestWrittenOff = calculateTotalInterestWrittenOff(repaymentScheduleInstallments, currency).getAmount();

        this.totalInterestOutstanding = totalInterestCharged.minus(this.totalInterestRepaid).minus(this.totalInterestWaived)
                .minus(this.totalInterestWrittenOff).getAmount();

        final Money totalFeeChargesCharged = calculateTotalFeeChargesCharged(repaymentScheduleInstallments, currency)
                .plus(this.totalFeeChargesDueAtDisbursement);
        this.totalFeeChargesCharged = totalFeeChargesCharged.getAmount();

        Money totalFeeChargesRepaidAtDisbursement = calculateTotalChargesRepaidAtDisbursement(charges, currency);
        Money totalFeeChargesRepaidAfterDisbursement = calculateTotalFeeChargesRepaid(repaymentScheduleInstallments, currency);
        this.totalFeeChargesRepaid = totalFeeChargesRepaidAfterDisbursement.plus(totalFeeChargesRepaidAtDisbursement).getAmount();

        if (charges != null) {
            this.totalFeeChargesWaived = calculateTotalFeeChargesWaived(charges, currency).getAmount();
        } else {
            this.totalFeeChargesWaived = BigDecimal.ZERO;
        }

        this.totalFeeChargesWrittenOff = calculateTotalFeeChargesWrittenOff(repaymentScheduleInstallments, currency).getAmount();

        this.totalFeeChargesOutstanding = totalFeeChargesCharged.minus(this.totalFeeChargesRepaid).minus(this.totalFeeChargesWaived)
                .minus(this.totalFeeChargesWrittenOff).getAmount();

        final Money totalPenaltyChargesCharged = calculateTotalPenaltyChargesCharged(repaymentScheduleInstallments, currency);
        this.totalPenaltyChargesCharged = totalPenaltyChargesCharged.getAmount();
        this.totalPenaltyChargesRepaid = calculateTotalPenaltyChargesRepaid(repaymentScheduleInstallments, currency).getAmount();
        this.totalPenaltyChargesWaived = calculateTotalPenaltyChargesWaived(repaymentScheduleInstallments, currency).getAmount();
        this.totalPenaltyChargesWrittenOff = calculateTotalPenaltyChargesWrittenOff(repaymentScheduleInstallments, currency).getAmount();

        this.totalPenaltyChargesOutstanding = totalPenaltyChargesCharged.minus(this.totalPenaltyChargesRepaid)
                .minus(this.totalPenaltyChargesWaived).minus(this.totalPenaltyChargesWrittenOff).getAmount();

        final Money totalExpectedRepayment = Money.of(currency, this.totalPrincipal).plus(this.totalInterestCharged)
                .plus(this.totalFeeChargesCharged).plus(this.totalPenaltyChargesCharged);
        this.totalExpectedRepayment = totalExpectedRepayment.getAmount();

        final Money totalRepayment = Money.of(currency, this.totalPrincipalRepaid).plus(this.totalInterestRepaid)
                .plus(this.totalFeeChargesRepaid).plus(this.totalPenaltyChargesRepaid);
        this.totalRepayment = totalRepayment.getAmount();

        final Money totalExpectedCostOfLoan = Money.of(currency, this.totalInterestCharged).plus(this.totalFeeChargesCharged)
                .plus(this.totalPenaltyChargesCharged);
        this.totalExpectedCostOfLoan = totalExpectedCostOfLoan.getAmount();

        final Money totalCostOfLoan = Money.of(currency, this.totalInterestRepaid).plus(this.totalFeeChargesRepaid)
                .plus(this.totalPenaltyChargesRepaid);
        this.totalCostOfLoan = totalCostOfLoan.getAmount();

        final Money totalWaived = Money.of(currency, this.totalInterestWaived).plus(this.totalFeeChargesWaived)
                .plus(this.totalPenaltyChargesWaived);
        this.totalWaived = totalWaived.getAmount();

        final Money totalWrittenOff = Money.of(currency, this.totalPrincipalWrittenOff).plus(this.totalInterestWrittenOff)
                .plus(this.totalFeeChargesWrittenOff).plus(this.totalPenaltyChargesWrittenOff);
        this.totalWrittenOff = totalWrittenOff.getAmount();

        final Money totalOutstanding = Money.of(currency, this.totalPrincipalOutstanding).plus(this.totalInterestOutstanding)
                .plus(this.totalFeeChargesOutstanding).plus(this.totalPenaltyChargesOutstanding);
        this.totalOutstanding = totalOutstanding.getAmount();
    }

    public void updateTotalFeeChargesDueAtDisbursement(final BigDecimal totalFeeChargesDueAtDisbursement) {
        this.totalFeeChargesDueAtDisbursement = totalFeeChargesDueAtDisbursement;
    }

    public Money getTotalFeeChargesDueAtDisbursement(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalFeeChargesDueAtDisbursement);
    }

    public Money getTotalOutstanding(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalOutstanding);
    }

    public void updateFeeChargeOutstanding(final BigDecimal totalFeeChargesOutstanding) {
        this.totalFeeChargesOutstanding = totalFeeChargesOutstanding;
    }

    public void updatePenaltyChargeOutstanding(final BigDecimal totalPenaltyChargesOutstanding) {
        this.totalPenaltyChargesOutstanding = totalPenaltyChargesOutstanding;
    }

    public void updateFeeChargesWaived(final BigDecimal totalFeeChargesWaived) {
        this.totalFeeChargesWaived = totalFeeChargesWaived;
    }

    public void updatePenaltyChargesWaived(final BigDecimal totalPenaltyChargesWaived) {
        this.totalPenaltyChargesWaived = totalPenaltyChargesWaived;
    }

    public boolean isRepaidInFull(final MonetaryCurrency currency) {
        return getTotalOutstanding(currency).isZero();
    }

    public void updateTotalOutstanding(final BigDecimal newTotalOutstanding) {
        this.totalOutstanding = newTotalOutstanding;
    }

    public void updateTotalWaived(final BigDecimal totalWaived) {
        this.totalWaived = totalWaived;
    }

    protected Money calculateTotalPrincipalRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getPrincipalCompleted(currency));
        }
        return total;
    }

    protected Money calculateTotalPrincipalAdjusted(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getCreditedPrincipal(currency));
        }
        return total;
    }

    protected Money calculateTotalFeeAdjusted(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getCreditedFee(currency));
        }
        return total;
    }

    protected Money calculateTotalPenaltyAdjusted(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getCreditedPenalty(currency));
        }
        return total;
    }

    protected Money calculateTotalPrincipalWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getPrincipalWrittenOff(currency));
        }
        return total;
    }

    protected Money calculateTotalInterestCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getInterestCharged(currency));
        }
        return total;
    }

    protected Money calculateTotalInterestRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getInterestPaid(currency));
        }
        return total;
    }

    protected Money calculateTotalInterestWaived(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getInterestWaived(currency));
        }
        return total;
    }

    protected Money calculateTotalInterestWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getInterestWrittenOff(currency));
        }
        return total;
    }

    protected Money calculateTotalFeeChargesCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getFeeChargesCharged(currency));
        }
        return total;
    }

    protected Money calculateTotalFeeChargesRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getFeeChargesPaid(currency));
        }
        return total;
    }

    protected Money calculateTotalFeeChargesWaived(Set<LoanCharge> charges, final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanCharge charge : charges) {
            if (charge.isActive() && !charge.isPenaltyCharge()) {
                total = total.plus(charge.getAmountWaived(currency));
            }
        }
        return total;
    }

    protected Money calculateTotalFeeChargesWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getFeeChargesWrittenOff(currency));
        }
        return total;
    }

    protected Money calculateTotalPenaltyChargesCharged(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getPenaltyChargesCharged(currency));
        }
        return total;
    }

    protected Money calculateTotalPenaltyChargesRepaid(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getPenaltyChargesPaid(currency));
        }
        return total;
    }

    protected Money calculateTotalPenaltyChargesWaived(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getPenaltyChargesWaived(currency));
        }
        return total;
    }

    protected Money calculateTotalPenaltyChargesWrittenOff(final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment installment : repaymentScheduleInstallments) {
            total = total.plus(installment.getPenaltyChargesWrittenOff(currency));
        }
        return total;
    }

    protected Money calculateTotalChargesRepaidAtDisbursement(Set<LoanCharge> charges, MonetaryCurrency currency) {
        Money total = Money.zero(currency);
        if (charges == null) {
            return total;
        }
        for (final LoanCharge loanCharge : charges) {
            if (!loanCharge.isPenaltyCharge() && loanCharge.getAmountPaid(currency).isGreaterThanZero()
                    && loanCharge.isDisbursementCharge()) {
                total = total.plus(loanCharge.getAmountPaid(currency));
            }
        }
        return total;

    }
}
