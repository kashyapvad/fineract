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
package org.apache.fineract.extend.common.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Provider-agnostic response DTO for credit bureau operations.
 *
 * This DTO standardizes credit bureau responses across different providers, allowing business logic to work with any
 * provider seamlessly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditBureauProviderResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Request reference ID for correlation.
     */
    private String referenceId;

    /**
     * Provider-specific transaction ID. Used for tracking and support purposes.
     */
    private String providerTransactionId;

    /**
     * Whether the request was successful.
     */
    private boolean success;

    /**
     * Provider-specific response code.
     */
    private String responseCode;

    /**
     * Human-readable response message.
     */
    private String message;

    /**
     * Credit score (if available).
     */
    private Integer creditScore;

    /**
     * Credit rating/grade (if available).
     */
    private String creditRating;

    /**
     * Date when the report was generated.
     */
    private LocalDate reportGeneratedOn;

    /**
     * Structured summary of the credit report.
     */
    private String reportSummary;

    /**
     * Additional credit information.
     */
    private CreditInformation creditInformation;

    /**
     * Original provider response (for audit and debugging). Stored as JSON for flexibility across providers.
     */
    private JsonNode rawProviderResponse;

    /**
     * Error information (if request failed).
     */
    private ErrorInformation error;

    /**
     * Provider metadata.
     */
    private ProviderMetadata providerMetadata;

    /**
     * Additional structured credit information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditInformation implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * Total number of credit accounts.
         */
        private Integer totalAccounts;

        /**
         * Number of active accounts.
         */
        private Integer activeAccounts;

        /**
         * Total credit limit across all accounts.
         */
        private String totalCreditLimit;

        /**
         * Current total outstanding amount.
         */
        private String totalOutstanding;

        /**
         * Number of overdue accounts.
         */
        private Integer overdueAccounts;

        /**
         * Credit utilization ratio.
         */
        private Double creditUtilization;

        /**
         * Payment history score.
         */
        private Integer paymentHistoryScore;

        /**
         * Additional credit metrics.
         */
        private Map<String, Object> additionalMetrics;
    }

    /**
     * Error information for failed requests.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorInformation implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * Error code.
         */
        private String errorCode;

        /**
         * Error message.
         */
        private String errorMessage;

        /**
         * Error category (TECHNICAL, BUSINESS, VALIDATION, etc.).
         */
        private String errorCategory;

        /**
         * Whether the error is retryable.
         */
        private boolean retryable;

        /**
         * Additional error details.
         */
        private Map<String, Object> errorDetails;
    }

    /**
     * Provider metadata information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderMetadata implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * Provider name.
         */
        private String providerName;

        /**
         * Provider version/API version used.
         */
        private String providerVersion;

        /**
         * Response timestamp.
         */
        private String responseTimestamp;

        /**
         * Processing time in milliseconds.
         */
        private Long processingTimeMs;

        /**
         * Provider-specific metadata.
         */
        private Map<String, Object> additionalMetadata;
    }
}
