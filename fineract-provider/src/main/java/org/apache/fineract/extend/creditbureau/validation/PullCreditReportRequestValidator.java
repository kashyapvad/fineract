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
package org.apache.fineract.extend.creditbureau.validation;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.extend.creditbureau.domain.CreditBureauReportType;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.stereotype.Component;

/**
 * Validator for pull credit report requests.
 *
 * This validator ensures that all required fields are present and valid before processing credit report pull requests.
 */
@Component
@RequiredArgsConstructor
public class PullCreditReportRequestValidator {

    private final FromJsonHelper fromApiJsonHelper;

    private static final Set<String> PULL_REQUEST_PARAMETERS = new HashSet<>(
            Arrays.asList("reportType", "provider", "notes", "panNumber", "aadhaarNumber"));

    private static final String RESOURCE_NAME = "pullCreditReportRequest";

    /**
     * Validates the JSON request for pulling credit reports.
     *
     * @param json
     *            The JSON request string to validate
     * @throws PlatformApiDataValidationException
     *             if validation fails
     */
    public void validateForPull(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, PULL_REQUEST_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        // Validate report type - required field
        final String reportType = this.fromApiJsonHelper.extractStringNamed("reportType", element);
        baseDataValidator.reset().parameter("reportType").value(reportType).notBlank();

        if (StringUtils.isNotBlank(reportType)) {
            try {
                CreditBureauReportType.valueOf(reportType);
            } catch (IllegalArgumentException e) {
                baseDataValidator.reset().parameter("reportType").value(reportType).failWithCode("invalid.report.type",
                        "Report type must be one of: " + Arrays.toString(CreditBureauReportType.values()));
            }
        }

        // Validate provider - optional, defaults to DECENTRO
        final String provider = this.fromApiJsonHelper.extractStringNamed("provider", element);
        if (StringUtils.isNotBlank(provider)) {
            baseDataValidator.reset().parameter("provider").value(provider).isOneOfTheseStringValues("DECENTRO", "SUREPASS", "EXPERIAN",
                    "CIBIL", "CRIF");
        }

        // Validate notes - optional field with length limit
        final String notes = this.fromApiJsonHelper.extractStringNamed("notes", element);
        if (StringUtils.isNotBlank(notes)) {
            baseDataValidator.reset().parameter("notes").value(notes).notExceedingLengthOf(1000);
        }

        // Validate PAN number format if provided
        final String panNumber = this.fromApiJsonHelper.extractStringNamed("panNumber", element);
        if (StringUtils.isNotBlank(panNumber)) {
            baseDataValidator.reset().parameter("panNumber").value(panNumber).matchesRegularExpression("[A-Z]{5}[0-9]{4}[A-Z]{1}",
                    "PAN number must be in format: AAAAA9999A");
        }

        // Validate Aadhaar number format if provided
        final String aadhaarNumber = this.fromApiJsonHelper.extractStringNamed("aadhaarNumber", element);
        if (StringUtils.isNotBlank(aadhaarNumber)) {
            baseDataValidator.reset().parameter("aadhaarNumber").value(aadhaarNumber).matchesRegularExpression("[0-9]{12}",
                    "Aadhaar number must be exactly 12 digits");
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
