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

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.extend.kyc.data.GuarantorKycData;
import org.apache.fineract.extend.kyc.domain.GuarantorKycDetails;
import org.apache.fineract.extend.kyc.domain.GuarantorKycDetailsRepository;
import org.apache.fineract.extend.kyc.domain.GuarantorKycDetailsRepositoryWrapper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Service;

/**
 * Implementation of the GuarantorKycReadPlatformService.
 *
 * This service provides read-only operations for retrieving guarantor KYC data. Each client can have multiple guarantor
 * KYC records (1-to-many relationship).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuarantorKycReadPlatformServiceImpl implements GuarantorKycReadPlatformService {

    private final GuarantorKycDetailsRepository repository;
    private final GuarantorKycDetailsRepositoryWrapper repositoryWrapper;
    private final PlatformSecurityContext context;

    @Override
    public List<GuarantorKycData> retrieveGuarantorKycDetailsByClientId(final Long clientId) {
        this.context.authenticatedUser().validateHasReadPermission("GUARANTOR_KYC");

        log.debug("Retrieving guarantor KYC details for client ID: {}", clientId);
        final List<GuarantorKycDetails> entities = this.repositoryWrapper.findByClientId(clientId);
        return entities.stream().map(this::mapToGuarantorKycData).collect(Collectors.toList());
    }

    @Override
    public GuarantorKycData retrieveGuarantorKycDetails(final Long guarantorKycId) {
        this.context.authenticatedUser().validateHasReadPermission("GUARANTOR_KYC");

        log.debug("Retrieving guarantor KYC details for ID: {}", guarantorKycId);
        final GuarantorKycDetails entity = this.repositoryWrapper.findOneThrowExceptionIfNotFound(guarantorKycId);
        return mapToGuarantorKycData(entity);
    }

    @Override
    public boolean hasGuarantorKycDetails(final Long clientId) {
        this.context.authenticatedUser().validateHasReadPermission("GUARANTOR_KYC");

        log.debug("Checking if client {} has guarantor KYC details", clientId);
        return this.repositoryWrapper.existsByClientId(clientId);
    }

    /**
     * Maps GuarantorKycDetails entity to GuarantorKycData DTO with enhanced error handling. Reused by both individual
     * and bulk retrieval methods (DRY principle).
     */
    private GuarantorKycData mapToGuarantorKycData(final GuarantorKycDetails entity) {
        try {
            // Create DTO with basic information
            final GuarantorKycData data = new GuarantorKycData();

            // Set entity ID
            data.setId(entity.getId());

            // Client Information
            data.setClientId(entity.getClient().getId());
            data.setClientName(entity.getClient().getDisplayName());
            data.setClientDisplayName(entity.getClient().getDisplayName());

            // Guarantor Core Identity
            data.setFullName(entity.getFullName());
            data.setMobileNumber(entity.getMobileNumber());
            data.setRelationshipToClient(entity.getRelationshipToClient());

            // Document Numbers - handle null values gracefully
            data.setPanNumber(entity.getPanNumber());
            data.setAadhaarNumber(entity.getAadhaarNumber());

            // Verification Status - ensure boolean values are never null
            data.setPanVerified(entity.getPanVerified() != null ? entity.getPanVerified() : false);
            data.setAadhaarVerified(entity.getAadhaarVerified() != null ? entity.getAadhaarVerified() : false);

            // OTP Verification Fields
            data.setAadhaarOtpVerified(entity.getAadhaarOtpVerified() != null ? entity.getAadhaarOtpVerified() : false);
            data.setOtpClientId(entity.getOtpClientId());
            data.setOtpLastRequestedOn(entity.getOtpLastRequestedOn());
            data.setOtpVerifiedOn(entity.getOtpVerifiedOn());

            // Manual Verification Fields
            data.setPanManuallyVerified(entity.getPanManuallyVerified() != null ? entity.getPanManuallyVerified() : false);
            data.setAadhaarManuallyVerified(entity.getAadhaarManuallyVerified() != null ? entity.getAadhaarManuallyVerified() : false);
            data.setManualVerificationReason(entity.getManualVerificationReason());

            // Verification Metadata
            data.setVerificationMethod(entity.getVerificationMethod());
            data.setVerificationMethodCode(entity.getVerificationMethod() != null ? entity.getVerificationMethod().getCode() : null);
            data.setVerificationMethodDescription(
                    entity.getVerificationMethod() != null ? entity.getVerificationMethod().getDescription() : null);
            data.setLastVerifiedOn(entity.getLastVerifiedOn());
            data.setVerificationProvider(entity.getVerificationProvider());

            // User Information - handle null user gracefully
            if (entity.getVerifiedByUser() != null) {
                data.setVerifiedByUserId(entity.getVerifiedByUser().getId());
                data.setVerifiedByUsername(entity.getVerifiedByUser().getUsername());
            }

            // API Response Data - convert JsonNode to String to avoid serialization issues
            data.setApiResponseData(entity.getApiResponseData() != null ? entity.getApiResponseData().toString() : null);
            data.setVerificationNotes(entity.getVerificationNotes());

            // Status Fields
            data.setIsActive(entity.getIsActive() != null ? entity.getIsActive() : true);
            data.setIsPrimaryGuarantor(entity.getIsPrimaryGuarantor() != null ? entity.getIsPrimaryGuarantor() : false);

            // Audit Information - handle optional values gracefully
            data.setCreatedDate(entity.getCreatedDate().map(d -> d.toLocalDateTime()).orElse(null));
            data.setLastModifiedDate(entity.getLastModifiedDate().map(d -> d.toLocalDateTime()).orElse(null));
            data.setCreatedByUserId(entity.getCreatedBy().orElse(null));
            data.setLastModifiedByUserId(entity.getLastModifiedBy().orElse(null));

            log.debug("Successfully mapped Guarantor KYC data for client {}: ID={}, Name={}, Mobile={}, PAN={}, Aadhaar={}",
                    entity.getClient().getId(), data.getId(), data.getFullName(), data.getMobileNumber(), data.getPanNumber(),
                    data.getAadhaarNumber());

            return data;

        } catch (Exception e) {
            log.error("Error mapping Guarantor KYC entity to DTO for client {}", entity.getClient().getId(), e);
            // Return a basic template if mapping fails
            return GuarantorKycData.template(entity.getClient().getId(), entity.getClient().getDisplayName());
        }
    }
}
