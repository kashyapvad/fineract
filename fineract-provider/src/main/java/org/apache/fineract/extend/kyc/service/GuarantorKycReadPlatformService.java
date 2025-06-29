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

import java.util.List;
import org.apache.fineract.extend.kyc.data.GuarantorKycData;

/**
 * Read platform service for Guarantor KYC operations.
 *
 * This service provides read-only operations for retrieving guarantor KYC data. Each client can have multiple guarantor
 * KYC records (1-to-many relationship).
 */
public interface GuarantorKycReadPlatformService {

    /**
     * Retrieves all guarantor KYC details for a specific client.
     *
     * @param clientId
     *            the client ID
     * @return list of guarantor KYC details for the client
     */
    List<GuarantorKycData> retrieveGuarantorKycDetailsByClientId(Long clientId);

    /**
     * Retrieves a specific guarantor KYC details by ID.
     *
     * @param guarantorKycId
     *            the guarantor KYC ID
     * @return the guarantor KYC details
     */
    GuarantorKycData retrieveGuarantorKycDetails(Long guarantorKycId);

    /**
     * Checks if any guarantor KYC details exist for the given client.
     *
     * @param clientId
     *            the client ID to check
     * @return true if guarantor KYC details exist for the client
     */
    boolean hasGuarantorKycDetails(Long clientId);
}
