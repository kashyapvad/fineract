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

import java.util.Map;
import org.apache.fineract.extend.kfs.dto.KfsDocumentData;

/**
 * Service interface for RBI-compliant KFS field mapping.
 *
 * This service provides standardized field mapping for Key Facts Statement (KFS) documents according to RBI guidelines,
 * extracting actual data from the database rather than using hardcoded values.
 */
public interface KfsRbiCompliantMappingService {

    /**
     * Creates RBI-compliant field mapping for KFS template with actual database data.
     *
     * @param documentData
     *            the KFS document data containing loan and client information
     * @return Map containing all field mappings for the KFS template
     */
    Map<String, Object> createRbiCompliantFieldMapping(KfsDocumentData documentData);
}
