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

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

/**
 * Write platform service interface for Client Credit Bureau operations.
 *
 * This service handles all state-changing operations for credit bureau functionality including pulling reports from
 * external providers, manual entry, and data management.
 */
public interface ClientCreditBureauWritePlatformService {

    /**
     * Pull a credit report from an external credit bureau provider.
     *
     * @param command
     *            JSON command containing client ID and pull request parameters
     * @return CommandProcessingResult with operation status and generated IDs
     */
    CommandProcessingResult pullCreditReport(JsonCommand command);

    /**
     * Create a new credit bureau report manually (for manual entry).
     *
     * @param command
     *            JSON command containing client ID and credit report data
     * @return CommandProcessingResult with operation status and generated IDs
     */
    CommandProcessingResult createCreditReport(JsonCommand command);

    /**
     * Update an existing credit bureau report (for manual editing or provider updates).
     *
     * @param command
     *            JSON command containing report ID and updated data
     * @return CommandProcessingResult with operation status
     */
    CommandProcessingResult updateCreditReport(JsonCommand command);

    /**
     * Delete a credit bureau report from the system.
     *
     * @param command
     *            JSON command containing client ID and report ID
     * @return CommandProcessingResult with operation status
     */
    CommandProcessingResult deleteCreditReport(JsonCommand command);
}
