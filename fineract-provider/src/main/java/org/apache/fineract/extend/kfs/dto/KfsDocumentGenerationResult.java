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
 * DTO for KFS document generation result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KfsDocumentGenerationResult {

    private String htmlContent;
    private byte[] pdfContent;
    private byte[] documentContent;
    private String documentReferenceNumber;
    private Long documentId;
    private String status;
    private String message;
    private String documentPath;
    private String filePath;
    private Long fileSize;
    private String checksum;
    private LocalDate generationDate;
    private boolean preview;

    // Compatibility methods for existing service code
    public boolean getSuccess() {
        return "SUCCESS".equals(status);
    }

    public void setSuccess(boolean success) {
        this.status = success ? "SUCCESS" : "FAILED";
    }

    public static KfsDocumentGenerationResult success(String htmlContent, byte[] pdfContent, String referenceNumber, Long documentId) {
        return KfsDocumentGenerationResult.builder().htmlContent(htmlContent).pdfContent(pdfContent)
                .documentReferenceNumber(referenceNumber).documentId(documentId).status("SUCCESS")
                .message("Document generated successfully").build();
    }

    public static KfsDocumentGenerationResult failure(String message) {
        return KfsDocumentGenerationResult.builder().status("FAILED").message(message).build();
    }
}
