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
package org.apache.fineract.extend.kyc.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

/**
 * Service interface for managing Guarantor KYC details operations.
 *
 * This service provides operations for creating, updating, deleting and verifying KYC details for guarantors. A client
 * can have multiple guarantors, each with their own KYC details.
 *
 * Relations: - Client (1) -> Guarantor KYC (many) - Each guarantor has independent KYC verification
 */
public interface GuarantorKycWritePlatformService {

    /**
     * Create new KYC details for a guarantor.
     *
     * @param command
     *            JSON command containing client ID and guarantor KYC data
     * @return CommandProcessingResult with operation status
     */
    CommandProcessingResult createGuarantorKycDetails(JsonCommand command);

    /**
     * Update existing KYC details for a guarantor.
     *
     * @param command
     *            JSON command containing client ID, guarantor KYC ID and updated data
     * @return CommandProcessingResult with operation status
     */
    CommandProcessingResult updateGuarantorKycDetails(JsonCommand command);

    /**
     * Delete a guarantor KYC details record from the system.
     *
     * @param command
     *            JSON command containing client ID and guarantor KYC ID
     * @return CommandProcessingResult with operation status
     */
    CommandProcessingResult deleteGuarantorKycDetails(JsonCommand command);

    /**
     * Verify guarantor KYC documents via external API providers.
     *
     * @param command
     *            JSON command containing client ID, guarantor KYC ID and verification parameters
     * @return CommandProcessingResult with verification results
     */
    CommandProcessingResult verifyGuarantorKycViaApi(JsonCommand command);

    /**
     * Manually verify guarantor KYC documents by staff.
     *
     * @param command
     *            JSON command containing client ID, guarantor KYC ID and manual verification details
     * @return CommandProcessingResult with verification status
     */
    CommandProcessingResult verifyGuarantorKycManually(JsonCommand command);

    /**
     * Manually unverify previously verified guarantor KYC documents.
     *
     * @param command
     *            JSON command containing client ID, guarantor KYC ID, reason, and unverification details
     * @return CommandProcessingResult with unverification status
     */
    CommandProcessingResult unverifyGuarantorKycManually(JsonCommand command);

    /**
     * Generate OTP for guarantor Aadhaar verification.
     *
     * @param command
     *            JSON command containing client ID, guarantor KYC ID and Aadhaar number
     * @return CommandProcessingResult with OTP generation status
     */
    CommandProcessingResult generateOtpForGuarantorAadhaarVerification(JsonCommand command);

    /**
     * Submit OTP for guarantor Aadhaar verification.
     *
     * @param command
     *            JSON command containing client ID, guarantor KYC ID and OTP
     * @return CommandProcessingResult with verification status
     */
    CommandProcessingResult submitOtpForGuarantorAadhaarVerification(JsonCommand command);
}
