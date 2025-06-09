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
package org.apache.fineract.extend.commands.service;

import org.apache.fineract.commands.domain.CommandWrapper;
import org.springframework.stereotype.Component;

/**
 * Custom Command Wrapper Builder for Extended KYC Module.
 *
 * This builder creates CommandWrapper objects for custom extend functionality without modifying upstream Apache
 * Fineract core files.
 *
 * FORK-SAFE: This approach avoids conflicts with upstream changes.
 */
@Component
public class ExtendCommandWrapperBuilder {

    /**
     * Creates CommandWrapper for pulling client credit bureau reports.
     */
    public static CommandWrapper pullClientCreditBureauReport(final Long clientId, final String json) {
        return new CommandWrapper(null, // officeId
                null, // groupId
                clientId, // clientId
                null, // loanId
                null, // savingsId
                "PULL", // actionName
                "CLIENT_CREDIT_REPORT", // entityName
                null, // entityId
                null, // subentityId
                "/clients/" + clientId + "/extend/creditreport/pull", // href
                json, // json
                null, // transactionId
                null, // productId
                null, // templateId
                null, // creditBureauId
                null, // organisationCreditBureauId
                null, // jobName
                null, // idempotencyKey
                null, // loanExternalId
                null // sanitizeJsonKeys
        );
    }

    /**
     * Creates CommandWrapper for deleting client credit bureau reports.
     */
    public static CommandWrapper deleteClientCreditBureauReport(final Long clientId, final Long reportId) {
        return new CommandWrapper(null, // officeId
                null, // groupId
                clientId, // clientId
                null, // loanId
                null, // savingsId
                "DELETE", // actionName
                "CLIENT_CREDIT_REPORT", // entityName
                reportId, // entityId
                null, // subentityId
                "/clients/" + clientId + "/extend/creditreport/reports/" + reportId, // href
                "{}", // json
                null, // transactionId
                null, // productId
                null, // templateId
                null, // creditBureauId
                null, // organisationCreditBureauId
                null, // jobName
                null, // idempotencyKey
                null, // loanExternalId
                null // sanitizeJsonKeys
        );
    }

    /**
     * Creates CommandWrapper for API verification of client KYC.
     */
    public static CommandWrapper verifyClientKycApi(final Long clientId, final String json) {
        return new CommandWrapper(null, // officeId
                null, // groupId
                clientId, // clientId
                null, // loanId
                null, // savingsId
                "VERIFY_API", // actionName
                "CLIENT_KYC", // entityName
                null, // entityId
                null, // subentityId
                "/clients/" + clientId + "/extend/kyc/verify", // href
                json, // json
                null, // transactionId
                null, // productId
                null, // templateId
                null, // creditBureauId
                null, // organisationCreditBureauId
                null, // jobName
                null, // idempotencyKey
                null, // loanExternalId
                null // sanitizeJsonKeys
        );
    }

    /**
     * Creates CommandWrapper for manual verification of client KYC.
     */
    public static CommandWrapper verifyClientKycManual(final Long clientId, final String json) {
        return new CommandWrapper(null, // officeId
                null, // groupId
                clientId, // clientId
                null, // loanId
                null, // savingsId
                "VERIFY_MANUAL", // actionName
                "CLIENT_KYC", // entityName
                null, // entityId
                null, // subentityId
                "/clients/" + clientId + "/extend/kyc/verify/manual", // href
                json, // json
                null, // transactionId
                null, // productId
                null, // templateId
                null, // creditBureauId
                null, // organisationCreditBureauId
                null, // jobName
                null, // idempotencyKey
                null, // loanExternalId
                null // sanitizeJsonKeys
        );
    }

    /**
     * Creates CommandWrapper for manual unverification of client KYC.
     */
    public static CommandWrapper unverifyClientKycManual(final Long clientId, final String json) {
        return new CommandWrapper(null, // officeId
                null, // groupId
                clientId, // clientId
                null, // loanId
                null, // savingsId
                "UNVERIFY_MANUAL", // actionName
                "CLIENT_KYC", // entityName
                null, // entityId
                null, // subentityId
                "/clients/" + clientId + "/extend/kyc/unverify/manual", // href
                json, // json
                null, // transactionId
                null, // productId
                null, // templateId
                null, // creditBureauId
                null, // organisationCreditBureauId
                null, // jobName
                null, // idempotencyKey
                null, // loanExternalId
                null // sanitizeJsonKeys
        );
    }

