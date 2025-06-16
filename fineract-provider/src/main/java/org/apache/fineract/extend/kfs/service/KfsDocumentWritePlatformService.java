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

import org.apache.fineract.extend.kfs.dto.KfsDocumentRequest;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

/**
 * Service interface for write operations on KFS documents. Handles create, update, delete, and status update
 * operations. Provides both JsonCommand-based methods (for command handlers) and DTO-based methods (for direct API
 * usage).
 */
public interface KfsDocumentWritePlatformService {

    // JsonCommand-based methods for command handlers

    /**
     * Create a new KFS document via command handler.
     *
     * @param command
     *            JsonCommand with KFS document data
     * @return Command processing result with the new document ID
     */
    CommandProcessingResult createKfsDocument(JsonCommand command);

    /**
     * Update an existing KFS document via command handler.
     *
     * @param command
     *            JsonCommand with updated document data
     * @return Command processing result with the updated document ID
     */
    CommandProcessingResult updateKfsDocument(JsonCommand command);

    /**
     * Delete a KFS document via command handler.
     *
     * @param command
     *            JsonCommand with document ID to delete
     * @return Command processing result
     */
    CommandProcessingResult deleteKfsDocument(JsonCommand command);

    // DTO-based methods for direct API usage

    /**
     * Create a new KFS document.
     *
     * @param request
     *            The KFS document creation request
     * @return Command processing result with the new document ID
     */
    CommandProcessingResult createKfsDocument(KfsDocumentRequest request);

    /**
     * Update an existing KFS document.
     *
     * @param documentId
     *            The ID of the document to update
     * @param request
     *            The KFS document update request
     * @return Command processing result with the updated document ID
     */
    CommandProcessingResult updateKfsDocument(Long documentId, KfsDocumentRequest request);

    /**
     * Delete a KFS document.
     *
     * @param documentId
     *            The ID of the document to delete
     * @return Command processing result
     */
    CommandProcessingResult deleteKfsDocument(Long documentId);

    /**
     * Update the status of a KFS document.
     *
     * @param documentId
     *            The ID of the document
     * @param status
     *            The new status to set
     * @return Command processing result
     */
    CommandProcessingResult updateDocumentStatus(Long documentId, String status);
}
