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

// NOTE: This service is temporarily disabled because docx4j dependencies have been removed
// in favor of the POI-based implementation. To re-enable, uncomment the docx4j dependencies
// in extend-dependencies.gradle and remove the @ConditionalOnClass annotation below.

/*

/**
 * Advanced docx4j-based KFS document generation service.
 *
 * NOTE: This service is currently disabled because docx4j dependencies have been removed
 * in favor of the POI-based implementation. The POI implementation provides the same
 * functionality without JAXB version conflicts.
 *
 * To re-enable this service:
 * 1. Uncomment the docx4j dependencies in extend-dependencies.gradle
 * 2. Remove the @ConditionalOnClass annotation below
 * 3. Uncomment the full implementation
 */
@Slf4j
@Service("docx4jKfsDocxGenerationService")
@ConditionalOnClass(name = "org.docx4j.openpackaging.packages.WordprocessingMLPackage")
public class KfsDocx4jGenerationServiceImpl implements KfsDocxGenerationService {

    // This implementation is disabled - POI implementation should be used instead
    // All methods will throw UnsupportedOperationException

    @Override
    public org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationResult generateKfsDocument(
            org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationRequest request) {

        log.warn("docx4j service is disabled - using POI implementation instead");
        throw new UnsupportedOperationException("docx4j service is disabled. Please use KfsPoiGenerationService instead. "
                + "To re-enable docx4j, uncomment dependencies in extend-dependencies.gradle");
    }

    @Override
    public org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationResult previewKfsDocument(
            org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationRequest request) {

        log.warn("docx4j service is disabled - using POI implementation instead");
        throw new UnsupportedOperationException("docx4j service is disabled. Please use KfsPoiGenerationService instead. "
                + "To re-enable docx4j, uncomment dependencies in extend-dependencies.gradle");
    }

    @Override
    public String getServiceName() {
        return "docx4j";
    }

    @Override
    public boolean isAvailable() {
        return false; // This service is disabled
    }
}
