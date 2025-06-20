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
package org.apache.fineract.infrastructure.survey.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.survey.data.ClientScoresOverview;
import org.apache.fineract.infrastructure.survey.data.SurveyData;
import org.apache.fineract.infrastructure.survey.data.SurveyDataTableData;
import org.apache.fineract.infrastructure.survey.service.ReadSurveyService;
import org.springframework.stereotype.Component;

/**
 * Created by Cieyou on 2/27/14.
 */
@Path("/v1/survey")
@Component
@Tag(name = "Survey", description = "")
@RequiredArgsConstructor
public class SurveyApiResource {

    private final DefaultToApiJsonSerializer<SurveyData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<ClientScoresOverview> toApiJsonClientScoreOverviewSerializer;
    private final PlatformSecurityContext context;
    private final ReadSurveyService readSurveyService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final GenericDataService genericDataService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve surveys", description = "Retrieve surveys. This allows to retrieve the list of survey tables registered .")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SurveyApiResourceSwagger.GetSurveyResponse.class)))) })
    public String retrieveSurveys() {

        this.context.authenticatedUser().validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);

        List<SurveyDataTableData> surveys = this.readSurveyService.retrieveAllSurveys();
        return this.toApiJsonSerializer.serialize(surveys);
    }

    @GET
    @Path("{surveyName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve survey", description = "Lists a registered survey table details and the Apache Fineract Core application table they are registered to.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SurveyApiResourceSwagger.GetSurveyResponse.class))) })
    public String retrieveSurvey(@PathParam("surveyName") @Parameter(description = "surveyName") final String surveyName) {

        this.context.authenticatedUser().validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);

        SurveyDataTableData surveys = this.readSurveyService.retrieveSurvey(surveyName);

        return this.toApiJsonSerializer.serialize(surveys);

    }

    @POST
    @Path("{surveyName}/{apptableId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an entry in the survey table", description = "Insert and entry in a survey table (full fill the survey)."
            + "\n" + "\n" + "Refer Link for sample Body:  [ https://fineract.apache.org/docs/legacy/#survey_create ] ")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SurveyApiResourceSwagger.PostSurveySurveyNameApptableIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SurveyApiResourceSwagger.PostSurveySurveyNameApptableIdResponse.class))) })
    public String createDatatableEntry(@PathParam("surveyName") @Parameter(description = "surveyName") final String datatable,
            @PathParam("apptableId") @Parameter(description = "apptableId") final Long apptableId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .fullFilSurvey(datatable, apptableId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    /** FIXME Vishwas what does this API really do? ***/
    @GET
    @Path("{surveyName}/{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getClientSurveyOverview(@PathParam("surveyName") final String surveyName, @PathParam("clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);

        List<ClientScoresOverview> scores = this.readSurveyService.retrieveClientSurveyScoreOverview(clientId);

        return this.toApiJsonClientScoreOverviewSerializer.serialize(scores);
    }

    @GET
    @Path("{surveyName}/{clientId}/{entryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getSurveyEntry(@PathParam("surveyName") final String surveyName, @PathParam("clientId") final Long clientId,
            @PathParam("entryId") final Long entryId) {

        this.context.authenticatedUser().validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);

        final GenericResultsetData results = this.readSurveyService.retrieveSurveyEntry(surveyName, clientId, entryId);

        return this.genericDataService.generateJsonFromGenericResultsetData(results);

    }

    @PUT
    @Path("register/{surveyName}/{apptable}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String register(@PathParam("surveyName") final String datatable, @PathParam("apptable") final String apptable,
            final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().registerSurvey(datatable, apptable).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @DELETE
    @Path("{surveyName}/{clientId}/{fulfilledId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteDatatableEntries(@PathParam("surveyName") final String surveyName, @PathParam("clientId") final Long clientId,
            @PathParam("fulfilledId") final Long fulfilledId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteDatatableEntry(surveyName, clientId, fulfilledId) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
