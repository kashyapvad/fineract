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
package org.apache.fineract.extend.kfs.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for repayment schedule data in KFS documents
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentScheduleData {

    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal totalAmount;
    private BigDecimal outstandingBalance;
    private String formattedEmiAmount;
    private String formattedOutstandingBalance;

    // Constructor for basic data
    public RepaymentScheduleData(Integer installmentNumber, LocalDate dueDate, BigDecimal principalAmount, BigDecimal interestAmount,
            BigDecimal outstandingBalance) {
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.outstandingBalance = outstandingBalance;
        this.totalAmount = principalAmount.add(interestAmount);

        // Format amounts with commas
        this.formattedEmiAmount = formatAmount(totalAmount);
        this.formattedOutstandingBalance = formatAmount(outstandingBalance);
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }
}
