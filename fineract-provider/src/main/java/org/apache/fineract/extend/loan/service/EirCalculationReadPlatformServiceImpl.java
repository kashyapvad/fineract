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

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.extend.loan.domain.EirCalculation;
import org.apache.fineract.extend.loan.domain.EirCalculationRepository;
import org.apache.fineract.extend.loan.dto.EirCalculationResponse;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of EirCalculationReadPlatformService.
 *
 * Handles retrieval and query operations for EIR calculations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EirCalculationReadPlatformServiceImpl implements EirCalculationReadPlatformService {

    private final PlatformSecurityContext context;
    private final EirCalculationRepository eirCalculationRepository;

    @Override
    @Transactional(readOnly = true)
    public EirCalculationResponse retrieveEirCalculation(Long calculationId) {
        this.context.authenticatedUser();

        log.info("Retrieving EIR calculation ID: {}", calculationId);

        try {
            EirCalculation calculation = this.eirCalculationRepository.findById(calculationId)
                    .orElseThrow(() -> new PlatformDataIntegrityException("error.msg.eir.calculation.not.found",
                            "EIR calculation with ID " + calculationId + " not found", calculationId));

            return convertToResponse(calculation);

        } catch (Exception e) {
            log.error("Error retrieving EIR calculation {}: {}", calculationId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EirCalculationResponse> retrieveEirCalculationsByLoanId(Long loanId) {
        this.context.authenticatedUser();

        log.info("Retrieving EIR calculations for loan ID: {}", loanId);

        try {
            List<EirCalculation> calculations = this.eirCalculationRepository.findAllByLoanIdOrderByCalculationDateDesc(loanId);

            List<EirCalculationResponse> responses = calculations.stream().map(this::convertToResponse).collect(Collectors.toList());

            log.info("Retrieved {} EIR calculations for loan: {}", responses.size(), loanId);
            return responses;

        } catch (Exception e) {
            log.error("Error retrieving EIR calculations for loan {}: {}", loanId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EirCalculationResponse retrieveLatestEirCalculation(Long loanId) {
        this.context.authenticatedUser();

        log.info("Retrieving latest EIR calculation for loan ID: {}", loanId);

        try {
            List<EirCalculation> calculations = this.eirCalculationRepository.findAllByLoanIdOrderByCalculationDateDesc(loanId);

            if (calculations.isEmpty()) {
                throw new PlatformDataIntegrityException("error.msg.eir.calculation.not.found.for.loan",
                        "No EIR calculation found for loan ID " + loanId, loanId);
            }

            EirCalculation latestCalculation = calculations.get(0); // First in desc order is latest
            EirCalculationResponse response = convertToResponse(latestCalculation);

            log.info("Retrieved latest EIR calculation ID {} for loan: {}", response.getCalculationId(), loanId);
            return response;

        } catch (Exception e) {
            log.error("Error retrieving latest EIR calculation for loan {}: {}", loanId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EirCalculationResponse> retrieveEirCalculationHistory(Long loanId) {
        this.context.authenticatedUser();

        log.info("Retrieving EIR calculation history for loan ID: {}", loanId);

        try {
            List<EirCalculation> history = this.eirCalculationRepository.findAllByLoanIdOrderByCalculationDateDesc(loanId);

            List<EirCalculationResponse> responses = history.stream().map(this::convertToResponse).collect(Collectors.toList());

            log.info("Retrieved {} historical EIR calculations for loan: {}", responses.size(), loanId);
            return responses;

        } catch (Exception e) {
            log.error("Error retrieving EIR calculation history for loan {}: {}", loanId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Convert EirCalculation entity to EirCalculationResponse DTO.
     */
    private EirCalculationResponse convertToResponse(EirCalculation calculation) {
        return EirCalculationResponse.builder().calculationId((Long) calculation.getId()).loanId(calculation.getLoanId())
                .calculationDate(calculation.getCalculationDate()).effectiveInterestRate(calculation.getEffectiveInterestRate())
                .principalAmount(calculation.getPrincipalAmount()).netDisbursementAmount(calculation.getNetDisbursementAmount())
                .chargesDueAtDisbursement(calculation.getChargesDueAtDisbursement()).emiAmount(calculation.getEmiAmount())
                .tenureInMonths(calculation.getTenureInMonths()).numberOfInstallments(calculation.getNumberOfInstallments())
                .currencyCode(calculation.getCurrencyCode()).formulaUsed(calculation.getFormulaUsed())
                .calculationMethod(calculation.getCalculationMethod())

                .calculationStatus("COMPLETED") // Default status for retrieved calculations
                .build();
    }
}
