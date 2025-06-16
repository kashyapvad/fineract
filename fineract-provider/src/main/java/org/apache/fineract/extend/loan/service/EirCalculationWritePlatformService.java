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
package org.apache.fineract.extend.loan.service;

import org.apache.fineract.extend.loan.dto.EirCalculationRequest;
import org.apache.fineract.extend.loan.dto.EirCalculationResponse;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

/**
 * Platform service for EIR calculation write operations.
 *
 * Handles creation, updates, and deletion of EIR calculations following Fineract patterns. Provides both
 * JsonCommand-based methods (for command handlers) and DTO-based methods (for direct API usage).
 */
public interface EirCalculationWritePlatformService {

    // JsonCommand-based methods for command handlers

    /**
     * Create a new EIR calculation record via command handler.
     *
     * @param command
     *            JsonCommand with EIR calculation data
     * @return CommandProcessingResult with created resource ID
     */
    CommandProcessingResult createEirCalculation(JsonCommand command);

    /**
     * Update an existing EIR calculation via command handler.
     *
     * @param command
     *            JsonCommand with updated calculation data
     * @return CommandProcessingResult with updated resource ID
     */
    CommandProcessingResult updateEirCalculation(JsonCommand command);

    /**
     * Delete an EIR calculation via command handler.
     *
     * @param command
     *            JsonCommand with calculation ID to delete
     * @return CommandProcessingResult with deleted resource ID
     */
    CommandProcessingResult deleteEirCalculation(JsonCommand command);

    // DTO-based methods for direct API usage

    /**
     * Create a new EIR calculation record.
     *
     * @param request
     *            EIR calculation request data
     * @return CommandProcessingResult with created resource ID
     */
    CommandProcessingResult createEirCalculation(EirCalculationRequest request);

    /**
     * Calculate EIR for a specific loan and return result.
     *
     * @param loanId
     *            Loan ID to calculate EIR for
     * @return EirCalculationResponse with calculation results
     */
    EirCalculationResponse calculateEir(Long loanId);

    /**
     * Update an existing EIR calculation.
     *
     * @param calculationId
     *            ID of calculation to update
     * @param request
     *            Updated calculation data
     * @return CommandProcessingResult with updated resource ID
     */
    CommandProcessingResult updateEirCalculation(Long calculationId, EirCalculationRequest request);

    /**
     * Delete an EIR calculation.
     *
     * @param calculationId
     *            ID of calculation to delete
     * @return CommandProcessingResult with deleted resource ID
     */
    CommandProcessingResult deleteEirCalculation(Long calculationId);
}
