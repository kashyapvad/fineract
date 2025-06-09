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
 * Data Transfer Object for Client KYC operations.
 *
 * This DTO represents KYC details and verification status for API responses and supports both API-based and manual
 * verification workflows.
 */
@Data
@NoArgsConstructor
public class ClientKycData implements Serializable {

    private static final long serialVersionUID = 1L;

    // Basic Information
    private Long id;
    private Long clientId;
    private String clientName;
    private String clientDisplayName;

    // Document Information
    private String panNumber;
    private String aadhaarNumber;
    private String voterIdNumber;
    private String drivingLicenseNumber;
    private String passportNumber;

    // Document Storage URLs
    private String panDocumentUrl;
    private String aadhaarDocumentUrl;
    private String voterIdDocumentUrl;
    private String drivingLicenseDocumentUrl;
    private String passportDocumentUrl;

    // Individual Document Verification Status
    private Boolean panVerified;
    private Boolean aadhaarVerified;
    private Boolean voterIdVerified;
    private Boolean drivingLicenseVerified;
    private Boolean passportVerified;

    // Verification Metadata
    private KycVerificationMethod verificationMethod;
    private String verificationMethodCode;
    private String verificationMethodDescription;

    private LocalDate lastVerifiedOn;
    private Long lastVerifiedByUserId;
    private String lastVerifiedByUsername;

    // API Verification Details
    private String apiProvider;
    private String apiResponseData;
    private String apiVerificationId;

    // Manual Verification Details
    private String manualVerificationNotes;
    private LocalDate manualVerificationDate;
    private Long manualVerifiedByUserId;
    private String manualVerifiedByUsername;

    // Manual Unverification Details (NEW)
    private LocalDate manualUnverificationDate;
    private Long manualUnverifiedByUserId;
    private String manualUnverifiedByUsername;
    private String manualUnverificationReason;

    // Audit Information
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private Long createdByUserId;
    private Long lastModifiedByUserId;

    /**
     * Constructor for complete KYC data.
     */
    public ClientKycData(Long id, Long clientId, String clientName, String clientDisplayName, String panNumber, String aadhaarNumber,
            String voterIdNumber, String drivingLicenseNumber, String passportNumber, Boolean panVerified, Boolean aadhaarVerified,
            Boolean voterIdVerified, Boolean drivingLicenseVerified, Boolean passportVerified, KycVerificationMethod verificationMethod,
            LocalDate lastVerifiedOn, Long lastVerifiedByUserId, String lastVerifiedByUsername) {
        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientDisplayName = clientDisplayName;
        this.panNumber = panNumber;
        this.aadhaarNumber = aadhaarNumber;
        this.voterIdNumber = voterIdNumber;
        this.drivingLicenseNumber = drivingLicenseNumber;
        this.passportNumber = passportNumber;
        this.panVerified = panVerified;
        this.aadhaarVerified = aadhaarVerified;
        this.voterIdVerified = voterIdVerified;
        this.drivingLicenseVerified = drivingLicenseVerified;
        this.passportVerified = passportVerified;
        this.verificationMethod = verificationMethod;
        this.verificationMethodCode = verificationMethod != null ? verificationMethod.getCode() : null;
        this.verificationMethodDescription = verificationMethod != null ? verificationMethod.getDescription() : null;
        this.lastVerifiedOn = lastVerifiedOn;
        this.lastVerifiedByUserId = lastVerifiedByUserId;
        this.lastVerifiedByUsername = lastVerifiedByUsername;
    }

    /**
     * Creates template data for KYC operations.
     */
    public static ClientKycData template() {
        ClientKycData template = new ClientKycData();
        // Template data includes available verification methods, document types, etc.
        return template;
    }

    /**
     * Creates template data for a specific client.
     */
    public static ClientKycData template(Long clientId, String clientName) {
        ClientKycData template = new ClientKycData();
        template.clientId = clientId;
        template.clientName = clientName;
        return template;
    }

    /**
     * Checks if all required documents are verified.
     */
    public boolean areAllDocumentsVerified() {
        return Boolean.TRUE.equals(panVerified) && Boolean.TRUE.equals(aadhaarVerified);
    }

    /**
     * Checks if any document is verified.
     */
    public boolean isAnyDocumentVerified() {
        return Boolean.TRUE.equals(panVerified) || Boolean.TRUE.equals(aadhaarVerified) || Boolean.TRUE.equals(voterIdVerified)
                || Boolean.TRUE.equals(drivingLicenseVerified) || Boolean.TRUE.equals(passportVerified);
    }

    /**
     * Checks if KYC was verified via API.
     */
    public boolean isApiVerified() {
        return KycVerificationMethod.API.equals(this.verificationMethod);
    }

    /**
     * Checks if KYC was verified manually.
     */
    public boolean isManuallyVerified() {
        return KycVerificationMethod.MANUAL.equals(this.verificationMethod);
    }

    /**
     * Checks if KYC has been manually unverified.
     */
    public boolean isManuallyUnverified() {
        return manualUnverificationDate != null && manualUnverifiedByUserId != null;
    }

    /**
     * Gets count of verified documents.
     */
    public int getVerifiedDocumentCount() {
        int count = 0;
        if (Boolean.TRUE.equals(panVerified)) count++;
        if (Boolean.TRUE.equals(aadhaarVerified)) count++;
        if (Boolean.TRUE.equals(voterIdVerified)) count++;
        if (Boolean.TRUE.equals(drivingLicenseVerified)) count++;
        if (Boolean.TRUE.equals(passportVerified)) count++;
        return count;
    }

    /**
     * Gets human-readable verification status.
     */
    public String getVerificationStatus() {
        if (isManuallyUnverified()) {
            return String.format("Manually Unverified - %s", manualUnverificationReason);
        }
        if (areAllDocumentsVerified()) {
            return String.format("Fully Verified (%s)", verificationMethodDescription);
        }
        if (isAnyDocumentVerified()) {
            return String.format("Partially Verified (%d/%d documents)", getVerifiedDocumentCount(), 5);
        }
        return "Not Verified";
    }
}
