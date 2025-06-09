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
package org.apache.fineract.extend.creditbureau.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.stereotype.Service;

/**
 * Repository wrapper for ClientCreditReportDetails entity operations.
 *
 * Provides consistent error handling and validation for credit report data access operations. Follows Fineract
 * repository wrapper patterns for proper exception handling and business validation.
 */
@Service
@RequiredArgsConstructor
public class ClientCreditReportDetailsRepositoryWrapper {

    private final ClientCreditReportDetailsRepository repository;

    /**
     * Finds a credit report by ID with proper error handling.
     *
     * @param creditReportId
     *            the credit report ID
     * @return the credit report entity
     * @throws CreditReportNotFoundException
     *             if the credit report is not found
     */
    public ClientCreditReportDetails findOneThrowExceptionIfNotFound(final Long creditReportId) {
        try {
            return this.repository.findById(creditReportId).orElseThrow(() -> new CreditReportNotFoundException(creditReportId));
        } catch (final JpaObjectRetrievalFailureException e) {
            throw new CreditReportNotFoundException(creditReportId, e);
        }
    }

    /**
     * Finds a credit report by ID with optional return.
     */
    public Optional<ClientCreditReportDetails> findById(final Long creditReportId) {
        return this.repository.findById(creditReportId);
    }

    /**
     * Saves a credit report with proper error handling.
     */
    public ClientCreditReportDetails save(final ClientCreditReportDetails creditReport) {
        try {
            return this.repository.save(creditReport);
        } catch (final DataIntegrityViolationException e) {
            handleDataIntegrityIssues(e);
            throw e;
        }
    }

    /**
     * Saves a credit report and flushes the persistence context.
     */
    public ClientCreditReportDetails saveAndFlush(final ClientCreditReportDetails creditReport) {
        try {
            return this.repository.saveAndFlush(creditReport);
        } catch (final DataIntegrityViolationException e) {
            handleDataIntegrityIssues(e);
            throw e;
        }
    }

    /**
     * Deletes a credit report by ID with proper error handling.
     */
    public void deleteById(final Long creditReportId) {
        try {
            this.repository.deleteById(creditReportId);
        } catch (final EmptyResultDataAccessException e) {
            throw new CreditReportNotFoundException(creditReportId, e);
        } catch (final DataIntegrityViolationException e) {
            handleDataIntegrityIssues(e);
            throw e;
        }
    }

    /**
     * Finds all credit reports for a specific client.
     */
    public List<ClientCreditReportDetails> findByClientId(final Long clientId) {
        return this.repository.findByClientIdOrderByCreatedDateDesc(clientId);
    }

    /**
     * Finds credit reports for a client and provider.
     */
    public List<ClientCreditReportDetails> findByClientIdAndProvider(final Long clientId, final String provider) {
        return this.repository.findByClientIdAndCreditBureauProviderOrderByCreatedDateDesc(clientId, provider);
    }

    /**
     * Finds the latest successful credit report for a client.
     */
    public Optional<ClientCreditReportDetails> findLatestSuccessfulReport(final Long clientId) {
        final List<ClientCreditReportDetails> reports = this.repository.findLatestSuccessfulReportByClientId(clientId);
        return reports.isEmpty() ? Optional.empty() : Optional.of(reports.get(0));
    }

    /**
     * Finds the latest successful credit report for a client and provider.
     */
    public Optional<ClientCreditReportDetails> findLatestSuccessfulReport(final Long clientId, final String provider) {
        final List<ClientCreditReportDetails> reports = this.repository.findLatestSuccessfulReportByClientIdAndProvider(clientId, provider);
        return reports.isEmpty() ? Optional.empty() : Optional.of(reports.get(0));
    }

    /**
     * Finds a credit report by provider report ID.
     */
    public Optional<ClientCreditReportDetails> findByProviderReportId(final String providerReportId) {
        return this.repository.findByProviderReportId(providerReportId);
    }

    /**
     * Checks if a recent successful report exists for client and provider.
     */
    public boolean hasRecentSuccessfulReport(final Long clientId, final String provider, final Integer withinDays) {
        final LocalDate cutoffDate = LocalDate.now().minusDays(withinDays);
        return this.repository.countRecentSuccessfulReports(clientId, provider, cutoffDate) > 0;
    }

    /**
     * Counts successful reports for a client.
     */
    public Long countSuccessfulReports(final Long clientId) {
        return this.repository.countSuccessfulReportsByClientId(clientId);
    }

    /**
     * Finds credit reports by customer PAN.
     */
    public List<ClientCreditReportDetails> findByCustomerPan(final String customerPan) {
        return this.repository.findByCustomerPanOrderByCreatedDateDesc(customerPan);
    }

    /**
     * Finds credit reports by customer Aadhaar.
     */
    public List<ClientCreditReportDetails> findByCustomerAadhaar(final String customerAadhaar) {
        return this.repository.findByCustomerAadhaarOrderByCreatedDateDesc(customerAadhaar);
    }

    /**
     * Finds all credit reports.
     */
    public List<ClientCreditReportDetails> findAll() {
        return this.repository.findAll();
    }

    /**
     * Handles data integrity violations with meaningful error messages.
     */
    private void handleDataIntegrityIssues(final DataIntegrityViolationException e) {
        final String rootMessage = e.getMostSpecificCause().getMessage();

        if (rootMessage.contains("credit_report_id") && rootMessage.contains("score_model") && rootMessage.contains("score_version")) {
            throw new PlatformDataIntegrityException("error.msg.credit.report.score.model.version.duplicate",
                    "A credit score with this model and version already exists for this report.", "scoreModel", "scoreVersion");
        }

        if (rootMessage.contains("provider_report_id")) {
            throw new PlatformDataIntegrityException("error.msg.credit.report.provider.report.id.duplicate",
                    "A credit report with this provider report ID already exists.", "providerReportId");
        }

        if (rootMessage.contains("client_id")) {
            throw new PlatformDataIntegrityException("error.msg.credit.report.client.invalid",
                    "Invalid client specified for credit report.", "clientId");
        }

        if (rootMessage.contains("requested_by_user_id")) {
            throw new PlatformDataIntegrityException("error.msg.credit.report.user.invalid",
                    "Invalid user specified for credit report request.", "requestedByUserId");
        }

        throw new PlatformDataIntegrityException("error.msg.credit.report.unknown.data.integrity.issue",
                "Unknown data integrity issue with credit report.", rootMessage);
    }

    /**
     * Custom exception for credit report not found scenarios.
     */
    public static class CreditReportNotFoundException extends RuntimeException {

        public CreditReportNotFoundException(final Long creditReportId) {
            super("Credit report with identifier " + creditReportId + " does not exist");
        }

        public CreditReportNotFoundException(final Long creditReportId, final Throwable cause) {
            super("Credit report with identifier " + creditReportId + " does not exist", cause);
        }
    }
}
