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
package org.apache.fineract.extend.creditbureau.data;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.extend.creditbureau.domain.CreditBureauReportStatus;
import org.apache.fineract.extend.creditbureau.domain.CreditBureauReportType;

/**
 * Data Transfer Object for Client Credit Bureau operations.
 *
 * This DTO represents credit bureau report data for API responses and supports multiple credit bureau providers through
 * generic structure.
 */
@Data
@NoArgsConstructor
public class ClientCreditBureauData implements Serializable {

    private static final long serialVersionUID = 1L;

    // Basic Information
    private Long id;
    private Long clientId;
    private String clientName;
    private String clientDisplayName;

    // Report Details
    private CreditBureauReportType reportType;
    private String reportTypeCode;
    private String reportTypeDescription;

    private CreditBureauReportStatus reportStatus;
    private String reportStatusCode;
    private String reportStatusDescription;

    // Provider Information
    private String creditBureauProvider;
    private String providerReportId;

    // Credit Score Information
    private Integer creditScore;
    private String creditRating;
    private LocalDate creditScoreDate;

    // Enhanced Credit Scores (detailed scores from separate table)
    private List<CreditScoreData> creditScores;

    // Customer Information
    private String customerName;
    private String customerPan;
    private String customerAadhaar;
    private String customerMobile;
    private String customerAddress;
    private LocalDate dateOfBirth;
    private String gender;

    // Credit Summary Information
    private Integer totalAccounts;
    private Integer activeAccounts;
    private Integer closedAccounts;
    private Integer overdueAccounts;

    // Financial Information
    private BigDecimal totalCreditLimit;
    private BigDecimal totalOutstandingAmount;
    private BigDecimal totalOverdueAmount;
    private BigDecimal highestCreditAmount;

    // Delinquency Information
    private Integer daysPastDue;
    private String worstStatus12Months;
    private String worstStatus24Months;

    // Enquiry Information
    private Integer enquiriesLast30Days;
    private Integer enquiriesLast90Days;
    private Integer enquiriesLast12Months;

    // Report Metadata
    private LocalDate reportGeneratedOn;
    private LocalDateTime requestedOn;
    private Long requestedByUserId;
    private String requestedByUsername;

    // API Response Data
    private JsonNode reportData;
    private JsonNode additionalData;
    private String reportSummary;
    private String reportNotes;

    // Error Information (for failed reports)
    private String errorCode;
    private String errorMessage;

    // Audit Information
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private Long createdByUserId;
    private Long lastModifiedByUserId;

    /**
     * Constructor for successful credit report data.
     */
    public ClientCreditBureauData(Long id, Long clientId, String clientName, String clientDisplayName, CreditBureauReportType reportType,
            CreditBureauReportStatus reportStatus, String creditBureauProvider, String providerReportId, Integer creditScore,
            String creditRating, LocalDate creditScoreDate, LocalDate reportGeneratedOn, LocalDateTime requestedOn, Long requestedByUserId,
            String requestedByUsername, JsonNode reportData, JsonNode additionalData, String reportSummary) {
        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientDisplayName = clientDisplayName;
        this.reportType = reportType;
        this.reportTypeCode = reportType != null ? reportType.getCode() : null;
        this.reportTypeDescription = reportType != null ? reportType.getDescription() : null;
        this.reportStatus = reportStatus;
        this.reportStatusCode = reportStatus != null ? reportStatus.getCode() : null;
        this.reportStatusDescription = reportStatus != null ? reportStatus.getDescription() : null;
        this.creditBureauProvider = creditBureauProvider;
        this.providerReportId = providerReportId;
        this.creditScore = creditScore;
        this.creditRating = creditRating;
        this.creditScoreDate = creditScoreDate;
        this.reportGeneratedOn = reportGeneratedOn;
        this.requestedOn = requestedOn;
        this.requestedByUserId = requestedByUserId;
        this.requestedByUsername = requestedByUsername;
        this.reportData = reportData;
        this.additionalData = additionalData;
        this.reportSummary = reportSummary;
    }

    /**
     * Constructor for failed credit report data.
     */
    public ClientCreditBureauData(Long clientId, String clientName, CreditBureauReportType reportType, String creditBureauProvider,
            String errorCode, String errorMessage, LocalDateTime requestedOn, Long requestedByUserId) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.reportType = reportType;
        this.reportTypeCode = reportType != null ? reportType.getCode() : null;
        this.reportTypeDescription = reportType != null ? reportType.getDescription() : null;
        this.reportStatus = CreditBureauReportStatus.FAILURE;
        this.reportStatusCode = CreditBureauReportStatus.FAILURE.getCode();
        this.reportStatusDescription = CreditBureauReportStatus.FAILURE.getDescription();
        this.creditBureauProvider = creditBureauProvider;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.requestedOn = requestedOn;
        this.requestedByUserId = requestedByUserId;
    }

    /**
     * Checks if the report was successfully generated.
     */
    public boolean isSuccessful() {
        return CreditBureauReportStatus.SUCCESS.equals(this.reportStatus);
    }

    /**
     * Checks if the report generation failed.
     */
    public boolean isFailed() {
        return CreditBureauReportStatus.FAILURE.equals(this.reportStatus);
    }

    /**
     * Checks if the report generation is pending.
     */
    public boolean isPending() {
        return CreditBureauReportStatus.PENDING.equals(this.reportStatus);
    }

    /**
     * Checks if this report includes credit score data.
     */
    public boolean hasCreditScore() {
        return this.creditScore != null && this.creditScore > 0;
    }

    /**
     * Gets a human-readable summary of the credit report.
     */
    public String getDisplaySummary() {
        if (isFailed()) {
            return String.format("Failed: %s", errorMessage != null ? errorMessage : "Unknown error");
        }
        if (isPending()) {
            return "Report generation in progress";
        }
        if (hasCreditScore()) {
            return String.format("Credit Score: %d (%s)", creditScore, creditRating);
        }
        return "Credit report available";
    }

    /**
     * Checks if this report has additional data.
     */
    public boolean hasAdditionalData() {
        return this.additionalData != null && !this.additionalData.isNull() && !this.additionalData.isEmpty();
    }

    /**
     * Checks if this report has raw provider response data.
     */
    public boolean hasProviderData() {
        return this.reportData != null && !this.reportData.isNull() && !this.reportData.isEmpty();
    }

    /**
     * Determines if this is a manual report (has additional data but no provider data).
     */
    public boolean isManualReport() {
        return hasAdditionalData() && !hasProviderData();
    }

    /**
     * Determines if this is a provider report with additional user data.
     */
    public boolean isProviderReportWithAdditionalData() {
        return hasProviderData() && hasAdditionalData();
    }
}