    /**
     * Creates CommandWrapper for creating client KYC details.
     */
    public static CommandWrapper createClientKyc(final Long clientId, final String json) {
        return new CommandWrapper(null, // officeId
                null, // groupId
                clientId, // clientId
                null, // loanId
                null, // savingsId
                "CREATE", // actionName
                "CLIENT_KYC", // entityName
                null, // entityId
                null, // subentityId
                "/clients/" + clientId + "/extend/kyc", // href
                json, // json
                null, // transactionId
                null, // productId
                null, // templateId
                null, // creditBureauId
                null, // organisationCreditBureauId
                null, // jobName
                null, // idempotencyKey
                null, // loanExternalId
                null // sanitizeJsonKeys
        );
    }

    /**
     * Creates CommandWrapper for updating client KYC details.
     */
    public static CommandWrapper updateClientKyc(final Long clientId, final Long kycId, final String json) {
        return new CommandWrapper(null, // officeId
                null, // groupId
                clientId, // clientId
                null, // loanId
                null, // savingsId
                "UPDATE", // actionName
                "CLIENT_KYC", // entityName
                kycId, // entityId
                null, // subentityId
                "/clients/" + clientId + "/extend/kyc/" + kycId, // href
                json, // json
                null, // transactionId
                null, // productId
                null, // templateId
                null, // creditBureauId
                null, // organisationCreditBureauId
                null, // jobName
                null, // idempotencyKey
                null, // loanExternalId
                null // sanitizeJsonKeys
        );
    }

    /**
     * Creates CommandWrapper for deleting client KYC details.
     */
    public static CommandWrapper deleteClientKyc(final Long clientId, final Long kycId) {
        return new CommandWrapper(null, // officeId
                null, // groupId
                clientId, // clientId
                null, // loanId
                null, // savingsId
                "DELETE", // actionName
                "CLIENT_KYC", // entityName
                kycId, // entityId
                null, // subentityId
                "/clients/" + clientId + "/extend/kyc/" + kycId, // href
                "{}", // json
                null, // transactionId
                null, // productId
                null, // templateId
                null, // creditBureauId
                null, // organisationCreditBureauId
                null, // jobName
                null, // idempotencyKey
                null, // loanExternalId
                null // sanitizeJsonKeys
        );
    }

    /**
     * Creates CommandWrapper for creating client credit bureau reports manually.
     */
    public static CommandWrapper createClientCreditBureauReport(final Long clientId, final String json) {
        return new CommandWrapper(null, // officeId
                null, // groupId
                clientId, // clientId
                null, // loanId
                null, // savingsId
                "CREATE", // actionName
                "CLIENT_CREDIT_REPORT", // entityName
                null, // entityId
                null, // subentityId
                "/clients/" + clientId + "/extend/creditreport/create", // href
                json, // json
                null, // transactionId
                null, // productId
                null, // templateId
                null, // creditBureauId
                null, // organisationCreditBureauId
                null, // jobName
                null, // idempotencyKey
                null, // loanExternalId
                null // sanitizeJsonKeys
        );
    }

    /**
     * Creates CommandWrapper for updating client credit bureau reports.
     */
    public static CommandWrapper updateClientCreditBureauReport(final Long clientId, final Long reportId, final String json) {
        return new CommandWrapper(null, // officeId
                null, // groupId
                clientId, // clientId
                null, // loanId
                null, // savingsId
                "UPDATE", // actionName
                "CLIENT_CREDIT_REPORT", // entityName
                reportId, // entityId
                null, // subentityId
                "/clients/" + clientId + "/extend/creditreport/reports/" + reportId, // href
                json, // json
                null, // transactionId
                null, // productId
                null, // templateId
                null, // creditBureauId
                null, // organisationCreditBureauId
                null, // jobName
                null, // idempotencyKey
                null, // loanExternalId
                null // sanitizeJsonKeys
        );
    }
}
