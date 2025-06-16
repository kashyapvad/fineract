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
package org.apache.fineract.extend.kfs.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for KfsDocument entities. Provides standard CRUD operations and custom query methods for KFS document
 * management.
 */
@Repository
public interface KfsDocumentRepository extends JpaRepository<KfsDocument, Long> {

    /**
     * Find all documents for a specific loan.
     */
    List<KfsDocument> findByLoanId(Long loanId);

    /**
     * Find all documents for a specific client.
     */
    List<KfsDocument> findByClientId(Long clientId);

    /**
     * Find document by unique reference number.
     */
    Optional<KfsDocument> findByDocumentReferenceNumber(String documentReferenceNumber);

    /**
     * Find documents by status.
     */
    List<KfsDocument> findByDocumentStatus(String documentStatus);

    /**
     * Find documents generated within a date range.
     */
    List<KfsDocument> findByGenerationDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find documents for a specific EIR calculation.
     */
    List<KfsDocument> findByEirCalculationId(Long eirCalculationId);

    /**
     * Find documents created using a specific template.
     */
    List<KfsDocument> findByKfsTemplateId(Long kfsTemplateId);

    /**
     * Find the latest document for a loan (by generation date). Note: JPQL doesn't support LIMIT, so we use
     * setMaxResults() in the service layer
     */
    @Query("SELECT d FROM KfsDocument d WHERE d.loanId = :loanId ORDER BY d.generationDate DESC, d.id DESC")
    List<KfsDocument> findByLoanIdOrderByGenerationDateDesc(@Param("loanId") Long loanId);

    /**
     * Count documents for a specific loan.
     */
    long countByLoanId(Long loanId);

    /**
     * Count documents by status.
     */
    long countByDocumentStatus(String documentStatus);

    /**
     * Check if document exists with specified reference number.
     */
    boolean existsByDocumentReferenceNumber(String documentReferenceNumber);

    /**
     * Delete all documents for a specific loan. Returns the number of deleted records.
     */
    long deleteByLoanId(Long loanId);

    /**
     * Find documents by delivery method.
     */
    List<KfsDocument> findByDeliveryMethod(String deliveryMethod);

    /**
     * Find documents by recipient acknowledgment status.
     */
    List<KfsDocument> findByRecipientAcknowledgment(Boolean recipientAcknowledgment);

    /**
     * Find the latest document for a loan with specific output format.
     */
    @Query("SELECT d FROM KfsDocument d WHERE d.loanId = :loanId AND d.outputFormat = :outputFormat ORDER BY d.generationDate DESC, d.id DESC")
    List<KfsDocument> findByLoanIdAndOutputFormatOrderByGenerationDateDesc(@Param("loanId") Long loanId,
            @Param("outputFormat") String outputFormat);

    /**
     * Find the latest document for a loan with specific template ID and output format.
     */
    @Query("SELECT d FROM KfsDocument d WHERE d.loanId = :loanId AND d.kfsTemplateId = :templateId AND d.outputFormat = :outputFormat ORDER BY d.generationDate DESC, d.id DESC")
    List<KfsDocument> findByLoanIdAndTemplateIdAndOutputFormatOrderByGenerationDateDesc(@Param("loanId") Long loanId,
            @Param("templateId") Long templateId, @Param("outputFormat") String outputFormat);

    /**
     * Find documents by output format.
     */
    List<KfsDocument> findByOutputFormat(String outputFormat);

    /**
     * Find documents by template format.
     */
    List<KfsDocument> findByTemplateFormat(String templateFormat);

    /**
     * Check if a document exists for loan with specific template and output format.
     */
    boolean existsByLoanIdAndKfsTemplateIdAndOutputFormat(Long loanId, Long kfsTemplateId, String outputFormat);
}
