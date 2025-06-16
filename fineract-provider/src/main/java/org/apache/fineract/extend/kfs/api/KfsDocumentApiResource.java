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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationRequest;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationResult;
import org.apache.fineract.extend.kfs.dto.KfsDocumentRequest;
import org.apache.fineract.extend.kfs.dto.KfsDocumentResponse;
import org.apache.fineract.extend.kfs.dto.KfsDocumentStatistics;
import org.apache.fineract.extend.kfs.service.KfsDocumentGenerationService;
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
    private final KfsDocumentGenerationService kfsDocumentGenerationService;

    @Autowired
    public KfsDocumentApiResource(final PlatformSecurityContext context,
            final KfsDocumentReadPlatformService kfsDocumentReadPlatformService,
            final KfsDocumentWritePlatformService kfsDocumentWritePlatformService,
            final DefaultToApiJsonSerializer<Object> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, final ObjectMapper objectMapper,
            final KfsDocumentGenerationService kfsDocumentGenerationService) {
        this.context = context;
        this.kfsDocumentReadPlatformService = kfsDocumentReadPlatformService;
        this.kfsDocumentWritePlatformService = kfsDocumentWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.objectMapper = objectMapper;
        this.kfsDocumentGenerationService = kfsDocumentGenerationService;
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
            throw new PlatformApiDataValidationException("Invalid JSON format", "json", apiRequestBodyAsJson, e);
        } catch (Exception e) {
            log.error("Error creating KFS document", e);
            throw new RuntimeException("Error creating KFS document", e);
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
            throw new PlatformApiDataValidationException("Invalid JSON format", "json", apiRequestBodyAsJson, e);
        } catch (Exception e) {
            log.error("Error updating KFS document with ID: {}", documentId, e);
            throw new RuntimeException("Error updating KFS document with ID: " + documentId, e);
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

        kfsDocumentWritePlatformService.deleteKfsDocument(documentId);

        log.info("KFS document deleted successfully with ID: {}", documentId);
        return Response.noContent().build();
    }

    /**
     * Download a KFS document file (supports both docx4j and POI engines)
     */
    @GET
    @Path("/{documentId}/download")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    @Operation(summary = "Download KFS Document", description = "Downloads the KFS document file")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "KFS document downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found") })
    public Response downloadKfsDocument(@PathParam("documentId") @Parameter(description = "Document ID") final Long documentId,
            @QueryParam("engine") @Parameter(description = "Generation engine: docx4j or poi") final String engine) {
        this.context.authenticatedUser();

        try {
            log.info("Download KFS document requested for ID: {} using engine: {}", documentId, engine);

            // Determine which generation engine to use
            boolean usePoi = "poi".equalsIgnoreCase(engine);
            String filename = "kfs_document_" + documentId + ".docx";

            // Use the main generation service (which handles both POI and docx4j internally)
            byte[] documentBytes;
            log.info("Generating KFS document for ID: {} using unified generation service", documentId);

            try {
                // Create request object - assuming documentId corresponds to loanId
                KfsDocumentGenerationRequest request = new KfsDocumentGenerationRequest();
                request.setLoanId(documentId);

                KfsDocumentGenerationResult result = kfsDocumentGenerationService.generateKfsDocument(request, null);
                if (!"SUCCESS".equals(result.getStatus())) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Failed to generate KFS document: " + result.getMessage()).build();
                }
                documentBytes = result.getDocumentContent();
            } catch (Exception e) {
                log.error("Error generating KFS document for ID: {}", documentId, e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error generating document: " + e.getMessage())
                        .build();
            }

            if (documentBytes == null || documentBytes.length == 0) {
                log.warn("Generated document is empty for ID: {}", documentId);
                return Response.status(Response.Status.NOT_FOUND).entity("Document content is empty").build();
            }

            log.info("KFS document generated successfully for ID: {}, size: {} bytes using {} engine", documentId, documentBytes.length,
                    usePoi ? "POI" : "docx4j");

            return Response.ok(documentBytes, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Length", documentBytes.length).build();

        } catch (Exception e) {
            log.error("Error downloading KFS document for ID: {}", documentId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error downloading KFS document: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Generate and preview KFS document using POI
     */
    @GET
    @Path("/{loanId}/generate-poi")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Generate KFS Document using POI", description = "Generates KFS document using Apache POI engine")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "KFS document generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Loan not found") })
    public String generateKfsDocumentWithPoi(@PathParam("loanId") @Parameter(description = "Loan ID") final Long loanId,
            @QueryParam("preview") @Parameter(description = "Generate preview") final Boolean preview) {
        this.context.authenticatedUser();

        try {
            log.info("Generate KFS document requested for loan ID: {}, preview: {}", loanId, preview);

            KfsDocumentGenerationRequest request = new KfsDocumentGenerationRequest();
            request.setLoanId(loanId);
            request.setPreview(Boolean.TRUE.equals(preview));

            KfsDocumentGenerationResult result;
            if (Boolean.TRUE.equals(preview)) {
                result = kfsDocumentGenerationService.previewKfsDocument(request, null);
            } else {
                result = kfsDocumentGenerationService.generateKfsDocument(request, null);
            }

            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("success", "SUCCESS".equals(result.getStatus()));
            response.put("message", result.getMessage());
            response.put("loanId", loanId);
            response.put("documentSizeBytes", result.getDocumentContent() != null ? result.getDocumentContent().length : 0);
            response.put("generationEngine", "Unified Generation Service");
            response.put("preview", Boolean.TRUE.equals(preview));

            if ("SUCCESS".equals(result.getStatus())) {
                log.info("KFS document generated successfully for loan ID: {}, size: {} bytes", loanId,
                        result.getDocumentContent() != null ? result.getDocumentContent().length : 0);
            } else {
                log.warn("KFS document generation failed for loan ID: {}, error: {}", loanId, result.getMessage());
            }

            return this.toApiJsonSerializer.serialize(response);

        } catch (Exception e) {
            log.error("Error generating KFS document with POI for loan ID: {}", loanId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error generating KFS document: " + e.getMessage());
            errorResponse.put("loanId", loanId);
            return this.toApiJsonSerializer.serialize(errorResponse);
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
                throw new PlatformApiDataValidationException("Invalid date format", "date", startDateStr + " - " + endDateStr, e);
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
            throw new PlatformApiDataValidationException("Invalid JSON format", "json", apiRequestBodyAsJson, e);
        } catch (Exception e) {
            log.error("Error updating document status for ID: {}", documentId, e);
            throw new RuntimeException("Error updating document status for ID: " + documentId, e);
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
