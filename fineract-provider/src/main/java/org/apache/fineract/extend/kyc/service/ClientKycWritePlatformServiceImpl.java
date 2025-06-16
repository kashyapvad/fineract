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
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.extend.common.dto.CustomerDataProviderRequest;
import org.apache.fineract.extend.common.dto.CustomerDataProviderResponse;
import org.apache.fineract.extend.common.service.ExtendProviderService;
import org.apache.fineract.extend.kyc.domain.ClientKycDetails;
import org.apache.fineract.extend.kyc.domain.ClientKycDetailsRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;

/**
 * Implementation of ClientKycWritePlatformService that handles KYC verification operations.
 *
 * This service uses dedicated entity tables instead of data tables for better performance and querying capabilities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientKycWritePlatformServiceImpl implements ClientKycWritePlatformService {

    // Dependencies for verification operations
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final PlatformSecurityContext context;
    private final ClientKycDetailsRepositoryWrapper kycRepositoryWrapper;

    // Common provider service for external credit bureau integrations
    private final ExtendProviderService extendProviderService;

    // All KYC operations now use dedicated entity tables instead of data tables
    // This provides better performance, querying capabilities, and type safety

    @Override
    @Transactional
    public CommandProcessingResult verifyKycViaApi(final JsonCommand command) {
        log.info("=== STARTING KYC API VERIFICATION ===");
        log.info("Command ID: {}", command.commandId());
        log.info("Client ID: {}", command.getClientId());
        
        try {
            log.info("Validating provider availability...");
            // Validate provider availability using common service
            this.extendProviderService.validateProviderAvailable();
            log.info("Provider validation completed successfully");

            // Tenant isolation handled by Fineract's database-level multi-tenant architecture
            // Each tenant has separate database/schema, queries automatically routed to correct tenant DB

            try {
                // Extract client ID from command
                final Long clientId = command.getClientId();
                log.info("Processing KYC verification for client: {}", clientId);

                // Validate client exists
                final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
                final AppUser currentUser = this.context.authenticatedUser();

                // Find existing KYC record
                final ClientKycDetails kycDetails = this.kycRepositoryWrapper.findByClientIdThrowExceptionIfNotFound(clientId);

                // Extract verification parameters - for bulk verification of available documents
                final Boolean verifyPan = command.booleanObjectValueOfParameterNamed("verifyPan");
                final Boolean verifyAadhaar = command.booleanObjectValueOfParameterNamed("verifyAadhaar");
                final String verificationNotes = command.stringValueOfParameterNamed("notes");

                final Map<String, Boolean> allVerificationResults = new HashMap<>();

                try {
                    // Verify PAN if requested and PAN number is available
                    if (Boolean.TRUE.equals(verifyPan) && StringUtils.isNotBlank(kycDetails.getPanNumber())) {
                        log.info("Verifying PAN for client {}", clientId);

                        final CustomerDataProviderRequest panRequest = CustomerDataProviderRequest.builder()
                                .referenceId(this.extendProviderService.createReferenceId("KYC_PAN", clientId)).consent(true).clientId(clientId)
                                .customerName(client.getDisplayName()).mobileNumber(client.mobileNo())
                                .gender(client.gender() != null ? client.gender().getLabel() : null).documentType("PAN")
                                .documentNumber(kycDetails.getPanNumber()).build();

                        final CustomerDataProviderResponse panResponse = this.extendProviderService.pullCustomerData(panRequest);

                        if (panResponse.getVerificationResults() != null) {
                            allVerificationResults.putAll(panResponse.getVerificationResults());
                        }

                        log.info("PAN verification completed for client {}: {}", clientId,
                                allVerificationResults.getOrDefault("panVerified", false));
                    }

                    // Verify Aadhaar if requested and Aadhaar number is available
                    if (Boolean.TRUE.equals(verifyAadhaar) && StringUtils.isNotBlank(kycDetails.getAadhaarNumber())) {
                        log.info("Verifying Aadhaar for client {}", clientId);

                        final CustomerDataProviderRequest aadhaarRequest = CustomerDataProviderRequest.builder()
                                .referenceId(this.extendProviderService.createReferenceId("KYC_AADHAAR", clientId)).consent(true)
                                .clientId(clientId).customerName(client.getDisplayName()).mobileNumber(client.mobileNo())
                                .gender(client.gender() != null ? client.gender().getLabel() : null).documentType("AADHAAR")
                                .documentNumber(kycDetails.getAadhaarNumber()).build();

                        final CustomerDataProviderResponse aadhaarResponse = this.extendProviderService.pullCustomerData(aadhaarRequest);

                        if (aadhaarResponse.getVerificationResults() != null) {
                            allVerificationResults.putAll(aadhaarResponse.getVerificationResults());
                        }

                        log.info("Aadhaar verification completed for client {}: {}", clientId,
                                allVerificationResults.getOrDefault("aadhaarVerified", false));
                    }

                    // Update KYC record with API verification results
                    kycDetails.markApiVerificationCompleted(this.extendProviderService.getProviderName(), null, currentUser,
                            allVerificationResults, verificationNotes);

                    final ClientKycDetails savedKyc = this.kycRepositoryWrapper.save(kycDetails);

                    log.info("Successfully completed API verification for client {} - Results: {}", clientId, allVerificationResults);

                    return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(savedKyc.getId())
                            .withClientId(clientId).build();

                } catch (Exception e) {
                    log.error("Error during API verification: {}", e.getMessage(), e);
                    throw new RuntimeException("API verification failed: " + e.getMessage(), e);
                }

            } catch (Exception e) {
                log.error("Error processing KYC API verification command: {}", e.getMessage(), e);
                throw e;
            }

        } catch (PlatformServiceUnavailableException e) {
            log.error("Provider not available during KYC verification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during KYC API verification: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult verifyKycManually(final JsonCommand command) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB

        try {
            // Extract client ID from command
            final Long clientId = command.getClientId();

            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Find existing KYC record
            final ClientKycDetails kycDetails = this.kycRepositoryWrapper.findByClientIdThrowExceptionIfNotFound(clientId);

            // Extract verification parameters
            final String verificationNotes = command.stringValueOfParameterNamed("notes");
            final Boolean panVerified = command.booleanObjectValueOfParameterNamed("panVerified");
            final Boolean aadhaarVerified = command.booleanObjectValueOfParameterNamed("aadhaarVerified");
            final Boolean drivingLicenseVerified = command.booleanObjectValueOfParameterNamed("drivingLicenseVerified");
            final Boolean voterIdVerified = command.booleanObjectValueOfParameterNamed("voterIdVerified");
            final Boolean passportVerified = command.booleanObjectValueOfParameterNamed("passportVerified");

            // Build verification results map
            final Map<String, Boolean> verificationResults = new HashMap<>();
            if (panVerified != null) verificationResults.put("panVerified", panVerified);
            if (aadhaarVerified != null) verificationResults.put("aadhaarVerified", aadhaarVerified);
            if (drivingLicenseVerified != null) verificationResults.put("drivingLicenseVerified", drivingLicenseVerified);
            if (voterIdVerified != null) verificationResults.put("voterIdVerified", voterIdVerified);
            if (passportVerified != null) verificationResults.put("passportVerified", passportVerified);

            // Update KYC record with manual verification
            kycDetails.markManualVerificationCompleted(currentUser, verificationResults, verificationNotes);

            final ClientKycDetails savedKyc = this.kycRepositoryWrapper.save(kycDetails);

            log.info("Successfully completed manual verification for client {} by user {}", clientId, currentUser.getId());

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(savedKyc.getId())
                    .withClientId(clientId).build();

        } catch (Exception e) {
            log.error("Error processing KYC manual verification command: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult unverifyKycManually(final JsonCommand command) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB

        try {
            // Extract client ID from command
            final Long clientId = command.getClientId();

            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Find existing KYC record
            final ClientKycDetails kycDetails = this.kycRepositoryWrapper.findByClientIdThrowExceptionIfNotFound(clientId);

            // Extract unverification parameters
            final String unverificationNotes = command.stringValueOfParameterNamed("notes");

            // Extract specific document unverification flags
            final Boolean unverifyPan = command.booleanObjectValueOfParameterNamed("unverifyPan");
            final Boolean unverifyAadhaar = command.booleanObjectValueOfParameterNamed("unverifyAadhaar");
            final Boolean unverifyDrivingLicense = command.booleanObjectValueOfParameterNamed("unverifyDrivingLicense");
            final Boolean unverifyVoterId = command.booleanObjectValueOfParameterNamed("unverifyVoterId");
            final Boolean unverifyPassport = command.booleanObjectValueOfParameterNamed("unverifyPassport");

            // Build selective verification state map - only update selected documents
            final Map<String, Boolean> unverificationResults = new HashMap<>();

            // Only set verification state to false for documents explicitly selected for unverification
            // This preserves the existing state of unselected documents
            if (Boolean.TRUE.equals(unverifyPan)) {
                unverificationResults.put("panVerified", false);
            }
            if (Boolean.TRUE.equals(unverifyAadhaar)) {
                unverificationResults.put("aadhaarVerified", false);
            }
            if (Boolean.TRUE.equals(unverifyDrivingLicense)) {
                unverificationResults.put("drivingLicenseVerified", false);
            }
            if (Boolean.TRUE.equals(unverifyVoterId)) {
                unverificationResults.put("voterIdVerified", false);
            }
            if (Boolean.TRUE.equals(unverifyPassport)) {
                unverificationResults.put("passportVerified", false);
            }

            // If no specific documents were selected, unverify all (backward compatibility)
            final boolean hasSpecificSelection = Boolean.TRUE.equals(unverifyPan) || Boolean.TRUE.equals(unverifyAadhaar)
                    || Boolean.TRUE.equals(unverifyDrivingLicense) || Boolean.TRUE.equals(unverifyVoterId)
                    || Boolean.TRUE.equals(unverifyPassport);

            if (!hasSpecificSelection) {
                unverificationResults.put("panVerified", false);
                unverificationResults.put("aadhaarVerified", false);
                unverificationResults.put("drivingLicenseVerified", false);
                unverificationResults.put("voterIdVerified", false);
                unverificationResults.put("passportVerified", false);
            }

            // Update KYC record with manual unverification
            kycDetails.markManualVerificationCompleted(currentUser, unverificationResults, "UNVERIFIED: " + unverificationNotes);

            final ClientKycDetails savedKyc = this.kycRepositoryWrapper.save(kycDetails);

            log.info("Successfully completed selective manual unverification for client {} by user {}", clientId, currentUser.getId());

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(savedKyc.getId())
                    .withClientId(clientId).build();

        } catch (Exception e) {
            log.error("Error processing KYC manual unverification command: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult createKycDetails(final JsonCommand command) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB

        try {
            // Extract client ID from command
            final Long clientId = command.getClientId();

            // Validate client exists and belongs to current tenant
            final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Check if KYC details already exist for this client (1-to-1 relationship)
            if (this.kycRepositoryWrapper.existsByClientId(clientId)) {
                throw new GeneralPlatformDomainRuleException("error.msg.client.kyc.already.exists", 
                    "KYC details already exist for this client. Please refresh the page to see existing data or use the update operation to modify them.");
            }

            // Create new KYC details record using factory method
            final ClientKycDetails kycDetails = ClientKycDetails.createNew(client);

            // Set document information if provided
            if (command.hasParameter("panNumber")) {
                kycDetails.setPanNumber(command.stringValueOfParameterNamed("panNumber"));
            }
            if (command.hasParameter("aadhaarNumber")) {
                kycDetails.setAadhaarNumber(command.stringValueOfParameterNamed("aadhaarNumber"));
            }
            if (command.hasParameter("voterId")) {
                kycDetails.setVoterId(command.stringValueOfParameterNamed("voterId"));
            }
            if (command.hasParameter("passportNumber")) {
                kycDetails.setPassportNumber(command.stringValueOfParameterNamed("passportNumber"));
            }
            if (command.hasParameter("drivingLicenseNumber")) {
                kycDetails.setDrivingLicenseNumber(command.stringValueOfParameterNamed("drivingLicenseNumber"));
            }

            // Set verification status for each document type if provided
            if (command.hasParameter("panVerified")) {
                kycDetails.setPanVerified(command.booleanPrimitiveValueOfParameterNamed("panVerified"));
            }
            if (command.hasParameter("aadhaarVerified")) {
                kycDetails.setAadhaarVerified(command.booleanPrimitiveValueOfParameterNamed("aadhaarVerified"));
            }
            if (command.hasParameter("voterIdVerified")) {
                kycDetails.setVoterIdVerified(command.booleanPrimitiveValueOfParameterNamed("voterIdVerified"));
            }
            if (command.hasParameter("passportVerified")) {
                kycDetails.setPassportVerified(command.booleanPrimitiveValueOfParameterNamed("passportVerified"));
            }
            if (command.hasParameter("drivingLicenseVerified")) {
                kycDetails.setDrivingLicenseVerified(command.booleanPrimitiveValueOfParameterNamed("drivingLicenseVerified"));
            }

            // Set additional details if provided
            // Handle verification notes from multiple possible field names for consistency
            if (command.hasParameter("verificationNotes")) {
                kycDetails.setVerificationNotes(command.stringValueOfParameterNamed("verificationNotes"));
            } else if (command.hasParameter("notes")) {
                kycDetails.setVerificationNotes(command.stringValueOfParameterNamed("notes"));
            } else if (command.hasParameter("manualVerificationNotes")) {
                kycDetails.setVerificationNotes(command.stringValueOfParameterNamed("manualVerificationNotes"));
            }
            if (command.hasParameter("verificationProvider")) {
                kycDetails.setVerificationProvider(command.stringValueOfParameterNamed("verificationProvider"));
            }
            if (command.hasParameter("lastVerifiedOn")) {
                kycDetails.setLastVerifiedOn(command.localDateValueOfParameterNamed("lastVerifiedOn"));
            }

            // Set verified by user
            kycDetails.setVerifiedByUser(currentUser);

            final ClientKycDetails savedDetails = this.kycRepositoryWrapper.save(kycDetails);

            log.info("Successfully created manual KYC details for client {} with ID {}", clientId, savedDetails.getId());

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(savedDetails.getId())
                    .withClientId(clientId).build();

        } catch (Exception e) {
            log.error("Error processing manual KYC details creation: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateKycDetails(final JsonCommand command) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB

        try {
            // Extract parameters
            final Long clientId = command.getClientId();
            final Long kycId = command.entityId();

            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Find and validate KYC record exists and belongs to client
            final ClientKycDetails kycDetails = this.kycRepositoryWrapper.findOneThrowExceptionIfNotFound(kycId);

            // Verify the KYC record belongs to the specified client
            if (!kycDetails.getClient().getId().equals(clientId)) {
                throw new RuntimeException("KYC record does not belong to the specified client");
            }

            // AUDIT TRAIL: Capture original state for compliance tracking
            final String originalState = String.format(
                    "PAN:%s|Aadhaar:%s|VoterID:%s|Passport:%s|DL:%s|PanVerified:%s|AadhaarVerified:%s|VoterVerified:%s|PassportVerified:%s|DLVerified:%s|Notes:%s",
                    kycDetails.getPanNumber(), kycDetails.getAadhaarNumber(), kycDetails.getVoterId(), kycDetails.getPassportNumber(),
                    kycDetails.getDrivingLicenseNumber(), kycDetails.getPanVerified(), kycDetails.getAadhaarVerified(),
                    kycDetails.getVoterIdVerified(), kycDetails.getPassportVerified(), kycDetails.getDrivingLicenseVerified(),
                    kycDetails.getVerificationNotes());

            // Update document information if provided
            if (command.hasParameter("panNumber")) {
                kycDetails.setPanNumber(command.stringValueOfParameterNamed("panNumber"));
            }
            if (command.hasParameter("aadhaarNumber")) {
                kycDetails.setAadhaarNumber(command.stringValueOfParameterNamed("aadhaarNumber"));
            }
            if (command.hasParameter("voterId")) {
                kycDetails.setVoterId(command.stringValueOfParameterNamed("voterId"));
            }
            if (command.hasParameter("passportNumber")) {
                kycDetails.setPassportNumber(command.stringValueOfParameterNamed("passportNumber"));
            }
            if (command.hasParameter("drivingLicenseNumber")) {
                kycDetails.setDrivingLicenseNumber(command.stringValueOfParameterNamed("drivingLicenseNumber"));
            }

            // Update verification status for each document type if provided
            if (command.hasParameter("panVerified")) {
                kycDetails.setPanVerified(command.booleanPrimitiveValueOfParameterNamed("panVerified"));
            }
            if (command.hasParameter("aadhaarVerified")) {
                kycDetails.setAadhaarVerified(command.booleanPrimitiveValueOfParameterNamed("aadhaarVerified"));
            }
            if (command.hasParameter("voterIdVerified")) {
                kycDetails.setVoterIdVerified(command.booleanPrimitiveValueOfParameterNamed("voterIdVerified"));
            }
            if (command.hasParameter("passportVerified")) {
                kycDetails.setPassportVerified(command.booleanPrimitiveValueOfParameterNamed("passportVerified"));
            }
            if (command.hasParameter("drivingLicenseVerified")) {
                kycDetails.setDrivingLicenseVerified(command.booleanPrimitiveValueOfParameterNamed("drivingLicenseVerified"));
            }

            // Update additional details if provided
            // Handle verification notes from multiple possible field names for consistency
            if (command.hasParameter("verificationNotes")) {
                kycDetails.setVerificationNotes(command.stringValueOfParameterNamed("verificationNotes"));
            } else if (command.hasParameter("notes")) {
                kycDetails.setVerificationNotes(command.stringValueOfParameterNamed("notes"));
            } else if (command.hasParameter("manualVerificationNotes")) {
                kycDetails.setVerificationNotes(command.stringValueOfParameterNamed("manualVerificationNotes"));
            }
            if (command.hasParameter("verificationProvider")) {
                kycDetails.setVerificationProvider(command.stringValueOfParameterNamed("verificationProvider"));
            }
            if (command.hasParameter("lastVerifiedOn")) {
                kycDetails.setLastVerifiedOn(command.localDateValueOfParameterNamed("lastVerifiedOn"));
            }

            this.kycRepositoryWrapper.save(kycDetails);

            // AUDIT TRAIL: Log the update operation with before/after state for compliance
            final String newState = String.format(
                    "PAN:%s|Aadhaar:%s|VoterID:%s|Passport:%s|DL:%s|PanVerified:%s|AadhaarVerified:%s|VoterVerified:%s|PassportVerified:%s|DLVerified:%s|Notes:%s",
                    kycDetails.getPanNumber(), kycDetails.getAadhaarNumber(), kycDetails.getVoterId(), kycDetails.getPassportNumber(),
                    kycDetails.getDrivingLicenseNumber(), kycDetails.getPanVerified(), kycDetails.getAadhaarVerified(),
                    kycDetails.getVoterIdVerified(), kycDetails.getPassportVerified(), kycDetails.getDrivingLicenseVerified(),
                    kycDetails.getVerificationNotes());

            log.info("AUDIT: KYC UPDATE - User: {} | Client: {} | KYC ID: {} | Command: {} | Original: [{}] | Updated: [{}]",
                    currentUser.getId(), clientId, kycId, command.commandId(), originalState, newState);

            log.info("Successfully updated KYC details {} for client {}", kycId, clientId);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(kycId).withClientId(clientId)
                    .build();

        } catch (Exception e) {
            log.error("Error processing KYC details update command: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteKycDetails(final JsonCommand command) {
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB

        try {
            // Extract parameters
            final Long clientId = command.getClientId();
            final Long kycId = command.entityId();

            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Find and validate KYC record exists and belongs to client
            final ClientKycDetails kycDetails = this.kycRepositoryWrapper.findOneThrowExceptionIfNotFound(kycId);

            // Verify the KYC record belongs to the specified client
            if (!kycDetails.getClient().getId().equals(clientId)) {
                throw new RuntimeException("KYC record does not belong to the specified client");
            }

            // AUDIT TRAIL: Capture state before deletion for compliance tracking
            final String deletedState = String.format(
                    "PAN:%s|Aadhaar:%s|VoterID:%s|Passport:%s|DL:%s|PanVerified:%s|AadhaarVerified:%s|VoterVerified:%s|PassportVerified:%s|DLVerified:%s|Notes:%s|CreatedDate:%s",
                    kycDetails.getPanNumber(), kycDetails.getAadhaarNumber(), kycDetails.getVoterId(), kycDetails.getPassportNumber(),
                    kycDetails.getDrivingLicenseNumber(), kycDetails.getPanVerified(), kycDetails.getAadhaarVerified(),
                    kycDetails.getVoterIdVerified(), kycDetails.getPassportVerified(), kycDetails.getDrivingLicenseVerified(),
                    kycDetails.getVerificationNotes(), kycDetails.getCreatedDate().orElse(null));

            // Delete the KYC record directly from the entity table
            this.kycRepositoryWrapper.deleteById(kycId);

            // AUDIT TRAIL: Log the deletion operation with complete record state for compliance
            log.info("AUDIT: KYC DELETE - User: {} | Client: {} | KYC ID: {} | Command: {} | Deleted Record: [{}]", currentUser.getId(),
                    clientId, kycId, command.commandId(), deletedState);

            log.info("Successfully deleted KYC details {} for client {}", kycId, clientId);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(kycId).withClientId(clientId)
                    .build();

        } catch (Exception e) {
            log.error("Error processing KYC details deletion command: {}", e.getMessage(), e);
            throw e;
        }
    }
}
