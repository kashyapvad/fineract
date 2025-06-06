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
package org.apache.fineract.accounting.journalentry.exception;

import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a GL Journal Entry is Invalid
 */
public class JournalEntryInvalidException extends AbstractPlatformDomainRuleException {

    /*** enum of reasons for invalid Journal Entry **/
    public enum GlJournalEntryInvalidReason {

        FUTURE_DATE, //
        ACCOUNTING_CLOSED, //
        NO_DEBITS_OR_CREDITS, //
        DEBIT_CREDIT_SUM_MISMATCH_WITH_AMOUNT, //
        DEBIT_CREDIT_SUM_MISMATCH, //
        DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY, //
        GL_ACCOUNT_DISABLED, //
        GL_ACCOUNT_MANUAL_ENTRIES_NOT_PERMITTED, //
        INVALID_DEBIT_OR_CREDIT_ACCOUNTS; //

        public String errorMessage() {
            if (name().equalsIgnoreCase("FUTURE_DATE")) {
                return "The journal entry cannot be made for a future date";
            } else if (name().equalsIgnoreCase("ACCOUNTING_CLOSED")) {
                return "Journal entry cannot be made prior to last account closing date for the branch";
            } else if (name().equalsIgnoreCase("NO_DEBITS_OR_CREDITS")) {
                return "Journal Entry must have at least one Debit and one Credit";
            } else if (name().equalsIgnoreCase("DEBIT_CREDIT_SUM_MISMATCH_WITH_AMOUNT")) {
                return "Sum of All Debits OR Credits must equal the Amount for a Journal Entry";
            } else if (name().equalsIgnoreCase("DEBIT_CREDIT_SUM_MISMATCH")) {
                return "Sum of All Debits must equal the sum of all Credits for a Journal Entry";
            } else if (name().equalsIgnoreCase("DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY")) {
                return "Both account and amount must be specified for all Debits and Credits";
            } else if (name().equalsIgnoreCase("GL_ACCOUNT_DISABLED")) {
                return "Target account has been disabled";
            } else if (name().equalsIgnoreCase("INVALID_DEBIT_OR_CREDIT_ACCOUNTS")) {
                return "Invalid debit or credit accounts are passed";
            } else if (name().equalsIgnoreCase("GL_ACCOUNT_MANUAL_ENTRIES_NOT_PERMITTED")) {
                return "Target account does not allow manual adjustments";
            }
            return name();
        }

        public String errorCode() {
            if (name().equalsIgnoreCase("FUTURE_DATE")) {
                return "error.msg.glJournalEntry.invalid.future.date";
            } else if (name().equalsIgnoreCase("ACCOUNTING_CLOSED")) {
                return "error.msg.glJournalEntry.invalid.accounting.closed";
            } else if (name().equalsIgnoreCase("NO_DEBITS_OR_CREDITS")) {
                return "error.msg.glJournalEntry.invalid.no.debits.or.credits";
            } else if (name().equalsIgnoreCase("DEBIT_CREDIT_SUM_MISMATCH")) {
                return "error.msg.glJournalEntry.invalid.mismatch.debits.credits";
            } else if (name().equalsIgnoreCase("DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY")) {
                return "error.msg.glJournalEntry.invalid.empty.account.or.amount";
            } else if (name().equalsIgnoreCase("GL_ACCOUNT_DISABLED")) {
                return "error.msg.glJournalEntry.invalid.account.disabled";
            } else if (name().equalsIgnoreCase("INVALID_DEBIT_OR_CREDIT_ACCOUNTS")) {
                return "error.msg.glJournalEntry.invalid.debit.or.credit.accounts";
            } else if (name().equalsIgnoreCase("GL_ACCOUNT_MANUAL_ENTRIES_NOT_PERMITTED")) {
                return "error.msg.glJournalEntry.invalid.account.manual.adjustments.not.permitted";
            }
            return name();
        }
    }

    public JournalEntryInvalidException(final GlJournalEntryInvalidReason reason, final LocalDate date, final String accountName,
            final String accountGLCode) {
        super(reason.errorCode(), reason.errorMessage(), date, accountName, accountGLCode);
    }
}
