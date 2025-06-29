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

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.springframework.stereotype.Service;

/**
 * Repository wrapper for GuarantorKycDetails entity operations.
 *
 * This wrapper provides additional validation and exception handling on top of the basic repository. It ensures
 * consistent error messages and validation rules across the application.
 */
@Service
@RequiredArgsConstructor
public class GuarantorKycDetailsRepositoryWrapper {

    private final GuarantorKycDetailsRepository repository;

    /**
     * Saves a guarantor KYC details entity.
     *
     * @param guarantorKycDetails
     *            the entity to save
     * @return the saved entity
     */
    public GuarantorKycDetails save(final GuarantorKycDetails guarantorKycDetails) {
        return this.repository.save(guarantorKycDetails);
    }

    /**
     * Finds a guarantor KYC details by ID, throwing exception if not found.
     *
     * @param guarantorKycId
     *            the guarantor KYC ID to find
     * @return the found entity
     * @throws GeneralPlatformDomainRuleException
     *             if not found
     */
    public GuarantorKycDetails findOneThrowExceptionIfNotFound(final Long guarantorKycId) {
        return this.repository.findById(guarantorKycId)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.guarantor.kyc.not.found",
                        "Guarantor KYC details not found with ID: " + guarantorKycId, guarantorKycId));
    }

    /**
     * Finds all guarantor KYC details for a specific client.
     *
     * @param clientId
     *            the client ID
     * @return list of guarantor KYC details for the client
     */
    public List<GuarantorKycDetails> findByClientId(final Long clientId) {
        return this.repository.findByClientId(clientId);
    }

    /**
     * Checks if any guarantor KYC details exist for the given client.
     *
     * @param clientId
     *            the client ID to check
     * @return true if guarantor KYC details exist for the client
     */
    public boolean existsByClientId(final Long clientId) {
        return this.repository.existsByClientId(clientId);
    }

    /**
     * Deletes a guarantor KYC details entity by ID.
     *
     * @param guarantorKycId
     *            the ID of the entity to delete
     */
    public void deleteById(final Long guarantorKycId) {
        this.repository.deleteById(guarantorKycId);
    }

    /**
     * Checks if guarantor KYC details with given ID belongs to the specified client.
     *
     * @param guarantorKycId
     *            the guarantor KYC ID
     * @param clientId
     *            the client ID
     * @return true if the guarantor KYC belongs to the client
     */
    public boolean isGuarantorKycBelongsToClient(final Long guarantorKycId, final Long clientId) {
        return this.repository.existsByIdAndClientId(guarantorKycId, clientId);
    }

    /**
     * Checks if mobile number already exists for the given client (for uniqueness validation).
     *
     * @param clientId
     *            the client ID
     * @param mobileNumber
     *            the mobile number to check
     * @return true if mobile number already exists for this client
     */
    public boolean existsByClientIdAndMobileNumber(final Long clientId, final String mobileNumber) {
        return this.repository.existsByClientIdAndMobileNumber(clientId, mobileNumber);
    }

    /**
     * Checks if mobile number already exists for the given client excluding a specific guarantor KYC record.
     *
     * @param clientId
     *            the client ID
     * @param mobileNumber
     *            the mobile number to check
     * @param excludeId
     *            the guarantor KYC ID to exclude from the check
     * @return true if mobile number already exists for this client (excluding the specified record)
     */
    public boolean existsByClientIdAndMobileNumberAndIdNot(final Long clientId, final String mobileNumber, final Long excludeId) {
        return this.repository.existsByClientIdAndMobileNumberAndIdNot(clientId, mobileNumber, excludeId);
    }

}
