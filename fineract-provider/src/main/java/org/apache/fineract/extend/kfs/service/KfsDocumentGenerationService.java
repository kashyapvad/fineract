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
package org.apache.fineract.extend.kfs.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.extend.kfs.domain.KfsTemplate;
import org.apache.fineract.extend.kfs.dto.*;

/**
 * Service interface for KFS document generation operations. Handles document generation, validation, preview, and
 * template management.
 */
public interface KfsDocumentGenerationService {

    /**
     * Generate a KFS document for a loan using the specified template and data.
     *
     * @param request
     *            The generation request containing template and data information
     * @param scheduleData
     *            The repayment schedule data for the loan
     * @return KFS document generation result
     */
    KfsDocumentGenerationResult generateKfsDocument(KfsDocumentGenerationRequest request, List<RepaymentScheduleData> scheduleData);

    /**
     * Generate KFS documents for multiple loans in bulk.
     *
     * @param loanIds
     *            List of loan IDs to generate documents for
     * @param baseRequest
     *            Base generation request to use for all loans
     * @return List of generation results
     */
    List<KfsDocumentGenerationResult> bulkGenerateKfsDocuments(List<Long> loanIds, KfsDocumentGenerationRequest baseRequest);

    /**
     * Preview a KFS document without actually generating it.
     *
     * @param request
     *            The generation request
     * @param scheduleData
     *            The repayment schedule data
     * @return Preview result containing the document content
     */
    KfsDocumentGenerationResult previewKfsDocument(KfsDocumentGenerationRequest request, List<RepaymentScheduleData> scheduleData);

    /**
     * Get list of available templates for document generation.
     *
     * @return List of available template information
     */
    List<KfsTemplateInfo> getAvailableTemplates();

    /**
     * Get template field configuration for a specific template.
     *
     * @param templateName
     *            The template name
     * @param templateVersion
     *            The template version
     * @return Template fields configuration
     */
    KfsTemplateFieldsConfig getTemplateFields(String templateName, String templateVersion);

    /**
     * Validate a generation request and the provided data.
     *
     * @param request
     *            The generation request to validate
     * @param scheduleData
     *            The repayment schedule data to validate
     * @return Validation result map
     */
    Map<String, Object> validateGenerationRequest(KfsDocumentGenerationRequest request, List<RepaymentScheduleData> scheduleData);

    /**
     * Get generation history for a specific loan.
     *
     * @param loanId
     *            The loan ID
     * @return List of generation history entries
     */
    List<KfsGenerationHistoryEntry> getGenerationHistory(Long loanId);

    /**
     * Regenerate an existing KFS document using the same parameters.
     *
     * @param existingDocumentId
     *            The ID of the existing document to regenerate
     * @return Generation result
     */
    KfsDocumentGenerationResult regenerateKfsDocument(Long existingDocumentId);

    /**
     * Get generation statistics across all documents.
     *
     * @return Generation statistics
     */
    KfsGenerationStatistics getGenerationStatistics();

    // Production implementation uses actual loan service for repayment schedule data

    // TEST CONTRACT METHODS - These support the test interfaces

    /**
     * Generate KFS document (test interface).
     *
     * @param request
     *            Generation request
     * @return Generated document response
     */
    KfsDocumentGenerationResponse generateKfsDocument(KfsDocumentGenerationRequest request);

    /**
     * Bulk generate KFS documents (test interface).
     *
     * @param requests
     *            List of generation requests
     * @return List of generated document responses
     */
    List<KfsDocumentGenerationResponse> bulkGenerateKfsDocuments(List<KfsDocumentGenerationRequest> requests);

    /**
     * Get generation status (test interface).
     *
     * @param jobId
     *            Job ID to check
     * @return Generation status response
     */
    KfsDocumentGenerationResponse getGenerationStatus(String jobId);

    /**
     * Upload a new KFS template.
     *
     * @param templateName
     *            Template name
     * @param templateVersion
     *            Template version
     * @param description
     *            Template description
     * @param fileName
     *            Original file name
     * @param fileContent
     *            File content as byte array
     * @return Uploaded template information
     */
    KfsTemplateInfo uploadTemplate(String templateName, String templateVersion, String description, String fileName, byte[] fileContent);

    /**
     * Get the latest active template.
     *
     * @return Optional containing the latest active template, or empty if none found
     */
    Optional<KfsTemplate> getLatestActiveTemplate();
}
