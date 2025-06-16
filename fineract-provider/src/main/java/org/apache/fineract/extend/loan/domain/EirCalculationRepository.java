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
package org.apache.fineract.extend.loan.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * EIR Calculation Repository - Data Access Layer
 *
 * Provides CRUD operations and custom queries for EIR calculations. Implements the repository pattern for
 * EirCalculation entities.
 *
 * @author fineract
 */
@Repository
public interface EirCalculationRepository extends JpaRepository<EirCalculation, Long> {

    /**
     * Find the most recent EIR calculation for a specific loan
     */
    Optional<EirCalculation> findTopByLoanIdOrderByCalculationDateDesc(Long loanId);

    /**
     * Find all EIR calculations for a specific loan, ordered by calculation date
     */
    @Query("SELECT ec FROM EirCalculation ec WHERE ec.loanId = :loanId ORDER BY ec.calculationDate DESC")
    List<EirCalculation> findAllByLoanIdOrderByCalculationDateDesc(@Param("loanId") Long loanId);

    /**
     * Find EIR calculations for multiple loans (batch processing)
     */
    @Query("SELECT ec FROM EirCalculation ec WHERE ec.loanId IN :loanIds")
    List<EirCalculation> findByLoanIds(@Param("loanIds") List<Long> loanIds);

    /**
     * Check if EIR calculation exists for a loan
     */
    boolean existsByLoanId(Long loanId);

    /**
     * Find latest EIR calculation for a loan (replaces compliance-based method)
     */
    @Query("SELECT ec FROM EirCalculation ec WHERE ec.loanId = :loanId ORDER BY ec.calculationDate DESC")
    Optional<EirCalculation> findLatestByLoanId(@Param("loanId") Long loanId);

    // Additional methods expected by tests
    /**
     * Find EIR calculations by loan ID ordered by calculation date descending
     */
    List<EirCalculation> findByLoanIdOrderByCalculationDateDesc(Long loanId);

    /**
     * Find EIR calculations by loan ID and calculation date range
     */
    List<EirCalculation> findByLoanIdAndCalculationDateBetween(Long loanId, LocalDate fromDate, LocalDate toDate);

    /**
     * Find first EIR calculation by loan ID ordered by calculation date descending
     */
    Optional<EirCalculation> findFirstByLoanIdOrderByCalculationDateDesc(Long loanId);

    /**
     * Count EIR calculations by loan ID
     */
    Long countByLoanId(Long loanId);

    /**
     * Find EIR calculations by principal amount range
     */
    List<EirCalculation> findByPrincipalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
}
