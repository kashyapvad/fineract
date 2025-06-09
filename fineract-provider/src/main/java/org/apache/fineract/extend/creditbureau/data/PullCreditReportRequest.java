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

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.extend.creditbureau.domain.CreditBureauReportType;

/**
 * Request DTO for pulling credit bureau reports.
 *
 * This request contains the necessary information to initiate a credit bureau report pull from external providers.
 */
@Data
@NoArgsConstructor
public class PullCreditReportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Type of credit report to pull. Must be one of: FULL_REPORT, CREDIT_SCORE, DATA_PULL
     */
    @NotNull(message = "Report type is required")
    @JsonProperty("reportType")
    private String reportType;

    /**
     * Credit bureau provider to use. If not specified, will use default provider from configuration.
     */
    @JsonProperty("provider")
    private String provider;

    /**
     * Additional notes for the credit report request.
     */
    @JsonProperty("notes")
    private String notes;

    /**
     * PAN number to use for the credit report. If not provided, will use client's KYC details.
     */
    @JsonProperty("panNumber")
    private String panNumber;

    /**
     * Aadhaar number to use for the credit report. If not provided, will use client's KYC details.
     */
    @JsonProperty("aadhaarNumber")
    private String aadhaarNumber;

    /**
     * Date format for the request. Used for API requests that require specific date formats.
     */
    @JsonProperty("dateFormat")
    private String dateFormat = "dd MMMM yyyy";

    /**
     * Locale for the request. Used for API requests that require specific locale.
     */
    @JsonProperty("locale")
    private String locale = "en";

    /**
     * Constructor with required fields.
     */
    public PullCreditReportRequest(String reportType) {
        this.reportType = reportType;
    }

    /**
     * Constructor with all common fields.
     */
    public PullCreditReportRequest(String reportType, String provider, String notes) {
        this.reportType = reportType;
        this.provider = provider;
        this.notes = notes;
    }

    /**
     * Gets the report type as enum.
     */
    public CreditBureauReportType getReportTypeEnum() {
        return reportType != null ? CreditBureauReportType.fromCode(reportType) : null;
    }

    /**
     * Validates if the request has required PAN number.
     */
    public boolean hasPanNumber() {
        return panNumber != null && !panNumber.trim().isEmpty();
    }

    /**
     * Validates if the request has required Aadhaar number.
     */
    public boolean hasAadhaarNumber() {
        return aadhaarNumber != null && !aadhaarNumber.trim().isEmpty();
    }
}
