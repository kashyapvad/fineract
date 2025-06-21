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
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.extend.loan.dto.EIRCalculationResult;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of EIR calculation service using IRR methodology.
 *
 * IRR Formula: IRR = (NET DISBURSE AMOUNT : EMI INFLOW) * EMI_COUNT/TENURE_IN_MONTHS * 12 Note: EMI_COUNT and
 * TENURE_IN_MONTHS are dynamically populated with actual loan values
 *
 * This service calculates the Effective Interest Rate for loans using the Internal Rate of Return method to provide
 * transparency in loan pricing.
 *
 * @author fineract
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EIRCalculationServiceImpl implements EIRCalculationService {

    private static final MathContext FINANCIAL_MATH_CONTEXT = new MathContext(34, RoundingMode.HALF_UP);
    private static final int PERCENTAGE_SCALE = 4; // 4 decimal places for percentage (e.g., 12.5678%)

    @Override
    public EIRCalculationResult calculateEIR(Loan loan) {
        log.debug("Starting EIR calculation for loan ID: {}", loan.getId());

        validateLoanForEIRCalculation(loan);

        try {
            // Calculate net disbursement amount using existing loan method
            BigDecimal netDisbursementAmount = calculateNetDisbursementAmount(loan);
            BigDecimal chargesDueAtDisbursement = loan.deriveSumTotalOfChargesDueAtDisbursement();

            // Get EMI amount from loan repayment schedule
            BigDecimal emiAmount = calculateEMIAmount(loan);

            // Get loan tenure details
            Integer tenureInMonths = calculateTenureInMonths(loan);
            Integer numberOfInstallments = loan.getNumberOfRepayments();

            // Apply IRR formula
            BigDecimal effectiveInterestRate = applyIRRFormula(netDisbursementAmount, loan, numberOfInstallments, tenureInMonths);

            // Create dynamic formula description with actual values
            String dynamicFormulaDescription = String.format("IRR = (NET DISBURSE AMOUNT : EMI INFLOW) * %d/%d * 12", numberOfInstallments,
                    tenureInMonths);

            EIRCalculationResult result = EIRCalculationResult.builder().effectiveInterestRate(effectiveInterestRate)
                    .principalAmount(loan.getPrincipal().getAmount()).netDisbursementAmount(netDisbursementAmount)
                    .chargesDueAtDisbursement(chargesDueAtDisbursement).emiAmount(emiAmount).tenureInMonths(tenureInMonths)
                    .numberOfInstallments(numberOfInstallments).calculationDate(LocalDate.now()).currencyCode(loan.getCurrencyCode())
                    .formulaUsed(dynamicFormulaDescription).build();

            log.info("EIR calculation completed for loan ID: {}, EIR: {}%", loan.getId(), effectiveInterestRate);
            return result;

        } catch (Exception e) {
            log.error("Error calculating EIR for loan ID: {}", loan.getId(), e);
            throw new PlatformApiDataValidationException("error.msg.loan.eir.calculation.failed",
                    "EIR calculation failed: " + e.getMessage(), "loanId", loan.getId(), e);
        }
    }

    @Override
    public BigDecimal calculateNetDisbursementAmount(Loan loan) {
        BigDecimal principalAmount = loan.getPrincipal().getAmount();
        BigDecimal chargesDueAtDisbursement = loan.deriveSumTotalOfChargesDueAtDisbursement();

        BigDecimal netAmount = principalAmount.subtract(chargesDueAtDisbursement, FINANCIAL_MATH_CONTEXT);

        log.debug("Net disbursement calculation - Principal: {}, Charges: {}, Net: {}", principalAmount, chargesDueAtDisbursement,
                netAmount);

        return netAmount;
    }

    @Override
    public boolean isEligibleForEIRCalculation(Loan loan) {
        if (loan == null) {
            return false;
        }

        // Check if loan is approved or disbursed (allowing calculation after approval)
        if (!loan.isApproved() && !loan.isDisbursed()) {
            log.debug("Loan {} not eligible for EIR - not approved or disbursed", loan.getId());
            return false;
        }

        // Check if loan has repayment schedule
        if (loan.getRepaymentScheduleInstallments().isEmpty()) {
            log.debug("Loan {} not eligible for EIR - no repayment schedule", loan.getId());
            return false;
        }

        // Check if net disbursement amount is positive
        BigDecimal netAmount = calculateNetDisbursementAmount(loan);
        if (netAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("Loan {} not eligible for EIR - non-positive net disbursement amount: {}", loan.getId(), netAmount);
            return false;
        }

        return true;
    }

    /**
     * Apply IRR formula: IRR = (NET DISBURSE AMOUNT : EMI INFLOW) * EMI COUNT/LOAN TENURE IN MONTHS * 12
     *
     * This uses the true IRR calculation where: - IRR finds the discount rate that makes NPV of cash flows = 0 - Cash
     * flows: {-NetDisbursement, EMI1, EMI2, ..., EMIn} - Formula: IRR({-D, E1, E2, ..., En}) * (n/N) * 12
     */
    private BigDecimal applyIRRFormula(BigDecimal netDisbursementAmount, Loan loan, Integer numberOfInstallments, Integer tenureInMonths) {

        // Get actual EMI amounts from repayment schedule
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        List<BigDecimal> emiAmounts = new ArrayList<>();

        // Extract EMI amounts from regular installments (not down payment or additional)
        for (LoanRepaymentScheduleInstallment installment : installments) {
            if (!installment.isDownPayment() && !installment.isAdditional()) {
                MonetaryCurrency currency = loan.getCurrency();
                BigDecimal emiAmount = installment.getDue(currency).getAmount();
                emiAmounts.add(emiAmount);
            }
        }

        if (emiAmounts.isEmpty()) {
            throw new IllegalStateException("No regular EMI installments found");
        }

        // Create cash flow array: initial disbursement (negative) + EMI payments (positive)
        double[] cashFlows = new double[emiAmounts.size() + 1];

        // Initial cash flow: net disbursement (negative because it's money going out to borrower)
        cashFlows[0] = netDisbursementAmount.negate().doubleValue();

        // Subsequent cash flows: actual EMI payments (positive because money coming back)
        for (int i = 0; i < emiAmounts.size(); i++) {
            cashFlows[i + 1] = emiAmounts.get(i).doubleValue();
        }

        // Calculate monthly IRR using Newton-Raphson method
        double monthlyIRR = calculateIRR(cashFlows);

        // Apply IRR formula: IRR * (n/N) * 12
        // where n = actual number of EMIs, N = original tenure in months
        double adjustmentFactor = (double) numberOfInstallments / tenureInMonths;
        double annualizedRate = monthlyIRR * adjustmentFactor * 12.0;

        // Convert to percentage and return with appropriate precision
        BigDecimal eir = BigDecimal.valueOf(annualizedRate * 100.0);
        return eir.setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate Internal Rate of Return (IRR) using Newton-Raphson method. IRR is the rate that makes NPV = 0.
     *
     * @param cashFlows
     *            Array of cash flows where cashFlows[0] is initial investment (negative) and subsequent values are
     *            returns (positive)
     * @return Monthly IRR as a decimal (e.g., 0.01 for 1% per month)
     */
    private double calculateIRR(double[] cashFlows) {
        final double PRECISION = 1e-10;
        final int MAX_ITERATIONS = 100;

        // Initial guess: 10% annual rate / 12 months = 0.833% monthly
        double rate = 0.10 / 12.0;

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            double npv = calculateNPV(cashFlows, rate);
            double npvDerivative = calculateNPVDerivative(cashFlows, rate);

            // Check for convergence
            if (Math.abs(npv) < PRECISION) {
                return rate;
            }

            // Avoid division by zero
            if (Math.abs(npvDerivative) < PRECISION) {
                break;
            }

            // Newton-Raphson iteration: rate_new = rate_old - f(rate)/f'(rate)
            double newRate = rate - (npv / npvDerivative);

            // Check for convergence
            if (Math.abs(newRate - rate) < PRECISION) {
                return newRate;
            }

            rate = newRate;

            // Ensure rate stays within reasonable bounds
            if (rate < -0.99 || rate > 10.0) {
                // Reset to a different starting point if we go out of bounds
                rate = 0.01 + (iteration * 0.001);
            }
        }

        // If we couldn't converge, try a simpler approximation
        log.warn("IRR calculation did not converge, using approximation");
        return approximateIRR(cashFlows);
    }

    /**
     * Calculate Net Present Value for given cash flows and discount rate.
     */
    private double calculateNPV(double[] cashFlows, double rate) {
        double npv = 0.0;
        for (int i = 0; i < cashFlows.length; i++) {
            npv += cashFlows[i] / Math.pow(1.0 + rate, i);
        }
        return npv;
    }

    /**
     * Calculate derivative of NPV with respect to rate (needed for Newton-Raphson).
     */
    private double calculateNPVDerivative(double[] cashFlows, double rate) {
        double derivative = 0.0;
        for (int i = 1; i < cashFlows.length; i++) {
            derivative -= (i * cashFlows[i]) / Math.pow(1.0 + rate, i + 1);
        }
        return derivative;
    }

    /**
     * Simple approximation of IRR when Newton-Raphson doesn't converge. Uses the fact that for regular annuities, IRR ≈
     * (Total Returns - Initial Investment) / Initial Investment / Number of Periods
     */
    private double approximateIRR(double[] cashFlows) {
        double initialInvestment = Math.abs(cashFlows[0]);
        double totalReturns = 0.0;

        for (int i = 1; i < cashFlows.length; i++) {
            totalReturns += cashFlows[i];
        }

        double totalReturn = (totalReturns - initialInvestment) / initialInvestment;
        int periods = cashFlows.length - 1;

        // Simple approximation: total return divided by number of periods
        return totalReturn / periods;
    }

    /**
     * Calculate EMI amount from the loan's repayment schedule.
     */
    private BigDecimal calculateEMIAmount(Loan loan) {
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();

        if (installments.isEmpty()) {
            throw new IllegalStateException("Loan has no repayment schedule installments");
        }

        // Find the first regular installment (not down payment or additional)
        LoanRepaymentScheduleInstallment firstRegularInstallment = installments.stream()
                .filter(installment -> !installment.isDownPayment() && !installment.isAdditional()).findFirst()
                .orElseThrow(() -> new IllegalStateException("No regular repayment installments found"));

        // Calculate total EMI amount (principal + interest + fees + penalties)
        MonetaryCurrency currency = loan.getCurrency();
        BigDecimal emiAmount = firstRegularInstallment.getDue(currency).getAmount();

        log.debug("Calculated EMI amount: {}", emiAmount);
        return emiAmount;
    }

    /**
     * Calculate tenure in months from loan details.
     */
    private Integer calculateTenureInMonths(Loan loan) {
        Integer termFrequency = loan.getTermFrequency();

        // Convert term frequency to months based on period frequency type
        return switch (loan.getTermPeriodFrequencyType()) {
            case MONTHS -> termFrequency;
            case WEEKS -> (int) Math.ceil(termFrequency * 4.0 / 12.0); // Approximate weeks to months
            case DAYS -> (int) Math.ceil(termFrequency / 30.0); // Approximate days to months
            case YEARS -> termFrequency * 12;
            default -> throw new IllegalArgumentException("Unsupported term period frequency type: " + loan.getTermPeriodFrequencyType());
        };
    }

    /**
     * Validate loan for EIR calculation requirements.
     */
    private void validateLoanForEIRCalculation(Loan loan) {
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(List.of()).reset().parameter("loanId");

        if (loan == null) {
            baseDataValidator.value(null).notNull();
        } else {
            if (!isEligibleForEIRCalculation(loan)) {
                baseDataValidator.failWithCode("loan.not.eligible.for.eir.calculation", "Loan is not eligible for EIR calculation");
            }
        }

        if (!baseDataValidator.getDataValidationErrors().isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    baseDataValidator.getDataValidationErrors());
        }
    }
}
