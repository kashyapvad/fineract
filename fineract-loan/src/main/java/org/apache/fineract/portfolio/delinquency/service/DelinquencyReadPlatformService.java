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
package org.apache.fineract.portfolio.delinquency.service;

import java.util.Collection;
import java.util.List;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.data.LoanDelinquencyTagHistoryData;
import org.apache.fineract.portfolio.delinquency.data.LoanInstallmentDelinquencyTagData;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;

public interface DelinquencyReadPlatformService {

    List<DelinquencyRangeData> retrieveAllDelinquencyRanges();

    DelinquencyRangeData retrieveDelinquencyRange(Long delinquencyRangeId);

    List<DelinquencyBucketData> retrieveAllDelinquencyBuckets();

    DelinquencyBucketData retrieveDelinquencyBucket(Long delinquencyBucketId);

    DelinquencyRangeData retrieveCurrentDelinquencyTag(Long loanId);

    Collection<LoanDelinquencyTagHistoryData> retrieveDelinquencyRangeHistory(Long loanId);

    CollectionData calculateLoanCollectionData(Long loanId);

    Collection<LoanInstallmentDelinquencyTagData> retrieveLoanInstallmentsCurrentDelinquencyTag(Long loanId);

    List<LoanDelinquencyAction> retrieveLoanDelinquencyActions(Long loanId);

}
