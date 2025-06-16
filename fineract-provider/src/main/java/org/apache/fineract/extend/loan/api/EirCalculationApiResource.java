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
package org.apache.fineract.extend.loan.api;

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
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.extend.commands.service.ExtendCommandWrapperBuilder;
import org.apache.fineract.extend.loan.dto.EirCalculationRequest;
import org.apache.fineract.extend.loan.dto.EirCalculationResponse;
import org.apache.fineract.extend.loan.service.EirCalculationReadPlatformService;
import org.apache.fineract.extend.loan.service.EirCalculationWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

/**
 * REST API Resource for EIR calculation operations.
 *
 * Provides endpoints for creating, retrieving, updating, and deleting EIR calculations following extend module patterns
 * and Fineract security requirements.
 */
@Path("/v1/extend/loans/{loanId}/eir-calculations")
@Component
@Tag(name = "EIR Calculation", description = "EIR calculation operations for loan effective interest rate calculations")
@RequiredArgsConstructor
public class EirCalculationApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOAN_EIR_CALCULATION";

    private final EirCalculationWritePlatformService eirCalculationWritePlatformService;
    private final EirCalculationReadPlatformService eirCalculationReadPlatformService;
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<CommandProcessingResult> commandResultToApiJsonSerializer;

    /**
     * Create a new EIR calculation.
     *
     * @param loanId
     *            Loan ID
     * @param apiRequestBodyAsJson
     *            EIR calculation request JSON
     * @return CommandProcessingResult with created resource ID
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create EIR Calculation", description = "Create a new EIR calculation for a loan.\n\n"
            + "This endpoint creates a new EIR calculation with the provided parameters.\n\n"
            + "Mandatory Fields: principalAmount, emiAmount, tenureInMonths, calculationMethod\n\n"
            + "Optional Fields: netDisbursementAmount, chargesDueAtDisbursement, currencyCode\n\n" + "Example Requests:\n\n"
            + "loans/4/eir-calculations\n\n" + "{\n" + "  \"principalAmount\": 100000,\n" + "  \"emiAmount\": 9000,\n"
            + "  \"tenureInMonths\": 12,\n" + "  \"calculationMethod\": \"IRR_METHOD\",\n" + "  \"currencyCode\": \"INR\"\n" + "}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = EirCalculationRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String createEirCalculation(@PathParam("loanId") @Parameter(description = "loanId") Long loanId,
            @Parameter(hidden = true) String apiRequestBodyAsJson) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.createEirCalculation(loanId, apiRequestBodyAsJson);
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    /**
     * Calculate EIR for a loan and return the result.
     *
     * @param loanId
     *            Loan ID to calculate EIR for
     * @return EirCalculationResponse with calculation results
     */
    @POST
    @Path("calculate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Calculate EIR", description = "Calculate EIR for a loan and return the result.\n\n"
            + "This endpoint calculates the effective interest rate for a loan based on its repayment schedule.\n\n"
            + "Example Requests:\n\n" + "loans/4/eir-calculations/calculate")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = EirCalculationResponse.class))) })
    public EirCalculationResponse calculateEir(@PathParam("loanId") @Parameter(description = "loanId") Long loanId) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return eirCalculationWritePlatformService.calculateEir(loanId);
    }

    /**
     * Retrieve a specific EIR calculation.
     *
     * @param loanId
     *            Loan ID
     * @param calculationId
     *            EIR calculation ID
     * @return EirCalculationResponse or null if not found
     */
    @GET
    @Path("{calculationId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve EIR Calculation", description = "Retrieve a specific EIR calculation by ID.\n\n"
            + "Example Requests:\n\n" + "loans/4/eir-calculations/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = EirCalculationResponse.class))) })
    public EirCalculationResponse getEirCalculation(@PathParam("loanId") @Parameter(description = "loanId") Long loanId,
            @PathParam("calculationId") @Parameter(description = "calculationId") Long calculationId) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return eirCalculationReadPlatformService.retrieveEirCalculation(calculationId);
    }

    /**
     * Retrieve all EIR calculations for a loan.
     *
     * @param loanId
     *            Loan ID
     * @return List of EirCalculationResponse
     */
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve All EIR Calculations", description = "Retrieve all EIR calculations for a loan.\n\n"
            + "Example Requests:\n\n" + "loans/4/eir-calculations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = EirCalculationResponse.class))) })
    public List<EirCalculationResponse> getEirCalculationsByLoanId(@PathParam("loanId") @Parameter(description = "loanId") Long loanId) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return eirCalculationReadPlatformService.retrieveEirCalculationsByLoanId(loanId);
    }

    /**
     * Retrieve the latest EIR calculation for a loan.
     *
     * @param loanId
     *            Loan ID
     * @return EirCalculationResponse with the most recent calculation
     * @throws RuntimeException
     *             if no calculation exists for the loan
     */
    @GET
    @Path("latest")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Latest EIR Calculation", description = "Retrieve the latest EIR calculation for a loan.\n\n"
            + "Example Requests:\n\n" + "loans/4/eir-calculations/latest")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = EirCalculationResponse.class))) })
    public EirCalculationResponse getLatestEirCalculation(@PathParam("loanId") @Parameter(description = "loanId") Long loanId) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return eirCalculationReadPlatformService.retrieveLatestEirCalculation(loanId);
    }

    /**
     * Retrieve EIR calculation history for a loan.
     *
     * @param loanId
     *            Loan ID
     * @return List of EirCalculationResponse ordered by calculation date
     */
    @GET
    @Path("history")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve EIR Calculation History", description = "Retrieve EIR calculation history for a loan.\n\n"
            + "Example Requests:\n\n" + "loans/4/eir-calculations/history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = EirCalculationResponse.class))) })
    public List<EirCalculationResponse> getEirCalculationHistory(@PathParam("loanId") @Parameter(description = "loanId") Long loanId) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return eirCalculationReadPlatformService.retrieveEirCalculationHistory(loanId);
    }

    /**
     * Update an existing EIR calculation.
     *
     * @param loanId
     *            Loan ID
     * @param calculationId
     *            EIR calculation ID
     * @param apiRequestBodyAsJson
     *            Updated calculation data JSON
     * @return CommandProcessingResult with updated resource ID
     */
    @PUT
    @Path("{calculationId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update EIR Calculation", description = "Update an existing EIR calculation.\n\n" + "Example Requests:\n\n"
            + "loans/4/eir-calculations/1\n\n" + "{\n" + "  \"effectiveInterestRate\": 15.25,\n" + "  \"effectiveInterestRate\": 15.25\n"
            + "}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = EirCalculationRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String updateEirCalculation(@PathParam("loanId") @Parameter(description = "loanId") Long loanId,
            @PathParam("calculationId") @Parameter(description = "calculationId") Long calculationId,
            @Parameter(hidden = true) String apiRequestBodyAsJson) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.updateEirCalculation(loanId, calculationId, apiRequestBodyAsJson);
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }

    /**
     * Delete an EIR calculation.
     *
     * @param loanId
     *            Loan ID
     * @param calculationId
     *            EIR calculation ID
     * @return CommandProcessingResult with deleted resource ID
     */
    @DELETE
    @Path("{calculationId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete EIR Calculation", description = "Delete an EIR calculation.\n\n" + "Example Requests:\n\n"
            + "DELETE loans/4/eir-calculations/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String deleteEirCalculation(@PathParam("loanId") @Parameter(description = "loanId") Long loanId,
            @PathParam("calculationId") @Parameter(description = "calculationId") Long calculationId) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final CommandWrapper commandRequest = ExtendCommandWrapperBuilder.deleteEirCalculation(loanId, calculationId);
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.commandResultToApiJsonSerializer.serialize(result);
    }
}
