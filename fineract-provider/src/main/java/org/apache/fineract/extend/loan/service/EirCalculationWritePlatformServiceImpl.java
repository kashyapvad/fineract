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
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.extend.loan.domain.EirCalculation;
import org.apache.fineract.extend.loan.domain.EirCalculationRepository;
import org.apache.fineract.extend.loan.dto.EIRCalculationResult;
import org.apache.fineract.extend.loan.dto.EirCalculationRequest;
import org.apache.fineract.extend.loan.dto.EirCalculationResponse;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of EirCalculationWritePlatformService.
 *
 * Handles creation, updates, and deletion of EIR calculations with real business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EirCalculationWritePlatformServiceImpl implements EirCalculationWritePlatformService {

    private final PlatformSecurityContext context;
    private final EirCalculationRepository eirCalculationRepository;
    private final EIRCalculationService eirCalculationService;
    private final LoanRepositoryWrapper loanRepositoryWrapper;

    // JsonCommand-based methods for command handlers

    @Override
    @Transactional
    public CommandProcessingResult createEirCalculation(JsonCommand command) {
        this.context.authenticatedUser();

        log.info("Creating EIR calculation via command for loan ID: {}", command.getLoanId());

        try {
            // Convert JsonCommand to EirCalculationRequest
            EirCalculationRequest request = convertJsonCommandToRequest(command);

            // Use the existing DTO-based method
            return createEirCalculation(request);

        } catch (Exception e) {
            log.error("Error creating EIR calculation via command: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateEirCalculation(JsonCommand command) {
        this.context.authenticatedUser();

        // For updates, entity ID should be passed as a parameter
        Long calculationId = command.longValueOfParameterNamed("calculationId");
        log.info("Updating EIR calculation via command for ID: {}", calculationId);

        try {
            // Convert JsonCommand to EirCalculationRequest
            EirCalculationRequest request = convertJsonCommandToRequest(command);

            // Use the existing DTO-based method
            return updateEirCalculation(calculationId, request);

        } catch (Exception e) {
            log.error("Error updating EIR calculation via command: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteEirCalculation(JsonCommand command) {
        this.context.authenticatedUser();

        // For deletes, entity ID should be passed as a parameter
        Long calculationId = command.longValueOfParameterNamed("calculationId");
        log.info("Deleting EIR calculation via command for ID: {}", calculationId);

        try {
            // Use the existing DTO-based method
            return deleteEirCalculation(calculationId);

        } catch (Exception e) {
            log.error("Error deleting EIR calculation via command: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Convert JsonCommand to EirCalculationRequest for processing.
     */
    private EirCalculationRequest convertJsonCommandToRequest(JsonCommand command) {
        EirCalculationRequest request = new EirCalculationRequest();

        // Extract loan ID from JsonCommand - this should come from the URL path parameter
        Long loanId = command.getLoanId();
        if (loanId == null) {
            log.error("Loan ID is null in JsonCommand. Command details: json={}", command.json());
            throw new PlatformDataIntegrityException("error.msg.eir.calculation.command.loan.id.null",
                    "Loan ID is missing from the command. This is likely a system error.", "loanId");
        }

        request.setLoanId(loanId);
        log.debug("Extracted loan ID {} from JsonCommand", loanId);

        // Extract optional parameters from JSON body
        if (command.hasParameter("calculationDate")) {
            request.setCalculationDate(command.localDateValueOfParameterNamed("calculationDate"));
        }

        if (command.hasParameter("principalAmount")) {
            request.setPrincipalAmount(command.bigDecimalValueOfParameterNamed("principalAmount"));
        }

        if (command.hasParameter("netDisbursementAmount")) {
            request.setNetDisbursementAmount(command.bigDecimalValueOfParameterNamed("netDisbursementAmount"));
        }

        if (command.hasParameter("chargesDueAtDisbursement")) {
            request.setChargesDueAtDisbursement(command.bigDecimalValueOfParameterNamed("chargesDueAtDisbursement"));
        }

        if (command.hasParameter("emiAmount")) {
            request.setEmiAmount(command.bigDecimalValueOfParameterNamed("emiAmount"));
        }

        if (command.hasParameter("tenureInMonths")) {
            request.setTenureInMonths(command.integerValueOfParameterNamed("tenureInMonths"));
        }

        if (command.hasParameter("numberOfInstallments")) {
            request.setNumberOfInstallments(command.integerValueOfParameterNamed("numberOfInstallments"));
        }

        if (command.hasParameter("currencyCode")) {
            request.setCurrencyCode(command.stringValueOfParameterNamed("currencyCode"));
        }

        log.debug("Converted JsonCommand to EirCalculationRequest for loan ID: {}", loanId);
        return request;
    }

    // DTO-based methods for direct API usage

    @Override
    @Transactional
    public CommandProcessingResult createEirCalculation(EirCalculationRequest request) {
        this.context.authenticatedUser();

        log.info("Creating EIR calculation for loan ID: {}", request.getLoanId());

        try {
            // Validate request
            if (request.getLoanId() == null) {
                throw new PlatformDataIntegrityException("error.msg.eir.calculation.loan.id.null",
                        "Loan ID cannot be null for EIR calculation", "loanId");
            }

            // Fetch loan data and validate loan exists
            Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(request.getLoanId());
            log.debug("Found loan: ID={}, Status={}, Disbursed={}", loan.getId(), loan.getStatus(), loan.isDisbursed());

            // Check if loan is eligible for EIR calculation before attempting calculation
            if (!this.eirCalculationService.isEligibleForEIRCalculation(loan)) {
                String reason = "";
                if (!loan.isApproved() && !loan.isDisbursed()) {
                    reason = "Loan is not approved yet. EIR calculation requires loan to be approved.";
                } else if (loan.getRepaymentScheduleInstallments().isEmpty()) {
                    reason = "Loan has no repayment schedule. EIR calculation requires a valid repayment schedule.";
                } else {
                    BigDecimal netAmount = this.eirCalculationService.calculateNetDisbursementAmount(loan);
                    if (netAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        reason = String.format(
                                "Net disbursement amount is not positive: %s. EIR calculation requires positive net disbursement.",
                                netAmount);
                    } else {
                        reason = "Loan does not meet EIR calculation requirements.";
                    }
                }

                throw new PlatformDataIntegrityException("error.msg.eir.calculation.loan.not.eligible",
                        "Loan " + request.getLoanId() + " is not eligible for EIR calculation: " + reason, request.getLoanId());
            }

            // Calculate EIR using the dedicated service
            EIRCalculationResult calculationResult = this.eirCalculationService.calculateEIR(loan);

            if (calculationResult == null) {
                throw new PlatformDataIntegrityException("error.msg.eir.calculation.result.null",
                        "EIR calculation service returned null result for loan " + request.getLoanId(), request.getLoanId());
            }

            // Validate calculation result
            if (calculationResult.getEffectiveInterestRate() == null) {
                throw new PlatformDataIntegrityException("error.msg.eir.calculation.rate.null",
                        "EIR calculation returned null effective interest rate for loan " + request.getLoanId(), request.getLoanId());
            }

            // Create EIR calculation entity using calculated results
            EirCalculation calculation = EirCalculation.builder().loanId(request.getLoanId()).calculationDate(LocalDate.now())
                    .effectiveInterestRate(calculationResult.getEffectiveInterestRate())
                    .principalAmount(calculationResult.getPrincipalAmount())
                    .netDisbursementAmount(calculationResult.getNetDisbursementAmount())
                    .chargesDueAtDisbursement(calculationResult.getChargesDueAtDisbursement()).emiAmount(calculationResult.getEmiAmount())
                    .tenureInMonths(calculationResult.getTenureInMonths()).numberOfInstallments(calculationResult.getNumberOfInstallments())
                    .currencyCode(calculationResult.getCurrencyCode()).formulaUsed(calculationResult.getFormulaUsed())
                    .calculationMethod("IRR_METHOD").build();

            // Save to database
            EirCalculation savedCalculation = this.eirCalculationRepository.save(calculation);

            log.info("EIR calculation created with ID: {} for loan: {}, EIR: {}%", savedCalculation.getId(), request.getLoanId(),
                    calculationResult.getEffectiveInterestRate());

            return new CommandProcessingResultBuilder().withCommandId(null)
                    .withEntityId(savedCalculation.getId() != null ? (Long) savedCalculation.getId() : 0L)
                    .withResourceIdAsString(savedCalculation.getId() != null ? savedCalculation.getId().toString() : "0").build();

        } catch (PlatformDataIntegrityException e) {
            // Re-throw platform exceptions as-is
            log.error("Platform error creating EIR calculation for loan {}: {}", request.getLoanId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating EIR calculation for loan {}: {}", request.getLoanId(), e.getMessage(), e);
            throw new PlatformDataIntegrityException("error.msg.eir.calculation.create.failed",
                    "Failed to create EIR calculation for loan " + request.getLoanId() + ": " + e.getMessage(), request.getLoanId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EirCalculationResponse calculateEir(Long loanId) {
        this.context.authenticatedUser();

        log.info("Calculating EIR for loan ID: {}", loanId);

        try {
            // Retrieve latest calculation for the loan or create a basic one for calculation
            EirCalculation latestCalculation = this.eirCalculationRepository.findTopByLoanIdOrderByCalculationDateDesc(loanId).orElse(null);

            if (latestCalculation != null) {
                // Return existing calculation
                return convertToResponse(latestCalculation);
            } else {
                // No existing calculation found - this would require loan service integration
                // to fetch loan details and calculate EIR
                throw new PlatformDataIntegrityException("error.msg.eir.calculation.no.data",
                        "No EIR calculation data found for loan " + loanId + ". Create a calculation first.", loanId);
            }

        } catch (Exception e) {
            log.error("Error calculating EIR for loan {}: {}", loanId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateEirCalculation(Long calculationId, EirCalculationRequest request) {
        this.context.authenticatedUser();

        log.info("Updating EIR calculation ID: {}", calculationId);

        try {
            EirCalculation existingCalculation = this.eirCalculationRepository.findById(calculationId)
                    .orElseThrow(() -> new PlatformDataIntegrityException("error.msg.eir.calculation.not.found",
                            "EIR calculation with ID " + calculationId + " not found", calculationId));

            // Recalculate EIR using the dedicated service
            Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(existingCalculation.getLoanId());
            EIRCalculationResult calculationResult = this.eirCalculationService.calculateEIR(loan);
            BigDecimal updatedEir = calculationResult.getEffectiveInterestRate();

            // Update entity fields
            existingCalculation.setCalculationDate(
                    request.getCalculationDate() != null ? request.getCalculationDate() : existingCalculation.getCalculationDate());
            existingCalculation.setEffectiveInterestRate(updatedEir);
            existingCalculation.setPrincipalAmount(request.getPrincipalAmount());
            existingCalculation.setNetDisbursementAmount(request.getNetDisbursementAmount());
            existingCalculation.setChargesDueAtDisbursement(request.getChargesDueAtDisbursement());
            existingCalculation.setEmiAmount(request.getEmiAmount());
            existingCalculation.setTenureInMonths(request.getTenureInMonths());
            existingCalculation.setNumberOfInstallments(request.getNumberOfInstallments());
            existingCalculation.setCurrencyCode(request.getCurrencyCode());

            // Save updated calculation
            EirCalculation updatedCalculation = this.eirCalculationRepository.save(existingCalculation);

            log.info("EIR calculation updated: {}", calculationId);

            return new CommandProcessingResultBuilder().withCommandId(null).withEntityId((Long) updatedCalculation.getId())
                    .withResourceIdAsString(updatedCalculation.getId().toString()).build();

        } catch (Exception e) {
            log.error("Error updating EIR calculation {}: {}", calculationId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteEirCalculation(Long calculationId) {
        this.context.authenticatedUser();

        log.info("Deleting EIR calculation ID: {}", calculationId);

        try {
            EirCalculation calculation = this.eirCalculationRepository.findById(calculationId)
                    .orElseThrow(() -> new PlatformDataIntegrityException("error.msg.eir.calculation.not.found",
                            "EIR calculation with ID " + calculationId + " not found", calculationId));

            this.eirCalculationRepository.delete(calculation);

            log.info("EIR calculation deleted: {}", calculationId);

            return new CommandProcessingResultBuilder().withCommandId(null).withEntityId(calculationId)
                    .withResourceIdAsString(calculationId.toString()).build();

        } catch (Exception e) {
            log.error("Error deleting EIR calculation {}: {}", calculationId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Convert EirCalculation entity to EirCalculationResponse DTO.
     */
    private EirCalculationResponse convertToResponse(EirCalculation calculation) {
        return EirCalculationResponse.builder().calculationId(calculation.getId()).loanId(calculation.getLoanId())
                .calculationDate(calculation.getCalculationDate()).effectiveInterestRate(calculation.getEffectiveInterestRate())
                .netDisbursementAmount(calculation.getNetDisbursementAmount()).emiAmount(calculation.getEmiAmount())
                .tenureInMonths(calculation.getTenureInMonths()).numberOfInstallments(calculation.getNumberOfInstallments())
                .formulaUsed(calculation.getFormulaUsed()).calculationMethod(calculation.getCalculationMethod())
                .calculationStatus("COMPLETED").build();
    }
}
