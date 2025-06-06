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

import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.data.TransactionChangeData;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;

@RequiredArgsConstructor
public class ReplayedTransactionBusinessEventServiceImpl implements ReplayedTransactionBusinessEventService {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanTransactionRepository loanTransactionRepository;

    @Override
    public void raiseTransactionReplayedEvents(final ChangedTransactionDetail changedTransactionDetail) {
        if (changedTransactionDetail == null || changedTransactionDetail.getTransactionChanges().isEmpty()) {
            return;
        }
        // Extra safety net to avoid event leaking
        try {
            businessEventNotifierService.startExternalEventRecording();

            for (TransactionChangeData change : changedTransactionDetail.getTransactionChanges()) {
                final LoanTransaction newTransaction = change.getNewTransaction();
                final LoanTransaction oldTransaction = change.getOldTransaction();

                if (oldTransaction != null) {
                    final LoanAdjustTransactionBusinessEvent.Data data = new LoanAdjustTransactionBusinessEvent.Data(oldTransaction);
                    data.setNewTransactionDetail(newTransaction);
                    businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(data));
                }
            }
            businessEventNotifierService.stopExternalEventRecording();
        } catch (Exception e) {
            businessEventNotifierService.resetEventRecording();
            throw e;
        }
    }
}
