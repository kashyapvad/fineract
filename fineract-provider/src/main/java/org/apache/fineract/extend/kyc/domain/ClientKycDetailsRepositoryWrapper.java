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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.extend.kyc.exception.ClientKycNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Wrapper for {@link ClientKycDetailsRepository} that provides additional business logic and exception handling.
 *
 * This wrapper ensures consistent error handling and validation patterns across the KYC module, following established
 * Fineract patterns.
 */
@Service
@RequiredArgsConstructor
public class ClientKycDetailsRepositoryWrapper {

    private final ClientKycDetailsRepository clientKycDetailsRepository;

    /**
     * Retrieves KYC details by ID with exception handling.
     *
     * @param kycId
     *            the KYC details ID
     * @return ClientKycDetails entity
     * @throws ClientKycNotFoundException
     *             if not found
     */
    public ClientKycDetails findOneThrowExceptionIfNotFound(final Long kycId) {
        return this.clientKycDetailsRepository.findById(kycId).orElseThrow(() -> new ClientKycNotFoundException(kycId, true));
    }

    /**
     * Retrieves KYC details by client ID with exception handling.
     *
     * @param clientId
     *            the client ID
     * @return ClientKycDetails entity
     * @throws ClientKycNotFoundException
     *             if not found
     */
    public ClientKycDetails findByClientIdThrowExceptionIfNotFound(final Long clientId) {
        return this.clientKycDetailsRepository.findByClient_Id(clientId).orElseThrow(() -> new ClientKycNotFoundException(clientId));
    }

    /**
     * Retrieves KYC details by client ID if it exists.
     *
     * @param clientId
     *            the client ID
     * @return Optional containing KYC details if found, empty otherwise
     */
    public Optional<ClientKycDetails> findByClientId(final Long clientId) {
        return this.clientKycDetailsRepository.findByClient_Id(clientId);
    }

    /**
     * Checks if KYC details exist for the specified client.
     *
     * @param clientId
     *            the client ID
     * @return true if KYC details exist for the client
     */
    public boolean existsByClientId(final Long clientId) {
        return this.clientKycDetailsRepository.countByClientId(clientId) > 0;
    }

    /**
     * Retrieves KYC details by PAN number.
     *
     * @param panNumber
     *            the PAN number
     * @return Optional containing KYC details if found
     */
    public Optional<ClientKycDetails> findByPanNumber(final String panNumber) {
        return this.clientKycDetailsRepository.findByPanNumber(panNumber);
    }

    /**
     * Retrieves KYC details by Aadhaar number.
     *
     * @param aadhaarNumber
     *            the Aadhaar number
     * @return Optional containing KYC details if found
     */
    public Optional<ClientKycDetails> findByAadhaarNumber(final String aadhaarNumber) {
        return this.clientKycDetailsRepository.findByAadhaarNumber(aadhaarNumber);
    }

    /**
     * Saves KYC details entity.
     *
     * @param clientKycDetails
     *            the entity to save
     * @return saved entity
     */
    public ClientKycDetails save(final ClientKycDetails clientKycDetails) {
        return this.clientKycDetailsRepository.save(clientKycDetails);
    }

    /**
     * Saves and flushes KYC details entity.
     *
     * @param clientKycDetails
     *            the entity to save
     * @return saved entity
     */
    public ClientKycDetails saveAndFlush(final ClientKycDetails clientKycDetails) {
        return this.clientKycDetailsRepository.saveAndFlush(clientKycDetails);
    }

    /**
     * Deletes KYC details entity.
     *
     * @param clientKycDetails
     *            the entity to delete
     */
    public void delete(final ClientKycDetails clientKycDetails) {
        this.clientKycDetailsRepository.delete(clientKycDetails);
    }

    /**
     * Deletes KYC details by ID.
     *
     * @param kycId
     *            the KYC details ID
     */
    public void deleteById(final Long kycId) {
        this.clientKycDetailsRepository.deleteById(kycId);
    }

    /**
     * Gets count of clients with complete KYC verification.
     *
     * @return count of clients with all provided documents verified
     */
    public long countClientsWithCompleteKyc() {
        return this.clientKycDetailsRepository.countClientsWithCompleteKyc();
    }

    /**
     * Validates that KYC details exist for the specified client and returns them. This method is useful for validation
     * in command handlers.
     *
     * @param clientId
     *            the client ID
     * @return ClientKycDetails entity
     * @throws ClientKycNotFoundException
     *             if not found
     */
    public ClientKycDetails validateAndGetByClientId(final Long clientId) {
        return findByClientIdThrowExceptionIfNotFound(clientId);
    }

    /**
     * Gets or creates KYC details for the specified client. If KYC details don't exist, returns an Optional.empty().
     * This method is useful when KYC details are optional.
     *
     * @param clientId
     *            the client ID
     * @return Optional containing existing KYC details
     */
    public Optional<ClientKycDetails> getByClientId(final Long clientId) {
        return this.clientKycDetailsRepository.findByClient_Id(clientId);
    }

    /**
     * Bulk retrieval: Gets KYC details for multiple clients in a single optimized query.
     * Uses the optimized findByClientIds query to avoid N+1 query problems.
     *
     * @param clientIds
     *            list of client IDs to retrieve KYC details for
     * @return list of KYC details for the specified clients
     */
    public List<ClientKycDetails> findByClientIds(final List<Long> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return new ArrayList<>();
        }
        return this.clientKycDetailsRepository.findByClientIds(clientIds);
    }

    // Removed findLatestKycWithDataByClientId - no longer needed with 1-to-1 relationship
}
