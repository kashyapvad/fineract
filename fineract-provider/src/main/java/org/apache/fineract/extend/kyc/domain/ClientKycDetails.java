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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.extend.converter.PostgresJsonbConverter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.useradministration.domain.AppUser;

/**
 * Entity representing KYC (Know Your Customer) details for a client.
 *
 * This entity stores various KYC documents and their verification status. Supports both manual and API-based
 * verification through credit bureau services.
 */
@Entity
@Table(name = "m_extend_client_kyc_details")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientKycDetails extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    // KYC Document Fields
    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "aadhaar_number", length = 12)
    private String aadhaarNumber;

    @Column(name = "driving_license_number", length = 20)
    private String drivingLicenseNumber;

    @Column(name = "voter_id", length = 20)
    private String voterId;

    @Column(name = "passport_number", length = 20)
    private String passportNumber;

    // Verification Status Fields
    @Column(name = "pan_verified", nullable = false)
    private Boolean panVerified = false;

    @Column(name = "aadhaar_verified", nullable = false)
    private Boolean aadhaarVerified = false;

    @Column(name = "driving_license_verified", nullable = false)
    private Boolean drivingLicenseVerified = false;

    @Column(name = "voter_id_verified", nullable = false)
    private Boolean voterIdVerified = false;

    @Column(name = "passport_verified", nullable = false)
    private Boolean passportVerified = false;

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

    /**
     * Creates a new ClientKycDetails instance for the given client.
     *
     * @param client
     *            the client for whom KYC details are being created
     * @return new ClientKycDetails instance
     */
    public static ClientKycDetails createNew(Client client) {
        ClientKycDetails kycDetails = new ClientKycDetails();
        kycDetails.client = client;
        kycDetails.verificationMethod = KycVerificationMethod.MANUAL;
        return kycDetails;
    }

    /**
     * Updates KYC details from the provided changes map.
     *
     * @param changes
     *            map containing the changes to apply
     * @return map of actual changes made
     */
    public Map<String, Object> update(Map<String, Object> changes) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        if (changes.containsKey("panNumber")) {
            final String panNumber = (String) changes.get("panNumber");
            if (!StringUtils.equals(this.panNumber, panNumber)) {
                actualChanges.put("panNumber", panNumber);
                this.panNumber = panNumber;
                // Reset verification when document number changes
                if (this.panVerified) {
                    this.panVerified = false;
                    actualChanges.put("panVerified", false);
                }
            }
        }

        if (changes.containsKey("aadhaarNumber")) {
            final String aadhaarNumber = (String) changes.get("aadhaarNumber");
            if (!StringUtils.equals(this.aadhaarNumber, aadhaarNumber)) {
                actualChanges.put("aadhaarNumber", aadhaarNumber);
                this.aadhaarNumber = aadhaarNumber;
                if (this.aadhaarVerified) {
                    this.aadhaarVerified = false;
                    actualChanges.put("aadhaarVerified", false);
                }
            }
        }

        if (changes.containsKey("drivingLicenseNumber")) {
            final String drivingLicenseNumber = (String) changes.get("drivingLicenseNumber");
            if (!StringUtils.equals(this.drivingLicenseNumber, drivingLicenseNumber)) {
                actualChanges.put("drivingLicenseNumber", drivingLicenseNumber);
                this.drivingLicenseNumber = drivingLicenseNumber;
                if (this.drivingLicenseVerified) {
                    this.drivingLicenseVerified = false;
                    actualChanges.put("drivingLicenseVerified", false);
                }
            }
        }

        if (changes.containsKey("voterId")) {
            final String voterId = (String) changes.get("voterId");
            if (!StringUtils.equals(this.voterId, voterId)) {
                actualChanges.put("voterId", voterId);
                this.voterId = voterId;
                if (this.voterIdVerified) {
                    this.voterIdVerified = false;
                    actualChanges.put("voterIdVerified", false);
                }
            }
        }

        if (changes.containsKey("passportNumber")) {
            final String passportNumber = (String) changes.get("passportNumber");
            if (!StringUtils.equals(this.passportNumber, passportNumber)) {
                actualChanges.put("passportNumber", passportNumber);
                this.passportNumber = passportNumber;
                if (this.passportVerified) {
                    this.passportVerified = false;
                    actualChanges.put("passportVerified", false);
                }
            }
        }

        if (changes.containsKey("verificationNotes")) {
            final String verificationNotes = (String) changes.get("verificationNotes");
            if (!StringUtils.equals(this.verificationNotes, verificationNotes)) {
                actualChanges.put("verificationNotes", verificationNotes);
                this.verificationNotes = verificationNotes;
            }
        }

        return actualChanges;
    }

    /**
     * Marks verification as completed through API with the provided response data.
     *
     * @param verificationProvider
     *            the provider used for verification
     * @param apiResponse
     *            the API response data
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
        this.lastVerifiedOn = LocalDate.now();
        this.verificationNotes = notes;

        // Update individual verification status based on results
        if (verificationResults.containsKey("panVerified")) {
            this.panVerified = verificationResults.get("panVerified");
        }
        if (verificationResults.containsKey("aadhaarVerified")) {
            this.aadhaarVerified = verificationResults.get("aadhaarVerified");
        }
        if (verificationResults.containsKey("drivingLicenseVerified")) {
            this.drivingLicenseVerified = verificationResults.get("drivingLicenseVerified");
        }
        if (verificationResults.containsKey("voterIdVerified")) {
            this.voterIdVerified = verificationResults.get("voterIdVerified");
        }
        if (verificationResults.containsKey("passportVerified")) {
            this.passportVerified = verificationResults.get("passportVerified");
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
        this.verificationMethod = KycVerificationMethod.MANUAL;
        this.verifiedByUser = verifiedByUser;
        this.lastVerifiedOn = LocalDate.now();
        this.verificationNotes = notes;
        this.verificationProvider = null; // Clear provider for manual verification
        this.apiResponseData = null; // Clear API response for manual verification

        // Update individual verification status based on results
        if (verificationResults.containsKey("panVerified")) {
            this.panVerified = verificationResults.get("panVerified");
        }
        if (verificationResults.containsKey("aadhaarVerified")) {
            this.aadhaarVerified = verificationResults.get("aadhaarVerified");
        }
        if (verificationResults.containsKey("drivingLicenseVerified")) {
            this.drivingLicenseVerified = verificationResults.get("drivingLicenseVerified");
        }
        if (verificationResults.containsKey("voterIdVerified")) {
            this.voterIdVerified = verificationResults.get("voterIdVerified");
        }
        if (verificationResults.containsKey("passportVerified")) {
            this.passportVerified = verificationResults.get("passportVerified");
        }
    }

    /**
     * Checks if any KYC document is verified.
     *
     * @return true if at least one document is verified
     */
    public boolean hasAnyVerifiedDocument() {
        return panVerified || aadhaarVerified || drivingLicenseVerified || voterIdVerified || passportVerified;
    }

    /**
     * Checks if all provided KYC documents are verified.
     *
     * @return true if all non-null documents are verified
     */
    public boolean areAllProvidedDocumentsVerified() {
        boolean allVerified = true;

        if (StringUtils.isNotBlank(panNumber) && !panVerified) {
            allVerified = false;
        }
        if (StringUtils.isNotBlank(aadhaarNumber) && !aadhaarVerified) {
            allVerified = false;
        }
        if (StringUtils.isNotBlank(drivingLicenseNumber) && !drivingLicenseVerified) {
            allVerified = false;
        }
        if (StringUtils.isNotBlank(voterId) && !voterIdVerified) {
            allVerified = false;
        }
        if (StringUtils.isNotBlank(passportNumber) && !passportVerified) {
            allVerified = false;
        }

        return allVerified;
    }

    /**
     * Gets the count of verified documents.
     *
     * @return number of verified documents
     */
    public int getVerifiedDocumentCount() {
        int count = 0;
        if (panVerified) {
            count++;
        }
        if (aadhaarVerified) {
            count++;
        }
        if (drivingLicenseVerified) {
            count++;
        }
        if (voterIdVerified) {
            count++;
        }
        if (passportVerified) {
            count++;
        }
        return count;
    }

    /**
     * Checks if this KYC record was verified through API.
     *
     * @return true if verification method is API
     */
    public boolean isApiVerified() {
        return KycVerificationMethod.API.equals(this.verificationMethod);
    }

    /**
     * Checks if this KYC record was verified manually.
     *
     * @return true if verification method is MANUAL
     */
    public boolean isManuallyVerified() {
        return KycVerificationMethod.MANUAL.equals(this.verificationMethod);
    }
}
