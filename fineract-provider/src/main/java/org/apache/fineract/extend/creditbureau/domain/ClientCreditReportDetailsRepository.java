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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ClientCreditReportDetails entity.
 *
 * Provides data access methods for credit report entities with support for custom queries and specifications for
 * complex filtering requirements.
 */
@Repository
public interface ClientCreditReportDetailsRepository
        extends JpaRepository<ClientCreditReportDetails, Long>, JpaSpecificationExecutor<ClientCreditReportDetails> {

    /**
     * Find all credit reports for a specific client.
     */
    @Query("SELECT cr FROM ClientCreditReportDetails cr WHERE cr.client.id = :clientId ORDER BY cr.createdDate DESC")
    List<ClientCreditReportDetails> findByClientIdOrderByCreatedDateDesc(@Param("clientId") Long clientId);

    /**
     * Find all credit reports for a specific client and provider.
     */
    @Query("SELECT cr FROM ClientCreditReportDetails cr WHERE cr.client.id = :clientId AND cr.creditBureauProvider = :provider ORDER BY cr.createdDate DESC")
    List<ClientCreditReportDetails> findByClientIdAndCreditBureauProviderOrderByCreatedDateDesc(@Param("clientId") Long clientId,
            @Param("provider") String creditBureauProvider);

    /**
     * Find credit reports for a specific client by report type.
     */
    @Query("SELECT cr FROM ClientCreditReportDetails cr WHERE cr.client.id = :clientId AND cr.reportType = :reportType ORDER BY cr.createdDate DESC")
    List<ClientCreditReportDetails> findByClientIdAndReportTypeOrderByCreatedDateDesc(@Param("clientId") Long clientId,
            @Param("reportType") CreditBureauReportType reportType);

    /**
     * Find credit reports for a specific client by status.
     */
    @Query("SELECT cr FROM ClientCreditReportDetails cr WHERE cr.client.id = :clientId AND cr.reportStatus = :reportStatus ORDER BY cr.createdDate DESC")
    List<ClientCreditReportDetails> findByClientIdAndReportStatusOrderByCreatedDateDesc(@Param("clientId") Long clientId,
            @Param("reportStatus") CreditBureauReportStatus reportStatus);

    /**
     * Find the most recent successful credit report for a client.
     */
    @Query("SELECT cr FROM ClientCreditReportDetails cr " + "WHERE cr.client.id = :clientId "
            + "AND cr.reportStatus = org.apache.fineract.extend.creditbureau.domain.CreditBureauReportStatus.SUCCESS "
            + "ORDER BY cr.reportGeneratedOn DESC, cr.createdDate DESC")
    List<ClientCreditReportDetails> findLatestSuccessfulReportByClientId(@Param("clientId") Long clientId);

    /**
     * Find the most recent successful credit report for a client and provider.
     */
    @Query("SELECT cr FROM ClientCreditReportDetails cr " + "WHERE cr.client.id = :clientId " + "AND cr.creditBureauProvider = :provider "
            + "AND cr.reportStatus = org.apache.fineract.extend.creditbureau.domain.CreditBureauReportStatus.SUCCESS "
            + "ORDER BY cr.reportGeneratedOn DESC, cr.createdDate DESC")
    List<ClientCreditReportDetails> findLatestSuccessfulReportByClientIdAndProvider(@Param("clientId") Long clientId,
            @Param("provider") String provider);

    /**
     * Find credit report by provider report ID.
     */
    Optional<ClientCreditReportDetails> findByProviderReportId(String providerReportId);

    /**
     * Find credit reports by customer PAN.
     */
    List<ClientCreditReportDetails> findByCustomerPanOrderByCreatedDateDesc(String customerPan);

    /**
     * Find credit reports by customer Aadhaar.
     */
    List<ClientCreditReportDetails> findByCustomerAadhaarOrderByCreatedDateDesc(String customerAadhaar);

    /**
     * Find credit reports generated within a date range.
     */
    @Query("SELECT cr FROM ClientCreditReportDetails cr " + "WHERE cr.reportGeneratedOn BETWEEN :startDate AND :endDate "
            + "ORDER BY cr.reportGeneratedOn DESC")
    List<ClientCreditReportDetails> findByReportGeneratedOnBetween(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find pending credit reports older than specified days.
     */
    @Query("SELECT cr FROM ClientCreditReportDetails cr "
            + "WHERE cr.reportStatus = org.apache.fineract.extend.creditbureau.domain.CreditBureauReportStatus.PENDING "
            + "AND cr.createdDate < :cutoffDate")
    List<ClientCreditReportDetails> findStalePendingReports(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Count successful credit reports for a client.
     */
    @Query("SELECT COUNT(cr) FROM ClientCreditReportDetails cr " + "WHERE cr.client.id = :clientId "
            + "AND cr.reportStatus = org.apache.fineract.extend.creditbureau.domain.CreditBureauReportStatus.SUCCESS")
    Long countSuccessfulReportsByClientId(@Param("clientId") Long clientId);

    /**
     * Count credit reports by provider.
     */
    Long countByCreditBureauProvider(String creditBureauProvider);

    /**
     * Find credit reports with primary credit score in a specific range.
     */
    @Query("SELECT cr FROM ClientCreditReportDetails cr " + "WHERE cr.primaryCreditScore BETWEEN :minScore AND :maxScore "
            + "ORDER BY cr.primaryCreditScore DESC")
    List<ClientCreditReportDetails> findByPrimaryCreditScoreBetween(@Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore);

    /**
     * Find credit reports that have multiple scoring models.
     */
    @Query("SELECT cr FROM ClientCreditReportDetails cr " + "WHERE cr.totalScoreModels > 1 " + "ORDER BY cr.totalScoreModels DESC")
    List<ClientCreditReportDetails> findReportsWithMultipleScoreModels();

    /**
     * Find credit reports by requested user.
     */
    List<ClientCreditReportDetails> findByRequestedByUser_IdOrderByCreatedDateDesc(Long requestedByUserId);

    /**
     * Check if a credit report exists for client and provider within specified days.
     */
    @Query("SELECT COUNT(cr) FROM ClientCreditReportDetails cr " + "WHERE cr.client.id = :clientId "
            + "AND cr.creditBureauProvider = :provider "
            + "AND cr.reportStatus = org.apache.fineract.extend.creditbureau.domain.CreditBureauReportStatus.SUCCESS "
            + "AND cr.reportGeneratedOn >= :cutoffDate")
    Long countRecentSuccessfulReports(@Param("clientId") Long clientId, @Param("provider") String provider,
            @Param("cutoffDate") LocalDate cutoffDate);
}
