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
package org.apache.fineract.extend.kfs.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.extend.kfs.dto.KfsDocumentRequest;
import org.apache.fineract.extend.kfs.dto.KfsDocumentResponse;
import org.apache.fineract.extend.kfs.dto.KfsDocumentStatistics;
import org.apache.fineract.extend.kfs.service.KfsDocumentReadPlatformService;
import org.apache.fineract.extend.kfs.service.KfsDocumentWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * REST API Resource for KFS Document operations. Provides endpoints for CRUD operations, file downloads, statistics,
 * and document management.
 *
 * Implements comprehensive KFS document lifecycle management with proper security, validation, and error handling
 * following Fineract patterns.
 */
@Path("/api/v1/kfs/documents")
@Component
@Tag(name = "KFS Documents", description = "KFS Document Management API")
public class KfsDocumentApiResource {

    private static final Logger log = LoggerFactory.getLogger(KfsDocumentApiResource.class);

    private final PlatformSecurityContext context;
    private final KfsDocumentReadPlatformService kfsDocumentReadPlatformService;
    private final KfsDocumentWritePlatformService kfsDocumentWritePlatformService;
    private final DefaultToApiJsonSerializer<Object> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ObjectMapper objectMapper;

    @Autowired
    public KfsDocumentApiResource(final PlatformSecurityContext context,
            final KfsDocumentReadPlatformService kfsDocumentReadPlatformService,
            final KfsDocumentWritePlatformService kfsDocumentWritePlatformService,
            final DefaultToApiJsonSerializer<Object> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, final ObjectMapper objectMapper) {
        this.context = context;
        this.kfsDocumentReadPlatformService = kfsDocumentReadPlatformService;
        this.kfsDocumentWritePlatformService = kfsDocumentWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a new KFS document
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create KFS Document", description = "Creates a new KFS document with the provided details")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "KFS document created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden") })
    public Response createKfsDocument(@Parameter(description = "KFS document creation request") final String apiRequestBodyAsJson) {

        try {
            log.debug("Creating KFS document with request: {}", apiRequestBodyAsJson);

            // Parse request
            KfsDocumentRequest request = objectMapper.readValue(apiRequestBodyAsJson, KfsDocumentRequest.class);

            // Create document
            CommandProcessingResult result = kfsDocumentWritePlatformService.createKfsDocument(request);

            log.info("KFS document created successfully with ID: {}", result.getResourceId());

            return Response.status(Response.Status.CREATED).entity(result).build();

        } catch (JsonProcessingException e) {
            log.error("Error parsing KFS document creation request", e);
            throw new PlatformApiDataValidationException("Invalid JSON format", "json", apiRequestBodyAsJson);
        } catch (Exception e) {
            log.error("Error creating KFS document", e);
            throw e;
        }
    }

    /**
     * Retrieve a KFS document by ID
     */
    @GET
    @Path("/{documentId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve KFS Document", description = "Retrieves a KFS document by its ID")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "KFS document retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "KFS document not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden") })
    public Response getKfsDocument(@PathParam("documentId") @Parameter(description = "KFS document ID") final Long documentId,
            @Context final UriInfo uriInfo) {

        log.debug("Retrieving KFS document with ID: {}", documentId);

        KfsDocumentResponse document = kfsDocumentReadPlatformService.retrieveKfsDocument(documentId);

        if (document != null) {

            final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return Response.ok().entity(toApiJsonSerializer.serialize(settings, document, null)).build();
        } else {
            log.warn("KFS document not found with ID: {}", documentId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Retrieve KFS documents by loan ID
     */
    @GET
    @Path("/loans/{loanId}/documents")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve KFS Documents by Loan", description = "Retrieves all KFS documents for a specific loan")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "KFS documents retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden") })
    public Response getKfsDocumentsByLoanId(@PathParam("loanId") @Parameter(description = "Loan ID") final Long loanId,
            @Context final UriInfo uriInfo) {

        log.debug("Retrieving KFS documents for loan ID: {}", loanId);

        List<KfsDocumentResponse> documents = kfsDocumentReadPlatformService.retrieveKfsDocumentsByLoanId(loanId);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return Response.ok().entity(toApiJsonSerializer.serialize(settings, documents, null)).build();
    }

    /**
     * Retrieve KFS documents by client ID
     */
    @GET
    @Path("/clients/{clientId}/documents")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve KFS Documents by Client", description = "Retrieves all KFS documents for a specific client")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "KFS documents retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden") })
    public Response getKfsDocumentsByClientId(@PathParam("clientId") @Parameter(description = "Client ID") final Long clientId,
            @Context final UriInfo uriInfo) {

        log.debug("Retrieving KFS documents for client ID: {}", clientId);

        List<KfsDocumentResponse> documents = kfsDocumentReadPlatformService.retrieveKfsDocumentsByClientId(clientId);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return Response.ok().entity(toApiJsonSerializer.serialize(settings, documents, null)).build();
    }

    /**
     * Update a KFS document
     */
    @PUT
    @Path("/{documentId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update KFS Document", description = "Updates an existing KFS document")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "KFS document updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "KFS document not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden") })
    public Response updateKfsDocument(@PathParam("documentId") @Parameter(description = "KFS document ID") final Long documentId,
            @Parameter(description = "KFS document update request") final String apiRequestBodyAsJson) {

        try {
            log.debug("Updating KFS document with ID: {} and request: {}", documentId, apiRequestBodyAsJson);

            // Parse request
            KfsDocumentRequest request = objectMapper.readValue(apiRequestBodyAsJson, KfsDocumentRequest.class);

            // Update document
            CommandProcessingResult result = kfsDocumentWritePlatformService.updateKfsDocument(documentId, request);

            log.info("KFS document updated successfully with ID: {}", documentId);
            return Response.ok().entity(result).build();

        } catch (JsonProcessingException e) {
            log.error("Error parsing KFS document update request", e);
            throw new PlatformApiDataValidationException("Invalid JSON format", "json", apiRequestBodyAsJson);
        } catch (Exception e) {
            log.error("Error updating KFS document with ID: {}", documentId, e);
            throw e;
        }
    }

    /**
     * Delete a KFS document
     */
    @DELETE
    @Path("/{documentId}")
    @Operation(summary = "Delete KFS Document", description = "Deletes an existing KFS document")
    @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "KFS document deleted successfully"),
            @ApiResponse(responseCode = "404", description = "KFS document not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden") })
    public Response deleteKfsDocument(@PathParam("documentId") @Parameter(description = "KFS document ID") final Long documentId) {

        log.debug("Deleting KFS document with ID: {}", documentId);

        CommandProcessingResult result = kfsDocumentWritePlatformService.deleteKfsDocument(documentId);

        log.info("KFS document deleted successfully with ID: {}", documentId);
        return Response.noContent().build();
    }

    /**
     * Download a KFS document file
     */
    @GET
    @Path("/{documentId}/download")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    @Operation(summary = "Download KFS Document", description = "Downloads the KFS document file")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "KFS document downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "KFS document not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden") })
    public ResponseEntity<byte[]> downloadKfsDocument(
            @PathParam("documentId") @Parameter(description = "KFS document ID") final Long documentId) {

        log.debug("Downloading KFS document with ID: {}", documentId);

        try {
            byte[] fileContent = kfsDocumentReadPlatformService.downloadKfsDocument(documentId);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"kfs-document-" + documentId + ".pdf\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

            log.info("KFS document download successful for ID: {}", documentId);

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);

        } catch (PlatformApiDataValidationException e) {
            log.error("Error downloading KFS document with ID: {}", documentId, e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Unexpected error downloading KFS document with ID: {}", documentId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieve KFS documents with filtering options
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve KFS Documents", description = "Retrieves KFS documents with optional filtering by status or date range")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "KFS documents retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden") })
    public Response getKfsDocuments(@QueryParam("status") @Parameter(description = "Filter by document status") final String status,
            @QueryParam("startDate") @Parameter(description = "Filter by start date (YYYY-MM-DD)") final String startDateStr,
            @QueryParam("endDate") @Parameter(description = "Filter by end date (YYYY-MM-DD)") final String endDateStr,
            @Context final UriInfo uriInfo) {

        log.debug("Retrieving KFS documents with filters - status: {}, startDate: {}, endDate: {}", status, startDateStr, endDateStr);

        List<KfsDocumentResponse> documents;

        // Filter by status
        if (status != null && !status.trim().isEmpty()) {
            documents = kfsDocumentReadPlatformService.retrieveKfsDocumentsByStatus(status.trim());
        }
        // Filter by date range
        else if (startDateStr != null && endDateStr != null) {
            try {
                LocalDate startDate = LocalDate.parse(startDateStr);
                LocalDate endDate = LocalDate.parse(endDateStr);
                documents = kfsDocumentReadPlatformService.retrieveKfsDocumentsByDateRange(startDate, endDate);
            } catch (Exception e) {
                log.error("Error parsing date parameters", e);
                throw new PlatformApiDataValidationException("Invalid date format", "date", startDateStr + " - " + endDateStr);
            }
        }
        // No filters - return all (this might need pagination in production)
        else {
            log.warn("Retrieving all KFS documents without filters - consider adding pagination");
            documents = kfsDocumentReadPlatformService.retrieveKfsDocumentsByStatus(null); // Service handles null
                                                                                           // gracefully
        }

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return Response.ok().entity(toApiJsonSerializer.serialize(settings, documents, null)).build();
    }

    /**
     * Update document status
     */
    @PUT
    @Path("/{documentId}/status")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Document Status", description = "Updates the status of a KFS document")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Document status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "KFS document not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden") })
    public Response updateDocumentStatus(@PathParam("documentId") @Parameter(description = "KFS document ID") final Long documentId,
            @Parameter(description = "Status update request") final String apiRequestBodyAsJson) {

        try {
            log.debug("Updating status for KFS document ID: {} with request: {}", documentId, apiRequestBodyAsJson);

            // Parse status from JSON
            @SuppressWarnings("unchecked")
            var requestMap = objectMapper.readValue(apiRequestBodyAsJson, java.util.Map.class);
            String newStatus = (String) requestMap.get("status");

            if (newStatus == null || newStatus.trim().isEmpty()) {
                throw new PlatformApiDataValidationException("Status is required", "status", newStatus);
            }

            // Update status
            CommandProcessingResult result = kfsDocumentWritePlatformService.updateDocumentStatus(documentId, newStatus.trim());

            log.info("Document status updated successfully for ID: {} to status: {}", documentId, newStatus);

            return Response.ok().entity(result).build();

        } catch (JsonProcessingException e) {
            log.error("Error parsing status update request", e);
            throw new PlatformApiDataValidationException("Invalid JSON format", "json", apiRequestBodyAsJson);
        } catch (Exception e) {
            log.error("Error updating document status for ID: {}", documentId, e);
            throw e;
        }
    }

    /**
     * Retrieve KFS document statistics
     */
    @GET
    @Path("/statistics")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get KFS Document Statistics", description = "Retrieves aggregated statistics for KFS documents")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden") })
    public Response getKfsDocumentStatistics(@Context final UriInfo uriInfo) {

        log.debug("Retrieving KFS document statistics");

        KfsDocumentStatistics statistics = kfsDocumentReadPlatformService.retrieveKfsDocumentStatistics();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return Response.ok().entity(toApiJsonSerializer.serialize(settings, statistics, null)).build();
    }
}
