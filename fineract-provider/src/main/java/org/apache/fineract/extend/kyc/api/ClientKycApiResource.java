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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.extend.commands.service.ExtendCommandWrapperBuilder;
import org.apache.fineract.extend.kyc.data.ClientKycData;
import org.apache.fineract.extend.kyc.data.ManualUnverifyKycRequest;
import org.apache.fineract.extend.kyc.service.ClientKycReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;
// ArrayList, Arrays, List, and Collectors imports removed since bulk endpoint moved to separate resource

/**
 * REST API Resource for Client KYC verification operations.
 *
 * This resource provides endpoints for: - Retrieving KYC details and templates - API-based verification of KYC
 * documents - Manual verification and unverification by staff
 *
 * Note: Basic CRUD operations for KYC details are handled by the datatables API. This resource focuses on
 * verification-specific business logic.
 */
@Path("/v1/clients/{clientId}/extend/kyc")
@Component
@Tag(name = "Client KYC", description = "Client KYC verification operations for API-based and manual verification workflows. Basic KYC details management is handled through datatables API.")
@RequiredArgsConstructor
public class ClientKycApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "CLIENT_KYC";

    private final PlatformSecurityContext context;
    private final ClientKycReadPlatformService clientKycReadPlatformService;
    private final DefaultToApiJsonSerializer<ClientKycData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<CommandProcessingResult> commandResultToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Client KYC Details", description = "Retrieve KYC details for a specific client.\n\n"
            + "This endpoint returns the KYC details for a client including document numbers, "
            + "verification status, and verification history. If no KYC details exist, "
            + "it returns a template with client information.\n\n" + "Example Requests:\n\n" + "clients/1/extend/kyc")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientKycData.class))) })
    public String retrieveClientKyc(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final ClientKycData kycData = this.clientKycReadPlatformService.retrieveClientKyc(clientId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, kycData);
    }

    // Bulk endpoint moved to ClientKycBulkApiResource for proper path structure
    // This avoids path conflicts and follows REST conventions

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve KYC Template", description = "Retrieve template data for KYC operations.\n\n"
            + "This endpoint returns template information including client details " + "and default values needed for KYC operations.\n\n"
            + "Example Requests:\n\n" + "clients/1/extend/kyc/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientKycData.class))) })
    public String retrieveKycTemplate(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final ClientKycData template = this.clientKycReadPlatformService.retrieveTemplate(clientId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, template);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create KYC Details", description = "Create new KYC details for a client.\n\n"
            + "This endpoint allows manual creation of KYC details for a client. "
            + "Staff members can enter document numbers and verification status.\n\n" + "Mandatory Fields: At least one document number\n\n"
            + "Optional Fields: verification status for each document, notes\n\n" + "Example Requests:\n\n" + "clients/1/extend/kyc\n\n"
            + "{\n" + "  \"panNumber\": \"ABCDE1234F\",\n" + "  \"aadhaarNumber\": \"123456789012\",\n" + "  \"panVerified\": false,\n"
            + "  \"aadhaarVerified\": false,\n" + "  \"verificationNotes\": \"Initial KYC entry\"\n" + "}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = Object.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String createKycDetails(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.createClientKyc(clientId, apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{kycId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update KYC Details", description = "Update existing KYC details for a client.\n\n"
            + "This endpoint allows updating KYC document numbers and verification status. "
            + "When document numbers are changed, verification status is automatically reset.\n\n"
            + "Optional Fields: Any KYC field can be updated\n\n" + "Example Requests:\n\n" + "clients/1/extend/kyc/5\n\n" + "{\n"
            + "  \"panNumber\": \"NEWPAN123F\",\n" + "  \"drivingLicenseNumber\": \"DL1234567890\",\n"
            + "  \"verificationNotes\": \"Updated with new documents\"\n" + "}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = Object.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String updateKycDetails(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("kycId") @Parameter(description = "kycId") final Long kycId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.updateClientKyc(clientId, kycId, apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{kycId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete KYC Details", description = "Delete KYC details for a client.\n\n"
            + "This endpoint allows deletion of KYC details. Use with caution as this "
            + "permanently removes KYC information and verification history.\n\n" + "Example Requests:\n\n"
            + "DELETE clients/1/extend/kyc/5")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String deleteKycDetails(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("kycId") @Parameter(description = "kycId") final Long kycId) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.deleteClientKyc(clientId, kycId);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("verify")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Verify KYC via API", description = "Initiate API-based verification of client KYC documents.\n\n"
            + "This endpoint initiates the process of verifying KYC documents against external provider databases. "
            + "The verification is performed using configured API providers (like Decentro) and updates the "
            + "verification status of individual documents based on the API response.\n\n"
            + "The operation updates the existing KYC data table entry with verification results.\n\n"
            + "Note: KYC details must already exist in the datatable before verification can be performed.\n\n"
            + "Optional Fields: provider, notes, specific documents to verify\n\n" + "Example Requests:\n\n"
            + "clients/1/extend/kyc/verify\n\n" + "{\n" + "  \"provider\": \"DECENTRO\",\n" + "  \"verifyPan\": true,\n"
            + "  \"verifyAadhaar\": true,\n" + "  \"notes\": \"Monthly verification check\"\n" + "}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = Object.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String verifyKycViaApi(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.verifyClientKycApi(clientId, apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("verify/manual")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Manual KYC Verification", description = "Manually verify client KYC documents by staff.\n\n"
            + "This endpoint allows staff members to manually verify KYC documents after physical or offline verification. "
            + "The verification is recorded with the staff member's details and timestamp for audit trail.\n\n"
            + "This operation is typically used when documents are verified through offline processes or "
            + "when API verification is not available.\n\n"
            + "Note: KYC details must already exist in the datatable before verification can be performed.\n\n"
            + "Mandatory Fields: At least one document verification flag\n\n" + "Optional Fields: notes, specific documents to verify\n\n"
            + "Example Requests:\n\n" + "clients/1/extend/kyc/verify/manual\n\n" + "{\n" + "  \"verifyPan\": true,\n"
            + "  \"verifyAadhaar\": true,\n" + "  \"notes\": \"Documents verified through physical inspection\"\n" + "}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = Object.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String verifyKycManually(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.verifyClientKycManual(clientId, apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("unverify/manual")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Manual KYC Unverification", description = "Manually unverify client KYC documents by staff.\n\n"
            + "This endpoint allows staff members to manually unverify previously verified KYC documents. "
            + "This operation is typically used when documents are found to be invalid, expired, or when "
            + "verification needs to be revoked for compliance reasons.\n\n"
            + "The unverification is recorded with the staff member's details, reason, and timestamp for complete audit trail.\n\n"
            + "Note: KYC details must already exist in the datatable before unverification can be performed.\n\n"
            + "Mandatory Fields: reason\n\n" + "Optional Fields: notes, specific documents to unverify\n\n" + "Example Requests:\n\n"
            + "clients/1/extend/kyc/unverify/manual\n\n" + "{\n" + "  \"reason\": \"Document found to be expired\",\n"
            + "  \"notes\": \"PAN card expired on previous month\",\n" + "  \"unverifyPan\": true\n" + "}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ManualUnverifyKycRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String unverifyKycManually(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.unverifyClientKycManual(clientId, apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    // parseClientIds method moved to ClientKycBulkApiResource to avoid duplication
}
