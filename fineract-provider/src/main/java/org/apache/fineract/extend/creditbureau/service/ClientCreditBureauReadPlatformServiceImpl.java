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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.extend.creditbureau.data.ClientCreditBureauData;
import org.apache.fineract.extend.creditbureau.data.CreditScoreData;
import org.apache.fineract.extend.creditbureau.domain.ClientCreditReportDetails;
import org.apache.fineract.extend.creditbureau.domain.ClientCreditReportDetailsRepositoryWrapper;
import org.apache.fineract.extend.creditbureau.domain.ClientCreditScoreDetails;
import org.apache.fineract.extend.creditbureau.domain.CreditBureauReportType;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ClientCreditBureauReadPlatformService.
 *
 * This service handles all read operations for credit bureau reports including: - Retrieving credit reports for clients
 * - Providing comprehensive credit report details - Supporting the new two-table design (report + scores)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientCreditBureauReadPlatformServiceImpl implements ClientCreditBureauReadPlatformService {

    private final ClientCreditReportDetailsRepositoryWrapper creditReportRepositoryWrapper;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final PlatformSecurityContext context;

    @Override
    @Transactional(readOnly = true)
    public Collection<ClientCreditBureauData> retrieveClientCreditReports(final Long clientId) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB

        try {
            // Validate client exists and belongs to current tenant
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

            // Retrieve all credit reports for the client
            final List<ClientCreditReportDetails> creditReports = this.creditReportRepositoryWrapper.findByClientId(clientId);

            // Map to DTO and return
            return creditReports.stream().map(this::mapToClientCreditBureauData).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error retrieving credit reports for client {}: {}", clientId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClientCreditBureauData retrieveCreditReport(final Long clientId, final Long reportId) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB

        try {
            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

            // Retrieve specific credit report
            final ClientCreditReportDetails creditReport = this.creditReportRepositoryWrapper.findOneThrowExceptionIfNotFound(reportId);

            // Verify the report belongs to the client
            if (!creditReport.getClientId().equals(clientId)) {
                throw new RuntimeException("Credit report does not belong to the specified client");
            }

            // Map to DTO and return
            return mapToClientCreditBureauData(creditReport);

        } catch (Exception e) {
            log.error("Error retrieving credit report {} for client {}: {}", reportId, clientId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Additional method: Retrieve credit reports by type.
     */
    @Transactional(readOnly = true)
    public List<ClientCreditBureauData> retrieveCreditReportsByType(final Long clientId, final String reportTypeCode) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB

        try {
            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

            // Parse report type
            final CreditBureauReportType reportType = CreditBureauReportType.fromCode(reportTypeCode);

            // Retrieve credit reports by type using the correct repository method
            final List<ClientCreditReportDetails> creditReports = this.creditReportRepositoryWrapper.findByClientId(clientId).stream()
                    .filter(report -> reportType.equals(report.getReportType())).collect(Collectors.toList());

            // Map to DTO and return
            return creditReports.stream().map(this::mapToClientCreditBureauData).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error retrieving credit reports by type {} for client {}: {}", reportTypeCode, clientId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Additional method: Retrieve credit reports by provider.
     */
    @Transactional(readOnly = true)
    public List<ClientCreditBureauData> retrieveCreditReportsByProvider(final Long clientId, final String provider) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB

        try {
            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

            // Retrieve credit reports by provider
            final List<ClientCreditReportDetails> creditReports = this.creditReportRepositoryWrapper.findByClientIdAndProvider(clientId,
                    provider);

            // Map to DTO and return
            return creditReports.stream().map(this::mapToClientCreditBureauData).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error retrieving credit reports by provider {} for client {}: {}", provider, clientId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Maps ClientCreditReportDetails entity to ClientCreditBureauData DTO. This maps the new comprehensive entity
     * structure to the existing DTO.
     */
    private ClientCreditBureauData mapToClientCreditBureauData(final ClientCreditReportDetails entity) {
        final ClientCreditBureauData data = new ClientCreditBureauData();

        // Basic Information
        data.setId(entity.getId());
        data.setClientId(entity.getClientId());
        data.setClientName(entity.getCustomerName());
        data.setClientDisplayName(entity.getCustomerName());

        // Report Details
        data.setReportType(entity.getReportType());
        data.setReportTypeCode(entity.getReportType() != null ? entity.getReportType().getCode() : null);
        data.setReportTypeDescription(entity.getReportType() != null ? entity.getReportType().getDescription() : null);

        data.setReportStatus(entity.getReportStatus());
        data.setReportStatusCode(entity.getReportStatus() != null ? entity.getReportStatus().getCode() : null);
        data.setReportStatusDescription(entity.getReportStatus() != null ? entity.getReportStatus().getDescription() : null);

        // Provider Information
        data.setCreditBureauProvider(entity.getCreditBureauProvider());
        data.setProviderReportId(entity.getProviderReportId());

        // Credit Score Information (using primary score from new design)
        data.setCreditScore(entity.getPrimaryCreditScore());
        data.setCreditRating(entity.getPrimaryCreditRating());
        data.setCreditScoreDate(entity.getReportGeneratedOn());

        // Enhanced Credit Scores (detailed scores from separate table)
        final List<CreditScoreData> creditScores = entity.getCreditScores().stream().map(this::mapToCreditScoreData)
                .collect(Collectors.toList());
        data.setCreditScores(creditScores);

        // Customer Information (ADDED - was missing)
        data.setCustomerName(entity.getCustomerName());
        data.setCustomerPan(entity.getCustomerPan());
        data.setCustomerAadhaar(entity.getCustomerAadhaar());
        data.setCustomerMobile(entity.getCustomerMobile());
        data.setCustomerAddress(entity.getCustomerAddress());
        data.setDateOfBirth(entity.getDateOfBirth());
        data.setGender(entity.getGender());

        // Credit Summary Information (ADDED - was missing)
        data.setTotalAccounts(entity.getTotalAccounts());
        data.setActiveAccounts(entity.getActiveAccounts());
        data.setClosedAccounts(entity.getClosedAccounts());
        data.setOverdueAccounts(entity.getOverdueAccounts());

        // Financial Information (ADDED - was missing)
        data.setTotalCreditLimit(entity.getTotalCreditLimit());
        data.setTotalOutstandingAmount(entity.getTotalOutstandingAmount());
        data.setTotalOverdueAmount(entity.getTotalOverdueAmount());
        data.setHighestCreditAmount(entity.getHighestCreditAmount());

        // Delinquency Information (ADDED - was missing)
        data.setDaysPastDue(entity.getDaysPastDue());
        data.setWorstStatus12Months(entity.getWorstStatus12Months());
        data.setWorstStatus24Months(entity.getWorstStatus24Months());

        // Enquiry Information (ADDED - was missing)
        data.setEnquiriesLast30Days(entity.getEnquiriesLast30Days());
        data.setEnquiriesLast90Days(entity.getEnquiriesLast90Days());
        data.setEnquiriesLast12Months(entity.getEnquiriesLast12Months());

        // Report Metadata
        data.setReportGeneratedOn(entity.getReportGeneratedOn());
        data.setRequestedOn(entity.getRequestedOn());
        data.setRequestedByUserId(entity.getRequestedByUserId());
        data.setRequestedByUsername(entity.getRequestedByUsername());

        // API Response Data
        data.setReportData(entity.getRawProviderResponse());
        data.setAdditionalData(entity.getAdditionalData());
        data.setReportSummary(entity.getReportSummary());
        data.setReportNotes(entity.getReportNotes());

        // Error Information
        data.setErrorCode(entity.getErrorCode());
        data.setErrorMessage(entity.getErrorMessage());

        // Audit Information
        data.setCreatedDate(entity.getCreatedDate().map(d -> d.toLocalDateTime()).orElse(null));
        data.setLastModifiedDate(entity.getLastModifiedDate().map(d -> d.toLocalDateTime()).orElse(null));
        data.setCreatedByUserId(entity.getCreatedBy().orElse(null));
        data.setLastModifiedByUserId(entity.getLastModifiedBy().orElse(null));

        return data;
    }

    /**
     * Maps ClientCreditScoreDetails entity to CreditScoreData DTO.
     */
    private CreditScoreData mapToCreditScoreData(final ClientCreditScoreDetails entity) {
        return new CreditScoreData(entity.getScoreModel(), entity.getScoreVersion(), entity.getScoreName(), entity.getCreditScore(),
                entity.getScoreDate(), entity.getScoreRangeMin(), entity.getScoreRangeMax(), entity.getScorePercentile(),
                entity.getScoringElements(), entity.getScoreReason(), entity.getProviderScoreId(), entity.getProviderMetadata());
    }
}
