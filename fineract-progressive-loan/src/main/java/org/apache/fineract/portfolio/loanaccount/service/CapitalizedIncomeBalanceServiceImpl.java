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

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;

@Slf4j
@RequiredArgsConstructor
public class CapitalizedIncomeBalanceServiceImpl implements CapitalizedIncomeBalanceService {

    private final LoanCapitalizedIncomeBalanceRepository capitalizedIncomeBalanceRepository;

    @Override
    public Money calculateCapitalizedIncome(Loan loan) {
        BigDecimal balance = capitalizedIncomeBalanceRepository.calculateCapitalizedIncome(loan.getId());
        return Money.of(loan.getCurrency(), balance);
    }

    @Override
    public Money calculateCapitalizedIncomeAdjustment(Loan loan) {
        BigDecimal balance = capitalizedIncomeBalanceRepository.calculateCapitalizedIncomeAdjustment(loan.getId());
        return Money.of(loan.getCurrency(), balance);
    }
}
