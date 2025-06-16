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

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.fineract.extend.converter.PostgresJsonbConverter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.useradministration.domain.AppUser;

/**
 * Entity representing comprehensive client credit report details.
 *
 * This entity stores the main credit report data including customer information, account summaries, financial details,
 * and report metadata. Credit scores are stored separately in ClientCreditScoreDetails entities with a one-to-many
 * relationship.
 *
 * Supports multiple providers like Decentro, CIBIL, Equifax, etc. through provider-agnostic design.
 */
@Entity
@Table(name = "m_extend_client_credit_report")
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class ClientCreditReportDetails extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private CreditBureauReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    private CreditBureauReportStatus reportStatus;

    @Column(name = "credit_report_provider", length = 100)
    private String creditBureauProvider;

    @Column(name = "provider_report_id", length = 255)
    private String providerReportId;

    // Primary Score Information (highest/best score for quick access)
    @Column(name = "primary_credit_score")
    private Integer primaryCreditScore;

    @Column(name = "primary_credit_rating", length = 50)
    private String primaryCreditRating;

    // Credit Report Summary (detailed scores are in separate table)
    @Column(name = "total_score_models")
    private Integer totalScoreModels = 0;

    // Customer Information (from API response)
    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "customer_pan", length = 10)
    private String customerPan;

    @Column(name = "customer_aadhaar", length = 12)
    private String customerAadhaar;

    @Column(name = "customer_mobile", length = 15)
    private String customerMobile;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    // Credit Summary Information
    @Column(name = "total_accounts")
    private Integer totalAccounts;

    @Column(name = "active_accounts")
    private Integer activeAccounts;

    @Column(name = "closed_accounts")
    private Integer closedAccounts;

    @Column(name = "overdue_accounts")
    private Integer overdueAccounts;

    // Financial Information
    @Column(name = "total_credit_limit", precision = 19, scale = 6)
    private BigDecimal totalCreditLimit;

    @Column(name = "total_outstanding_amount", precision = 19, scale = 6)
    private BigDecimal totalOutstandingAmount;

    @Column(name = "total_overdue_amount", precision = 19, scale = 6)
    private BigDecimal totalOverdueAmount;

    @Column(name = "highest_credit_amount", precision = 19, scale = 6)
    private BigDecimal highestCreditAmount;

    // Delinquency Information
    @Column(name = "days_past_due")
    private Integer daysPastDue;

    @Column(name = "worst_status_12_months", length = 20)
    private String worstStatus12Months;

    @Column(name = "worst_status_24_months", length = 20)
    private String worstStatus24Months;

    // Enquiry Information
    @Column(name = "enquiries_last_30_days")
    private Integer enquiriesLast30Days;

    @Column(name = "enquiries_last_90_days")
    private Integer enquiriesLast90Days;

    @Column(name = "enquiries_last_12_months")
    private Integer enquiriesLast12Months;

    // Report Metadata
    @Column(name = "report_generated_on")
    private LocalDate reportGeneratedOn;

    @Column(name = "requested_on")
    private LocalDateTime requestedOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id")
    private AppUser requestedByUser;

    @Column(name = "report_summary", columnDefinition = "TEXT")
    private String reportSummary;

    @Column(name = "report_notes", columnDefinition = "TEXT")
    private String reportNotes;

    // Error Information
    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // One-to-Many relationship with credit scores
    @OneToMany(mappedBy = "creditReport", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClientCreditScoreDetails> creditScores = new ArrayList<>();

    // Keep raw response for audit and debugging purposes
    @Convert(converter = PostgresJsonbConverter.class)
    @Column(name = "raw_provider_response", columnDefinition = "JSONB")
    private JsonNode rawProviderResponse;

    // Additional data field for manual entry and supplemental information
    @Convert(converter = PostgresJsonbConverter.class)
    @Column(name = "additional_data", columnDefinition = "JSONB")
    private JsonNode additionalData;

    /**
     * Static factory method for creating successful credit bureau reports.
     */
    public static ClientCreditReportDetails createSuccessfulReport(Client client, CreditBureauReportType reportType,
            String creditBureauProvider, String providerReportId, JsonNode rawResponse, AppUser requestedByUser) {

        return new ClientCreditReportDetails().setClient(client).setReportType(reportType).setReportStatus(CreditBureauReportStatus.SUCCESS)
                .setCreditBureauProvider(creditBureauProvider).setProviderReportId(providerReportId).setRawProviderResponse(rawResponse)
                .setRequestedOn(LocalDateTime.now()).setRequestedByUser(requestedByUser).setReportGeneratedOn(LocalDate.now());
    }

    /**
     * Static factory method for creating failed credit bureau reports.
     */
    public static ClientCreditReportDetails createFailedReport(Client client, CreditBureauReportType reportType,
            String creditBureauProvider, String errorCode, String errorMessage, AppUser requestedByUser) {

        return new ClientCreditReportDetails().setClient(client).setReportType(reportType).setReportStatus(CreditBureauReportStatus.FAILURE)
                .setCreditBureauProvider(creditBureauProvider).setErrorCode(errorCode).setErrorMessage(errorMessage)
                .setRequestedOn(LocalDateTime.now()).setRequestedByUser(requestedByUser);
    }

    /**
     * Static factory method for creating pending credit bureau reports.
     */
    public static ClientCreditReportDetails createPendingReport(Client client, CreditBureauReportType reportType,
            String creditBureauProvider, String providerReportId, AppUser requestedByUser) {

        return new ClientCreditReportDetails().setClient(client).setReportType(reportType).setReportStatus(CreditBureauReportStatus.PENDING)
                .setCreditBureauProvider(creditBureauProvider).setProviderReportId(providerReportId).setRequestedOn(LocalDateTime.now())
                .setRequestedByUser(requestedByUser);
    }

    /**
     * Marks the report as successful and sets basic report metadata.
     */
    public void markAsSuccessful(JsonNode rawResponse, LocalDate reportGeneratedOn) {
        this.reportStatus = CreditBureauReportStatus.SUCCESS;
        this.rawProviderResponse = rawResponse;
        this.reportGeneratedOn = reportGeneratedOn;
        this.errorCode = null;
        this.errorMessage = null;
    }

    /**
     * Updates customer information from provider response.
     */
    public void updateCustomerInformation(String customerName, String customerPan, String customerAadhaar, String customerMobile,
            String customerAddress, LocalDate dateOfBirth, String gender) {
        this.customerName = customerName;
        this.customerPan = customerPan;
        this.customerAadhaar = customerAadhaar;
        this.customerMobile = customerMobile;
        this.customerAddress = customerAddress;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    /**
     * Updates credit summary information from provider response. Only updates non-null values to allow partial updates
     * without overwriting existing data.
     */
    public void updateCreditSummary(Integer totalAccounts, Integer activeAccounts, Integer closedAccounts, Integer overdueAccounts,
            BigDecimal totalCreditLimit, BigDecimal totalOutstandingAmount, BigDecimal totalOverdueAmount, BigDecimal highestCreditAmount) {

        // Only update account fields if they are not null
        if (totalAccounts != null) {
            this.totalAccounts = totalAccounts;
        }
        if (activeAccounts != null) {
            this.activeAccounts = activeAccounts;
        }
        if (closedAccounts != null) {
            this.closedAccounts = closedAccounts;
        }
        if (overdueAccounts != null) {
            this.overdueAccounts = overdueAccounts;
        }

        // Only update financial fields if they are not null
        if (totalCreditLimit != null) {
            this.totalCreditLimit = totalCreditLimit;
        }
        if (totalOutstandingAmount != null) {
            this.totalOutstandingAmount = totalOutstandingAmount;
        }
        if (totalOverdueAmount != null) {
            this.totalOverdueAmount = totalOverdueAmount;
        }
        if (highestCreditAmount != null) {
            this.highestCreditAmount = highestCreditAmount;
        }
    }

    /**
     * Updates delinquency information from provider response.
     */
    public void updateDelinquencyInformation(Integer daysPastDue, String worstStatus12Months, String worstStatus24Months) {
        this.daysPastDue = daysPastDue;
        this.worstStatus12Months = worstStatus12Months;
        this.worstStatus24Months = worstStatus24Months;
    }

    /**
     * Updates enquiry information from provider response.
     */
    public void updateEnquiryInformation(Integer enquiriesLast30Days, Integer enquiriesLast90Days, Integer enquiriesLast12Months) {
        this.enquiriesLast30Days = enquiriesLast30Days;
        this.enquiriesLast90Days = enquiriesLast90Days;
        this.enquiriesLast12Months = enquiriesLast12Months;
    }

    /**
     * Adds a credit score to this report and updates primary score if it's the highest.
     */
    public void addCreditScore(ClientCreditScoreDetails scoreDetails) {
        scoreDetails.setCreditReport(this);
        this.creditScores.add(scoreDetails);

        // Update primary score if this is the highest score
        if (this.primaryCreditScore == null || scoreDetails.getCreditScore() > this.primaryCreditScore) {
            this.primaryCreditScore = scoreDetails.getCreditScore();
            this.primaryCreditRating = scoreDetails.getScoreRating();
        }

        // Update total score models count
        this.totalScoreModels = this.creditScores.size();
    }

    /**
     * Removes all credit scores and resets primary score information.
     */
    public void clearCreditScores() {
        this.creditScores.clear();
        this.primaryCreditScore = null;
        this.primaryCreditRating = null;
        this.totalScoreModels = 0;
    }

    /**
     * Recalculates primary score from all credit scores.
     */
    public void recalculatePrimaryScore() {
        if (this.creditScores.isEmpty()) {
            this.primaryCreditScore = null;
            this.primaryCreditRating = null;
            this.totalScoreModels = 0;
            return;
        }

        // Find the highest credit score
        ClientCreditScoreDetails bestScore = this.creditScores.stream().filter(score -> score.getCreditScore() != null)
                .max((s1, s2) -> Integer.compare(s1.getCreditScore(), s2.getCreditScore())).orElse(null);

        if (bestScore != null) {
            this.primaryCreditScore = bestScore.getCreditScore();
            this.primaryCreditRating = bestScore.getScoreRating();
        }

        this.totalScoreModels = this.creditScores.size();
    }

    /**
     * Marks the report as failed with error details.
     */
    public void markAsFailed(String errorCode, String errorMessage) {
        this.reportStatus = CreditBureauReportStatus.FAILURE;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.reportGeneratedOn = LocalDate.now();
    }

    // Status Check Methods
    public boolean isSuccessful() {
        return CreditBureauReportStatus.SUCCESS.equals(this.reportStatus);
    }

    public boolean isFailed() {
        return CreditBureauReportStatus.FAILURE.equals(this.reportStatus);
    }

    public boolean isPending() {
        return CreditBureauReportStatus.PENDING.equals(this.reportStatus);
    }

    // Convenience Methods
    public Long getClientId() {
        return this.client != null ? this.client.getId() : null;
    }

    public String getClientName() {
        return this.client != null ? this.client.getDisplayName() : null;
    }

    public Long getRequestedByUserId() {
        return this.requestedByUser != null ? this.requestedByUser.getId() : null;
    }

    public String getRequestedByUsername() {
        return this.requestedByUser != null ? this.requestedByUser.getUsername() : null;
    }

    /**
     * Gets credit score by model and version.
     */
    public ClientCreditScoreDetails getCreditScore(String scoreModel, String scoreVersion) {
        return this.creditScores.stream().filter(
                score -> scoreModel.equals(score.getScoreModel()) && (scoreVersion == null || scoreVersion.equals(score.getScoreVersion())))
                .findFirst().orElse(null);
    }

    /**
     * Gets all credit scores for a specific model.
     */
    public List<ClientCreditScoreDetails> getCreditScoresByModel(String scoreModel) {
        return this.creditScores.stream().filter(score -> scoreModel.equals(score.getScoreModel())).toList();
    }

    /**
     * Checks if the report has any credit scores.
     */
    public boolean hasCreditScores() {
        return !this.creditScores.isEmpty();
    }

    /**
     * Sets additional data for manual entry or supplemental information.
     */
    public void setAdditionalDataFromJson(JsonNode additionalData) {
        this.additionalData = additionalData;
    }

    /**
     * Checks if the report has additional data.
     */
    public boolean hasAdditionalData() {
        return this.additionalData != null && !this.additionalData.isNull() && !this.additionalData.isEmpty();
    }

    /**
     * Checks if the report has raw provider response data.
     */
    public boolean hasRawProviderResponse() {
        return this.rawProviderResponse != null && !this.rawProviderResponse.isNull() && !this.rawProviderResponse.isEmpty();
    }
}
