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

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Provider-agnostic request DTO for credit bureau operations.
 *
 * This DTO abstracts customer information needed for credit bureau requests across different providers (Decentro,
 * Experian, Equifax, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditBureauProviderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique reference ID for this request. Used for tracking and correlation across systems.
     */
    private String referenceId;

    /**
     * Customer consent for credit bureau access. Required by most providers and regulatory compliance.
     */
    private Boolean consent;

    /**
     * Client/Customer unique identifier in Fineract.
     */
    private Long clientId;

    /**
     * Customer's full name.
     */
    private String customerName;

    /**
     * Customer's mobile number. Format: 10 digits starting with 6,7,8,9 for Indian providers
     */
    private String mobileNumber;

    /**
     * Customer's PAN number (if available). Used for higher accuracy in credit reports.
     */
    private String panNumber;

    /**
     * Customer's Aadhaar number (if available). Used for identity verification and data matching.
     */
    private String aadhaarNumber;

    /**
     * Customer's email address (if available).
     */
    private String emailAddress;

    /**
     * Customer's date of birth (if available). Format: YYYY-MM-DD
     */
    private String dateOfBirth;

    /**
     * Type of report requested. Values: FULL_REPORT, CREDIT_SCORE, DATA_PULL
     */
    private String reportType;

    /**
     * Customer's address information.
     */
    private AddressInformation address;

    /**
     * Provider-specific additional parameters. Allows for provider-specific customization without changing interface.
     */
    private java.util.Map<String, Object> additionalParameters;

    /**
     * Address information for credit bureau requests.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInformation implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * Address type: H (Home), O (Office), X (Others)
         */
        private String addressType;

        /**
         * Address line 1
         */
        private String addressLine1;

        /**
         * Address line 2
         */
        private String addressLine2;

        /**
         * City name
         */
        private String city;

        /**
         * State name
         */
        private String state;

        /**
         * PIN code (6 digits for India)
         */
        private String pincode;

        /**
         * Country code (default: IN for India)
         */
        private String country;
    }
}
