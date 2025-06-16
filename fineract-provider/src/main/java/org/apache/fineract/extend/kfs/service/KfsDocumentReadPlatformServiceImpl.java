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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.fineract.extend.kfs.domain.KfsDocument;
import org.apache.fineract.extend.kfs.domain.KfsDocumentRepository;
import org.apache.fineract.extend.kfs.dto.KfsDocumentResponse;
import org.apache.fineract.extend.kfs.dto.KfsDocumentStatistics;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for read operations on KFS documents. Handles retrieval, download, and statistics operations
 * with proper security and error handling.
 */
@Service
public class KfsDocumentReadPlatformServiceImpl implements KfsDocumentReadPlatformService {

    private static final Logger log = LoggerFactory.getLogger(KfsDocumentReadPlatformServiceImpl.class);

    private final PlatformSecurityContext context;
    private final KfsDocumentRepository kfsDocumentRepository;

    @Autowired
    public KfsDocumentReadPlatformServiceImpl(final PlatformSecurityContext context, final KfsDocumentRepository kfsDocumentRepository) {
        this.context = context;
        this.kfsDocumentRepository = kfsDocumentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public KfsDocumentResponse retrieveKfsDocument(Long documentId) {
        this.context.authenticatedUser();

        if (documentId == null) {
            throw new PlatformDataIntegrityException("error.msg.kfs.document.id.required", "Document ID is required");
        }

        KfsDocument document = this.kfsDocumentRepository.findById(documentId)
                .orElseThrow(() -> new PlatformDataIntegrityException("error.msg.kfs.document.not.found",
                        "KFS Document with ID " + documentId + " not found"));

        log.debug("Retrieved KFS Document with ID: {}", documentId);
        return mapEntityToResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KfsDocumentResponse> retrieveKfsDocumentsByLoanId(Long loanId) {
        this.context.authenticatedUser();

        if (loanId == null) {
            throw new PlatformDataIntegrityException("error.msg.kfs.document.loan.id.required", "Loan ID is required");
        }

        List<KfsDocument> documents = this.kfsDocumentRepository.findByLoanId(loanId);

        log.debug("Retrieved {} KFS Documents for loan ID: {}", documents.size(), loanId);

        return documents.stream().map(this::mapEntityToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KfsDocumentResponse> retrieveKfsDocumentsByClientId(Long clientId) {
        this.context.authenticatedUser();

        if (clientId == null) {
            throw new PlatformDataIntegrityException("error.msg.kfs.document.client.id.required", "Client ID is required");
        }

        List<KfsDocument> documents = this.kfsDocumentRepository.findByClientId(clientId);

        log.debug("Retrieved {} KFS Documents for client ID: {}", documents.size(), clientId);

        return documents.stream().map(this::mapEntityToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KfsDocumentResponse> retrieveKfsDocumentsByStatus(String status) {
        this.context.authenticatedUser();

        if (status == null || status.trim().isEmpty()) {
            throw new PlatformDataIntegrityException("error.msg.kfs.document.status.required", "Document status is required");
        }

        List<KfsDocument> documents = this.kfsDocumentRepository.findByDocumentStatus(status);

        log.debug("Retrieved {} KFS Documents with status: {}", documents.size(), status);

        return documents.stream().map(this::mapEntityToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KfsDocumentResponse> retrieveKfsDocumentsByDateRange(LocalDate startDate, LocalDate endDate) {
        this.context.authenticatedUser();

        if (startDate == null) {
            throw new PlatformDataIntegrityException("error.msg.kfs.document.start.date.required", "Start date is required");
        }

        if (endDate == null) {
            throw new PlatformDataIntegrityException("error.msg.kfs.document.end.date.required", "End date is required");
        }

        if (startDate.isAfter(endDate)) {
            throw new PlatformDataIntegrityException("error.msg.kfs.document.invalid.date.range", "Start date cannot be after end date");
        }

        List<KfsDocument> documents = this.kfsDocumentRepository.findByGenerationDateBetween(startDate, endDate);

        log.debug("Retrieved {} KFS Documents between {} and {}", documents.size(), startDate, endDate);

        return documents.stream().map(this::mapEntityToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadKfsDocument(Long documentId) {
        this.context.authenticatedUser();

        if (documentId == null) {
            throw new PlatformDataIntegrityException("error.msg.kfs.document.id.required", "Document ID is required");
        }

        KfsDocument document = this.kfsDocumentRepository.findById(documentId)
                .orElseThrow(() -> new PlatformDataIntegrityException("error.msg.kfs.document.not.found",
                        "KFS Document with ID " + documentId + " not found"));

        if (document.getGeneratedFilePath() == null || document.getGeneratedFilePath().trim().isEmpty()) {
            throw new PlatformDataIntegrityException("error.msg.kfs.document.no.file.path",
                    "No file path available for document with ID " + documentId);
        }

        try {
            Path filePath = Paths.get(document.getGeneratedFilePath());

            if (!Files.exists(filePath)) {
                throw new PlatformDataIntegrityException("error.msg.kfs.document.file.not.found",
                        "Document file not found at path: " + document.getGeneratedFilePath());
            }

            byte[] fileContent = Files.readAllBytes(filePath);

            // Verify file size matches stored size if available
            if (document.getFileSize() != null && fileContent.length != document.getFileSize()) {
                log.warn("File size mismatch for document {}: stored={}, actual={}", documentId, document.getFileSize(),
                        fileContent.length);
            }

            log.info("Downloaded KFS Document with ID: {}, size: {} bytes", documentId, fileContent.length);
            return fileContent;

        } catch (IOException e) {
            log.error("Error reading file for document ID {}: {}", documentId, e.getMessage(), e);
            throw new PlatformDataIntegrityException("error.msg.kfs.document.file.read.error",
                    "Error reading document file: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public KfsDocumentStatistics retrieveKfsDocumentStatistics() {
        this.context.authenticatedUser();

        try {
            // Get total document count
            long totalDocuments = this.kfsDocumentRepository.count();

            // Get document counts by status
            long generatedCount = this.kfsDocumentRepository.countByDocumentStatus("GENERATED");
            long processingCount = this.kfsDocumentRepository.countByDocumentStatus("PROCESSING");
            long deliveredCount = this.kfsDocumentRepository.countByDocumentStatus("DELIVERED");
            long acknowledgedCount = this.kfsDocumentRepository.countByDocumentStatus("ACKNOWLEDGED");
            long failedCount = this.kfsDocumentRepository.countByDocumentStatus("FAILED");
            long cancelledCount = this.kfsDocumentRepository.countByDocumentStatus("CANCELLED");

            // Calculate total file size of all documents
            List<KfsDocument> allDocuments = this.kfsDocumentRepository.findAll();
            long totalFileSize = allDocuments.stream().filter(doc -> doc.getFileSize() != null).mapToLong(KfsDocument::getFileSize).sum();

            // Get average file size
            double averageFileSize = allDocuments.stream().filter(doc -> doc.getFileSize() != null).mapToLong(KfsDocument::getFileSize)
                    .average().orElse(0.0);

            // Get documents generated today
            long documentsGeneratedToday = this.kfsDocumentRepository.findByGenerationDateBetween(LocalDate.now(), LocalDate.now()).size();

            // Get documents delivered today
            long documentsDeliveredToday = allDocuments.stream()
                    .filter(doc -> "DELIVERED".equals(doc.getDocumentStatus()) || "ACKNOWLEDGED".equals(doc.getDocumentStatus()))
                    .filter(doc -> doc.getDeliveryDate() != null && doc.getDeliveryDate().equals(LocalDate.now())).count();

            KfsDocumentStatistics statistics = new KfsDocumentStatistics();
            statistics.setTotalDocuments(totalDocuments);
            statistics.setGeneratedCount(generatedCount);
            statistics.setProcessingCount(processingCount);
            statistics.setDeliveredCount(deliveredCount);
            statistics.setAcknowledgedCount(acknowledgedCount);
            statistics.setFailedCount(failedCount);
            statistics.setCancelledCount(cancelledCount);
            statistics.setTotalFileSize(totalFileSize);
            statistics.setAverageFileSize(averageFileSize);
            statistics.setDocumentsGeneratedToday(documentsGeneratedToday);
            statistics.setDocumentsDeliveredToday(documentsDeliveredToday);

            log.debug("Generated KFS Document statistics: {} total documents", totalDocuments);
            return statistics;

        } catch (Exception e) {
            log.error("Error generating KFS document statistics: {}", e.getMessage(), e);
            throw new PlatformDataIntegrityException("error.msg.kfs.document.statistics.error",
                    "Error generating document statistics: " + e.getMessage());
        }
    }

    private KfsDocumentResponse mapEntityToResponse(KfsDocument document) {
        KfsDocumentResponse response = new KfsDocumentResponse();

        response.setDocumentId(document.getId());
        response.setLoanId(document.getLoanId());
        response.setClientId(document.getClientId());
        response.setEirCalculationId(document.getEirCalculationId());
        response.setKfsTemplateId(document.getKfsTemplateId());
        response.setDocumentReferenceNumber(document.getDocumentReferenceNumber());
        response.setGenerationDate(document.getGenerationDate());
        response.setGeneratedFilePath(document.getGeneratedFilePath());
        response.setFileSize(document.getFileSize());
        response.setChecksum(document.getChecksum());
        response.setDocumentStatus(document.getDocumentStatus());
        response.setDeliveryMethod(document.getDeliveryMethod());
        response.setDeliveryDate(document.getDeliveryDate());
        response.setRecipientAcknowledgment(document.getRecipientAcknowledgment());

        // Add audit fields
        response.setCreatedDate(document.getCreatedDate().map(OffsetDateTime::toLocalDateTime).orElse(null));
        response.setLastModifiedDate(document.getLastModifiedDate().map(OffsetDateTime::toLocalDateTime).orElse(null));
        response.setCreatedBy(document.getCreatedBy().map(String::valueOf).orElse(null));
        response.setLastModifiedBy(document.getLastModifiedBy().map(String::valueOf).orElse(null));

        return response;
    }
}
