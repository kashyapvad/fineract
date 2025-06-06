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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanOriginationValidationTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(AdvancedPaymentAllocationLoanRepaymentScheduleTest.class);
    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static BusinessDateHelper businessDateHelper;
    private static LoanTransactionHelper loanTransactionHelper;
    private static AccountHelper accountHelper;
    private static Integer commonLoanProductId;
    private static PostClientsResponse client;
    private static LoanRescheduleRequestHelper loanRescheduleRequestHelper;
    private static ChargesHelper chargesHelper;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        businessDateHelper = new BusinessDateHelper();
        accountHelper = new AccountHelper(requestSpec, responseSpec);
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(requestSpec, responseSpec);
        chargesHelper = new ChargesHelper();

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        commonLoanProductId = createLoanProduct("500", "15", "4", true, "25", true, LoanScheduleType.PROGRESSIVE,
                LoanScheduleProcessingType.HORIZONTAL, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
    }

    // uc1: Negative Test: Loan approval transaction without approvedOnDate parameter
    // 1. Create a Loan product
    // 2. Submit a Loan application
    // 3. Try to Approve a Loan account without approvedOnDate parameter to catch The request was invalid error due
    // missed required attribute
    @Test
    public void uc1() {
        String operationDate = "15 August 2024";
        runAt(operationDate, () -> {

            LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
            PostLoanProductsResponse loanProductResponse = loanProductHelper
                    .createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                            .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()));
            PostLoansRequest applicationRequest = applyLoanRequest(client.getClientId(), loanProductResponse.getResourceId(), operationDate,
                    100.0, 5);

            applicationRequest = applicationRequest.numberOfRepayments(6)//
                    .loanTermFrequency(6)//
                    .loanTermFrequencyType(2)//
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                    .repaymentEvery(1)//
                    .repaymentFrequencyType(2)//
                    .maxOutstandingLoanBalance(BigDecimal.valueOf(10000.0))//
            ;//

            PostLoansResponse loanResponse = loanTransactionHelper.applyLoan(applicationRequest);

            CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.approveLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest()
                            .approvedLoanAmount(BigDecimal.valueOf(100)).dateFormat(DATETIME_PATTERN).approvedOnDate(null).locale("en")));

            Assertions.assertTrue(callFailedRuntimeException.getMessage().contains("The parameter `approvedOnDate` is mandatory."));
        });
    }

    // uc2: Negative Test: Loan disbursement transaction without actualDisbursementDate parameter
    // 1. Create a Loan product
    // 2. Submit and Approve Loan application
    // 3. Try to Disburse a Loan account without actualDisbursementDate parameter to catch The request was invalid error
    // due missed required attribute
    @Test
    public void uc2() {
        String operationDate = "15 August 2024";
        runAt(operationDate, () -> {

            LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
            PostLoanProductsResponse loanProductResponse = loanProductHelper
                    .createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                            .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()));
            PostLoansRequest applicationRequest = applyLoanRequest(client.getClientId(), loanProductResponse.getResourceId(), operationDate,
                    100.0, 5);

            applicationRequest = applicationRequest.numberOfRepayments(6)//
                    .loanTermFrequency(6)//
                    .loanTermFrequencyType(2)//
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                    .repaymentEvery(1)//
                    .repaymentFrequencyType(2)//
                    .maxOutstandingLoanBalance(BigDecimal.valueOf(10000.0))//
            ;//

            PostLoansResponse loanResponse = loanTransactionHelper.applyLoan(applicationRequest);

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest()
                    .approvedLoanAmount(BigDecimal.valueOf(100)).dateFormat(DATETIME_PATTERN).approvedOnDate(operationDate).locale("en"));

            CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                            new PostLoansLoanIdRequest().actualDisbursementDate(null).dateFormat(DATETIME_PATTERN)
                                    .transactionAmount(BigDecimal.valueOf(100.0)).locale("en")));

            Assertions.assertTrue(callFailedRuntimeException.getMessage().contains("The parameter `actualDisbursementDate` is mandatory."));
        });
    }

    // uc3: Negative Test: Loan application without required parameters
    // 1. Create a Loan product
    // 2. Submit Loan application without required parameters
    @Test
    public void uc3() {
        String operationDate = "15 August 2024";
        runAt(operationDate, () -> {

            LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
            PostLoanProductsResponse loanProductResponse = loanProductHelper
                    .createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                            .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()));
            // Product Id null
            final PostLoansRequest applicationRequest01 = applyLoanRequest(client.getClientId(), null, operationDate, 100.0, 5)
                    .numberOfRepayments(6)//
                    .loanTermFrequency(6)//
                    .loanTermFrequencyType(2)//
                    .repaymentEvery(1)//
                    .repaymentFrequencyType(2)//
            ;//
            CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.applyLoan(applicationRequest01));
            assertEquals(400, callFailedRuntimeException.getResponse().code());
            Assertions.assertTrue(callFailedRuntimeException.getMessage().contains("The parameter `productId` is mandatory."));

            // Transaction Processing Strategy Code null
            final PostLoansRequest applicationRequest02 = applyLoanRequest(client.getClientId(), loanProductResponse.getResourceId(),
                    operationDate, 100.0, 5).numberOfRepayments(6)//
                    .loanTermFrequency(6)//
                    .loanTermFrequencyType(2)//
                    .transactionProcessingStrategyCode(null)//
                    .repaymentEvery(1)//
                    .repaymentFrequencyType(2)//
            ;//

            callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.applyLoan(applicationRequest02));
            assertEquals(400, callFailedRuntimeException.getResponse().code());
            Assertions.assertTrue(
                    callFailedRuntimeException.getMessage().contains("The parameter `transactionProcessingStrategyCode` is mandatory."));

            final PostLoansRequest applicationRequest03 = applyLoanRequest(null, loanProductResponse.getResourceId(), operationDate, 100.0,
                    5).numberOfRepayments(6)//
                    .loanTermFrequency(6)//
                    .loanTermFrequencyType(2)//
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                    .repaymentEvery(1)//
                    .repaymentFrequencyType(2)//
            ;//
            callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.applyLoan(applicationRequest03));
            LOG.info("DETAIL: {}", callFailedRuntimeException.getMessage());
            assertEquals(400, callFailedRuntimeException.getResponse().code());
            Assertions.assertTrue(callFailedRuntimeException.getMessage().contains("The parameter `clientId` is mandatory."));

            // Submitted Date null
            final PostLoansRequest applicationRequest04 = applyLoanRequest(client.getClientId(), loanProductResponse.getResourceId(),
                    operationDate, 100.0, 5).numberOfRepayments(6)//
                    .submittedOnDate(null) //
                    .loanTermFrequency(6)//
                    .loanTermFrequencyType(2)//
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                    .repaymentEvery(1)//
                    .repaymentFrequencyType(2)//
            ;//
            callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.applyLoan(applicationRequest04));
            assertEquals(400, callFailedRuntimeException.getResponse().code());
            Assertions.assertTrue(callFailedRuntimeException.getMessage().contains("The parameter `submittedOnDate` is mandatory."));

            // Expected disbursement Date null
            final PostLoansRequest applicationRequest05 = applyLoanRequest(client.getClientId(), loanProductResponse.getResourceId(),
                    operationDate, 100.0, 5).numberOfRepayments(6)//
                    .expectedDisbursementDate(null) //
                    .loanTermFrequency(6)//
                    .loanTermFrequencyType(2)//
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                    .repaymentEvery(1)//
                    .repaymentFrequencyType(2)//
            ;//
            callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.applyLoan(applicationRequest05));
            assertEquals(400, callFailedRuntimeException.getResponse().code());
            Assertions
                    .assertTrue(callFailedRuntimeException.getMessage().contains("The parameter `expectedDisbursementDate` is mandatory."));
        });
    }

    private static Integer createLoanProduct(final String principal, final String repaymentAfterEvery, final String numberOfRepayments,
            boolean downPaymentEnabled, String downPaymentPercentage, boolean autoPayForDownPayment, LoanScheduleType loanScheduleType,
            LoanScheduleProcessingType loanScheduleProcessingType, final Account... accounts) {
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        AdvancedPaymentData goodwillCreditAllocation = createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT");
        AdvancedPaymentData merchantIssuedRefundAllocation = createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION");
        AdvancedPaymentData payoutRefundAllocation = createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT");
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withMinPrincipal(principal).withPrincipal(principal)
                .withRepaymentTypeAsDays().withRepaymentAfterEvery(repaymentAfterEvery).withNumberOfRepayments(numberOfRepayments)
                .withEnableDownPayment(downPaymentEnabled, downPaymentPercentage, autoPayForDownPayment).withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths()
                .withRepaymentStrategy(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .addAdvancedPaymentAllocation(defaultAllocation, goodwillCreditAllocation, merchantIssuedRefundAllocation,
                        payoutRefundAllocation)
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withInterestTypeAsDecliningBalance().withMultiDisburse()
                .withDisallowExpectedDisbursements(true).withLoanScheduleType(loanScheduleType)
                .withLoanScheduleProcessingType(loanScheduleProcessingType).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").build(null);
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

}
