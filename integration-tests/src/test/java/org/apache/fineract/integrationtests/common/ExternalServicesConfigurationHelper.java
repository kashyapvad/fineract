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
package org.apache.fineract.integrationtests.common;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;

public class ExternalServicesConfigurationHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public ExternalServicesConfigurationHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static ArrayList<HashMap> getExternalServicesConfigurationByServiceName(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String serviceName) {
        final String GET_EXTERNAL_SERVICES_CONFIG_BY_SERVICE_NAME_URL = "/fineract-provider/api/v1/externalservice/" + serviceName + "?"
                + Utils.TENANT_IDENTIFIER;
        // system.out.println("------------------------ RETRIEVING GLOBAL
        // CONFIGURATION
        // BY ID -------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_EXTERNAL_SERVICES_CONFIG_BY_SERVICE_NAME_URL, "");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static HashMap updateValueForExternaServicesConfiguration(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String serviceName, final String name, final String value) {
        final String EXTERNAL_SERVICES_CONFIG_UPDATE_URL = "/fineract-provider/api/v1/externalservice/" + serviceName + "?"
                + Utils.TENANT_IDENTIFIER;
        // system.out.println("---------------------------------UPDATE VALUE FOR
        // GLOBAL
        // CONFIG---------------------------------------------");
        HashMap map = Utils.performServerPut(requestSpec, responseSpec, EXTERNAL_SERVICES_CONFIG_UPDATE_URL,
                updateExternalServicesConfigUpdateValueAsJSON(name, value), "");

        return (HashMap) map.get("changes");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String updateExternalServicesConfigUpdateValueAsJSON(final String name, final String value) {
        final HashMap<String, String> map = new HashMap<>();
        map.put(name, value);
        // system.out.println("map : " + map);
        return new Gson().toJson(map);
    }

}
