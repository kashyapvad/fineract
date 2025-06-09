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
package org.apache.fineract.extend.creditbureau.api;

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
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.extend.commands.service.ExtendCommandWrapperBuilder;
import org.apache.fineract.extend.creditbureau.data.ClientCreditBureauData;
import org.apache.fineract.extend.creditbureau.data.PullCreditReportRequest;
import org.apache.fineract.extend.creditbureau.service.ClientCreditBureauReadPlatformService;
import org.apache.fineract.extend.common.service.ExtendProviderService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

/**
 * REST API Resource for Client Credit Bureau operations.
 *
 * This resource provides endpoints for: - Pulling credit reports from external providers - Managing credit bureau data
 * - Retrieving credit history and scores
 */
@Path("/v1/clients/{clientId}/extend/creditreport")
@Component
@Tag(name = "Client Credit Bureau", description = "Client Credit Bureau Management provides APIs for pulling credit reports, managing credit scores, and integrating with external credit bureau providers like Decentro")
@RequiredArgsConstructor
public class ClientCreditBureauApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "CLIENT_CREDIT_REPORT";

    private final PlatformSecurityContext context;
    private final ClientCreditBureauReadPlatformService clientCreditBureauReadPlatformService;
    private final DefaultToApiJsonSerializer<ClientCreditBureauData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ExtendProviderService extendProviderService;

    @GET
    @Path("reports")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Client Credit Bureau Reports", description = "Retrieve all credit bureau reports for a specific client.\n\n"
            + "This endpoint returns a list of all credit reports that have been pulled for the client, "
            + "including successful reports, failed attempts, and pending requests.\n\n" + "Example Requests:\n\n"
            + "clients/1/extend/creditreport/reports")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientCreditBureauData.class))) })
    public String retrieveAllClientCreditReports(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final Collection<ClientCreditBureauData> creditReports = this.clientCreditBureauReadPlatformService
                .retrieveClientCreditReports(clientId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, creditReports);
    }

    @GET
    @Path("reports/{reportId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Specific Credit Bureau Report", description = "Retrieve details of a specific credit bureau report for a client.\n\n"
            + "This endpoint returns detailed information about a single credit report, "
            + "including the full report data, credit scores, and any error information.\n\n" + "Example Requests:\n\n"
            + "clients/1/extend/creditreport/reports/5")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientCreditBureauData.class))) })
    public String retrieveClientCreditReport(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("reportId") @Parameter(description = "reportId") final Long reportId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final ClientCreditBureauData creditReport = this.clientCreditBureauReadPlatformService.retrieveCreditReport(clientId, reportId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, creditReport);
    }



    @POST
    @Path("pull")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Pull Credit Bureau Report", description = "Initiate a credit bureau report pull for a client.\n\n"
            + "This endpoint initiates the process of pulling a credit report from an external provider. "
            + "The report type determines what kind of information is retrieved:\n"
            + "- FULL_REPORT: Complete credit history and detailed financial information\n"
            + "- CREDIT_SCORE: Credit score and rating information only\n" + "- DATA_PULL: Customer verification data pull\n\n"
            + "The operation creates a new credit bureau data table entry that will appear as a tab in the client interface.\n\n"
            + "Mandatory Fields: reportType\n\n" + "Optional Fields: provider, notes, panNumber, aadhaarNumber\n\n"
            + "Example Requests:\n\n" + "clients/1/extend/creditreport/pull\n\n" + "{\n" + "  \"reportType\": \"FULL_REPORT\",\n"
            + "  \"provider\": \"DECENTRO\",\n" + "  \"notes\": \"Monthly credit review\"\n" + "}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = PullCreditReportRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String pullCreditReport(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        // Validate provider availability using common service
        this.extendProviderService.validateProviderAvailable();

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.pullClientCreditBureauReport(clientId, apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("reports/{reportId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete Credit Bureau Report", description = "Delete a specific credit bureau report for a client.\n\n"
            + "This endpoint allows deletion of credit bureau reports, typically used to remove "
            + "outdated or incorrect reports. Note that this operation should be used carefully "
            + "as it permanently removes credit history data.\n\n" + "Example Requests:\n\n"
            + "DELETE clients/1/extend/creditreport/reports/5")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String deleteCreditReport(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("reportId") @Parameter(description = "reportId") final Long reportId) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.deleteClientCreditBureauReport(clientId, reportId);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("create")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create Credit Report Manually", description = "Create a new credit report manually for a client.\n\n"
            + "This endpoint allows manual creation of credit reports with custom data. "
            + "This is useful for entering legacy data or data from other sources.\n\n" + "Mandatory Fields: reportType\n\n"
            + "Optional Fields: creditScore, reportData, customerInfo, financialInfo, additionalData\n\n"
            + "The additionalData field accepts JSON structure for custom data entry that doesn't fit standard fields.\n\n"
            + "Example Requests:\n\n"
            + "clients/1/extend/creditreport/create\n\n" + "{\n" + "  \"reportType\": \"MANUAL_ENTRY\",\n"
            + "  \"creditBureauProvider\": \"MANUAL\",\n" + "  \"creditScore\": 750,\n" + "  \"scoreModel\": \"TRANSUNION_CIBIL\",\n"
            + "  \"reportSummary\": \"Good credit history\",\n" + "  \"reportNotes\": \"Manually entered from legacy system\",\n"
            + "  \"additionalData\": {\n"
            + "    \"section1\": {\n"
            + "      \"customField1\": \"value1\",\n"
            + "      \"customField2\": \"value2\"\n"
            + "    },\n"
            + "    \"notes\": \"Additional context information\"\n"
            + "  }\n"
            + "}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = Object.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String createCreditReport(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.createClientCreditBureauReport(clientId, apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("reports/{reportId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Credit Report", description = "Update an existing credit report for a client.\n\n"
            + "This endpoint allows updating credit report data, scores, and metadata. "
            + "Useful for correcting information or adding additional details.\n\n"
            + "Optional Fields: Any credit report field can be updated including additionalData\n\n"
            + "The additionalData field can be used to add supplemental information to existing reports.\n\n"
            + "Example Requests:\n\n"
            + "clients/1/extend/creditreport/reports/5\n\n" + "{\n" + "  \"creditScore\": 780,\n"
            + "  \"reportSummary\": \"Updated after recent payment\",\n" + "  \"reportNotes\": \"Score improved due to on-time payments\",\n"
            + "  \"additionalData\": {\n"
            + "    \"updateNotes\": \"Manual adjustment after payment verification\",\n"
            + "    \"verificationDetails\": {\n"
            + "      \"verifiedBy\": \"John Doe\",\n"
            + "      \"verificationDate\": \"2024-01-15\"\n"
            + "    }\n"
            + "  }\n"
            + "}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = Object.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String updateCreditReport(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("reportId") @Parameter(description = "reportId") final Long reportId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.updateClientCreditBureauReport(clientId, reportId,
                apiRequestBodyAsJson);

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
