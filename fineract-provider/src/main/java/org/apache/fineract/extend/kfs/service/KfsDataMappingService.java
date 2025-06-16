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

import org.apache.fineract.extend.kfs.dto.KfsDocumentData;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationRequest;

/**
 * Service interface for mapping loan and client data to KFS document format. Handles data extraction and transformation
 * for KFS document generation.
 */
public interface KfsDataMappingService {

    /**
     * Maps loan and client data from database to KFS document format
     *
     * @param request
     *            KFS document generation request
     * @return Mapped KFS document data ready for template processing
     */
    KfsDocumentData mapLoanDataToKfsFormat(KfsDocumentGenerationRequest request);

    /**
     * Validates that all required data is available for KFS generation
     *
     * @param request
     *            KFS document generation request
     * @throws org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException
     *             if required data is missing
     */
    void validateRequiredDataAvailability(KfsDocumentGenerationRequest request);
}
