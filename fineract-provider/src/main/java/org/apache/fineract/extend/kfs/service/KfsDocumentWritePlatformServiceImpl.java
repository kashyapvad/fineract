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

import java.time.LocalDate;
import org.apache.fineract.extend.kfs.domain.KfsDocument;
import org.apache.fineract.extend.kfs.domain.KfsDocumentRepository;
import org.apache.fineract.extend.kfs.domain.KfsTemplate;
import org.apache.fineract.extend.kfs.domain.KfsTemplateRepository;
import org.apache.fineract.extend.kfs.dto.KfsDocumentRequest;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for write operations on KFS documents. Handles create, update, delete, and status update
 * operations with proper validation and error handling.
 */
@Service
public class KfsDocumentWritePlatformServiceImpl implements KfsDocumentWritePlatformService {

    private static final Logger log = LoggerFactory.getLogger(KfsDocumentWritePlatformServiceImpl.class);

    private final PlatformSecurityContext context;
    private final KfsDocumentRepository kfsDocumentRepository;
    private final KfsTemplateRepository kfsTemplateRepository;

    @Autowired
    public KfsDocumentWritePlatformServiceImpl(final PlatformSecurityContext context, final KfsDocumentRepository kfsDocumentRepository,
            final KfsTemplateRepository kfsTemplateRepository) {
        this.context = context;
        this.kfsDocumentRepository = kfsDocumentRepository;
        this.kfsTemplateRepository = kfsTemplateRepository;
    }

    // JsonCommand-based methods for command handlers

