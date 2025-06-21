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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.extend.kyc.data.ClientKycData;
import org.apache.fineract.extend.kyc.domain.ClientKycDetails;
import org.apache.fineract.extend.kyc.domain.ClientKycDetailsRepositoryWrapper;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Implementation of the ClientKycReadPlatformService.
 *
 * This service provides read-only operations for retrieving KYC data and templates. Enforces 1-to-1 relationship: Each
 * client has exactly zero or one KYC record (enforced by unique constraint on client_id).
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
        try {
            log.debug("Retrieving KYC data for client ID: {}", clientId);

            // Step 1: Validate client exists - this throws ClientNotFoundException (404) if not found
            final var client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            log.debug("Client found: {} ({})", client.getDisplayName(), clientId);

            // Step 2: Try to find KYC details - first check for any existing record
            try {
                final var kycDetailsOpt = this.kycRepositoryWrapper.findByClientId(clientId);

                if (kycDetailsOpt.isPresent()) {
                    final var kycDetails = kycDetailsOpt.get();
                    log.debug("KYC details found for client ID: {}", clientId);
                    return mapToClientKycData(kycDetails);
                } else {
                    log.debug("No KYC details found for client ID: {}, returning template", clientId);
                    return ClientKycData.template(clientId, client.getDisplayName());
                }

            } catch (DataAccessException dae) {
                log.warn("Database access error while retrieving KYC details for client {}: {}", clientId, dae.getMessage());
                return ClientKycData.template(clientId, client.getDisplayName());
            }

        } catch (ClientNotFoundException cnfe) {
            log.warn("Client not found: {}", clientId);
            throw cnfe;
        } catch (AbstractPlatformDomainRuleException apdre) {
            log.warn("Platform domain rule exception for client {}: {}", clientId, apdre.getMessage());
            throw apdre;
        } catch (Exception e) {
            log.error("Unexpected error retrieving KYC data for client {}: {} - {}", clientId, e.getClass().getSimpleName(), e.getMessage(),
                    e);

            // For unexpected errors, try to return a template if we can at least validate the client
            try {
                final var client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
                log.warn("Returning empty template for client {} due to unexpected error", clientId);
                return ClientKycData.template(clientId, client.getDisplayName());
            } catch (Exception fallbackException) {
                log.error("Fallback failed for client {}: {}", clientId, fallbackException.getMessage());
                throw new RuntimeException("Failed to retrieve KYC data and fallback failed", fallbackException);
            }
        }
    }

    @Override
    public Map<Long, ClientKycData> retrieveClientKycBulk(List<Long> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            log.debug("Bulk retrieving KYC data for {} clients", clientIds.size());

            // Step 1: Validate all clients exist first (fail fast approach)
            final Map<Long, Client> clientMap = this.clientRepositoryWrapper.findAll(clientIds).stream()
                    .collect(Collectors.toMap(Client::getId, client -> client));

            // Check for missing clients
            final List<Long> missingClientIds = clientIds.stream().filter(id -> !clientMap.containsKey(id)).collect(Collectors.toList());

            if (!missingClientIds.isEmpty()) {
                log.warn("Some clients not found in bulk request: {}", missingClientIds);
                // We could either throw an exception or proceed with found clients
                // For robustness, we'll proceed with found clients
            }

            // Step 2: Bulk fetch KYC details for all clients in one query
            final List<ClientKycDetails> kycDetailsList = this.kycRepositoryWrapper
                    .findByClientIds(clientMap.keySet().stream().collect(Collectors.toList()));

            // Step 3: Create map of client ID to KYC details for fast lookup
            final Map<Long, ClientKycDetails> kycDetailsMap = kycDetailsList.stream()
                    .collect(Collectors.toMap(kyc -> kyc.getClient().getId(), kyc -> kyc));

            // Step 4: Build result map - reusing existing mapping logic (DRY principle)
            final Map<Long, ClientKycData> resultMap = new HashMap<>();

            for (Map.Entry<Long, Client> entry : clientMap.entrySet()) {
                final Long clientId = entry.getKey();
                final Client client = entry.getValue();

                final ClientKycDetails kycDetails = kycDetailsMap.get(clientId);
                final ClientKycData kycData;

                if (kycDetails != null) {
                    // Use existing mapping method (DRY principle)
                    kycData = mapToClientKycData(kycDetails);
                } else {
                    // Return template for clients without KYC data
                    kycData = ClientKycData.template(clientId, client.getDisplayName());
                }

                resultMap.put(clientId, kycData);
            }

            log.debug("Successfully bulk retrieved KYC data for {} clients ({} with KYC data, {} templates)", clientMap.size(),
                    kycDetailsList.size(), clientMap.size() - kycDetailsList.size());

            return resultMap;

        } catch (Exception e) {
            log.error("Error in bulk KYC retrieval for {} clients: {}", clientIds.size(), e.getMessage(), e);

            // Fallback: return templates for all requested clients if possible
            try {
                final Map<Long, Client> clientMap = this.clientRepositoryWrapper.findAll(clientIds).stream()
                        .collect(Collectors.toMap(Client::getId, client -> client));

                final Map<Long, ClientKycData> fallbackMap = new HashMap<>();
                for (Map.Entry<Long, Client> entry : clientMap.entrySet()) {
                    fallbackMap.put(entry.getKey(), ClientKycData.template(entry.getKey(), entry.getValue().getDisplayName()));
                }

                log.warn("Returning templates for {} clients due to bulk retrieval error", fallbackMap.size());
                return fallbackMap;

            } catch (Exception fallbackException) {
                log.error("Fallback failed for bulk retrieval: {}", fallbackException.getMessage());
                throw new RuntimeException("Failed to retrieve bulk KYC data and fallback failed", fallbackException);
            }
        }
    }

    @Override
    public ClientKycData retrieveTemplate(Long clientId) {
        try {
            log.debug("Retrieving KYC template for client ID: {}", clientId);

            // Validate client exists
            final var client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

            // Create template with client information
            return ClientKycData.template(clientId, client.getDisplayName());

        } catch (ClientNotFoundException cnfe) {
            log.warn("Client not found for template: {}", clientId);
            throw cnfe;
        } catch (Exception e) {
            log.error("Error retrieving KYC template for client {}: {}", clientId, e.getMessage(), e);
            throw e;
        }
    }

    // Removed hasActualKycData method - no longer needed with simplified 1-to-1 relationship

    /**
     * Maps ClientKycDetails entity to ClientKycData DTO with enhanced error handling. Reused by both individual and
     * bulk retrieval methods (DRY principle).
     */
    private ClientKycData mapToClientKycData(final ClientKycDetails entity) {
        try {
            // Create DTO with basic client information
            final ClientKycData data = ClientKycData.template(entity.getClient().getId(), entity.getClient().getDisplayName());

            // Set entity ID
            data.setId(entity.getId());

            // Document Numbers - handle null values gracefully
            data.setPanNumber(entity.getPanNumber());
            data.setAadhaarNumber(entity.getAadhaarNumber());
            data.setVoterIdNumber(entity.getVoterId());
            data.setPassportNumber(entity.getPassportNumber());
            data.setDrivingLicenseNumber(entity.getDrivingLicenseNumber());

            // Verification Status - ensure boolean values are never null
            data.setPanVerified(entity.getPanVerified() != null ? entity.getPanVerified() : false);
            data.setAadhaarVerified(entity.getAadhaarVerified() != null ? entity.getAadhaarVerified() : false);
            data.setVoterIdVerified(entity.getVoterIdVerified() != null ? entity.getVoterIdVerified() : false);
            data.setPassportVerified(entity.getPassportVerified() != null ? entity.getPassportVerified() : false);
            data.setDrivingLicenseVerified(entity.getDrivingLicenseVerified() != null ? entity.getDrivingLicenseVerified() : false);

            // Verification Metadata
            data.setVerificationMethod(entity.getVerificationMethod());
            data.setVerificationMethodCode(entity.getVerificationMethod() != null ? entity.getVerificationMethod().getCode() : null);
            data.setVerificationMethodDescription(
                    entity.getVerificationMethod() != null ? entity.getVerificationMethod().getDescription() : null);
            data.setLastVerifiedOn(entity.getLastVerifiedOn());

            // User Information - handle null user gracefully
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

            // Audit Information - handle optional values gracefully
            data.setCreatedDate(entity.getCreatedDate().map(d -> d.toLocalDateTime()).orElse(null));
            data.setLastModifiedDate(entity.getLastModifiedDate().map(d -> d.toLocalDateTime()).orElse(null));
            data.setCreatedByUserId(entity.getCreatedBy().orElse(null));
            data.setLastModifiedByUserId(entity.getLastModifiedBy().orElse(null));

            log.debug("Successfully mapped KYC data for client {}: PAN={}, Aadhaar={}, DL={}, Voter={}, Passport={}",
                    entity.getClient().getId(), data.getPanNumber(), data.getAadhaarNumber(), data.getDrivingLicenseNumber(),
                    data.getVoterIdNumber(), data.getPassportNumber());

            return data;

        } catch (Exception e) {
            log.error("Error mapping KYC entity to DTO for client {}: {}", entity.getClient().getId(), e.getMessage(), e);
            // Return a basic template if mapping fails
            return ClientKycData.template(entity.getClient().getId(), entity.getClient().getDisplayName());
        }
    }
}
