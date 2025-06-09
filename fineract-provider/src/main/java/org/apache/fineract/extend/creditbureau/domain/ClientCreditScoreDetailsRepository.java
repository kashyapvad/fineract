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
package org.apache.fineract.extend.creditbureau.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ClientCreditScoreDetails entity.
 *
 * Provides data access methods for individual credit scores that belong to credit reports.
 */
@Repository
public interface ClientCreditScoreDetailsRepository
        extends JpaRepository<ClientCreditScoreDetails, Long>, JpaSpecificationExecutor<ClientCreditScoreDetails> {

    /**
     * Find all credit scores for a specific credit report.
     */
    List<ClientCreditScoreDetails> findByCreditReport_IdOrderByScoreModelAsc(Long creditReportId);

    /**
     * Find credit scores for a specific credit report and score model.
     */
    List<ClientCreditScoreDetails> findByCreditReport_IdAndScoreModelOrderByScoreVersionDesc(Long creditReportId, String scoreModel);

    /**
     * Find a specific credit score by report, model, and version.
     */
    Optional<ClientCreditScoreDetails> findByCreditReport_IdAndScoreModelAndScoreVersion(Long creditReportId, String scoreModel,
            String scoreVersion);

    /**
     * Find the highest credit score for a credit report.
     */
    @Query("SELECT cs FROM ClientCreditScoreDetails cs " + "WHERE cs.creditReport.id = :creditReportId " + "ORDER BY cs.creditScore DESC")
    List<ClientCreditScoreDetails> findHighestScoreForReport(@Param("creditReportId") Long creditReportId);

    /**
     * Find credit scores within a specific score range.
     */
    @Query("SELECT cs FROM ClientCreditScoreDetails cs " + "WHERE cs.creditScore BETWEEN :minScore AND :maxScore "
            + "ORDER BY cs.creditScore DESC")
    List<ClientCreditScoreDetails> findByScoreRange(@Param("minScore") Integer minScore, @Param("maxScore") Integer maxScore);

    /**
     * Find all credit scores for a specific score model across all reports.
     */
    List<ClientCreditScoreDetails> findByScoreModelOrderByCreditScoreDesc(String scoreModel);

    /**
     * Count credit scores for a specific credit report.
     */
    Long countByCreditReport_Id(Long creditReportId);

    /**
     * Find credit scores by provider score ID.
     */
    Optional<ClientCreditScoreDetails> findByProviderScoreId(String providerScoreId);

    /**
     * Find all credit scores for scores that have scoring elements.
     */
    @Query("SELECT cs FROM ClientCreditScoreDetails cs " + "WHERE cs.scoringElements IS NOT NULL")
    List<ClientCreditScoreDetails> findScoresWithScoringElements();

    /**
     * Delete all credit scores for a specific credit report.
     */
    void deleteByCreditReport_Id(Long creditReportId);
}
