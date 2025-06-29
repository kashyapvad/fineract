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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA Repository for GuarantorKycDetails entity.
 *
 * This repository provides database operations for guarantor KYC details. Each client can have multiple guarantors, and
 * each guarantor has their own KYC details.
 */
public interface GuarantorKycDetailsRepository extends JpaRepository<GuarantorKycDetails, Long> {

    /**
     * Finds all guarantor KYC details for a specific client.
     *
     * @param clientId
     *            the client ID
     * @return list of guarantor KYC details for the client
     */
    @Query("SELECT g FROM GuarantorKycDetails g WHERE g.client.id = :clientId ORDER BY g.createdDate DESC")
    List<GuarantorKycDetails> findByClientId(@Param("clientId") Long clientId);

    /**
     * Checks if any guarantor KYC details exist for the given client.
     *
     * @param clientId
     *            the client ID to check
     * @return true if guarantor KYC details exist for the client
     */
    boolean existsByClientId(Long clientId);

    /**
     * Checks if guarantor KYC details with given ID belongs to the specified client.
     *
     * @param guarantorKycId
     *            the guarantor KYC ID
     * @param clientId
     *            the client ID
     * @return true if the guarantor KYC belongs to the client
     */
    boolean existsByIdAndClientId(Long guarantorKycId, Long clientId);

    /**
     * Finds guarantor KYC details by guarantor name (for search/lookup).
     *
     * @param clientId
     *            the client ID
     * @param guarantorName
     *            the guarantor name to search for
     * @return list of matching guarantor KYC details
     */
    @Query("SELECT g FROM GuarantorKycDetails g WHERE g.client.id = :clientId AND LOWER(g.fullName) LIKE LOWER(CONCAT('%', :guarantorName, '%'))")
    List<GuarantorKycDetails> findByClientIdAndGuarantorNameContaining(@Param("clientId") Long clientId,
            @Param("guarantorName") String guarantorName);

    /**
     * Checks if mobile number already exists for the given client (for uniqueness validation).
     *
     * @param clientId
     *            the client ID
     * @param mobileNumber
     *            the mobile number to check
     * @return true if mobile number already exists for this client
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GuarantorKycDetails g WHERE g.client.id = :clientId AND g.mobileNumber = :mobileNumber AND g.isActive = true")
    boolean existsByClientIdAndMobileNumber(@Param("clientId") Long clientId, @Param("mobileNumber") String mobileNumber);

    /**
     * Checks if mobile number already exists for the given client excluding a specific guarantor KYC record (for update
     * validation).
     *
     * @param clientId
     *            the client ID
     * @param mobileNumber
     *            the mobile number to check
     * @param excludeId
     *            the guarantor KYC ID to exclude from the check
     * @return true if mobile number already exists for this client (excluding the specified record)
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GuarantorKycDetails g WHERE g.client.id = :clientId AND g.mobileNumber = :mobileNumber AND g.isActive = true AND g.id != :excludeId")
    boolean existsByClientIdAndMobileNumberAndIdNot(@Param("clientId") Long clientId, @Param("mobileNumber") String mobileNumber,
            @Param("excludeId") Long excludeId);

}
