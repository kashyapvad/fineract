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
package org.apache.fineract.extend.kyc.data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.extend.kyc.domain.KycVerificationMethod;

/**
 * Data Transfer Object for Guarantor KYC operations.
 *
 * This DTO represents guarantor KYC details and verification status for API responses and supports both API-based and
 * manual verification workflows.
 */
@Data
@NoArgsConstructor
public class GuarantorKycData implements Serializable {

    private static final long serialVersionUID = 1L;

    // Basic Information
    private Long id;
    private Long clientId;
    private String clientName;
    private String clientDisplayName;

    // Guarantor Core Identity
    private String fullName;
    private String mobileNumber;
    private String relationshipToClient;

    // KYC Document Information
    private String panNumber;
    private String aadhaarNumber;

    // Verification Status
    private Boolean panVerified;
    private Boolean aadhaarVerified;

    // OTP Verification Fields
    private Boolean aadhaarOtpVerified;
    private String otpClientId;
    private LocalDateTime otpLastRequestedOn;
    private LocalDateTime otpVerifiedOn;

    // Manual Verification Fields
    private Boolean panManuallyVerified;
    private Boolean aadhaarManuallyVerified;
    private String manualVerificationReason;

    // Verification Metadata
    private KycVerificationMethod verificationMethod;
    private String verificationMethodCode;
    private String verificationMethodDescription;
    private LocalDate lastVerifiedOn;
    private String verificationProvider;
    private Long verifiedByUserId;
    private String verifiedByUsername;

    // API Response Data (as String to avoid JsonNode serialization issues)
    private String apiResponseData;
    private String verificationNotes;

    // Status Fields
    private Boolean isActive;
    private Boolean isPrimaryGuarantor;

    // Audit Information
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private Long createdByUserId;
    private Long lastModifiedByUserId;

    /**
     * Constructor for complete Guarantor KYC data.
     */
    public GuarantorKycData(Long id, Long clientId, String clientName, String clientDisplayName, String fullName, String mobileNumber,
            String relationshipToClient, String panNumber, String aadhaarNumber, Boolean panVerified, Boolean aadhaarVerified,
            Boolean aadhaarOtpVerified, Boolean panManuallyVerified, Boolean aadhaarManuallyVerified,
            KycVerificationMethod verificationMethod, LocalDate lastVerifiedOn, Long verifiedByUserId, String verifiedByUsername,
            Boolean isActive, Boolean isPrimaryGuarantor) {
        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientDisplayName = clientDisplayName;
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.relationshipToClient = relationshipToClient;
        this.panNumber = panNumber;
        this.aadhaarNumber = aadhaarNumber;
        this.panVerified = panVerified;
        this.aadhaarVerified = aadhaarVerified;
        this.aadhaarOtpVerified = aadhaarOtpVerified;
        this.panManuallyVerified = panManuallyVerified;
        this.aadhaarManuallyVerified = aadhaarManuallyVerified;
        this.verificationMethod = verificationMethod;
        this.verificationMethodCode = verificationMethod != null ? verificationMethod.getCode() : null;
        this.verificationMethodDescription = verificationMethod != null ? verificationMethod.getDescription() : null;
        this.lastVerifiedOn = lastVerifiedOn;
        this.verifiedByUserId = verifiedByUserId;
        this.verifiedByUsername = verifiedByUsername;
        this.isActive = isActive;
        this.isPrimaryGuarantor = isPrimaryGuarantor;
    }

    /**
     * Creates template data for Guarantor KYC operations.
     */
    public static GuarantorKycData template() {
        GuarantorKycData template = new GuarantorKycData();
        // Template data includes available verification methods, etc.
        return template;
    }

    /**
     * Creates template data for a specific client.
     */
    public static GuarantorKycData template(Long clientId, String clientDisplayName) {
        GuarantorKycData template = new GuarantorKycData();
        template.clientId = clientId;
        template.clientDisplayName = clientDisplayName;
        return template;
    }
}
