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
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for ClientKycDetails entity.
 *
 * Provides standard CRUD operations and custom query methods for client KYC details management. Enforces 1-to-1
 * relationship: Each client has exactly zero or one KYC record (enforced by unique constraint on client_id).
 */
public interface ClientKycDetailsRepository extends JpaRepository<ClientKycDetails, Long>, JpaSpecificationExecutor<ClientKycDetails> {

    /**
     * Finds KYC details for a specific client. With 1-to-1 relationship enforced by unique constraint, this returns at
     * most one record.
     *
     * @param clientId
     *            the client ID
     * @return Optional containing KYC details if found
     */
    Optional<ClientKycDetails> findByClient_Id(Long clientId);

    /**
     * Bulk retrieval: Finds KYC details for multiple clients in a single optimized query. Uses JOIN FETCH to avoid N+1
     * queries and optimize performance.
     *
     * @param clientIds
     *            list of client IDs to retrieve KYC details for
     * @return list of KYC details for the specified clients
     */
    @Query("SELECT k FROM ClientKycDetails k JOIN FETCH k.client c WHERE c.id IN :clientIds")
    List<ClientKycDetails> findByClientIds(@Param("clientIds") List<Long> clientIds);

    /**
     * Counts KYC details for a specific client. With 1-to-1 relationship, this should return 0 or 1.
     *
     * @param clientId
     *            the client ID
     * @return count of KYC details for the client (0 or 1)
     */
    @Query("SELECT COUNT(k) FROM ClientKycDetails k WHERE k.client.id = :clientId")
    long countByClientId(@Param("clientId") Long clientId);

    /**
     * Finds KYC details by PAN number.
     *
     * @param panNumber
     *            the PAN number
     * @return Optional containing KYC details if found
     */
    Optional<ClientKycDetails> findByPanNumber(String panNumber);

    /**
     * Finds KYC details by Aadhaar number.
     *
     * @param aadhaarNumber
     *            the Aadhaar number
     * @return Optional containing KYC details if found
     */
    Optional<ClientKycDetails> findByAadhaarNumber(String aadhaarNumber);

    /**
     * Finds all KYC details verified by a specific verification method.
     *
     * @param verificationMethod
     *            the verification method
     * @return list of KYC details with the specified verification method
     */
    @Query("SELECT k FROM ClientKycDetails k WHERE k.verificationMethod = :verificationMethod")
    List<ClientKycDetails> findByVerificationMethod(@Param("verificationMethod") KycVerificationMethod verificationMethod);

    /**
     * Finds all KYC details verified by a specific provider.
     *
     * @param verificationProvider
     *            the verification provider
     * @return list of KYC details verified by the provider
     */
    @Query("SELECT k FROM ClientKycDetails k WHERE k.verificationProvider = :verificationProvider")
    List<ClientKycDetails> findByVerificationProvider(@Param("verificationProvider") String verificationProvider);

    /**
     * Finds all clients with at least one verified KYC document.
     *
     * @return list of KYC details where at least one document is verified
     */
    @Query("SELECT k FROM ClientKycDetails k WHERE k.panVerified = true OR k.aadhaarVerified = true OR k.drivingLicenseVerified = true OR k.voterIdVerified = true OR k.passportVerified = true")
    List<ClientKycDetails> findAllWithVerifiedDocuments();

    /**
     * Counts clients with complete KYC verification (all provided documents verified).
     *
     * @return count of clients with complete KYC
     */
    @Query("""
            SELECT COUNT(k) FROM ClientKycDetails k WHERE
            (k.panNumber IS NULL OR k.panVerified = true) AND
            (k.aadhaarNumber IS NULL OR k.aadhaarVerified = true) AND
            (k.drivingLicenseNumber IS NULL OR k.drivingLicenseVerified = true) AND
            (k.voterId IS NULL OR k.voterIdVerified = true) AND
            (k.passportNumber IS NULL OR k.passportVerified = true)
            """)
    long countClientsWithCompleteKyc();
}
