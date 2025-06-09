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
package org.apache.fineract.extend.creditbureau.service;

import java.util.Collection;
import org.apache.fineract.extend.creditbureau.data.ClientCreditBureauData;

/**
 * Read platform service for Client Credit Bureau operations.
 *
 * This service provides read-only operations for retrieving credit bureau data and templates.
 */
public interface ClientCreditBureauReadPlatformService {

    /**
     * Retrieves all credit bureau reports for a specific client.
     *
     * @param clientId
     *            the client ID
     * @return collection of credit bureau reports
     */
    Collection<ClientCreditBureauData> retrieveClientCreditReports(Long clientId);

    /**
     * Retrieves a specific credit bureau report.
     *
     * @param clientId
     *            the client ID
     * @param reportId
     *            the report ID
     * @return credit bureau report data
     */
    ClientCreditBureauData retrieveCreditReport(Long clientId, Long reportId);


}
