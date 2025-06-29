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
package org.apache.fineract.extend.kyc.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.extend.converter.PostgresJsonbConverter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.useradministration.domain.AppUser;

/**
 * Entity representing KYC (Know Your Customer) details for client guarantors.
 *
 * This entity stores guarantor personal information and their KYC documents with verification status. Supports both
 * manual, API-based, and OTP-based verification for guarantor Aadhaar documents.
 */
@Entity
@Table(name = "m_extend_guarantor_kyc_details")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GuarantorKycDetails extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // Guarantor Core Identity (Master Record Only)
    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "mobile_number", length = 15, nullable = false)
    private String mobileNumber;

    @Column(name = "relationship_to_client", length = 50, nullable = false)
    private String relationshipToClient;

    // KYC Document Fields
    @Column(name = "pan_number", length = 10, nullable = false)
    private String panNumber;

    @Column(name = "aadhaar_number", length = 12, nullable = false)
    private String aadhaarNumber;

    // Verification Status Fields
    @Column(name = "pan_verified", nullable = false)
    private Boolean panVerified = false;

    @Column(name = "aadhaar_verified", nullable = false)
    private Boolean aadhaarVerified = false;

    // OTP Verification Fields
    @Column(name = "aadhaar_otp_verified", nullable = false)
    private Boolean aadhaarOtpVerified = false;

    @Column(name = "otp_client_id", length = 100)
    private String otpClientId;

    @Column(name = "otp_last_requested_on")
    private LocalDateTime otpLastRequestedOn;

    @Column(name = "otp_verified_on")
    private LocalDateTime otpVerifiedOn;

    // Manual Verification Fields
    @Column(name = "pan_manually_verified", nullable = false)
    private Boolean panManuallyVerified = false;

    @Column(name = "aadhaar_manually_verified", nullable = false)
    private Boolean aadhaarManuallyVerified = false;

    @Column(name = "manual_verification_reason", columnDefinition = "TEXT")
    private String manualVerificationReason;

    // Verification Metadata
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_method", nullable = false)
    private KycVerificationMethod verificationMethod = KycVerificationMethod.MANUAL;

    @Column(name = "last_verified_on")
    private LocalDate lastVerifiedOn;

    @Column(name = "verification_provider", length = 50)
    private String verificationProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_user_id")
    private AppUser verifiedByUser;

    // Provider-Agnostic Response Storage
    @Convert(converter = PostgresJsonbConverter.class)
    @Column(name = "api_response_data", columnDefinition = "JSONB")
    private JsonNode apiResponseData;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    // Status Fields
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_primary_guarantor", nullable = false)
    private Boolean isPrimaryGuarantor = false;

    /**
     * Creates a new GuarantorKycDetails instance for the given client.
     *
     * @param client
     *            the client for whom guarantor KYC details are being created
     * @param fullName
     *            the full name of the guarantor
     * @param mobileNumber
     *            the mobile number of the guarantor
     * @param relationshipToClient
     *            the relationship to client
     * @param panNumber
     *            the PAN number of the guarantor (required)
     * @param aadhaarNumber
     *            the Aadhaar number of the guarantor
     * @return new GuarantorKycDetails instance
     */
    public static GuarantorKycDetails createNew(Client client, String fullName, String mobileNumber, String relationshipToClient,
            String panNumber, String aadhaarNumber) {
        GuarantorKycDetails guarantorKyc = new GuarantorKycDetails();
        guarantorKyc.client = client;
        guarantorKyc.fullName = fullName;
        guarantorKyc.mobileNumber = mobileNumber;
        guarantorKyc.relationshipToClient = relationshipToClient;
        guarantorKyc.panNumber = panNumber;
        guarantorKyc.aadhaarNumber = aadhaarNumber;
        guarantorKyc.verificationMethod = KycVerificationMethod.MANUAL;
        guarantorKyc.isActive = true;
        guarantorKyc.isPrimaryGuarantor = false;
        return guarantorKyc;
    }

    /**
     * Updates guarantor KYC details from the provided changes map.
     *
     * @param changes
     *            map containing the changes to apply
     * @return map of actual changes made
     */
    public Map<String, Object> update(final Map<String, Object> changes) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        if (changes.containsKey("fullName")) {
            final String newValue = (String) changes.get("fullName");
            if (!StringUtils.equals(this.fullName, newValue)) {
                actualChanges.put("fullName", newValue);
                this.fullName = newValue;
            }
        }

        if (changes.containsKey("mobileNumber")) {
            final String newValue = (String) changes.get("mobileNumber");
            if (!StringUtils.equals(this.mobileNumber, newValue)) {
                actualChanges.put("mobileNumber", newValue);
                this.mobileNumber = newValue;
            }
        }

        if (changes.containsKey("relationshipToClient")) {
            final String newValue = (String) changes.get("relationshipToClient");
            if (!StringUtils.equals(this.relationshipToClient, newValue)) {
                actualChanges.put("relationshipToClient", newValue);
                this.relationshipToClient = newValue;
            }
        }

        if (changes.containsKey("panNumber")) {
            final String newValue = (String) changes.get("panNumber");
            if (!StringUtils.equals(this.panNumber, newValue)) {
                actualChanges.put("panNumber", newValue);
                this.panNumber = newValue;
                // Reset PAN verification if number changed
                this.panVerified = false;
                this.panManuallyVerified = false;
                actualChanges.put("panVerified", false);
                actualChanges.put("panManuallyVerified", false);
            }
        }

        if (changes.containsKey("aadhaarNumber")) {
            final String newValue = (String) changes.get("aadhaarNumber");
            if (!StringUtils.equals(this.aadhaarNumber, newValue)) {
                actualChanges.put("aadhaarNumber", newValue);
                this.aadhaarNumber = newValue;
                // Reset Aadhaar verification if number changed
                this.aadhaarVerified = false;
                this.aadhaarOtpVerified = false;
                this.aadhaarManuallyVerified = false;
                actualChanges.put("aadhaarVerified", false);
                actualChanges.put("aadhaarOtpVerified", false);
                actualChanges.put("aadhaarManuallyVerified", false);
                // Clear OTP-related fields
                this.otpClientId = null;
                this.otpLastRequestedOn = null;
                this.otpVerifiedOn = null;
            }
        }

        if (changes.containsKey("isPrimaryGuarantor")) {
            final Boolean newValue = (Boolean) changes.get("isPrimaryGuarantor");
            if (!this.isPrimaryGuarantor.equals(newValue)) {
                actualChanges.put("isPrimaryGuarantor", newValue);
                this.isPrimaryGuarantor = newValue;
            }
        }

        if (changes.containsKey("isActive")) {
            final Boolean newValue = (Boolean) changes.get("isActive");
            if (!this.isActive.equals(newValue)) {
                actualChanges.put("isActive", newValue);
                this.isActive = newValue;
            }
        }

        return actualChanges;
    }

    /**
     * Marks OTP generation for Aadhaar verification.
     *
     * @param otpClientId
     *            the client ID from OTP provider
     */
    public void markOtpGenerated(String otpClientId) {
        this.otpClientId = otpClientId;
        this.otpLastRequestedOn = DateUtils.getLocalDateTimeOfTenant();
    }

    /**
     * Marks OTP verification as completed for Aadhaar.
     *
     * @param verifiedByUser
     *            the user who completed the verification
     * @param notes
     *            optional verification notes
     */
    public void markOtpVerificationCompleted(AppUser verifiedByUser, String notes) {
        this.verificationMethod = KycVerificationMethod.OTP;
        this.aadhaarOtpVerified = true;
        // Keep OTP and API verification separate - don't set aadhaarVerified for OTP
        this.otpVerifiedOn = DateUtils.getLocalDateTimeOfTenant();
        this.verifiedByUser = verifiedByUser;
        this.lastVerifiedOn = LocalDate.now(ZoneId.systemDefault());
        this.verificationNotes = notes;
    }

    /**
     * Marks API verification as completed.
     *
     * @param verificationProvider
     *            provider used for verification
     * @param apiResponse
     *            API response data
     * @param verifiedByUser
     *            the user who initiated the verification
     * @param verificationResults
     *            map indicating which documents were verified
     * @param notes
     *            optional verification notes
     */
    public void markApiVerificationCompleted(String verificationProvider, JsonNode apiResponse, AppUser verifiedByUser,
            Map<String, Boolean> verificationResults, String notes) {
        this.verificationMethod = KycVerificationMethod.API;
        this.verificationProvider = verificationProvider;
        this.apiResponseData = apiResponse;
        this.verifiedByUser = verifiedByUser;
        this.lastVerifiedOn = LocalDate.now(ZoneId.systemDefault());
        this.verificationNotes = notes;

        // Update individual verification status based on results
        // Keep existing OTP verification status - don't clear it
        if (verificationResults.containsKey("panVerified")) {
            this.panVerified = verificationResults.get("panVerified");
        }
        if (verificationResults.containsKey("aadhaarVerified")) {
            this.aadhaarVerified = verificationResults.get("aadhaarVerified");
        }
    }

    /**
     * Marks verification as completed manually by a user.
     *
     * @param verifiedByUser
     *            the user who performed the verification
     * @param verificationResults
     *            map indicating which documents were verified
     * @param notes
     *            optional verification notes
     */
    public void markManualVerificationCompleted(AppUser verifiedByUser, Map<String, Boolean> verificationResults, String notes) {
        this.markManualVerificationCompleted(verifiedByUser, verificationResults, notes, null);
    }

    /**
     * Marks verification as completed manually by a user with reason.
     *
     * @param verifiedByUser
     *            the user who performed the verification
     * @param verificationResults
     *            map indicating which documents were verified
     * @param notes
     *            optional verification notes
     * @param reason
     *            reason for manual verification
     */
    public void markManualVerificationCompleted(AppUser verifiedByUser, Map<String, Boolean> verificationResults, String notes,
            String reason) {
        this.verificationMethod = KycVerificationMethod.MANUAL;
        this.verifiedByUser = verifiedByUser;
        this.lastVerifiedOn = LocalDate.now(ZoneId.systemDefault());
        this.verificationNotes = notes;
        this.manualVerificationReason = reason;
        this.verificationProvider = null; // Clear provider for manual verification
        this.apiResponseData = null; // Clear API response for manual verification

        // Update individual verification status based on results - only update fields that are explicitly provided
        if (verificationResults.containsKey("panVerified")) {
            this.panVerified = verificationResults.get("panVerified");
            this.panManuallyVerified = verificationResults.get("panVerified");
        }
        if (verificationResults.containsKey("aadhaarVerified")) {
            this.aadhaarVerified = verificationResults.get("aadhaarVerified");
            this.aadhaarManuallyVerified = verificationResults.get("aadhaarVerified");
        }
    }

    /**
     * Checks if the guarantor has both PAN and Aadhaar verified.
     *
     * @return true if both PAN and Aadhaar are verified
     */
    public boolean isFullyVerified() {
        return Boolean.TRUE.equals(this.panVerified) && Boolean.TRUE.equals(this.aadhaarVerified);
    }

    /**
     * Checks if this guarantor KYC record was verified through OTP.
     *
     * @return true if verification method is OTP
     */
    public boolean isOtpVerified() {
        return KycVerificationMethod.OTP.equals(this.verificationMethod);
    }

    /**
     * Checks if this guarantor KYC record was verified through API.
     *
     * @return true if verification method is API
     */
    public boolean isApiVerified() {
        return KycVerificationMethod.API.equals(this.verificationMethod);
    }

    /**
     * Checks if this guarantor KYC record was verified manually.
     *
     * @return true if verification method is MANUAL
     */
    public boolean isManuallyVerified() {
        return KycVerificationMethod.MANUAL.equals(this.verificationMethod);
    }

    /**
     * Gets the count of verified documents.
     *
     * @return number of verified documents
     */
    public int getVerifiedDocumentCount() {
        int count = 0;
        if (Boolean.TRUE.equals(panVerified)) {
            count++;
        }
        if (Boolean.TRUE.equals(aadhaarVerified)) {
            count++;
        }
        return count;
    }

    /**
     * Marks KYC as unverified by resetting verification status.
     *
     * @param unverifiedByUser
     *            the user who performed the unverification
     * @param reason
     *            reason for unverification
     * @param notes
     *            optional unverification notes
     */
    public void markAsUnverified(AppUser unverifiedByUser, String reason, String notes) {
        this.verificationMethod = KycVerificationMethod.MANUAL;
        this.panVerified = false;
        this.aadhaarVerified = false;
        this.aadhaarOtpVerified = false;
        this.panManuallyVerified = false;
        this.aadhaarManuallyVerified = false;
        this.verifiedByUser = unverifiedByUser;
        this.lastVerifiedOn = LocalDate.now(ZoneId.systemDefault());
        this.verificationNotes = (notes != null ? notes : "") + (reason != null ? " [Reason: " + reason + "]" : "");
        this.manualVerificationReason = reason;
        this.verificationProvider = null;
        this.apiResponseData = null;
        // Clear OTP-related fields
        this.otpClientId = null;
        this.otpLastRequestedOn = null;
        this.otpVerifiedOn = null;
    }
}
