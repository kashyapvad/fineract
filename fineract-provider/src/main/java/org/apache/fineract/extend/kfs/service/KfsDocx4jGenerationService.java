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

import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationRequest;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationResult;

/**
 * Modern KFS document generation service using docx4j with Content Controls (SDTs).
 *
 * This service uses structured MS Word templates with Content Controls instead of text placeholder replacement,
 * providing: - Clean template design in MS Word - Structured data binding - Support for complex layouts, tables, and
 * repeating sections - Professional document generation
 */
public interface KfsDocx4jGenerationService {

    /**
     * Generate KFS document using docx4j with Content Controls.
     *
     * @param request
     *            The generation request containing loan and client data
     * @return Generation result with file path and metadata
     */
    KfsDocumentGenerationResult generateKfsDocument(KfsDocumentGenerationRequest request);

    /**
     * Preview KFS document without saving to database.
     *
     * @param request
     *            The generation request
     * @return Preview result with temporary file
     */
    KfsDocumentGenerationResult previewKfsDocument(KfsDocumentGenerationRequest request);

    // Additional methods can be added here as needed
}