    @Override
    @Transactional
    public CommandProcessingResult createKfsDocument(JsonCommand command) {
        this.context.authenticatedUser();

        log.info("Creating KFS document via command");

        try {
            // Convert JsonCommand to KfsDocumentRequest
            KfsDocumentRequest request = convertJsonCommandToRequest(command);

            // Use the existing DTO-based method
            return createKfsDocument(request);

        } catch (Exception e) {
            log.error("Error creating KFS document via command: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateKfsDocument(JsonCommand command) {
        this.context.authenticatedUser();

        // For updates, document ID should be passed as a parameter
        Long documentId = command.longValueOfParameterNamed("documentId");
        log.info("Updating KFS document via command for ID: {}", documentId);

        try {
            // Convert JsonCommand to KfsDocumentRequest
            KfsDocumentRequest request = convertJsonCommandToRequest(command);

            // Use the existing DTO-based method
            return updateKfsDocument(documentId, request);

        } catch (Exception e) {
            log.error("Error updating KFS document via command: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteKfsDocument(JsonCommand command) {
        this.context.authenticatedUser();

        // For deletes, document ID should be passed as a parameter
        Long documentId = command.longValueOfParameterNamed("documentId");
        log.info("Deleting KFS document via command for ID: {}", documentId);

        try {
            // Use the existing DTO-based method
            return deleteKfsDocument(documentId);

        } catch (Exception e) {
            log.error("Error deleting KFS document via command: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Convert JsonCommand to KfsDocumentRequest for processing.
     */
    private KfsDocumentRequest convertJsonCommandToRequest(JsonCommand command) {
        KfsDocumentRequest request = new KfsDocumentRequest();

        // Extract data from JsonCommand using available KfsDocumentRequest fields
        if (command.hasParameter("loanId")) {
            request.setLoanId(command.longValueOfParameterNamed("loanId"));
        }

        if (command.hasParameter("clientId")) {
            request.setClientId(command.longValueOfParameterNamed("clientId"));
        }

        if (command.hasParameter("eirCalculationId")) {
            request.setEirCalculationId(command.longValueOfParameterNamed("eirCalculationId"));
        }

        if (command.hasParameter("kfsTemplateId")) {
            request.setKfsTemplateId(command.longValueOfParameterNamed("kfsTemplateId"));
        }

        if (command.hasParameter("documentReferenceNumber")) {
            request.setDocumentReferenceNumber(command.stringValueOfParameterNamed("documentReferenceNumber"));
        }

        if (command.hasParameter("generationDate")) {
            request.setGenerationDate(command.localDateValueOfParameterNamed("generationDate"));
        }

        if (command.hasParameter("generatedFilePath")) {
            request.setGeneratedFilePath(command.stringValueOfParameterNamed("generatedFilePath"));
        }

        if (command.hasParameter("fileSize")) {
            request.setFileSize(command.longValueOfParameterNamed("fileSize"));
        }

        if (command.hasParameter("checksum")) {
            request.setChecksum(command.stringValueOfParameterNamed("checksum"));
        }

        if (command.hasParameter("documentStatus")) {
            request.setDocumentStatus(command.stringValueOfParameterNamed("documentStatus"));
        }

        if (command.hasParameter("deliveryMethod")) {
            request.setDeliveryMethod(command.stringValueOfParameterNamed("deliveryMethod"));
        }

        if (command.hasParameter("deliveryDate")) {
            request.setDeliveryDate(command.localDateValueOfParameterNamed("deliveryDate"));
        }

        if (command.hasParameter("recipientAcknowledgment")) {
            request.setRecipientAcknowledgment(command.booleanObjectValueOfParameterNamed("recipientAcknowledgment"));
        }

        if (command.hasParameter("templateFormat")) {
            request.setTemplateFormat(command.stringValueOfParameterNamed("templateFormat"));
        }

        if (command.hasParameter("outputFormat")) {
            request.setOutputFormat(command.stringValueOfParameterNamed("outputFormat"));
        }

        return request;
    }

    // DTO-based methods for direct API usage

    @Override
    @Transactional
    public CommandProcessingResult createKfsDocument(KfsDocumentRequest request) {
        try {
            this.context.authenticatedUser();

            validateKfsDocumentRequest(request, true);

            // Check if template exists and is active
            KfsTemplate template = this.kfsTemplateRepository.findById(request.getKfsTemplateId())
                    .orElseThrow(() -> new PlatformDataIntegrityException("error.msg.kfs.template.not.found",
                            "KFS Template with ID " + request.getKfsTemplateId() + " not found"));

            if (!template.getIsActiveVersion()) {
                throw new PlatformDataIntegrityException("error.msg.kfs.template.not.active",
                        "KFS Template with ID " + request.getKfsTemplateId() + " is not active");
            }

            // Check for duplicate document reference number
            if (request.getDocumentReferenceNumber() != null
                    && this.kfsDocumentRepository.existsByDocumentReferenceNumber(request.getDocumentReferenceNumber())) {
                throw new PlatformDataIntegrityException("error.msg.kfs.document.duplicate.reference.number",
                        "Document with reference number " + request.getDocumentReferenceNumber() + " already exists");
            }

            // Create new KFS document
            KfsDocument document = KfsDocument.createNew();
            mapRequestToEntity(request, document);

            // Set generation date if not provided
            if (document.getGenerationDate() == null) {
                document.setGenerationDate(LocalDate.now());
            }

            // Set default status if not provided
            if (document.getDocumentStatus() == null) {
                document.setDocumentStatus("GENERATED");
            }

            this.kfsDocumentRepository.saveAndFlush(document);

            log.info("KFS Document created successfully with ID: {}", document.getId());

            return new CommandProcessingResultBuilder().withCommandId(null).withEntityId(document.getId()).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(dve, "error.msg.kfs.document.unknown.data.integrity.issue",
                    "Unknown data integrity issue with KFS document creation");
            return CommandProcessingResult.empty();
        } catch (final PlatformServiceUnavailableException e) {
            log.error("KFS Document creation failed due to service unavailable", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateKfsDocument(Long documentId, KfsDocumentRequest request) {
        try {
            this.context.authenticatedUser();

            validateKfsDocumentRequest(request, false);

            KfsDocument document = this.kfsDocumentRepository.findById(documentId)
                    .orElseThrow(() -> new PlatformDataIntegrityException("error.msg.kfs.document.not.found",
                            "KFS Document with ID " + documentId + " not found"));

            // Validate template if template ID is being updated
            if (request.getKfsTemplateId() != null && !request.getKfsTemplateId().equals(document.getKfsTemplateId())) {
                KfsTemplate template = this.kfsTemplateRepository.findById(request.getKfsTemplateId())
                        .orElseThrow(() -> new PlatformDataIntegrityException("error.msg.kfs.template.not.found",
                                "KFS Template with ID " + request.getKfsTemplateId() + " not found"));

                if (!template.getIsActiveVersion()) {
                    throw new PlatformDataIntegrityException("error.msg.kfs.template.not.active",
                            "KFS Template with ID " + request.getKfsTemplateId() + " is not active");
                }
            }

            // Check for duplicate document reference number (excluding current document)
            if (request.getDocumentReferenceNumber() != null
                    && !request.getDocumentReferenceNumber().equals(document.getDocumentReferenceNumber())
                    && this.kfsDocumentRepository.existsByDocumentReferenceNumber(request.getDocumentReferenceNumber())) {
                throw new PlatformDataIntegrityException("error.msg.kfs.document.duplicate.reference.number",
                        "Document with reference number " + request.getDocumentReferenceNumber() + " already exists");
            }

            mapRequestToEntity(request, document);
            this.kfsDocumentRepository.saveAndFlush(document);

            log.info("KFS Document updated successfully with ID: {}", documentId);

            return new CommandProcessingResultBuilder().withCommandId(null).withEntityId(documentId).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(dve, "error.msg.kfs.document.unknown.data.integrity.issue",
                    "Unknown data integrity issue with KFS document update");
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteKfsDocument(Long documentId) {
        try {
            this.context.authenticatedUser();

            KfsDocument document = this.kfsDocumentRepository.findById(documentId)
                    .orElseThrow(() -> new PlatformDataIntegrityException("error.msg.kfs.document.not.found",
                            "KFS Document with ID " + documentId + " not found"));

            // Check if document can be deleted (e.g., not in certain statuses)
            if ("DELIVERED".equals(document.getDocumentStatus()) && Boolean.TRUE.equals(document.getRecipientAcknowledgment())) {
                throw new PlatformDataIntegrityException("error.msg.kfs.document.cannot.delete.delivered.acknowledged",
                        "Cannot delete KFS document that has been delivered and acknowledged");
            }

            this.kfsDocumentRepository.delete(document);

            log.info("KFS Document deleted successfully with ID: {}", documentId);

            return new CommandProcessingResultBuilder().withCommandId(null).withEntityId(documentId).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(dve, "error.msg.kfs.document.unknown.data.integrity.issue",
                    "Unknown data integrity issue with KFS document deletion");
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateDocumentStatus(Long documentId, String status) {
        try {
            this.context.authenticatedUser();

            if (status == null || status.trim().isEmpty()) {
                throw new PlatformDataIntegrityException("error.msg.kfs.document.status.required", "Document status is required");
            }

            KfsDocument document = this.kfsDocumentRepository.findById(documentId)
                    .orElseThrow(() -> new PlatformDataIntegrityException("error.msg.kfs.document.not.found",
                            "KFS Document with ID " + documentId + " not found"));

            // Validate status transition
            validateStatusTransition(document.getDocumentStatus(), status);

            String oldStatus = document.getDocumentStatus();
            document.setDocumentStatus(status);

            // Update delivery date if status is DELIVERED
            if ("DELIVERED".equals(status) && document.getDeliveryDate() == null) {
                document.setDeliveryDate(LocalDate.now());
            }

            this.kfsDocumentRepository.saveAndFlush(document);

            log.info("KFS Document status updated from {} to {} for ID: {}", oldStatus, status, documentId);

            return new CommandProcessingResultBuilder().withCommandId(null).withEntityId(documentId).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(dve, "error.msg.kfs.document.unknown.data.integrity.issue",
                    "Unknown data integrity issue with KFS document status update");
            return CommandProcessingResult.empty();
        }
    }

    private void validateKfsDocumentRequest(KfsDocumentRequest request, boolean isCreate) {
        if (request == null) {
            throw new PlatformDataIntegrityException("error.msg.kfs.document.request.required", "KFS Document request is required");
        }

        if (isCreate) {
            if (request.getLoanId() == null) {
                throw new PlatformDataIntegrityException("error.msg.kfs.document.loan.id.required",
                        "Loan ID is required for KFS document creation");
            }

            if (request.getClientId() == null) {
                throw new PlatformDataIntegrityException("error.msg.kfs.document.client.id.required",
                        "Client ID is required for KFS document creation");
            }

            if (request.getEirCalculationId() == null) {
                throw new PlatformDataIntegrityException("error.msg.kfs.document.eir.calculation.id.required",
                        "EIR Calculation ID is required for KFS document creation");
            }

            if (request.getKfsTemplateId() == null) {
                throw new PlatformDataIntegrityException("error.msg.kfs.document.template.id.required",
                        "Template ID is required for KFS document creation");
            }

            // Document reference number and file path will be generated if not provided
            // No validation needed as they are auto-generated
        }

        // Validate document reference number format if provided
        if (request.getDocumentReferenceNumber() != null && request.getDocumentReferenceNumber().length() > 100) {
            throw new PlatformDataIntegrityException("error.msg.kfs.document.reference.number.too.long",
                    "Document reference number cannot exceed 100 characters");
        }

        // Validate file size if provided
        if (request.getFileSize() != null && request.getFileSize() < 0) {
            throw new PlatformDataIntegrityException("error.msg.kfs.document.file.size.invalid", "File size cannot be negative");
        }
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Define valid status transitions
        if (currentStatus == null) {
            return; // Allow any status for new documents
        }

        boolean isValidTransition = false;

        switch (currentStatus) {
            case "GENERATED":
                isValidTransition = "PROCESSING".equals(newStatus) || "CANCELLED".equals(newStatus) || "DELIVERED".equals(newStatus);
            break;
            case "PROCESSING":
                isValidTransition = "DELIVERED".equals(newStatus) || "FAILED".equals(newStatus) || "CANCELLED".equals(newStatus);
            break;
            case "DELIVERED":
                isValidTransition = "ACKNOWLEDGED".equals(newStatus);
            break;
            case "FAILED":
                isValidTransition = "PROCESSING".equals(newStatus) || "CANCELLED".equals(newStatus);
            break;
            case "CANCELLED":
                isValidTransition = "GENERATED".equals(newStatus);
            break;
            case "ACKNOWLEDGED":
                // Final status - no transitions allowed
                isValidTransition = false;
            break;
            default:
                isValidTransition = true; // Allow any transition for unknown statuses
        }

        if (!isValidTransition) {
            throw new PlatformDataIntegrityException("error.msg.kfs.document.invalid.status.transition",
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private void mapRequestToEntity(KfsDocumentRequest request, KfsDocument document) {
        // Set required fields
        if (request.getLoanId() != null) {
            document.setLoanId(request.getLoanId());
        }
        if (request.getClientId() != null) {
            document.setClientId(request.getClientId());
        }
        if (request.getEirCalculationId() != null) {
            document.setEirCalculationId(request.getEirCalculationId());
        }
        if (request.getKfsTemplateId() != null) {
            document.setKfsTemplateId(request.getKfsTemplateId());
        }

        // Set document reference number - generate if not provided
        if (request.getDocumentReferenceNumber() != null) {
            document.setDocumentReferenceNumber(request.getDocumentReferenceNumber());
        } else {
            // Generate unique reference number: KFS-LOAN{loanId}-{timestamp}
            String refNumber = "KFS-LOAN" + request.getLoanId() + "-" + System.currentTimeMillis();
            document.setDocumentReferenceNumber(refNumber);
        }

        // Set generation date - use current date if not provided
        if (request.getGenerationDate() != null) {
            document.setGenerationDate(request.getGenerationDate());
        } else {
            document.setGenerationDate(LocalDate.now());
        }

        // Set generated file path - generate if not provided
        if (request.getGeneratedFilePath() != null) {
            document.setGeneratedFilePath(request.getGeneratedFilePath());
        } else {
            // Generate default file path
            String fileName = "kfs_document_" + request.getLoanId() + "_" + System.currentTimeMillis() + ".pdf";
            String filePath = "/documents/kfs/" + fileName;
            document.setGeneratedFilePath(filePath);
        }

        // Set optional fields
        if (request.getFileSize() != null) {
            document.setFileSize(request.getFileSize());
        }
        if (request.getChecksum() != null) {
            document.setChecksum(request.getChecksum());
        }
        if (request.getDocumentStatus() != null) {
            document.setDocumentStatus(request.getDocumentStatus());
        } else {
            document.setDocumentStatus("GENERATED"); // Default status
        }
        if (request.getDeliveryMethod() != null) {
            document.setDeliveryMethod(request.getDeliveryMethod());
        }
        if (request.getDeliveryDate() != null) {
            document.setDeliveryDate(request.getDeliveryDate());
        }
        if (request.getRecipientAcknowledgment() != null) {
            document.setRecipientAcknowledgment(request.getRecipientAcknowledgment());
        } else {
            document.setRecipientAcknowledgment(false); // Default value
        }

        // Set format tracking fields
        if (request.getTemplateFormat() != null) {
            document.setTemplateFormat(request.getTemplateFormat());
        }
        if (request.getOutputFormat() != null) {
            document.setOutputFormat(request.getOutputFormat());
        }
    }

    private void handleDataIntegrityIssues(final Exception dve, final String errorCode, final String errorMessage) {
        final Throwable realCause = dve.getCause() != null ? dve.getCause() : dve;
        log.error("Data integrity issue: {}", realCause.getMessage(), dve);

        throw new PlatformDataIntegrityException(errorCode, errorMessage, "Data integrity violation: " + realCause.getMessage());
    }
}
