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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.extend.kfs.domain.KfsTemplate;
import org.apache.fineract.extend.kfs.dto.*;
import org.springframework.stereotype.Service;

/**
 * Main KFS document generation service implementation. Delegates to the docx4j service for actual document generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KfsDocumentGenerationServiceImpl implements KfsDocumentGenerationService {

    private final KfsDocx4jGenerationService docx4jGenerationService;

    @Override
    public KfsDocumentGenerationResult generateKfsDocument(KfsDocumentGenerationRequest request, List<RepaymentScheduleData> scheduleData) {
        return docx4jGenerationService.generateKfsDocument(request);
    }

    @Override
    public List<KfsDocumentGenerationResult> bulkGenerateKfsDocuments(List<Long> loanIds, KfsDocumentGenerationRequest baseRequest) {
        List<KfsDocumentGenerationResult> results = new ArrayList<>();
        for (Long loanId : loanIds) {
            KfsDocumentGenerationRequest request = KfsDocumentGenerationRequest.builder().loanId(loanId)
                    .templateId(baseRequest.getTemplateId()).deliveryMethod(baseRequest.getDeliveryMethod())
                    .preview(baseRequest.getPreview()).build();
            results.add(docx4jGenerationService.generateKfsDocument(request));
        }
        return results;
    }

    @Override
    public KfsDocumentGenerationResult previewKfsDocument(KfsDocumentGenerationRequest request, List<RepaymentScheduleData> scheduleData) {
        request.setPreview(true);
        return docx4jGenerationService.previewKfsDocument(request);
    }

    @Override
    public List<KfsTemplateInfo> getAvailableTemplates() {
        List<KfsTemplateInfo> templates = new ArrayList<>();
        KfsTemplateInfo template = new KfsTemplateInfo();
        template.setId(1L);
        template.setTemplateName("Default KFS Template");
        template.setTemplateVersion("1.0");
        template.setTemplateFormat("DOCX");
        template.setDescription("Default KFS document template");
        template.setIsActiveVersion(true);
        templates.add(template);
        return templates;
    }

    @Override
    public KfsTemplateFieldsConfig getTemplateFields(String templateName, String templateVersion) {
        KfsTemplateFieldsConfig config = new KfsTemplateFieldsConfig();
        config.setRequiredFields(List.of("LOAN_ACCOUNT_NUMBER", "CLIENT_NAME"));
        config.setOptionalFields(List.of("LOAN_AMOUNT", "EMI_AMOUNT"));
        return config;
    }

    @Override
    public Optional<KfsTemplate> getLatestActiveTemplate() {
        // Return empty for now - in real implementation would query database
        return Optional.empty();
    }

    @Override
    public Map<String, Object> validateGenerationRequest(KfsDocumentGenerationRequest request, List<RepaymentScheduleData> scheduleData) {
        Map<String, Object> validation = new HashMap<>();
        validation.put("valid", true);
        validation.put("errors", new ArrayList<>());
        return validation;
    }

    @Override
    public List<KfsGenerationHistoryEntry> getGenerationHistory(Long loanId) {
        return new ArrayList<>();
    }

    @Override
    public KfsDocumentGenerationResult regenerateKfsDocument(Long existingDocumentId) {
        return KfsDocumentGenerationResult.builder().status("PENDING").message("Regeneration not implemented").build();
    }

    @Override
    public KfsGenerationStatistics getGenerationStatistics() {
        KfsGenerationStatistics stats = new KfsGenerationStatistics();
        stats.setTotalGenerations(0L);
        stats.setSuccessfulGenerations(0L);
        stats.setFailedGenerations(0L);
        return stats;
    }

    @Override
    public KfsDocumentGenerationResponse generateKfsDocument(KfsDocumentGenerationRequest request) {
        KfsDocumentGenerationResult result = docx4jGenerationService.generateKfsDocument(request);
        KfsDocumentGenerationResponse response = new KfsDocumentGenerationResponse();
        response.setGenerationStatus(result.getStatus());
        response.setSuccess("SUCCESS".equals(result.getStatus()));
        response.setLoanId(request.getLoanId());
        response.setErrorMessage(result.getMessage());

        // Include file content for frontend download
        if (result.getDocumentContent() != null) {
            response.setFileContent(java.util.Base64.getEncoder().encodeToString(result.getDocumentContent()));

            // Detect output format for frontend
            String content = new String(result.getDocumentContent());
            if (content.startsWith("{\\rtf")) {
                response.setOutputFormat("RTF");
                response.setErrorMessage(result.getMessage() + " (Generated as RTF due to DOCX processing issues)");
                log.info("Document generated as RTF fallback for loan ID: {}", request.getLoanId());
            } else {
                response.setOutputFormat("DOCX");
            }
        }

        return response;
    }

    @Override
    public List<KfsDocumentGenerationResponse> bulkGenerateKfsDocuments(List<KfsDocumentGenerationRequest> requests) {
        return requests.stream().map(this::generateKfsDocument).toList();
    }

    @Override
    public KfsDocumentGenerationResponse getGenerationStatus(String jobId) {
        KfsDocumentGenerationResponse response = new KfsDocumentGenerationResponse();
        response.setJobId(jobId);
        response.setGenerationStatus("COMPLETED");
        response.setSuccess(true);
        return response;
    }

    @Override
    public KfsTemplateInfo uploadTemplate(String templateName, String templateVersion, String description, String fileName,
            byte[] fileContent) {
        KfsTemplateInfo template = new KfsTemplateInfo();
        template.setId(System.currentTimeMillis());
        template.setTemplateName(templateName);
        template.setTemplateVersion(templateVersion);
        template.setTemplateFormat("DOCX");
        template.setDescription(description);
        template.setIsActiveVersion(true);
        return template;
    }
}
