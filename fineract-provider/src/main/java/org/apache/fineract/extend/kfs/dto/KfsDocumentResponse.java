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
package org.apache.fineract.extend.kfs.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for KFS document response data. Used for API responses in KFS document operations.
 */
public class KfsDocumentResponse {

    private Long documentId;
    private Long loanId;
    private Long clientId;
    private Long eirCalculationId;
    private Long kfsTemplateId;
    private String documentReferenceNumber;
    private LocalDate generationDate;
    private String generatedFilePath;
    private Long fileSize;
    private String checksum;
    private String documentStatus;
    private String deliveryMethod;
    private LocalDate deliveryDate;
    private Boolean recipientAcknowledgment;

    // Audit fields
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;

    // Default constructor
    public KfsDocumentResponse() {}

    // Constructor with all fields
    public KfsDocumentResponse(Long documentId, Long loanId, Long clientId, Long eirCalculationId, Long kfsTemplateId,
            String documentReferenceNumber, LocalDate generationDate, String generatedFilePath, Long fileSize, String checksum,
            String documentStatus, String deliveryMethod, LocalDate deliveryDate, Boolean recipientAcknowledgment,
            LocalDateTime createdDate, LocalDateTime lastModifiedDate, String createdBy, String lastModifiedBy) {
        this.documentId = documentId;
        this.loanId = loanId;
        this.clientId = clientId;
        this.eirCalculationId = eirCalculationId;
        this.kfsTemplateId = kfsTemplateId;
        this.documentReferenceNumber = documentReferenceNumber;
        this.generationDate = generationDate;
        this.generatedFilePath = generatedFilePath;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.documentStatus = documentStatus;
        this.deliveryMethod = deliveryMethod;
        this.deliveryDate = deliveryDate;
        this.recipientAcknowledgment = recipientAcknowledgment;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
    }

    // Getters and Setters
    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getEirCalculationId() {
        return eirCalculationId;
    }

    public void setEirCalculationId(Long eirCalculationId) {
        this.eirCalculationId = eirCalculationId;
    }

    public Long getKfsTemplateId() {
        return kfsTemplateId;
    }

    public void setKfsTemplateId(Long kfsTemplateId) {
        this.kfsTemplateId = kfsTemplateId;
    }

    public String getDocumentReferenceNumber() {
        return documentReferenceNumber;
    }

    public void setDocumentReferenceNumber(String documentReferenceNumber) {
        this.documentReferenceNumber = documentReferenceNumber;
    }

    public LocalDate getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(LocalDate generationDate) {
        this.generationDate = generationDate;
    }

    public String getGeneratedFilePath() {
        return generatedFilePath;
    }

    public void setGeneratedFilePath(String generatedFilePath) {
        this.generatedFilePath = generatedFilePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(String documentStatus) {
        this.documentStatus = documentStatus;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Boolean getRecipientAcknowledgment() {
        return recipientAcknowledgment;
    }

    public void setRecipientAcknowledgment(Boolean recipientAcknowledgment) {
        this.recipientAcknowledgment = recipientAcknowledgment;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}
