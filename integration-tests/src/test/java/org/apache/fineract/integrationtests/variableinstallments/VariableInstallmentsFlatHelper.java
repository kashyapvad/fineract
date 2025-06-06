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
package org.apache.fineract.integrationtests.variableinstallments;

import com.google.gson.Gson;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class VariableInstallmentsFlatHelper {

    private static final Logger LOG = LoggerFactory.getLogger(VariableInstallmentsFlatHelper.class);

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String createLoanProductWithVaribleConfig(final boolean multiDisburseLoan, final String accountingRule,
            final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1,00,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat() //
                .withTranches(multiDisburseLoan) //
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true)//
                .withVariableInstallmentsConfig(Boolean.TRUE, Integer.valueOf(5), Integer.valueOf(90))//
                .withAccounting(accountingRule, accounts).build(null);
        return loanProductJSON;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String applyForLoanApplication(final Integer clientID, final Integer loanProductID, List<HashMap> charges,
            final String savingsId, String principal, List<HashMap> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualPrincipalPayments() //
                .withInterestTypeAsFlatBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("20 September 2011") //
                .withSubmittedOnDate("20 September 2011") //
                .withCollaterals(collaterals).withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return loanApplicationJSON;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String createDeleteVariations(ArrayList<Map> deletedInstallments) {
        Map<String, Object> toReturn = new HashMap<>();
        toReturn.put("locale", "en");
        toReturn.put("dateFormat", "dd MMMM yyyy");
        Map exceptions = new HashMap<>();
        exceptions.put("deletedinstallments", createDeletedMap(deletedInstallments));
        toReturn.put("exceptions", exceptions);
        String json = new Gson().toJson(toReturn);
        return json;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static ArrayList createDeletedMap(ArrayList<Map> deletedItems) {
        ArrayList toReturn = new ArrayList<>();
        for (Map map : deletedItems) {
            ArrayList dueDate = (ArrayList) map.get("dueDate");
            Map tosend = new HashMap();
            tosend.put("dueDate", formatDate(dueDate));
            toReturn.add(tosend);
        }
        return toReturn;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String createAddVariations() {
        Map<String, Object> toReturn = new HashMap<>();
        toReturn.put("locale", "en");
        toReturn.put("dateFormat", "dd MMMM yyyy");
        Map exceptions = new HashMap<>();
        exceptions.put("newinstallments", createNewInstallments());
        toReturn.put("exceptions", exceptions);
        String json = new Gson().toJson(toReturn);
        return json;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static ArrayList createNewInstallments() {
        ArrayList toReturn = new ArrayList<>();
        Map tosend = new HashMap();
        tosend.put("dueDate", "31 October 2011");
        tosend.put("principal", "5000");
        toReturn.add(tosend);
        return toReturn;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String createModifiyVariations(Map firstSchedule) {
        Map<String, Object> toReturn = new HashMap<>();
        toReturn.put("locale", "en");
        toReturn.put("dateFormat", "dd MMMM yyyy");
        Map exceptions = new HashMap<>();
        exceptions.put("modifiedinstallments", createModifyMap(firstSchedule));
        toReturn.put("exceptions", exceptions);
        String json = new Gson().toJson(toReturn);
        return json;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static ArrayList createNewInstallments(String date) {
        ArrayList toReturn = new ArrayList<>();
        Map tosend = new HashMap();
        tosend.put("dueDate", date);
        tosend.put("principal", "5000");
        toReturn.add(tosend);
        return toReturn;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String createModifiyDateVariations(String[] date, String[] newdate, String[] principal) {
        Map<String, Object> toReturn = new HashMap<>();
        toReturn.put("locale", "en");
        toReturn.put("dateFormat", "dd MMMM yyyy");
        Map exceptions = new HashMap<>();
        exceptions.put("modifiedinstallments", createDateModifyMap(date, newdate, principal));
        toReturn.put("exceptions", exceptions);
        String json = new Gson().toJson(toReturn);
        return json;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static ArrayList createDateModifyMap(String[] date, String[] newdate, String[] principal) {
        ArrayList toReturn = new ArrayList<>();
        for (int i = 0; i < date.length; i++) {
            Map tosend = new HashMap();
            tosend.put("dueDate", date[i]);
            tosend.put("modifiedDueDate", newdate[i]);
            if (i < principal.length) {
                tosend.put("principal", principal[i]);
            }
            toReturn.add(tosend);
        }
        return toReturn;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static ArrayList createModifyMap(Map firstSchedule) {
        ArrayList toReturn = new ArrayList<>();
        ArrayList dueDate = (ArrayList) firstSchedule.get("dueDate");
        Map tosend = new HashMap();
        tosend.put("dueDate", formatDate(dueDate));
        tosend.put("principal", 30000);
        toReturn.add(tosend);
        return toReturn;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String createAllVariations() {
        Map<String, Object> toReturn = new HashMap<>();
        toReturn.put("locale", "en");
        toReturn.put("dateFormat", "dd MMMM yyyy");
        Map exceptions = new HashMap<>();
        exceptions.put("modifiedinstallments", createModifyMap("20 November 2011"));
        exceptions.put("newinstallments", createNewInstallments("25 December 2011"));
        exceptions.put("deletedinstallments", createDeletedMap("20 December 2011"));
        toReturn.put("exceptions", exceptions);
        String json = new Gson().toJson(toReturn);
        return json;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static ArrayList createDeletedMap(String date) {
        ArrayList toReturn = new ArrayList<>();
        Map tosend = new HashMap();
        tosend.put("dueDate", date);
        toReturn.add(tosend);
        return toReturn;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static ArrayList createModifyMap(String date) {
        ArrayList toReturn = new ArrayList<>();
        Map tosend = new HashMap();
        tosend.put("dueDate", date);
        tosend.put("principal", 30000);
        toReturn.add(tosend);
        return toReturn;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String formatDate(ArrayList list) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, (int) list.get(0));
        cal.set(Calendar.MONTH, (int) list.get(1) - 1);
        cal.set(Calendar.DAY_OF_MONTH, (int) list.get(2));
        Date date = cal.getTime();
        DateFormat requiredFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        return requiredFormat.format(date);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Map<String, Object> createModifyVarations() {
        return null;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static ArrayList<Map> constructVerifyData(String[] dates, String[] installments) {
        ArrayList<Map> toReturn = new ArrayList<>();
        for (int i = 0; i < dates.length; i++) {
            Map<String, String> map = new HashMap<>();
            map.put("dueDate", dates[i]);
            map.put("installmentAmount", installments[i]);
            toReturn.add(map);
        }
        return toReturn;
    }
}
