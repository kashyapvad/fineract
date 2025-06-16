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
import java.util.List;
import org.apache.fineract.extend.kfs.dto.KfsDocumentResponse;
import org.apache.fineract.extend.kfs.dto.KfsDocumentStatistics;

/**
 * Service interface for read operations on KFS documents. Handles retrieval, download, and statistics operations.
 */
public interface KfsDocumentReadPlatformService {

    /**
     * Retrieve a KFS document by ID.
     *
     * @param documentId
     *            The ID of the document to retrieve
     * @return KFS document response
     */
    KfsDocumentResponse retrieveKfsDocument(Long documentId);

    /**
     * Retrieve all KFS documents for a specific loan.
     *
     * @param loanId
     *            The loan ID
     * @return List of KFS document responses
     */
    List<KfsDocumentResponse> retrieveKfsDocumentsByLoanId(Long loanId);

    /**
     * Retrieve all KFS documents for a specific client.
     *
     * @param clientId
     *            The client ID
     * @return List of KFS document responses
     */
    List<KfsDocumentResponse> retrieveKfsDocumentsByClientId(Long clientId);

    /**
     * Retrieve KFS documents by status.
     *
     * @param status
     *            The document status
     * @return List of KFS document responses
     */
    List<KfsDocumentResponse> retrieveKfsDocumentsByStatus(String status);

    /**
     * Retrieve KFS documents within a date range.
     *
     * @param startDate
     *            The start date
     * @param endDate
     *            The end date
     * @return List of KFS document responses
     */
    List<KfsDocumentResponse> retrieveKfsDocumentsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Download a KFS document.
     *
     * @param documentId
     *            The ID of the document to download
     * @return Byte array containing the document data
     */
    byte[] downloadKfsDocument(Long documentId);

    /**
     * Retrieve KFS document statistics.
     *
     * @return KFS document statistics
     */
    KfsDocumentStatistics retrieveKfsDocumentStatistics();
}
