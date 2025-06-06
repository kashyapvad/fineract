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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unused", "rawtypes" })
public class HolidayHelper {

    private static final Logger LOG = LoggerFactory.getLogger(HolidayHelper.class);
    private static final String HOLIDAYS_URL = "/fineract-provider/api/v1/holidays";
    private static final String CREATE_HOLIDAY_URL = HOLIDAYS_URL + "?" + Utils.TENANT_IDENTIFIER;

    private static final String OFFICE_ID = "1";

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HolidayHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getCreateHolidayDataAsJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        List<HashMap<String, String>> offices = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> officeMap = new HashMap<>();
        officeMap.put("officeId", OFFICE_ID);
        offices.add(officeMap);

        map.put("offices", offices);
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("name", Utils.uniqueRandomStringGenerator("HOLIDAY_", 5));
        map.put("fromDate", "01 April 2013");
        map.put("toDate", "01 April 2013");
        map.put("repaymentsRescheduledTo", "08 April 2013");
        map.put("reschedulingType", 2);
        String HolidayCreateJson = new Gson().toJson(map);
        LOG.info("{}", HolidayCreateJson);
        return HolidayCreateJson;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getCreateType1HolidayDataAsJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        List<HashMap<String, String>> offices = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> officeMap = new HashMap<>();
        officeMap.put("officeId", OFFICE_ID);
        offices.add(officeMap);

        map.put("offices", offices);
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("name", Utils.uniqueRandomStringGenerator("HOLIDAY_", 5));
        map.put("fromDate", "04 April 2024");
        map.put("toDate", "04 April 2024");
        map.put("reschedulingType", 1);
        String HolidayCreateJson = new Gson().toJson(map);
        LOG.info("{}", HolidayCreateJson);
        return HolidayCreateJson;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getActivateHolidayDataAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        String activateHoliday = new Gson().toJson(map);
        LOG.info("{}", activateHoliday);
        return activateHoliday;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createHolidays(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_HOLIDAY_URL, getCreateHolidayDataAsJSON(), "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createTyoe1Holidays(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_HOLIDAY_URL, getCreateType1HolidayDataAsJSON(), "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer activateHolidays(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String holidayID) {
        final String ACTIVATE_HOLIDAY_URL = HOLIDAYS_URL + "/" + holidayID + "?command=activate&" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, ACTIVATE_HOLIDAY_URL, getActivateHolidayDataAsJSON(), "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static HashMap getHolidayById(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String holidayID) {
        final String GET_HOLIDAY_BY_ID_URL = HOLIDAYS_URL + "/" + holidayID + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING HOLIDAY BY ID -------------------------");
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, GET_HOLIDAY_BY_ID_URL, "");
        return response;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer deleteHoliday(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer holidayID) {
        final String DELETE_HOLIDAY_URL = HOLIDAYS_URL + "/" + holidayID + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerDelete(requestSpec, responseSpec, DELETE_HOLIDAY_URL, "{}", "resourceId");
    }

}
