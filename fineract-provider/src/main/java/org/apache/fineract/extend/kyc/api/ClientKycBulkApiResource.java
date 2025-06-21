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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.extend.kyc.data.ClientKycData;
import org.apache.fineract.extend.kyc.service.ClientKycReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

/**
 * REST API Resource for Bulk Client KYC operations.
 *
 * This resource provides optimized bulk endpoints for retrieving KYC data for multiple clients
 * in a single request, significantly improving performance for table/list views.
 */
@Path("/v1/clients/extend/kyc/bulk")
@Component
@Tag(name = "Client KYC Bulk", description = "Bulk KYC operations for optimized performance in table/list views.")
@RequiredArgsConstructor
public class ClientKycBulkApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "CLIENT_KYC";

    private final PlatformSecurityContext context;
    private final ClientKycReadPlatformService clientKycReadPlatformService;
    private final DefaultToApiJsonSerializer<ClientKycData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Bulk Retrieve Client KYC Details", description = "Retrieve KYC details for multiple clients in a single optimized request.\n\n"
            + "This endpoint returns KYC details for multiple clients including document numbers, "
            + "verification status, and verification history. For clients without KYC details, "
            + "it returns template data with client information.\n\n"
            + "This bulk endpoint significantly improves performance for table/list views by "
            + "reducing API calls from N requests to 1 request.\n\n"
            + "Example Requests:\n\n" 
            + "GET /v1/clients/extend/kyc/bulk?clientIds=1,2,3,4,5\n\n"
            + "Response: Map<clientId, ClientKycData>")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Map.class))) })
    public String retrieveClientKycBulk(@QueryParam("clientIds") @Parameter(description = "Comma-separated list of client IDs") final String clientIdsParam,
            @Context final UriInfo uriInfo) {

        // Reuse existing permission validation pattern
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        // Parse client IDs from query parameter (reusing existing logic)
        final List<Long> clientIds = parseClientIds(clientIdsParam);
        
        if (clientIds.isEmpty()) {
            // Return empty JSON object for empty request
            return "{}";
        }

        // Bulk retrieve KYC data using existing service method
        final Map<Long, ClientKycData> kycDataMap = this.clientKycReadPlatformService.retrieveClientKycBulk(clientIds);

        // Reuse existing serialization settings
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        
        // Build JSON response manually to maintain Map<clientId, ClientKycData> structure
        // This approach reuses existing serialization for each ClientKycData object
        final StringBuilder jsonResponse = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<Long, ClientKycData> entry : kycDataMap.entrySet()) {
            if (!first) {
                jsonResponse.append(",");
            }
            first = false;
            
            // Serialize each KYC data object using existing serializer
            jsonResponse.append("\"").append(entry.getKey()).append("\":")
                       .append(this.toApiJsonSerializer.serialize(settings, entry.getValue()));
        }
        
        jsonResponse.append("}");
        return jsonResponse.toString();
    }

    /**
     * Helper method to parse comma-separated client IDs from query parameter.
     * Reuses the same logic pattern as the individual endpoint for consistency.
     */
    private List<Long> parseClientIds(String clientIdsParam) {
        if (clientIdsParam == null || clientIdsParam.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return Arrays.stream(clientIdsParam.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
        } catch (NumberFormatException e) {
            throw new PlatformApiDataValidationException(
                "error.msg.invalid.client.ids",
                "Invalid client IDs format. Expected comma-separated list of numbers.",
                "clientIds", clientIdsParam
            );
        }
    }
} 