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
 * Service interface for generating KFS documents in DOCX format. This interface provides a common contract for
 * different DOCX generation implementations such as POI-based and docx4j-based generators.
 */
public interface KfsDocxGenerationService {

    /**
     * Generate a KFS document in DOCX format based on the provided request.
     *
     * @param request
     *            The document generation request containing loan and borrower details
     * @return KfsDocumentGenerationResult containing the generated document bytes and metadata
     */
    KfsDocumentGenerationResult generateKfsDocument(KfsDocumentGenerationRequest request);

    /**
     * Preview a KFS document without persisting it.
     *
     * @param request
     *            The document generation request containing loan and borrower details
     * @return KfsDocumentGenerationResult containing the preview document bytes and metadata
     */
    KfsDocumentGenerationResult previewKfsDocument(KfsDocumentGenerationRequest request);

    /**
     * Get the name/identifier of this generation service implementation.
     *
     * @return String identifier for this service (e.g., "poi", "docx4j")
     */
    String getServiceName();

    /**
     * Check if this service is available and properly configured.
     *
     * @return true if the service is available, false otherwise
     */
    boolean isAvailable();
}
