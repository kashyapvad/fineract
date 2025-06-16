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
import java.util.Map;
import org.apache.fineract.extend.kyc.data.ClientKycData;

/**
 * Read platform service for Client KYC operations.
 *
 * This service provides read-only operations for retrieving KYC data and templates.
 */
public interface ClientKycReadPlatformService {

    /**
     * Retrieves KYC details for a specific client.
     *
     * @param clientId
     *            the client ID
     * @return client KYC data
     */
    ClientKycData retrieveClientKyc(Long clientId);

    /**
     * Bulk retrieval: Retrieves KYC details for multiple clients in a single optimized operation. Returns a map with
     * client ID as key and KYC data as value. Clients without KYC data will have template data in the response.
     *
     * @param clientIds
     *            list of client IDs to retrieve KYC details for
     * @return map of client ID to KYC data
     */
    Map<Long, ClientKycData> retrieveClientKycBulk(List<Long> clientIds);

    /**
     * Retrieves template data for KYC operations.
     *
     * @param clientId
     *            the client ID
     * @return template data
     */
    ClientKycData retrieveTemplate(Long clientId);
}
