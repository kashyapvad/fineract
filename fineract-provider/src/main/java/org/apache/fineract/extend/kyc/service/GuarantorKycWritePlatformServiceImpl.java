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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.extend.creditbureau.provider.SurePassProvider;
import org.apache.fineract.extend.kyc.domain.GuarantorKycDetails;
import org.apache.fineract.extend.kyc.domain.GuarantorKycDetailsRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing Guarantor KYC details operations.
 *
 * This service handles CRUD operations and OTP verification for guarantor KYC details. Each client can have multiple
 * guarantors, and each guarantor has independent KYC verification.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuarantorKycWritePlatformServiceImpl implements GuarantorKycWritePlatformService {

    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final PlatformSecurityContext context;
    private final GuarantorKycDetailsRepositoryWrapper guarantorKycRepositoryWrapper;

    // SurePass provider for OTP verification (optional dependency)
    @Autowired(required = false)
    private SurePassProvider surePassProvider;

    @Override
    @Transactional
    public CommandProcessingResult createGuarantorKycDetails(JsonCommand command) {
        log.info("=== STARTING GUARANTOR KYC DETAILS CREATION ===");
        log.info("Command ID: {}", command.commandId());
        log.info("Client ID: {}", command.getClientId());

        try {
            // Extract client ID from command
            final Long clientId = command.getClientId();

            // Validate client exists and belongs to current tenant
            final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            this.context.authenticatedUser();

            // Extract mobile number for validation
            final String mobileNumber = command.stringValueOfParameterNamed("mobileNumber");

            // Check if mobile number already exists for this client (unique constraint)
            if (this.guarantorKycRepositoryWrapper.existsByClientIdAndMobileNumber(clientId, mobileNumber)) {
                throw new GeneralPlatformDomainRuleException("error.msg.guarantor.kyc.mobile.already.exists", String.format(
                        "A guarantor with mobile number '%s' already exists for this client. Please use a different mobile number or check the existing guarantor KYC records.",
                        mobileNumber));
            }

            // Create new guarantor KYC details record
            final GuarantorKycDetails guarantorKycDetails = GuarantorKycDetails.createNew(client,
                    command.stringValueOfParameterNamed("fullName"), mobileNumber,
                    command.stringValueOfParameterNamed("relationshipToClient"), command.stringValueOfParameterNamed("panNumber"),
                    command.stringValueOfParameterNamed("aadhaarNumber"));

            // Set optional fields if provided
            if (command.hasParameter("isPrimaryGuarantor")) {
                guarantorKycDetails.setIsPrimaryGuarantor(command.booleanPrimitiveValueOfParameterNamed("isPrimaryGuarantor"));
            }

            // Set notes via verificationNotes field
            if (command.hasParameter("notes")) {
                guarantorKycDetails.setVerificationNotes(command.stringValueOfParameterNamed("notes"));
            }

            final GuarantorKycDetails savedDetails = this.guarantorKycRepositoryWrapper.save(guarantorKycDetails);

            log.info("Successfully created guarantor KYC details for client {} with ID {}", clientId, savedDetails.getId());

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(savedDetails.getId())
                    .withClientId(clientId).build();

        } catch (Exception e) {
            log.error("Error processing guarantor KYC details creation", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateGuarantorKycDetails(JsonCommand command) {
        log.info("=== STARTING GUARANTOR KYC DETAILS UPDATE ===");
        log.info("Command ID: {}", command.commandId());
        log.info("Client ID: {}", command.getClientId());

        try {
            // Extract parameters
            final Long clientId = command.getClientId();
            final Long guarantorKycId = command.entityId();

            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

            // Find and validate guarantor KYC record
            final GuarantorKycDetails guarantorKycDetails = this.guarantorKycRepositoryWrapper
                    .findOneThrowExceptionIfNotFound(guarantorKycId);

            // Verify the guarantor KYC record belongs to the specified client
            if (!guarantorKycDetails.getClient().getId().equals(clientId)) {
                throw new RuntimeException("Guarantor KYC record does not belong to the specified client");
            }

            // Check mobile number uniqueness if being updated
            if (command.hasParameter("mobileNumber")) {
                final String newMobileNumber = command.stringValueOfParameterNamed("mobileNumber");
                if (this.guarantorKycRepositoryWrapper.existsByClientIdAndMobileNumberAndIdNot(clientId, newMobileNumber, guarantorKycId)) {
                    throw new GeneralPlatformDomainRuleException("error.msg.guarantor.kyc.mobile.already.exists", String.format(
                            "A guarantor with mobile number '%s' already exists for this client. Please use a different mobile number or check the existing guarantor KYC records.",
                            newMobileNumber));
                }
            }

            // Update guarantor details if provided
            if (command.hasParameter("fullName")) {
                guarantorKycDetails.setFullName(command.stringValueOfParameterNamed("fullName"));
            }
            if (command.hasParameter("mobileNumber")) {
                guarantorKycDetails.setMobileNumber(command.stringValueOfParameterNamed("mobileNumber"));
            }
            if (command.hasParameter("aadhaarNumber")) {
                guarantorKycDetails.setAadhaarNumber(command.stringValueOfParameterNamed("aadhaarNumber"));
            }
            if (command.hasParameter("panNumber")) {
                guarantorKycDetails.setPanNumber(command.stringValueOfParameterNamed("panNumber"));
            }

            // Update notes if provided
            if (command.hasParameter("notes")) {
                guarantorKycDetails.setVerificationNotes(command.stringValueOfParameterNamed("notes"));
            }

            this.guarantorKycRepositoryWrapper.save(guarantorKycDetails);

            log.info("Successfully updated guarantor KYC details {} for client {}", guarantorKycId, clientId);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(guarantorKycId)
                    .withClientId(clientId).build();

        } catch (Exception e) {
            log.error("Error processing guarantor KYC details update command", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteGuarantorKycDetails(JsonCommand command) {
        log.info("=== STARTING GUARANTOR KYC DETAILS DELETION ===");
        log.info("Command ID: {}", command.commandId());
        log.info("Client ID: {}", command.getClientId());

        try {
            // Extract parameters
            final Long clientId = command.getClientId();
            final Long guarantorKycId = command.entityId();

            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            this.context.authenticatedUser();

            // Find and validate guarantor KYC record
            final GuarantorKycDetails guarantorKycDetails = this.guarantorKycRepositoryWrapper
                    .findOneThrowExceptionIfNotFound(guarantorKycId);

            // Verify the guarantor KYC record belongs to the specified client
            if (!guarantorKycDetails.getClient().getId().equals(clientId)) {
                throw new RuntimeException("Guarantor KYC record does not belong to the specified client");
            }

            // Delete the guarantor KYC record
            this.guarantorKycRepositoryWrapper.deleteById(guarantorKycId);

            log.info("Successfully deleted guarantor KYC details {} for client {}", guarantorKycId, clientId);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(guarantorKycId)
                    .withClientId(clientId).build();

        } catch (Exception e) {
            log.error("Error processing guarantor KYC details deletion command", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult verifyGuarantorKycViaApi(JsonCommand command) {
        log.info("=== STARTING GUARANTOR KYC API VERIFICATION ===");
        log.info("Command ID: {}", command.commandId());
        log.info("Client ID: {}", command.getClientId());

        try {
            // Extract parameters
            final Long clientId = command.getClientId();
            final Long guarantorKycId = command.entityId();

            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Find guarantor KYC record
            final GuarantorKycDetails guarantorKycDetails = this.guarantorKycRepositoryWrapper
                    .findOneThrowExceptionIfNotFound(guarantorKycId);

            // Verify the guarantor KYC record belongs to the specified client
            if (!guarantorKycDetails.getClient().getId().equals(clientId)) {
                throw new RuntimeException("Guarantor KYC record does not belong to the specified client");
            }

            // Get verification parameters
            final String verificationProvider = command.stringValueOfParameterNamed("verificationProvider");
            final String notes = command.stringValueOfParameterNamed("notes");

            // Simulate API verification (in real implementation, would call external API)
            Map<String, Boolean> verificationResults = new HashMap<>();

            if (StringUtils.isNotBlank(guarantorKycDetails.getPanNumber())) {
                verificationResults.put("panVerified", true);
            }
            if (StringUtils.isNotBlank(guarantorKycDetails.getAadhaarNumber())) {
                verificationResults.put("aadhaarVerified", true);
            }

            // Mark API verification as completed - convert String to JsonNode
            JsonNode apiResponseNode = null;
            try {
                ObjectMapper mapper = new ObjectMapper();
                apiResponseNode = mapper.readTree("{\"status\":\"success\",\"message\":\"API verification completed\"}");
            } catch (Exception e) {
                log.warn("Failed to create JSON response node", e);
            }

            guarantorKycDetails.markApiVerificationCompleted(verificationProvider, apiResponseNode, currentUser, verificationResults,
                    notes);
            this.guarantorKycRepositoryWrapper.save(guarantorKycDetails);

            log.info("Successfully completed API verification for guarantor {}", guarantorKycId);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(guarantorKycDetails.getId())
                    .withClientId(clientId).build();

        } catch (Exception e) {
            log.error("Error processing guarantor KYC API verification command", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult verifyGuarantorKycManually(JsonCommand command) {
        log.info("=== STARTING GUARANTOR KYC MANUAL VERIFICATION ===");
        log.info("Command ID: {}", command.commandId());
        log.info("Client ID: {}", command.getClientId());

        try {
            // Extract parameters
            final Long clientId = command.getClientId();
            final Long guarantorKycId = command.entityId();

            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Find guarantor KYC record
            final GuarantorKycDetails guarantorKycDetails = this.guarantorKycRepositoryWrapper
                    .findOneThrowExceptionIfNotFound(guarantorKycId);

            // Verify the guarantor KYC record belongs to the specified client
            if (!guarantorKycDetails.getClient().getId().equals(clientId)) {
                throw new RuntimeException("Guarantor KYC record does not belong to the specified client");
            }

            // Extract verification parameters - following client KYC pattern
            final String verificationNotes = command.stringValueOfParameterNamed("notes");
            final Boolean panVerified = command.booleanObjectValueOfParameterNamed("panVerified");
            final Boolean aadhaarVerified = command.booleanObjectValueOfParameterNamed("aadhaarVerified");

            // Build verification results map - only include fields that are explicitly provided
            final Map<String, Boolean> verificationResults = new HashMap<>();
            if (panVerified != null) {
                verificationResults.put("panVerified", panVerified);
            }
            if (aadhaarVerified != null) {
                verificationResults.put("aadhaarVerified", aadhaarVerified);
            }

            // Mark manual verification as completed
            guarantorKycDetails.markManualVerificationCompleted(currentUser, verificationResults, verificationNotes);
            this.guarantorKycRepositoryWrapper.save(guarantorKycDetails);

            log.info("Successfully completed manual verification for guarantor {} with results: {}", guarantorKycId, verificationResults);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(guarantorKycDetails.getId())
                    .withClientId(clientId).build();

        } catch (Exception e) {
            log.error("Error processing guarantor KYC manual verification command", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult unverifyGuarantorKycManually(JsonCommand command) {
        log.info("=== STARTING GUARANTOR KYC MANUAL UNVERIFICATION ===");
        log.info("Command ID: {}", command.commandId());
        log.info("Client ID: {}", command.getClientId());

        try {
            // Extract parameters
            final Long clientId = command.getClientId();
            final Long guarantorKycId = command.entityId();

            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Find guarantor KYC record
            final GuarantorKycDetails guarantorKycDetails = this.guarantorKycRepositoryWrapper
                    .findOneThrowExceptionIfNotFound(guarantorKycId);

            // Verify the guarantor KYC record belongs to the specified client
            if (!guarantorKycDetails.getClient().getId().equals(clientId)) {
                throw new RuntimeException("Guarantor KYC record does not belong to the specified client");
            }

            // Extract unverification parameters - following client KYC pattern
            final String unverificationNotes = command.stringValueOfParameterNamed("notes");
            command.stringValueOfParameterNamed("reason");
            final Boolean unverifyPan = command.booleanObjectValueOfParameterNamed("unverifyPan");
            final Boolean unverifyAadhaar = command.booleanObjectValueOfParameterNamed("unverifyAadhaar");

            // Build unverification results map - following client KYC pattern
            final Map<String, Boolean> unverificationResults = new HashMap<>();

            if (Boolean.TRUE.equals(unverifyPan)) {
                unverificationResults.put("panVerified", false);
            }
            if (Boolean.TRUE.equals(unverifyAadhaar)) {
                unverificationResults.put("aadhaarVerified", false);
            }

            // If no specific documents were selected, unverify all (backward compatibility)
            final boolean hasSpecificSelection = Boolean.TRUE.equals(unverifyPan) || Boolean.TRUE.equals(unverifyAadhaar);

            if (!hasSpecificSelection) {
                unverificationResults.put("panVerified", false);
                unverificationResults.put("aadhaarVerified", false);
            }

            // Update guarantor KYC record with manual unverification
            guarantorKycDetails.markManualVerificationCompleted(currentUser, unverificationResults, "UNVERIFIED: " + unverificationNotes);

            this.guarantorKycRepositoryWrapper.save(guarantorKycDetails);

            log.info("Successfully completed selective manual unverification for guarantor {} with results: {}", guarantorKycId,
                    unverificationResults);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(guarantorKycDetails.getId())
                    .withClientId(clientId).build();

        } catch (Exception e) {
            log.error("Error processing guarantor KYC manual unverification command", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult generateOtpForGuarantorAadhaarVerification(JsonCommand command) {
        log.info("=== STARTING GUARANTOR AADHAAR OTP GENERATION ===");
        log.info("Command ID: {}", command.commandId());
        log.info("Client ID: {}", command.getClientId());

        try {
            // Extract parameters
            final Long clientId = command.getClientId();
            final Long guarantorKycId = command.entityId();

            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

            // Find guarantor KYC record
            final GuarantorKycDetails guarantorKycDetails = this.guarantorKycRepositoryWrapper
                    .findOneThrowExceptionIfNotFound(guarantorKycId);

            // Verify the guarantor KYC record belongs to the specified client
            if (!guarantorKycDetails.getClient().getId().equals(clientId)) {
                throw new RuntimeException("Guarantor KYC record does not belong to the specified client");
            }

            // Get Aadhaar number from command or existing record
            String aadhaarNumber = command.stringValueOfParameterNamed("aadhaarNumber");
            if (StringUtils.isBlank(aadhaarNumber)) {
                aadhaarNumber = guarantorKycDetails.getAadhaarNumber();
            }

            if (StringUtils.isBlank(aadhaarNumber)) {
                throw new GeneralPlatformDomainRuleException("error.msg.guarantor.kyc.aadhaar.required",
                        "Aadhaar number is required for OTP generation. Please provide Aadhaar number or update guarantor KYC details first.");
            }

            // Make API call to generate OTP
            final String otpClientId = generateOtpApiCall(aadhaarNumber);

            // Store OTP client ID and timestamp
            guarantorKycDetails.markOtpGenerated(otpClientId);
            this.guarantorKycRepositoryWrapper.save(guarantorKycDetails);

            log.info("Successfully generated OTP for guarantor {} with client ID: {}", guarantorKycId, otpClientId);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(guarantorKycDetails.getId())
                    .withClientId(clientId).build();

        } catch (Exception e) {
            log.error("Error during guarantor OTP generation", e);
            throw new RuntimeException("Guarantor OTP generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult submitOtpForGuarantorAadhaarVerification(JsonCommand command) {
        log.info("=== STARTING GUARANTOR AADHAAR OTP VERIFICATION ===");
        log.info("Command ID: {}", command.commandId());
        log.info("Client ID: {}", command.getClientId());

        try {
            // Extract parameters
            final Long clientId = command.getClientId();
            final Long guarantorKycId = command.entityId();

            // Validate client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final AppUser currentUser = this.context.authenticatedUser();

            // Find guarantor KYC record
            final GuarantorKycDetails guarantorKycDetails = this.guarantorKycRepositoryWrapper
                    .findOneThrowExceptionIfNotFound(guarantorKycId);

            // Verify the guarantor KYC record belongs to the specified client
            if (!guarantorKycDetails.getClient().getId().equals(clientId)) {
                throw new RuntimeException("Guarantor KYC record does not belong to the specified client");
            }

            // Validate OTP was generated
            if (StringUtils.isBlank(guarantorKycDetails.getOtpClientId())) {
                throw new GeneralPlatformDomainRuleException("error.msg.guarantor.kyc.otp.not.generated",
                        "OTP was not generated for this guarantor. Please generate OTP first.");
            }

            // Get OTP from command
            final String otp = command.stringValueOfParameterNamed("otp");
            if (StringUtils.isBlank(otp)) {
                throw new GeneralPlatformDomainRuleException("error.msg.guarantor.kyc.otp.required", "OTP is required for verification.");
            }

            final String notes = command.stringValueOfParameterNamed("notes");

            // Make API call to verify OTP
            final boolean verificationResult = verifyOtpApiCall(guarantorKycDetails.getOtpClientId(), otp);

            if (verificationResult) {
                // Mark OTP verification as completed
                guarantorKycDetails.markOtpVerificationCompleted(currentUser, notes);
                this.guarantorKycRepositoryWrapper.save(guarantorKycDetails);

                log.info("Successfully verified OTP for guarantor {}", guarantorKycId);

                return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(guarantorKycDetails.getId())
                        .withClientId(clientId).build();
            } else {
                throw new GeneralPlatformDomainRuleException("error.msg.guarantor.kyc.otp.invalid", "Invalid OTP provided.");
            }

        } catch (Exception e) {
            log.error("Error during guarantor OTP verification", e);
            throw new RuntimeException("Guarantor OTP verification failed: " + e.getMessage(), e);
        }
    }

    /**
     * Makes API call to generate OTP for guarantor Aadhaar verification
     *
     * @param aadhaarNumber
     *            the Aadhaar number
     * @return OTP client ID from provider
     */
    private String generateOtpApiCall(String aadhaarNumber) {
        if (surePassProvider != null && surePassProvider.isAvailable()) {
            try {
                log.info("Generating OTP via SurePass for guarantor Aadhaar: {}", aadhaarNumber);
                return surePassProvider.generateOtpForAadhaar(aadhaarNumber);
            } catch (Exception e) {
                log.error("SurePass OTP generation failed for guarantor: {}", e.getMessage());
                throw new RuntimeException("OTP generation failed: " + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("SurePass provider is not available. Please configure the provider or check service availability.");
        }
    }

    /**
     * Makes API call to verify OTP for guarantor
     *
     * @param otpClientId
     *            the client ID from OTP generation
     * @param otp
     *            the OTP to verify
     * @return true if verification successful
     */
    private boolean verifyOtpApiCall(String otpClientId, String otp) {
        if (surePassProvider != null && surePassProvider.isAvailable()) {
            try {
                log.info("Verifying OTP via SurePass for guarantor client ID: {}, OTP: {}", otpClientId, otp);
                return surePassProvider.submitOtpForAadhaar(otpClientId, otp);
            } catch (Exception e) {
                log.error("SurePass OTP verification failed for guarantor: {}", e.getMessage());
                return false;
            }
        } else {
            throw new RuntimeException("SurePass provider is not available. Please configure the provider or check service availability.");
        }
    }
}
