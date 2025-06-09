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
     * Retrieves template data for KYC operations.
     *
     * @param clientId
     *            the client ID
     * @return template data
     */
    ClientKycData retrieveTemplate(Long clientId);
}
