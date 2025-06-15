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
package org.apache.fineract.extend.kyc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.extend.kyc.data.ClientKycData;
import org.apache.fineract.extend.kyc.domain.ClientKycDetails;
import org.apache.fineract.extend.kyc.domain.ClientKycDetailsRepositoryWrapper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.springframework.stereotype.Service;

/**
 * Implementation of the ClientKycReadPlatformService.
 *
 * This service provides read-only operations for retrieving KYC data and templates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientKycReadPlatformServiceImpl implements ClientKycReadPlatformService {

    private final ClientKycDetailsRepositoryWrapper kycRepositoryWrapper;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final PlatformSecurityContext context;

    @Override
    public ClientKycData retrieveClientKyc(Long clientId) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB
        
        try {
            // Step 1: Validate client exists
            
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            

            // Step 2: Try to find KYC details
            
            final var kycDetailsOpt = this.kycRepositoryWrapper.findByClientId(clientId);
            

            if (kycDetailsOpt.isPresent()) {
                
                return mapToClientKycData(kycDetailsOpt.get());
            } else {
                
                // Temporarily return empty template instead of null to test serialization
                return ClientKycData.template(clientId, this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId).getDisplayName());
            }

        } catch (Exception e) {
            log.error("ERROR at step in retrieveClientKyc for client {}: {} - {}", clientId, e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ClientKycData retrieveTemplate(Long clientId) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB
        
        try {
            // Validate client exists
            final var client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

            // Create template with client information
            return ClientKycData.template(clientId, client.getDisplayName());

        } catch (Exception e) {
            log.error("Error retrieving KYC template for client {}: {}", clientId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Maps ClientKycDetails entity to ClientKycData DTO.
     */
    private ClientKycData mapToClientKycData(final ClientKycDetails entity) {
        // Create DTO with basic client information
        final ClientKycData data = ClientKycData.template(entity.getClient().getId(), entity.getClient().getDisplayName());

        // Set entity ID
        data.setId(entity.getId());

        // Document Numbers
        data.setPanNumber(entity.getPanNumber());
        data.setAadhaarNumber(entity.getAadhaarNumber());
        data.setVoterIdNumber(entity.getVoterId());
        data.setPassportNumber(entity.getPassportNumber());
        data.setDrivingLicenseNumber(entity.getDrivingLicenseNumber());

        // Verification Status
        data.setPanVerified(entity.getPanVerified());
        data.setAadhaarVerified(entity.getAadhaarVerified());
        data.setVoterIdVerified(entity.getVoterIdVerified());
        data.setPassportVerified(entity.getPassportVerified());
        data.setDrivingLicenseVerified(entity.getDrivingLicenseVerified());

        // Verification Metadata
        data.setVerificationMethod(entity.getVerificationMethod());
        data.setVerificationMethodCode(entity.getVerificationMethod() != null ? entity.getVerificationMethod().getCode() : null);
        data.setVerificationMethodDescription(
                entity.getVerificationMethod() != null ? entity.getVerificationMethod().getDescription() : null);
        data.setLastVerifiedOn(entity.getLastVerifiedOn());

        // User Information
        if (entity.getVerifiedByUser() != null) {
            data.setLastVerifiedByUserId(entity.getVerifiedByUser().getId());
            data.setLastVerifiedByUsername(entity.getVerifiedByUser().getUsername());
        }

        // API/Manual Verification Details
        if (entity.getVerificationMethod() == org.apache.fineract.extend.kyc.domain.KycVerificationMethod.API) {
            data.setApiProvider(entity.getVerificationProvider());
            data.setApiResponseData(entity.getApiResponseData() != null ? entity.getApiResponseData().toString() : null);
        } else if (entity.getVerificationMethod() == org.apache.fineract.extend.kyc.domain.KycVerificationMethod.MANUAL) {
            data.setManualVerificationNotes(entity.getVerificationNotes());
            data.setManualVerificationDate(entity.getLastVerifiedOn());
            if (entity.getVerifiedByUser() != null) {
                data.setManualVerifiedByUserId(entity.getVerifiedByUser().getId());
                data.setManualVerifiedByUsername(entity.getVerifiedByUser().getUsername());
            }
        }

        // Audit Information
        data.setCreatedDate(entity.getCreatedDate().map(d -> d.toLocalDateTime()).orElse(null));
        data.setLastModifiedDate(entity.getLastModifiedDate().map(d -> d.toLocalDateTime()).orElse(null));
        data.setCreatedByUserId(entity.getCreatedBy().orElse(null));
        data.setLastModifiedByUserId(entity.getLastModifiedBy().orElse(null));

        return data;
    }
}
