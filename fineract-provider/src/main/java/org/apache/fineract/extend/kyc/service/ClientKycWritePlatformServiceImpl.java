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
        // Validate provider availability using common service
        this.extendProviderService.validateProviderAvailable();
        
        // Tenant isolation handled by Fineract's database-level multi-tenant architecture
        // Each tenant has separate database/schema, queries automatically routed to correct tenant DB
        
        try {
            // Extract client ID from command
            final Long clientId = command.getClientId();

            // Validate client exists
            final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Find existing KYC record
            final ClientKycDetails kycDetails = this.kycRepositoryWrapper.findByClientIdThrowExceptionIfNotFound(clientId);

            // Extract verification parameters
            final String documentType = command.stringValueOfParameterNamed("documentType");
            final String documentNumber = command.stringValueOfParameterNamed("documentNumber");

            try {
                // Build provider-agnostic request for customer data pull using common service
                final CustomerDataProviderRequest providerRequest = CustomerDataProviderRequest.builder()
                        .referenceId(this.extendProviderService.createReferenceId("KYC", clientId)).consent(true).clientId(clientId)
                        .customerName(client.getDisplayName()).mobileNumber(client.mobileNo()).documentType(documentType)
                        .documentNumber(documentNumber).build();

                // Call provider-agnostic API for customer data pull using common service
                final CustomerDataProviderResponse providerResponse = this.extendProviderService.pullCustomerData(providerRequest);

                if (!providerResponse.isSuccess()) {
                    throw new RuntimeException("Customer data pull failed: " + providerResponse.getMessage());
                }

                // Parse verification results from provider response
                final Map<String, Boolean> verificationResults = providerResponse.getVerificationResults() != null
                        ? providerResponse.getVerificationResults()
                        : new HashMap<>();

                // Update KYC record with API verification results
                kycDetails.markApiVerificationCompleted(this.extendProviderService.getProviderName(),
                        providerResponse.getRawProviderResponse(), currentUser, verificationResults);

                final ClientKycDetails savedKyc = this.kycRepositoryWrapper.save(kycDetails);

                log.info("Successfully completed API verification for client {} document {}", clientId, documentType);

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

            // Build complete verification state map - need to send all document states
            final Map<String, Boolean> unverificationResults = new HashMap<>();
            
            // Set verification state for each document: false if selected for unverification, true if not selected
            unverificationResults.put("panVerified", !Boolean.TRUE.equals(unverifyPan));
            unverificationResults.put("aadhaarVerified", !Boolean.TRUE.equals(unverifyAadhaar));
            unverificationResults.put("drivingLicenseVerified", !Boolean.TRUE.equals(unverifyDrivingLicense));
            unverificationResults.put("voterIdVerified", !Boolean.TRUE.equals(unverifyVoterId));
            unverificationResults.put("passportVerified", !Boolean.TRUE.equals(unverifyPassport));
            
            // If no specific documents were selected, unverify all (backward compatibility)
            final boolean hasSpecificSelection = Boolean.TRUE.equals(unverifyPan) || Boolean.TRUE.equals(unverifyAadhaar) ||
                    Boolean.TRUE.equals(unverifyDrivingLicense) || Boolean.TRUE.equals(unverifyVoterId) || Boolean.TRUE.equals(unverifyPassport);
            
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
            if (command.hasParameter("verificationNotes")) {
                kycDetails.setVerificationNotes(command.stringValueOfParameterNamed("verificationNotes"));
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
            final String originalState = String.format("PAN:%s|Aadhaar:%s|VoterID:%s|Passport:%s|DL:%s|PanVerified:%s|AadhaarVerified:%s|VoterVerified:%s|PassportVerified:%s|DLVerified:%s|Notes:%s", 
                kycDetails.getPanNumber(), kycDetails.getAadhaarNumber(), kycDetails.getVoterId(), 
                kycDetails.getPassportNumber(), kycDetails.getDrivingLicenseNumber(),
                kycDetails.getPanVerified(), kycDetails.getAadhaarVerified(), kycDetails.getVoterIdVerified(),
                kycDetails.getPassportVerified(), kycDetails.getDrivingLicenseVerified(), kycDetails.getVerificationNotes());

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
            if (command.hasParameter("verificationNotes")) {
                kycDetails.setVerificationNotes(command.stringValueOfParameterNamed("verificationNotes"));
            }
            if (command.hasParameter("verificationProvider")) {
                kycDetails.setVerificationProvider(command.stringValueOfParameterNamed("verificationProvider"));
            }
            if (command.hasParameter("lastVerifiedOn")) {
                kycDetails.setLastVerifiedOn(command.localDateValueOfParameterNamed("lastVerifiedOn"));
            }

            this.kycRepositoryWrapper.save(kycDetails);

            // AUDIT TRAIL: Log the update operation with before/after state for compliance
            final String newState = String.format("PAN:%s|Aadhaar:%s|VoterID:%s|Passport:%s|DL:%s|PanVerified:%s|AadhaarVerified:%s|VoterVerified:%s|PassportVerified:%s|DLVerified:%s|Notes:%s", 
                kycDetails.getPanNumber(), kycDetails.getAadhaarNumber(), kycDetails.getVoterId(), 
                kycDetails.getPassportNumber(), kycDetails.getDrivingLicenseNumber(),
                kycDetails.getPanVerified(), kycDetails.getAadhaarVerified(), kycDetails.getVoterIdVerified(),
                kycDetails.getPassportVerified(), kycDetails.getDrivingLicenseVerified(), kycDetails.getVerificationNotes());
            
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
            final String deletedState = String.format("PAN:%s|Aadhaar:%s|VoterID:%s|Passport:%s|DL:%s|PanVerified:%s|AadhaarVerified:%s|VoterVerified:%s|PassportVerified:%s|DLVerified:%s|Notes:%s|CreatedDate:%s", 
                kycDetails.getPanNumber(), kycDetails.getAadhaarNumber(), kycDetails.getVoterId(), 
                kycDetails.getPassportNumber(), kycDetails.getDrivingLicenseNumber(),
                kycDetails.getPanVerified(), kycDetails.getAadhaarVerified(), kycDetails.getVoterIdVerified(),
                kycDetails.getPassportVerified(), kycDetails.getDrivingLicenseVerified(), kycDetails.getVerificationNotes(),
                kycDetails.getCreatedDate().orElse(null));

            // Delete the KYC record directly from the entity table
            this.kycRepositoryWrapper.deleteById(kycId);

            // AUDIT TRAIL: Log the deletion operation with complete record state for compliance
            log.info("AUDIT: KYC DELETE - User: {} | Client: {} | KYC ID: {} | Command: {} | Deleted Record: [{}]", 
                currentUser.getId(), clientId, kycId, command.commandId(), deletedState);

            log.info("Successfully deleted KYC details {} for client {}", kycId, clientId);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(kycId).withClientId(clientId)
                    .build();

        } catch (Exception e) {
            log.error("Error processing KYC details deletion command: {}", e.getMessage(), e);
            throw e;
        }
    }
}
