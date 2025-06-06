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
package org.apache.fineract.portfolio.loanaccount.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.loanaccount.data.OutstandingAmountsDTO;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleDTO;

public interface LoanTransactionProcessingService {

    boolean canProcessLatestTransactionOnly(Loan loan, LoanTransaction loanTransaction,
            LoanRepaymentScheduleInstallment currentInstallment);

    ChangedTransactionDetail processLatestTransaction(String transactionProcessingStrategyCode, LoanTransaction loanTransaction,
            TransactionCtx ctx);

    ChangedTransactionDetail reprocessLoanTransactions(String transactionProcessingStrategyCode, LocalDate disbursementDate,
            List<LoanTransaction> repaymentsOrWaivers, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Set<LoanCharge> charges);

    LoanRepaymentScheduleTransactionProcessor getTransactionProcessor(String transactionProcessingStrategyCode);

    Optional<ChangedTransactionDetail> processPostDisbursementTransactions(Loan loan);

    LoanScheduleDTO getRecalculatedSchedule(ScheduleGeneratorDTO generatorDTO, Loan loan);

    OutstandingAmountsDTO fetchPrepaymentDetail(ScheduleGeneratorDTO scheduleGeneratorDTO, LocalDate onDate, Loan loan);

}
