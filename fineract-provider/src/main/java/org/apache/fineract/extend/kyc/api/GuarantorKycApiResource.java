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
package org.apache.fineract.extend.kyc.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.extend.commands.service.ExtendCommandWrapperBuilder;
import org.apache.fineract.extend.kyc.data.GuarantorKycData;
import org.apache.fineract.extend.kyc.service.GuarantorKycReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

/**
 * API Resource for Guarantor KYC Management.
 *
 * Provides REST endpoints for managing guarantor KYC details including: - CRUD operations for guarantor KYC records -
 * OTP-based Aadhaar verification for guarantors
 *
 * Relations: Client (1) -> Guarantor KYC (many)
 */
@Path("/v1/clients/{clientId}/extend/guarantor-kyc")
@Component
@Tag(name = "Guarantor KYC", description = "Guarantor KYC management operations")
@RequiredArgsConstructor
public class GuarantorKycApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<GuarantorKycData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<CommandProcessingResult> commandResultToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final GuarantorKycReadPlatformService guarantorKycReadPlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Guarantor KYC Details", description = "Retrieve all guarantor KYC details for a client")
    public String retrieveGuarantorKycDetails(@Context final UriInfo uriInfo,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission("GUARANTOR_KYC");

        final List<GuarantorKycData> guarantorKycDetailsList = this.guarantorKycReadPlatformService
                .retrieveGuarantorKycDetailsByClientId(clientId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, guarantorKycDetailsList);
    }

    @GET
    @Path("{guarantorKycId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Single Guarantor KYC Details", description = "Retrieve guarantor KYC details by ID")
    public String retrieveGuarantorKycDetails(@Context final UriInfo uriInfo,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("guarantorKycId") @Parameter(description = "guarantorKycId") final Long guarantorKycId) {

        this.context.authenticatedUser().validateHasReadPermission("GUARANTOR_KYC");

        final GuarantorKycData guarantorKycDetails = this.guarantorKycReadPlatformService.retrieveGuarantorKycDetails(guarantorKycId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, guarantorKycDetails);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create Guarantor KYC Details", description = "Create new guarantor KYC details for a client")
    public String createGuarantorKycDetails(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(description = "guarantorKycData") final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.createGuarantorKyc(clientId, apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{guarantorKycId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Guarantor KYC Details", description = "Update existing guarantor KYC details")
    public String updateGuarantorKycDetails(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("guarantorKycId") @Parameter(description = "guarantorKycId") final Long guarantorKycId,
            @Parameter(description = "guarantorKycData") final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.updateGuarantorKyc(clientId, guarantorKycId,
                apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{guarantorKycId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete Guarantor KYC Details", description = "Delete guarantor KYC details")
    public String deleteGuarantorKycDetails(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("guarantorKycId") @Parameter(description = "guarantorKycId") final Long guarantorKycId) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.deleteGuarantorKyc(clientId, guarantorKycId);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{guarantorKycId}/verify/manual")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Manually Verify Guarantor KYC", description = "Manually verify guarantor KYC documents by staff")
    public String verifyGuarantorKycManually(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("guarantorKycId") @Parameter(description = "guarantorKycId") final Long guarantorKycId,
            @Parameter(description = "manualVerificationData") final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.verifyGuarantorKycManually(clientId, guarantorKycId,
                apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{guarantorKycId}/verify/api")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Verify Guarantor KYC via API", description = "Verify guarantor KYC documents via external API providers")
    public String verifyGuarantorKycViaApi(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("guarantorKycId") @Parameter(description = "guarantorKycId") final Long guarantorKycId,
            @Parameter(description = "apiVerificationData") final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.verifyGuarantorKycViaApi(clientId, guarantorKycId,
                apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{guarantorKycId}/unverify")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Unverify Guarantor KYC", description = "Manually unverify previously verified guarantor KYC documents")
    public String unverifyGuarantorKycManually(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("guarantorKycId") @Parameter(description = "guarantorKycId") final Long guarantorKycId,
            @Parameter(description = "unverificationData") final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.unverifyGuarantorKycManually(clientId, guarantorKycId,
                apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{guarantorKycId}/verify/otp/generate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Generate OTP for Guarantor Aadhaar Verification", description = "Generate OTP for guarantor Aadhaar verification")
    public String generateOtpForGuarantorAadhaarVerification(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("guarantorKycId") @Parameter(description = "guarantorKycId") final Long guarantorKycId,
            @Parameter(description = "otpGenerationData") final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.generateOtpGuarantorKyc(clientId, guarantorKycId,
                apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{guarantorKycId}/verify/otp/submit")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Submit OTP for Guarantor Aadhaar Verification", description = "Submit OTP for guarantor Aadhaar verification")
    public String submitOtpForGuarantorAadhaarVerification(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("guarantorKycId") @Parameter(description = "guarantorKycId") final Long guarantorKycId,
            @Parameter(description = "otpSubmissionData") final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.submitOtpGuarantorKyc(clientId, guarantorKycId,
                apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

}
