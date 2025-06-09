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

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for manual unverification of KYC documents.
 *
 * This request contains the necessary information to manually unverify previously verified KYC documents with audit
 * trail.
 */
@Data
@NoArgsConstructor
public class ManualUnverifyKycRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Reason for manual unverification. Must be provided to explain why the KYC is being unverified.
     */
    @NotBlank(message = "Unverification reason is required")
    @JsonProperty("reason")
    private String reason;

    /**
     * Additional notes for the manual unverification.
     */
    @JsonProperty("notes")
    private String notes;

    /**
     * Specific documents to unverify. If not specified, all documents will be unverified.
     */
    @JsonProperty("unverifyPan")
    private Boolean unverifyPan;

    @JsonProperty("unverifyAadhaar")
    private Boolean unverifyAadhaar;

    @JsonProperty("unverifyVoterId")
    private Boolean unverifyVoterId;

    @JsonProperty("unverifyDrivingLicense")
    private Boolean unverifyDrivingLicense;

    @JsonProperty("unverifyPassport")
    private Boolean unverifyPassport;

    /**
     * Date format for the request.
     */
    @JsonProperty("dateFormat")
    private String dateFormat = "dd MMMM yyyy";

    /**
     * Locale for the request.
     */
    @JsonProperty("locale")
    private String locale = "en";

    /**
     * Constructor with required fields.
     */
    public ManualUnverifyKycRequest(String reason) {
        this.reason = reason;
    }

    /**
     * Constructor with reason and notes.
     */
    public ManualUnverifyKycRequest(String reason, String notes) {
        this.reason = reason;
        this.notes = notes;
    }

    /**
     * Checks if any specific document unverification is requested.
     */
    public boolean hasSpecificDocuments() {
        return Boolean.TRUE.equals(unverifyPan) || Boolean.TRUE.equals(unverifyAadhaar) || Boolean.TRUE.equals(unverifyVoterId)
                || Boolean.TRUE.equals(unverifyDrivingLicense) || Boolean.TRUE.equals(unverifyPassport);
    }

    /**
     * Checks if PAN should be unverified.
     */
    public boolean shouldUnverifyPan() {
        return !hasSpecificDocuments() || Boolean.TRUE.equals(unverifyPan);
    }

    /**
     * Checks if Aadhaar should be unverified.
     */
    public boolean shouldUnverifyAadhaar() {
        return !hasSpecificDocuments() || Boolean.TRUE.equals(unverifyAadhaar);
    }

    /**
     * Checks if Voter ID should be unverified.
     */
    public boolean shouldUnverifyVoterId() {
        return !hasSpecificDocuments() || Boolean.TRUE.equals(unverifyVoterId);
    }

    /**
     * Checks if Driving License should be unverified.
     */
    public boolean shouldUnverifyDrivingLicense() {
        return !hasSpecificDocuments() || Boolean.TRUE.equals(unverifyDrivingLicense);
    }

    /**
     * Checks if Passport should be unverified.
     */
    public boolean shouldUnverifyPassport() {
        return !hasSpecificDocuments() || Boolean.TRUE.equals(unverifyPassport);
    }
}
