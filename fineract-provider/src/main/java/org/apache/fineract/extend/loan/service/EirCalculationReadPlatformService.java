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

import java.util.List;
import org.apache.fineract.extend.loan.dto.EirCalculationResponse;

/**
 * Platform service for EIR calculation read operations.
 *
 * Handles retrieval and query operations for EIR calculations.
 */
public interface EirCalculationReadPlatformService {

    /**
     * Retrieve a single EIR calculation by ID.
     *
     * @param calculationId
     *            ID of the calculation to retrieve
     * @return EirCalculationResponse or null if not found
     */
    EirCalculationResponse retrieveEirCalculation(Long calculationId);

    /**
     * Retrieve all EIR calculations for a specific loan.
     *
     * @param loanId
     *            Loan ID to retrieve calculations for
     * @return List of EirCalculationResponse
     */
    List<EirCalculationResponse> retrieveEirCalculationsByLoanId(Long loanId);

    /**
     * Retrieve the latest EIR calculation for a loan.
     *
     * @param loanId
     *            Loan ID to retrieve latest calculation for
     * @return EirCalculationResponse with the most recent calculation
     * @throws RuntimeException
     *             if no calculation exists for the loan
     */
    EirCalculationResponse retrieveLatestEirCalculation(Long loanId);

    /**
     * Retrieve calculation history for a loan.
     *
     * @param loanId
     *            Loan ID to retrieve history for
     * @return List of EirCalculationResponse ordered by calculation date
     */
    List<EirCalculationResponse> retrieveEirCalculationHistory(Long loanId);
}
