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
 * Write platform service for Client KYC verification operations.
 *
 * This service provides write operations for KYC verification workflows and CRUD operations for KYC details using
 * dedicated entity tables.
 */
public interface ClientKycWritePlatformService {

    /**
     * Verify KYC documents via external API providers.
     *
     * @param command
     *            JSON command containing client ID and verification parameters
     * @return CommandProcessingResult with verification results
     */
    CommandProcessingResult verifyKycViaApi(JsonCommand command);

    /**
     * Manually verify KYC documents by staff.
     *
     * @param command
     *            JSON command containing client ID and manual verification details
     * @return CommandProcessingResult with verification status
     */
    CommandProcessingResult verifyKycManually(JsonCommand command);

    /**
     * Manually unverify previously verified KYC documents.
     *
     * @param command
     *            JSON command containing client ID, reason, and unverification details
     * @return CommandProcessingResult with unverification status
     */
    CommandProcessingResult unverifyKycManually(JsonCommand command);

    /**
     * Create a new KYC details record manually.
     *
     * @param command
     *            JSON command containing client ID and KYC data
     * @return CommandProcessingResult with operation status and generated IDs
     */
    CommandProcessingResult createKycDetails(JsonCommand command);

    /**
     * Update an existing KYC details record.
     *
     * @param command
     *            JSON command containing KYC ID and updated data
     * @return CommandProcessingResult with operation status
     */
    CommandProcessingResult updateKycDetails(JsonCommand command);

    /**
     * Delete a KYC details record from the system.
     *
     * @param command
     *            JSON command containing client ID and KYC ID
     * @return CommandProcessingResult with operation status
     */
    CommandProcessingResult deleteKycDetails(JsonCommand command);
}
