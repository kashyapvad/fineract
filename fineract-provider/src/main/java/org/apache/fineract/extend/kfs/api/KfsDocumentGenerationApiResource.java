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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationRequest;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationResponse;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationResult;
import org.apache.fineract.extend.kfs.dto.KfsGenerationStatistics;
import org.apache.fineract.extend.kfs.dto.KfsTemplateInfo;
import org.apache.fineract.extend.kfs.service.KfsDocumentGenerationRequestValidator;
import org.apache.fineract.extend.kfs.service.KfsDocumentGenerationService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * REST API Resource for KFS Document Generation operations.
 *
 * Provides endpoints for generating KFS documents following extend module patterns.
 */
@Path("/v1/extend/kfs/generation")
@Component
@Tag(name = "KFS Document Generation", description = "KFS document generation operations for creating loan documents")
@RequiredArgsConstructor
public class KfsDocumentGenerationApiResource {

    private static final Logger log = LoggerFactory.getLogger(KfsDocumentGenerationApiResource.class);
    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "KFS_DOCUMENT";

    private final PlatformSecurityContext context;
    private final KfsDocumentGenerationService kfsDocumentGenerationService;
    private final KfsDocumentGenerationRequestValidator requestValidator;

    /**
     * Generate a single KFS document.
     *
     * @param request
     *            Document generation request
     * @return Generated document response
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Generate KFS Document", description = "Generate a single KFS document for a loan.\n\n"
            + "This endpoint generates a KFS document with the provided parameters.\n\n" + "Mandatory Fields: loanId\n\n"
            + "Optional Fields: templateId (uses latest active template if not provided), deliveryMethod, preview, customData\n\n"
            + "Example Requests:\n\n" + "kfs/generation\n\n" + "{\n" + "  \"loanId\": 4,\n" + "  \"deliveryMethod\": \"EMAIL\",\n"
            + "  \"preview\": false\n" + "}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = KfsDocumentGenerationRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = KfsDocumentGenerationResponse.class))) })
    public KfsDocumentGenerationResponse generateKfsDocument(@Valid KfsDocumentGenerationRequest request) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        // Validate request using dedicated validator
        requestValidator.validateGenerationRequest(request);

        // Use actual service method that integrates with loan service
        return kfsDocumentGenerationService.generateKfsDocument(request);
    }

    /**
     * Generate multiple KFS documents in bulk.
     *
     * @param loanIds
     *            List of loan IDs to generate documents for
     * @return List of generated document responses
     */
    @POST
    @Path("bulk")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Bulk Generate KFS Documents", description = "Generate multiple KFS documents in bulk.\n\n"
            + "This endpoint generates KFS documents for multiple loans using the latest active template.\n\n"
            + "Mandatory Fields: Array of loan IDs\n\n" + "Example Requests:\n\n" + "kfs/generation/bulk\n\n" + "[\n" + "  4, 5, 6, 7\n"
            + "]")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = List.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = KfsDocumentGenerationResponse.class))) })
    public List<KfsDocumentGenerationResponse> bulkGenerateKfsDocuments(@Parameter(hidden = true) List<Long> loanIds) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        // Use a base request for bulk generation - no templateId to use latest active template
        KfsDocumentGenerationRequest baseRequest = KfsDocumentGenerationRequest.builder().deliveryMethod("EMAIL").build();
        var results = kfsDocumentGenerationService.bulkGenerateKfsDocuments(loanIds, baseRequest);
        return results.stream().map(this::convertToResponse).toList();
    }

    /**
     * Get generation statistics.
     *
     * @return Generation statistics
     */
    @GET
    @Path("statistics")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get Generation Statistics", description = "Retrieve aggregated statistics for KFS document generation.\n\n"
            + "This endpoint returns statistics about document generation including success rates, error counts, and performance metrics.\n\n"
            + "Example Requests:\n\n" + "kfs/generation/statistics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = KfsGenerationStatistics.class))) })
    public KfsGenerationStatistics getGenerationStatistics() {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return kfsDocumentGenerationService.getGenerationStatistics();
    }

    /**
     * Get available KFS templates.
     *
     * @return List of template information
     */
    @GET
    @Path("templates")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get Available KFS Templates", description = "Retrieve list of available KFS document templates.\n\n"
            + "This endpoint returns all active templates that can be used for KFS document generation.\n\n" + "Example Requests:\n\n"
            + "kfs/generation/templates")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Available templates retrieved successfully", content = @Content(schema = @Schema(implementation = KfsTemplateInfo.class))) })
    public List<KfsTemplateInfo> getAvailableTemplates() {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return kfsDocumentGenerationService.getAvailableTemplates();
    }

    /**
     * Get template format for tooltip information.
     *
     * @param loanId
     *            Loan ID (kept for compatibility)
     * @return Template format information (always DOCX now)
     */
    @GET
    @Path("templates/format/{loanId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get Template Format", description = "Retrieve the format of the active KFS template for tooltip display.\n\n"
            + "This endpoint returns DOCX format since KFS is now DOCX-only using docx4j.\n\n" + "Example Requests:\n\n"
            + "kfs/generation/templates/format/123")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Template format retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No active template found") })
    public Response getTemplateFormat(@PathParam("loanId") Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        // KFS is now DOCX-only with docx4j - always return DOCX
        return Response.ok().entity(Map.of("templateFormat", "DOCX")).build();
    }

    /**
     * Upload a KFS template.
     *
     * @param templateName
     *            Template name
     * @param templateVersion
     *            Template version
     * @param description
     *            Template description
     * @param file
     *            Template file
     * @param fileMetaData
     *            File metadata
     * @return Template upload response
     */
    @POST
    @Path("templates")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Upload KFS Template", description = "Upload a new KFS document template.\n\n"
            + "This endpoint uploads a template file and stores it in the KFS template repository.\n\n"
            + "Mandatory Fields: file, templateName\n\n" + "Optional Fields: templateVersion, description\n\n" + "Example Requests:\n\n"
            + "kfs/generation/templates (multipart form)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template uploaded successfully", content = @Content(schema = @Schema(implementation = KfsTemplateInfo.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data") })
    public Response uploadKfsTemplate(@FormDataParam("templateName") String templateName,
            @FormDataParam("templateVersion") String templateVersion, @FormDataParam("description") String description,
            @FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FormDataContentDisposition fileMetaData) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        try {
            // Read file content
            byte[] fileContent = fileInputStream.readAllBytes();

            // Use service method
            KfsTemplateInfo templateInfo = kfsDocumentGenerationService.uploadTemplate(
                    templateName != null ? templateName : fileMetaData.getFileName(), templateVersion != null ? templateVersion : "1.0",
                    description != null ? description : "KFS Template", fileMetaData.getFileName(), fileContent);

            return Response.status(Response.Status.CREATED).entity(templateInfo).build();

        } catch (Exception e) {
            log.error("Error uploading KFS template: {}", e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity("Template upload failed: " + e.getMessage()).build();
        }
    }

    /**
     * Convert KfsDocumentGenerationResult to KfsDocumentGenerationResponse.
     *
     * @param result
     *            The service result to convert
     * @return API response DTO
     */
    private KfsDocumentGenerationResponse convertToResponse(KfsDocumentGenerationResult result) {
        return KfsDocumentGenerationResponse.builder().documentId(result.getDocumentId())
                .documentReferenceNumber(result.getDocumentReferenceNumber()).generationStatus(result.getStatus())
                .generatedFilePath(result.getFilePath()).generationDate(result.getGenerationDate()).fileSize(result.getFileSize())
                .checksum(result.getChecksum()).success(result.getSuccess()).errorMessage(result.getMessage()).preview(result.isPreview())
                .build();
    }
}
