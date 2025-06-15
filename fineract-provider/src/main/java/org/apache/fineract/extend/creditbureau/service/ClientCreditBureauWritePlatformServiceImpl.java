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
package org.apache.fineract.extend.creditbureau.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.extend.common.dto.CreditBureauProviderRequest;
import org.apache.fineract.extend.common.dto.CreditBureauProviderResponse;
import org.apache.fineract.extend.common.service.ExtendProviderService;
import org.apache.fineract.extend.creditbureau.exception.ClientCreditBureauNotFoundException;
import org.apache.fineract.extend.creditbureau.domain.ClientCreditReportDetails;
import org.apache.fineract.extend.creditbureau.domain.ClientCreditReportDetailsRepositoryWrapper;
import org.apache.fineract.extend.creditbureau.domain.ClientCreditScoreDetails;
import org.apache.fineract.extend.creditbureau.domain.ClientCreditScoreDetailsRepository;
import org.apache.fineract.extend.creditbureau.domain.CreditBureauReportType;
import org.apache.fineract.extend.creditbureau.validation.PullCreditReportRequestValidator;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.PersistenceException;

/**
 * Implementation of ClientCreditBureauWritePlatformService.
 *
 * This service handles all write operations for credit bureau reports including: - Pulling reports from external
 * providers (Decentro, CIBIL, etc.) - Manual creation and updates of credit reports - Properly extracting and storing
 * multiple credit scores from provider responses - Managing the normalized two-table design (report + scores)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientCreditBureauWritePlatformServiceImpl implements ClientCreditBureauWritePlatformService {

    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final PlatformSecurityContext context;
    private final PullCreditReportRequestValidator pullCreditReportValidator;
    private final ExtendProviderService extendProviderService;
    private final ClientCreditReportDetailsRepositoryWrapper creditReportRepositoryWrapper;
    private final ClientCreditScoreDetailsRepository creditScoreRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CommandProcessingResult pullCreditReport(final JsonCommand command) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB
        
        try {
            // Validate provider availability using common service
            this.extendProviderService.validateProviderAvailable();

            // Validate command using existing validator
            this.pullCreditReportValidator.validateForPull(command.json());

            // Extract client ID from command
            final Long clientId = command.getClientId();

            // Validate client exists and belongs to current tenant
            final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Extract report parameters
            final String reportTypeCode = command.stringValueOfParameterNamed("reportType");
            final CreditBureauReportType reportType = CreditBureauReportType.fromCode(reportTypeCode);
            final String creditBureauProvider = this.extendProviderService.getProviderName();

            // Check for recent reports to prevent unnecessary API calls
            if (this.creditReportRepositoryWrapper.hasRecentSuccessfulReport(clientId, creditBureauProvider, 7)) {
                log.warn("Client {} already has a recent successful credit report from provider {}", clientId, creditBureauProvider);
                throw new RuntimeException("Client already has a recent credit report. Please use the existing report or wait 7 days.");
            }

            // Create initial pending record
            final ClientCreditReportDetails creditReportDetails = ClientCreditReportDetails.createPendingReport(client, reportType,
                    creditBureauProvider, null, currentUser);

            final ClientCreditReportDetails savedDetails = this.creditReportRepositoryWrapper.save(creditReportDetails);

            try {
                // Build provider-agnostic request using common service
                final CreditBureauProviderRequest providerRequest = CreditBureauProviderRequest.builder()
                        .referenceId(this.extendProviderService.createReferenceId("CREDIT_REPORT", clientId)).consent(true).clientId(clientId)
                        .customerName(client.getDisplayName()).mobileNumber(client.mobileNo()).reportType("FULL_REPORT").build();

                // Call provider-agnostic API to generate credit report using common service
                final CreditBureauProviderResponse providerResponse = this.extendProviderService.generateCreditReport(providerRequest);

                if (!providerResponse.isSuccess()) {
                    throw new RuntimeException("Credit report generation failed: " + providerResponse.getMessage());
                }

                // Extract data from provider-agnostic response
                final JsonNode reportData = providerResponse.getRawProviderResponse();
                final String providerReportId = providerResponse.getProviderTransactionId();
                final String reportSummary = providerResponse.getReportSummary();
                final LocalDate reportGeneratedOn = providerResponse.getReportGeneratedOn() != null
                        ? providerResponse.getReportGeneratedOn()
                        : LocalDate.now();

                // Update report as successful with basic metadata
                savedDetails.markAsSuccessful(reportData, reportGeneratedOn);
                savedDetails.setProviderReportId(providerReportId);
                savedDetails.setReportSummary(reportSummary);

                // Extract comprehensive customer and financial data from report
                extractAndUpdateCustomerInformation(savedDetails, reportData);
                extractAndUpdateCreditSummary(savedDetails, reportData);
                extractAndUpdateFinancialInformation(savedDetails, reportData);
                extractAndUpdateDelinquencyInformation(savedDetails, reportData);
                extractAndUpdateEnquiryInformation(savedDetails, reportData);

                // Extract and save multiple credit scores (NEW: supports multiple scoring models)
                extractAndSaveMultipleCreditScores(savedDetails, reportData);

                // Save the updated report
                this.creditReportRepositoryWrapper.save(savedDetails);

                log.info("Successfully pulled credit report for client {} with ID {}, {} score models processed", clientId,
                        savedDetails.getId(), savedDetails.getTotalScoreModels());

            } catch (Exception apiException) {
                // Update record as failed
                savedDetails.markAsFailed("API_ERROR", apiException.getMessage());
                this.creditReportRepositoryWrapper.save(savedDetails);

                log.error("Failed to pull credit report for client {}: {}", clientId, apiException.getMessage());
                throw new RuntimeException("Failed to pull credit report: " + apiException.getMessage(), apiException);
            }

            // Return standardized result
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(savedDetails.getId())
                    .withClientId(clientId).build();

        } catch (Exception e) {
            log.error("Error processing credit report pull command: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteCreditReport(final JsonCommand command) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB
        
        try {
            // Extract parameters
            final Long clientId = command.getClientId();
            final Long reportId = command.entityId();

            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Find and validate credit report record exists and belongs to client
            final ClientCreditReportDetails creditReportDetails = this.creditReportRepositoryWrapper
                    .findOneThrowExceptionIfNotFound(reportId);

            if (!creditReportDetails.getClientId().equals(clientId)) {
                throw new ClientCreditBureauNotFoundException(reportId);
            }

            // AUDIT TRAIL: Capture state before deletion for compliance tracking
            final String deletedState = String.format("Provider:%s|ReportType:%s|Status:%s|Score:%s|Rating:%s|Customer:%s|TotalAccounts:%s|ActiveAccounts:%s|TotalCreditLimit:%s|TotalOutstanding:%s|Summary:%s|CreatedDate:%s|ScoreCount:%s", 
                creditReportDetails.getCreditBureauProvider(), creditReportDetails.getReportType(),
                creditReportDetails.getReportStatus(), creditReportDetails.getPrimaryCreditScore(),
                creditReportDetails.getPrimaryCreditRating(), creditReportDetails.getCustomerName(),
                creditReportDetails.getTotalAccounts(), creditReportDetails.getActiveAccounts(),
                creditReportDetails.getTotalCreditLimit(), creditReportDetails.getTotalOutstandingAmount(),
                creditReportDetails.getReportSummary(), creditReportDetails.getCreatedDate().orElse(null),
                creditReportDetails.getCreditScores().size());

            // Delete the credit report record (cascades to credit scores)
            this.creditReportRepositoryWrapper.deleteById(reportId);

            // AUDIT TRAIL: Log the deletion operation with complete record state for compliance
            log.info("AUDIT: CREDIT REPORT DELETE - User: {} | Client: {} | Report ID: {} | Command: {} | Deleted Record: [{}]", 
                currentUser.getId(), clientId, reportId, command.commandId(), deletedState);

            log.info("Successfully deleted credit report {} for client {}", reportId, clientId);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(reportId).withClientId(clientId)
                    .build();

        } catch (Exception e) {
            log.error("Error processing credit report deletion command: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult createCreditReport(final JsonCommand command) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB
        
        try {
            // Extract client ID from command
            final Long clientId = command.getClientId();

            // Validate client exists and belongs to current tenant
            final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Extract report parameters
            final String reportTypeCode = command.stringValueOfParameterNamed("reportType");
            final CreditBureauReportType reportType = CreditBureauReportType.fromCode(reportTypeCode);
            final String creditBureauProvider = command.stringValueOfParameterNamed("creditBureauProvider");

            // Create new credit report with manual data
            final ClientCreditReportDetails creditReportDetails = ClientCreditReportDetails.createSuccessfulReport(client, reportType,
                    creditBureauProvider, null, null, currentUser);

            // Set report metadata if provided
            if (command.hasParameter("reportGeneratedOn")) {
                creditReportDetails.setReportGeneratedOn(command.localDateValueOfParameterNamed("reportGeneratedOn"));
            }
            if (command.hasParameter("reportSummary")) {
                creditReportDetails.setReportSummary(command.stringValueOfParameterNamed("reportSummary"));
            }
            if (command.hasParameter("reportNotes")) {
                creditReportDetails.setReportNotes(command.stringValueOfParameterNamed("reportNotes"));
            }

            // Create and process credit scores (multiple scores support)
            this.createCreditScoresFromCommand(creditReportDetails, command);

            // Process additional data if provided
            this.updateAdditionalDataFromCommand(creditReportDetails, command);

            // Process manual data input
            this.updateCustomerInformationFromCommand(creditReportDetails, command);
            
            // Update credit summary if provided
            this.updateCreditSummaryFromCommand(creditReportDetails, command);
            
            // Update financial information if provided
            this.updateFinancialInformationFromCommand(creditReportDetails, command);
            
            // Update delinquency information if provided
            this.updateDelinquencyInformationFromCommand(creditReportDetails, command);
            
            // Update enquiry information if provided
            this.updateEnquiryInformationFromCommand(creditReportDetails, command);

            final ClientCreditReportDetails savedDetails = this.creditReportRepositoryWrapper.save(creditReportDetails);

            // Recalculate primary score after adding all scores
            savedDetails.recalculatePrimaryScore();
            this.creditReportRepositoryWrapper.save(savedDetails);

            log.info("Successfully created manual credit report for client {} with ID {}", clientId, savedDetails.getId());

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(savedDetails.getId())
                    .withClientId(clientId).build();

        } catch (final DataIntegrityViolationException dve) {
            log.error("Data integrity violation in createCreditReport: {}", dve.getMessage(), dve);
            throw handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
        } catch (final JpaSystemException dve) {
            log.error("JPA exception in createCreditReport: {}", dve.getMessage(), dve);
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleDataIntegrityIssues(command, throwable, dve);
        } catch (final PersistenceException dve) {
            log.error("Persistence exception in createCreditReport: {}", dve.getMessage(), dve);
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleDataIntegrityIssues(command, throwable, dve);
        } catch (Exception e) {
            log.error("Error processing manual credit report creation: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateCreditReport(final JsonCommand command) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB
        
        try {
            // Extract parameters
            final Long clientId = command.getClientId();
            final Long reportId = command.entityId();

            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Find and validate credit report record exists and belongs to client
            final ClientCreditReportDetails creditReportDetails = this.creditReportRepositoryWrapper
                    .findOneThrowExceptionIfNotFound(reportId);

            if (!creditReportDetails.getClientId().equals(clientId)) {
                throw new ClientCreditBureauNotFoundException(reportId);
            }

            // AUDIT TRAIL: Capture original state for compliance tracking
            final String originalState = String.format("Provider:%s|ReportType:%s|Status:%s|Score:%s|Rating:%s|Customer:%s|TotalAccounts:%s|ActiveAccounts:%s|TotalCreditLimit:%s|TotalOutstanding:%s|Summary:%s", 
                creditReportDetails.getCreditBureauProvider(), creditReportDetails.getReportType(),
                creditReportDetails.getReportStatus(), creditReportDetails.getPrimaryCreditScore(),
                creditReportDetails.getPrimaryCreditRating(), creditReportDetails.getCustomerName(),
                creditReportDetails.getTotalAccounts(), creditReportDetails.getActiveAccounts(),
                creditReportDetails.getTotalCreditLimit(), creditReportDetails.getTotalOutstandingAmount(),
                creditReportDetails.getReportSummary());

            // Update customer information if provided
            updateCustomerInformationFromCommand(creditReportDetails, command);

            // Update credit summary if provided
            updateCreditSummaryFromCommand(creditReportDetails, command);

            // Update financial information if provided
            updateFinancialInformationFromCommand(creditReportDetails, command);

            // Update delinquency information if provided
            updateDelinquencyInformationFromCommand(creditReportDetails, command);

            // Update enquiry information if provided
            updateEnquiryInformationFromCommand(creditReportDetails, command);

            // Update report metadata if provided
            if (command.hasParameter("reportSummary")) {
                creditReportDetails.setReportSummary(command.stringValueOfParameterNamed("reportSummary"));
            }
            if (command.hasParameter("reportNotes")) {
                creditReportDetails.setReportNotes(command.stringValueOfParameterNamed("reportNotes"));
            }

            // Update additional data if provided
            updateAdditionalDataFromCommand(creditReportDetails, command);

            // Update credit scores if provided (supports multiple scores)
            updateCreditScoresFromCommand(creditReportDetails, command);

            final ClientCreditReportDetails savedDetails = this.creditReportRepositoryWrapper.save(creditReportDetails);

            // AUDIT TRAIL: Log the update operation with before/after state for compliance
            final String newState = String.format("Provider:%s|ReportType:%s|Status:%s|Score:%s|Rating:%s|Customer:%s|TotalAccounts:%s|ActiveAccounts:%s|TotalCreditLimit:%s|TotalOutstanding:%s|Summary:%s", 
                savedDetails.getCreditBureauProvider(), savedDetails.getReportType(),
                savedDetails.getReportStatus(), savedDetails.getPrimaryCreditScore(),
                savedDetails.getPrimaryCreditRating(), savedDetails.getCustomerName(),
                savedDetails.getTotalAccounts(), savedDetails.getActiveAccounts(),
                savedDetails.getTotalCreditLimit(), savedDetails.getTotalOutstandingAmount(),
                savedDetails.getReportSummary());
            
            log.info("AUDIT: CREDIT REPORT UPDATE - User: {} | Client: {} | Report ID: {} | Command: {} | Original: [{}] | Updated: [{}]", 
                currentUser.getId(), clientId, reportId, command.commandId(), originalState, newState);

            log.info("Successfully updated credit report {} for client {}", reportId, clientId);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(reportId).withClientId(clientId)
                    .build();

        } catch (final DataIntegrityViolationException dve) {
            log.error("Data integrity violation in updateCreditReport: {}", dve.getMessage(), dve);
            throw handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
        } catch (final JpaSystemException dve) {
            log.error("JPA exception in updateCreditReport: {}", dve.getMessage(), dve);
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleDataIntegrityIssues(command, throwable, dve);
        } catch (final PersistenceException dve) {
            log.error("Persistence exception in updateCreditReport: {}", dve.getMessage(), dve);
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleDataIntegrityIssues(command, throwable, dve);
        } catch (Exception e) {
            log.error("Error processing credit report update: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * NEW METHOD: Extract and save multiple credit scores from Decentro API response. This handles the scoreDetails
     * array that Decentro returns with multiple scoring models.
     */
    private void extractAndSaveMultipleCreditScores(ClientCreditReportDetails creditReport, JsonNode reportData) {
        if (reportData == null || !reportData.has("data")) {
            
            return;
        }

        final JsonNode data = reportData.get("data");

        // Look for scoreDetails array (Decentro Credit Report API structure)
        if (data.has("scoreDetails") && data.get("scoreDetails").isArray()) {
            final JsonNode scoreDetailsArray = data.get("scoreDetails");

            

            for (JsonNode scoreDetail : scoreDetailsArray) {
                try {
                    final ClientCreditScoreDetails scoreDetails = ClientCreditScoreDetails.createScore(creditReport,
                            scoreDetail.path("type").asText("UNKNOWN"), scoreDetail.path("version").asText("1.0"),
                            scoreDetail.path("name").asText(), scoreDetail.path("value").asInt(), LocalDate.now());

                    // Add scoring elements if available
                    if (scoreDetail.has("scoringElements") && scoreDetail.get("scoringElements").isArray()) {
                        scoreDetails.setScoringElements(scoreDetail.get("scoringElements"));
                    }

                    creditReport.addCreditScore(scoreDetails);

                } catch (Exception e) {
                    log.warn("Failed to extract score from scoreDetail: {}", e.getMessage());
                }
            }
        }

        // Fallback: Look for single score in data section
        else if (data.has("creditScore") || data.has("score")) {
            final Integer score = data.has("creditScore") ? data.get("creditScore").asInt() : data.get("score").asInt();
            final String rating = data.has("creditRating") ? data.get("creditRating").asText() : null;

            final ClientCreditScoreDetails scoreDetails = ClientCreditScoreDetails.createScore(creditReport, "GENERAL", "1.0",
                    "General Credit Score", score, LocalDate.now());

            if (rating != null) {
                scoreDetails.setScoreReason(rating);
            }

            creditReport.addCreditScore(scoreDetails);

            
        }

        // Recalculate primary score after adding all scores
        creditReport.recalculatePrimaryScore();

        log.info("Processed {} credit score models for report {}", creditReport.getTotalScoreModels(), creditReport.getId());
    }

    /**
     * Extract customer information from API response.
     */
    private void extractAndUpdateCustomerInformation(ClientCreditReportDetails creditReport, JsonNode reportData) {
        if (reportData == null || !reportData.has("data")) return;

        final JsonNode data = reportData.get("data");

        String customerName = data.path("name").asText(null);
        String customerPan = data.path("pan").asText(null);
        String customerAadhaar = data.path("aadhaar").asText(null);
        String customerMobile = data.path("mobile").asText(null);
        String customerAddress = data.path("address").asText(null);
        String gender = data.path("gender").asText(null);

        // Parse date of birth if available
        LocalDate dateOfBirth = null;
        if (data.has("dateOfBirth")) {
            try {
                dateOfBirth = LocalDate.parse(data.get("dateOfBirth").asText());
            } catch (Exception e) {
                
            }
        }

        creditReport.updateCustomerInformation(customerName, customerPan, customerAadhaar, customerMobile, customerAddress, dateOfBirth,
                gender);
    }

    /**
     * Extract credit summary from API response.
     */
    private void extractAndUpdateCreditSummary(ClientCreditReportDetails creditReport, JsonNode reportData) {
        if (reportData == null || !reportData.has("data")) return;

        final JsonNode data = reportData.get("data");

        Integer totalAccounts = data.has("totalAccounts") ? data.get("totalAccounts").asInt() : null;
        Integer activeAccounts = data.has("activeAccounts") ? data.get("activeAccounts").asInt() : null;
        Integer closedAccounts = data.has("closedAccounts") ? data.get("closedAccounts").asInt() : null;
        Integer overdueAccounts = data.has("overdueAccounts") ? data.get("overdueAccounts").asInt() : null;

        creditReport.updateCreditSummary(totalAccounts, activeAccounts, closedAccounts, overdueAccounts, null, null, null, null);
    }

    /**
     * Extract financial information from API response.
     */
    private void extractAndUpdateFinancialInformation(ClientCreditReportDetails creditReport, JsonNode reportData) {
        if (reportData == null || !reportData.has("data")) return;

        final JsonNode data = reportData.get("data");

        BigDecimal totalCreditLimit = data.has("totalCreditLimit") ? BigDecimal.valueOf(data.get("totalCreditLimit").asDouble()) : null;
        BigDecimal totalOutstanding = data.has("totalOutstandingAmount") ? BigDecimal.valueOf(data.get("totalOutstandingAmount").asDouble())
                : null;
        BigDecimal totalOverdue = data.has("totalOverdueAmount") ? BigDecimal.valueOf(data.get("totalOverdueAmount").asDouble()) : null;
        BigDecimal highestCredit = data.has("highestCreditAmount") ? BigDecimal.valueOf(data.get("highestCreditAmount").asDouble()) : null;

        creditReport.updateCreditSummary(null, null, null, null, totalCreditLimit, totalOutstanding, totalOverdue, highestCredit);
    }

    /**
     * Extract delinquency information from API response.
     */
    private void extractAndUpdateDelinquencyInformation(ClientCreditReportDetails creditReport, JsonNode reportData) {
        if (reportData == null || !reportData.has("data")) return;

        final JsonNode data = reportData.get("data");

        Integer daysPastDue = data.has("daysPastDue") ? data.get("daysPastDue").asInt() : null;
        String worstStatus12Months = data.path("worstStatus12Months").asText(null);
        String worstStatus24Months = data.path("worstStatus24Months").asText(null);

        creditReport.updateDelinquencyInformation(daysPastDue, worstStatus12Months, worstStatus24Months);
    }

    /**
     * Extract enquiry information from API response.
     */
    private void extractAndUpdateEnquiryInformation(ClientCreditReportDetails creditReport, JsonNode reportData) {
        if (reportData == null || !reportData.has("data")) return;

        final JsonNode data = reportData.get("data");

        Integer enquiries30Days = data.has("enquiriesLast30Days") ? data.get("enquiriesLast30Days").asInt() : null;
        Integer enquiries90Days = data.has("enquiriesLast90Days") ? data.get("enquiriesLast90Days").asInt() : null;
        Integer enquiries12Months = data.has("enquiriesLast12Months") ? data.get("enquiriesLast12Months").asInt() : null;

        creditReport.updateEnquiryInformation(enquiries30Days, enquiries90Days, enquiries12Months);
    }

    /**
     * Update customer information from manual command.
     */
    private void updateCustomerInformationFromCommand(ClientCreditReportDetails creditReport, JsonCommand command) {
        if (command.hasParameter("customerName") || command.hasParameter("customerPan") || command.hasParameter("customerAadhaar")
                || command.hasParameter("customerMobile") || command.hasParameter("customerAddress") || command.hasParameter("dateOfBirth")
                || command.hasParameter("gender")) {

            creditReport.updateCustomerInformation(command.stringValueOfParameterNamed("customerName"),
                    command.stringValueOfParameterNamed("customerPan"), command.stringValueOfParameterNamed("customerAadhaar"),
                    command.stringValueOfParameterNamed("customerMobile"), command.stringValueOfParameterNamed("customerAddress"),
                    command.localDateValueOfParameterNamed("dateOfBirth"), command.stringValueOfParameterNamed("gender"));
        }
    }

    /**
     * Update credit summary from manual command.
     */
    private void updateCreditSummaryFromCommand(ClientCreditReportDetails creditReport, JsonCommand command) {
        if (command.hasParameter("totalAccounts") || command.hasParameter("activeAccounts") || command.hasParameter("closedAccounts")
                || command.hasParameter("overdueAccounts")) {

            // Use integerValueOfParameterNamed to preserve 0 values instead of converting them to null
            creditReport.updateCreditSummary(
                    command.hasParameter("totalAccounts") ? command.integerValueOfParameterNamed("totalAccounts") : null,
                    command.hasParameter("activeAccounts") ? command.integerValueOfParameterNamed("activeAccounts") : null,
                    command.hasParameter("closedAccounts") ? command.integerValueOfParameterNamed("closedAccounts") : null,
                    command.hasParameter("overdueAccounts") ? command.integerValueOfParameterNamed("overdueAccounts") : null, 
                    null, null, null, null);
        }
    }

    /**
     * Update financial information from manual command.
     */
    private void updateFinancialInformationFromCommand(ClientCreditReportDetails creditReport, JsonCommand command) {
        if (command.hasParameter("totalCreditLimit") || command.hasParameter("totalOutstandingAmount")
                || command.hasParameter("totalOverdueAmount") || command.hasParameter("highestCreditAmount")) {

            // Use bigDecimalValueOfParameterNamed to preserve 0 values instead of converting them to null
            creditReport.updateCreditSummary(null, null, null, null, 
                    command.hasParameter("totalCreditLimit") ? command.bigDecimalValueOfParameterNamed("totalCreditLimit") : null,
                    command.hasParameter("totalOutstandingAmount") ? command.bigDecimalValueOfParameterNamed("totalOutstandingAmount") : null,
                    command.hasParameter("totalOverdueAmount") ? command.bigDecimalValueOfParameterNamed("totalOverdueAmount") : null,
                    command.hasParameter("highestCreditAmount") ? command.bigDecimalValueOfParameterNamed("highestCreditAmount") : null);
        }
    }

    /**
     * Update delinquency information from manual command.
     */
    private void updateDelinquencyInformationFromCommand(ClientCreditReportDetails creditReport, JsonCommand command) {
        if (command.hasParameter("daysPastDue") || command.hasParameter("worstStatus12Months") 
                || command.hasParameter("worstStatus24Months")) {

            // Use integerValueOfParameterNamed to preserve 0 values for daysPastDue
            creditReport.updateDelinquencyInformation(
                    command.hasParameter("daysPastDue") ? command.integerValueOfParameterNamed("daysPastDue") : null,
                    command.stringValueOfParameterNamed("worstStatus12Months"),
                    command.stringValueOfParameterNamed("worstStatus24Months"));
        }
    }

    /**
     * Update enquiry information from manual command.
     */
    private void updateEnquiryInformationFromCommand(ClientCreditReportDetails creditReport, JsonCommand command) {
        if (command.hasParameter("enquiriesLast30Days") || command.hasParameter("enquiriesLast90Days") 
                || command.hasParameter("enquiriesLast12Months")) {

            // Use integerValueOfParameterNamed to preserve 0 values for enquiry fields
            creditReport.updateEnquiryInformation(
                    command.hasParameter("enquiriesLast30Days") ? command.integerValueOfParameterNamed("enquiriesLast30Days") : null,
                    command.hasParameter("enquiriesLast90Days") ? command.integerValueOfParameterNamed("enquiriesLast90Days") : null,
                    command.hasParameter("enquiriesLast12Months") ? command.integerValueOfParameterNamed("enquiriesLast12Months") : null);
        }
    }

    /**
     * Create credit scores from command - supports both single score (backward compatibility) and multiple scores array.
     * Following Java Backend KB: Service method patterns with proper validation
     */
    private void createCreditScoresFromCommand(ClientCreditReportDetails creditReport, JsonCommand command) {
        // Check for multiple scores array (new format)
        if (command.hasParameter("creditScores") && command.arrayOfParameterNamed("creditScores") != null) {
            final JsonArray scoresArray = command.arrayOfParameterNamed("creditScores");
            
            log.info("Processing {} credit scores for report {}", scoresArray.size(), creditReport.getId());
            
            for (int i = 0; i < scoresArray.size(); i++) {
                final JsonObject scoreObject = scoresArray.get(i).getAsJsonObject();
                
                try {
                    String scoreModel = scoreObject.has("scoreModel") ? scoreObject.get("scoreModel").getAsString() : "MANUAL";
                    String scoreVersion = scoreObject.has("scoreVersion") ? scoreObject.get("scoreVersion").getAsString() : "1.0";
                    String scoreName = scoreObject.has("scoreName") ? scoreObject.get("scoreName").getAsString() : "Manual Entry";
                    Integer creditScore = scoreObject.has("creditScore") ? scoreObject.get("creditScore").getAsInt() : null;
                    String scoreReason = scoreObject.has("scoreReason") ? scoreObject.get("scoreReason").getAsString() : null;
                    
                    if (creditScore != null) {
                        final ClientCreditScoreDetails scoreDetails = ClientCreditScoreDetails.createScore(
                            creditReport, scoreModel, scoreVersion, scoreName, creditScore, LocalDate.now());
                        
                        if (scoreReason != null) {
                            scoreDetails.setScoreReason(scoreReason);
                        }
                        
                        creditReport.addCreditScore(scoreDetails);
                    }
                } catch (Exception e) {
                    log.warn("Failed to process credit score at index {}: {}", i, e.getMessage());
                }
            }
        }
        // Fallback to single score format (backward compatibility)
        else if (command.hasParameter("creditScore")) {
            String scoreModel = command.hasParameter("scoreModel") ? command.stringValueOfParameterNamed("scoreModel") : "MANUAL";
            String scoreVersion = command.hasParameter("scoreVersion") ? command.stringValueOfParameterNamed("scoreVersion") : "1.0";
            String scoreName = command.hasParameter("scoreName") ? command.stringValueOfParameterNamed("scoreName") : "Manual Entry";
            LocalDate scoreDate = command.hasParameter("scoreDate") ? command.localDateValueOfParameterNamed("scoreDate") : LocalDate.now();

            final ClientCreditScoreDetails scoreDetails = ClientCreditScoreDetails.createScore(
                creditReport, scoreModel, scoreVersion, scoreName, command.integerValueOfParameterNamedDefaultToNullIfZero("creditScore"), scoreDate);

            if (command.hasParameter("scoreReason")) {
                scoreDetails.setScoreReason(command.stringValueOfParameterNamed("scoreReason"));
            }

            creditReport.addCreditScore(scoreDetails);
        }
    }

    /**
     * Update credit scores from command - supports multiple scores
     * Following Database KB: Proper transaction management and constraint handling
     */
    private void updateCreditScoresFromCommand(ClientCreditReportDetails creditReport, JsonCommand command) {
        // Check for multiple scores array (new format)
        if (command.hasParameter("creditScores") && command.arrayOfParameterNamed("creditScores") != null) {
            final JsonArray scoresArray = command.arrayOfParameterNamed("creditScores");
            
            log.info("Updating {} credit scores for report {}", scoresArray.size(), creditReport.getId());
            
            // Get existing credit scores mapped by scoreModel for efficient lookup
            Map<String, ClientCreditScoreDetails> existingScoresByModel = new HashMap<>();
            for (ClientCreditScoreDetails existingScore : creditReport.getCreditScores()) {
                existingScoresByModel.put(existingScore.getScoreModel(), existingScore);
            }
            log.info("Found {} existing credit scores for report {}", existingScoresByModel.size(), creditReport.getId());
            
            // Track which score models are in the new request
            Set<String> requestedScoreModels = new HashSet<>();
            
            // Process each score in the request
            for (int i = 0; i < scoresArray.size(); i++) {
                final JsonObject scoreObject = scoresArray.get(i).getAsJsonObject();
                
                try {
                    String scoreModel = scoreObject.has("scoreModel") ? scoreObject.get("scoreModel").getAsString() : "MANUAL";
                    String scoreVersion = scoreObject.has("scoreVersion") ? scoreObject.get("scoreVersion").getAsString() : "1.0";
                    String scoreName = scoreObject.has("scoreName") ? scoreObject.get("scoreName").getAsString() : "Manual Entry";
                    Integer creditScore = scoreObject.has("creditScore") ? scoreObject.get("creditScore").getAsInt() : null;
                    String scoreReason = scoreObject.has("scoreReason") ? scoreObject.get("scoreReason").getAsString() : null;
                    
                    log.info("PROCESSING score {}: model={}, version={}, name={}, score={}", 
                        i, scoreModel, scoreVersion, scoreName, creditScore);
                    
                    if (creditScore != null) {
                        requestedScoreModels.add(scoreModel);
                        
                        // Check if this score model already exists
                        ClientCreditScoreDetails existingScore = existingScoresByModel.get(scoreModel);
                        
                        if (existingScore != null) {
                            // UPDATE existing score
                            log.info("UPDATING existing score for model={}, old score={}, new score={}", 
                                scoreModel, existingScore.getCreditScore(), creditScore);
                            
                            existingScore.setCreditScore(creditScore);
                            existingScore.setScoreVersion(scoreVersion);
                            existingScore.setScoreName(scoreName);
                            existingScore.setScoreReason(scoreReason);
                            existingScore.setScoreDate(LocalDate.now());
                            
                            log.info("UPDATED existing score for model={}", scoreModel);
                        } else {
                            // ADD new score
                            log.info("ADDING new score for model={}, score={}", scoreModel, creditScore);
                            
                            final ClientCreditScoreDetails newScoreDetails = ClientCreditScoreDetails.createScore(
                                creditReport, scoreModel, scoreVersion, scoreName, creditScore, LocalDate.now());
                            
                            if (scoreReason != null) {
                                newScoreDetails.setScoreReason(scoreReason);
                            }
                            
                            creditReport.addCreditScore(newScoreDetails);
                            log.info("ADDED new score for model={}", scoreModel);
                        }
                    } else {
                        log.warn("SKIPPING score {} because creditScore is null", i);
                    }
                } catch (Exception e) {
                    log.warn("Failed to process credit score at index {}: {}", i, e.getMessage());
                }
            }
            
            // REMOVE scores that are no longer in the request
            List<ClientCreditScoreDetails> scoresToRemove = new ArrayList<>();
            for (String existingModel : existingScoresByModel.keySet()) {
                if (!requestedScoreModels.contains(existingModel)) {
                    ClientCreditScoreDetails scoreToRemove = existingScoresByModel.get(existingModel);
                    scoresToRemove.add(scoreToRemove);
                    log.info("MARKING for removal: score model={} (no longer in request)", existingModel);
                }
            }
            
            // Remove obsolete scores
            for (ClientCreditScoreDetails scoreToRemove : scoresToRemove) {
                log.info("REMOVING obsolete score for model={}", scoreToRemove.getScoreModel());
                creditReport.getCreditScores().remove(scoreToRemove);
            }
            
            // Recalculate primary score after updating all scores
            log.info("RECALCULATING primary score for report {}", creditReport.getId());
            creditReport.recalculatePrimaryScore();
            log.info("COMPLETED credit score update for report {} - {} scores processed, {} removed", 
                creditReport.getId(), requestedScoreModels.size(), scoresToRemove.size());
        }
        // Fallback to single score format (backward compatibility)
        else if (command.hasParameter("creditScore")) {
            String scoreModel = command.hasParameter("scoreModel") ? command.stringValueOfParameterNamed("scoreModel") : "MANUAL";
            String scoreVersion = command.hasParameter("scoreVersion") ? command.stringValueOfParameterNamed("scoreVersion") : "1.0";
            String scoreName = command.hasParameter("scoreName") ? command.stringValueOfParameterNamed("scoreName") : "Manual Entry";
            LocalDate scoreDate = command.hasParameter("scoreDate") ? command.localDateValueOfParameterNamed("scoreDate") : LocalDate.now();
            Integer creditScore = command.integerValueOfParameterNamedDefaultToNullIfZero("creditScore");

            log.info("SINGLE SCORE UPDATE: Processing single score for model={}, score={}", scoreModel, creditScore);
            
            // For single score update, find existing score with same model or create new one
            ClientCreditScoreDetails existingScore = null;
            for (ClientCreditScoreDetails score : creditReport.getCreditScores()) {
                if (scoreModel.equals(score.getScoreModel())) {
                    existingScore = score;
                    break;
                }
            }
            
            if (existingScore != null) {
                // Update existing score
                log.info("UPDATING existing single score for model={}", scoreModel);
                existingScore.setCreditScore(creditScore);
                existingScore.setScoreVersion(scoreVersion);
                existingScore.setScoreName(scoreName);
                existingScore.setScoreDate(scoreDate);
                
                if (command.hasParameter("scoreReason")) {
                    existingScore.setScoreReason(command.stringValueOfParameterNamed("scoreReason"));
                }
            } else {
                // Add new score
                log.info("ADDING new single score for model={}", scoreModel);
                final ClientCreditScoreDetails scoreDetails = ClientCreditScoreDetails.createScore(
                    creditReport, scoreModel, scoreVersion, scoreName, creditScore, scoreDate);

                if (command.hasParameter("scoreReason")) {
                    scoreDetails.setScoreReason(command.stringValueOfParameterNamed("scoreReason"));
                }

                creditReport.addCreditScore(scoreDetails);
            }
            
            creditReport.recalculatePrimaryScore();
            log.info("COMPLETED single score update for model={}", scoreModel);
        }
    }

    /**
     * Process additional data from command.
     */
    private void updateAdditionalDataFromCommand(ClientCreditReportDetails creditReport, JsonCommand command) {
        if (command.hasParameter("additionalData")) {
            try {
                // Get the additional data as a string and parse it as JSON
                final String additionalDataString = command.stringValueOfParameterNamed("additionalData");
                log.info("PROCESSING additional data: {}", additionalDataString);
                
                if (additionalDataString != null && !additionalDataString.trim().isEmpty()) {
                    // Parse the JSON string into a JsonNode
                    final JsonNode additionalDataNode = this.objectMapper.readTree(additionalDataString);
                    creditReport.setAdditionalDataFromJson(additionalDataNode);
                    log.info("SAVED additional data for credit report {}: {}", creditReport.getId(), additionalDataNode);
                } else {
                    log.info("CLEARING additional data for credit report {} (empty or null)", creditReport.getId());
                    creditReport.setAdditionalDataFromJson(null);
                }
            } catch (Exception e) {
                log.error("Failed to process additional data for credit report {}: {}", creditReport.getId(), e.getMessage(), e);
                // Don't fail the entire update if additional data parsing fails
            }
        }
    }

    /**
     * Handle data integrity issues following Fineract patterns
     * This method converts database constraint violations into user-friendly error messages
     */
    private RuntimeException handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        final String mostSpecificMessage = realCause.getMessage().toLowerCase();
        
        // Handle duplicate credit score constraint
        if (mostSpecificMessage.contains("uk_extend_credit_score_report_model") || 
            mostSpecificMessage.contains("duplicate entry") && mostSpecificMessage.contains("credit_report_id")) {
            
            final String defaultMessage = "A credit score with this model already exists for this report. Please use a different score model or update the existing score.";
            return new PlatformDataIntegrityException("error.msg.credit.score.duplicate.model", 
                defaultMessage, "scoreModel", "Duplicate credit score model for this report");
        }
        
        // Handle other potential constraints
        if (mostSpecificMessage.contains("fk_extend_credit_score_report")) {
            final String defaultMessage = "Credit score cannot be saved because the associated credit report does not exist.";
            return new PlatformDataIntegrityException("error.msg.credit.score.invalid.report", 
                defaultMessage, "creditReportId", "Invalid credit report reference");
        }
        
        // Generic data integrity error
        log.error("Unhandled data integrity issue: {}", realCause.getMessage());
        final String defaultMessage = "Data integrity constraint violation: " + realCause.getMessage();
        return new PlatformDataIntegrityException("error.msg.data.integrity.issue", 
            defaultMessage, "data", realCause.getMessage());
    }
}
