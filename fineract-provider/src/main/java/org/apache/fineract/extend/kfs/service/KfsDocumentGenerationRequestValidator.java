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
package org.apache.fineract.extend.kfs.service;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationRequest;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.springframework.stereotype.Component;

/**
 * Validator for KFS document generation requests. Implements comprehensive validation following Fineract patterns.
 */
@Component
public class KfsDocumentGenerationRequestValidator {

    /**
     * Validate KFS document generation request with comprehensive error collection.
     *
     * @param request
     *            The request to validate
     * @throws PlatformApiDataValidationException
     *             if validation fails
     */
    public void validateGenerationRequest(KfsDocumentGenerationRequest request) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("kfs.document.generation");

        // Required field validation
        baseDataValidator.reset().parameter("loanId").value(request.getLoanId()).notNull().positiveAmount();

        // Optional field validation
        if (request.getClientId() != null) {
            baseDataValidator.reset().parameter("clientId").value(request.getClientId()).positiveAmount();
        }

        if (request.getEirCalculationId() != null) {
            baseDataValidator.reset().parameter("eirCalculationId").value(request.getEirCalculationId()).positiveAmount();
        }

        if (request.getTemplateId() != null) {
            baseDataValidator.reset().parameter("templateId").value(request.getTemplateId()).positiveAmount();
        }

        // String field validation
        if (request.getTemplateName() != null) {
            baseDataValidator.reset().parameter("templateName").value(request.getTemplateName()).notBlank().notExceedingLengthOf(100);
        }

        if (request.getTemplateVersion() != null) {
            baseDataValidator.reset().parameter("templateVersion").value(request.getTemplateVersion()).notBlank().notExceedingLengthOf(20);
        }

        if (request.getDeliveryMethod() != null) {
            baseDataValidator.reset().parameter("deliveryMethod").value(request.getDeliveryMethod()).notBlank().notExceedingLengthOf(50)
                    .isOneOfTheseValues("EMAIL", "SMS", "DOWNLOAD", "PRINT");
        }

        // Customer details validation - only validate if provided and non-empty
        if (request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()) {
            baseDataValidator.reset().parameter("customerName").value(request.getCustomerName()).notBlank().notExceedingLengthOf(200);
        }

        if (request.getCustomerMobile() != null && !request.getCustomerMobile().trim().isEmpty()) {
            baseDataValidator.reset().parameter("customerMobile").value(request.getCustomerMobile()).notBlank().notExceedingLengthOf(20)
                    .matchesRegularExpression("^[+]?[0-9]{10,15}$", "Invalid mobile number format");
        }

        if (request.getCustomerEmail() != null && !request.getCustomerEmail().trim().isEmpty()) {
            baseDataValidator.reset().parameter("customerEmail").value(request.getCustomerEmail()).notBlank().notExceedingLengthOf(100)
                    .matchesRegularExpression("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", "Invalid email format");
        }

        // Amount validation
        if (request.getPrincipalAmount() != null) {
            baseDataValidator.reset().parameter("principalAmount").value(request.getPrincipalAmount()).positiveAmount();
        }

        if (request.getNetDisbursementAmount() != null) {
            baseDataValidator.reset().parameter("netDisbursementAmount").value(request.getNetDisbursementAmount()).positiveAmount();
        }

        if (request.getChargesDueAtDisbursement() != null) {
            baseDataValidator.reset().parameter("chargesDueAtDisbursement").value(request.getChargesDueAtDisbursement())
                    .zeroOrPositiveAmount();
        }

        if (request.getEmiAmount() != null) {
            baseDataValidator.reset().parameter("emiAmount").value(request.getEmiAmount()).positiveAmount();
        }

        if (request.getTenureInMonths() != null) {
            baseDataValidator.reset().parameter("tenureInMonths").value(request.getTenureInMonths()).notNull().integerGreaterThanZero()
                    .inMinMaxRange(1, 360);
        }

        // Business rule validation
        validateBusinessRules(request, baseDataValidator);

        // Throw exception if validation errors found
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    /**
     * Validate business rules for KFS document generation.
     *
     * @param request
     *            The request to validate
     * @param baseDataValidator
     *            The validator builder
     */
    private void validateBusinessRules(KfsDocumentGenerationRequest request, DataValidatorBuilder baseDataValidator) {
        // Template specification validation
        if (request.getTemplateId() == null && request.getTemplateName() == null) {
            // Allow this - will use latest active template
        }

        if (request.getTemplateId() != null && request.getTemplateName() != null) {
            baseDataValidator.reset().parameter("templateId").failWithCode("cannot.specify.both.template.id.and.name",
                    "Cannot specify both templateId and templateName");
        }

        // Amount consistency validation
        if (request.getPrincipalAmount() != null && request.getNetDisbursementAmount() != null
                && request.getChargesDueAtDisbursement() != null) {

            if (!request.getPrincipalAmount().equals(request.getNetDisbursementAmount().add(request.getChargesDueAtDisbursement()))) {
                baseDataValidator.reset().parameter("principalAmount").failWithCode("principal.amount.mismatch",
                        "Principal amount should equal net disbursement amount plus charges");
            }
        }

        // Delivery method specific validation
        if ("EMAIL".equals(request.getDeliveryMethod())
                && (request.getCustomerEmail() == null || request.getCustomerEmail().trim().isEmpty())) {
            baseDataValidator.reset().parameter("customerEmail").failWithCode("email.required.for.email.delivery",
                    "Customer email is required when delivery method is EMAIL");
        }

        if ("SMS".equals(request.getDeliveryMethod())
                && (request.getCustomerMobile() == null || request.getCustomerMobile().trim().isEmpty())) {
            baseDataValidator.reset().parameter("customerMobile").failWithCode("mobile.required.for.sms.delivery",
                    "Customer mobile is required when delivery method is SMS");
        }
    }
}
