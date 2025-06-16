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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for KFS document generation operations.
 *
 * Contains the results of KFS document generation including document metadata and status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KfsDocumentGenerationResponse {

    /**
     * Generated document ID.
     */
    private Long documentId;

    /**
     * Document reference number.
     */
    private String documentReferenceNumber;

    /**
     * Generation status (e.g., "GENERATED", "PENDING", "FAILED").
     */
    private String generationStatus;

    /**
     * Path to the generated file.
     */
    private String generatedFilePath;

    /**
     * Date when document was generated.
     */
    private LocalDate generationDate;

    /**
     * File size in bytes.
     */
    private Long fileSize;

    /**
     * Document checksum.
     */
    private String checksum;

    /**
     * Generation job ID for tracking.
     */
    private String jobId;

    /**
     * Success flag.
     */
    private Boolean success;

    /**
     * Error message if generation failed.
     */
    private String errorMessage;

    /**
     * Template ID used for generation.
     */
    private Long templateId;

    /**
     * Template name used for generation.
     */
    private String templateName;

    /**
     * Loan ID for which document was generated.
     */
    private Long loanId;

    /**
     * Client ID for which document was generated.
     */
    private Long clientId;

    /**
     * Delivery method used.
     */
    private String deliveryMethod;

    /**
     * Whether this is a preview generation.
     */
    private Boolean preview;

    /**
     * Base64 encoded file content for direct download.
     */
    private String fileContent;

    /**
     * Output format of the generated document (e.g., "DOCX", "RTF", "PDF").
     */
    private String outputFormat;
}
