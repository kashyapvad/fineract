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
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing all mapped data for KFS document generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KfsDocumentData {

    // Loan Information
    private Long loanId;
    private String loanAccountNumber;
    private String loanProductName;
    private BigDecimal loanAmount;
    private BigDecimal netDisbursalAmount;
    private LocalDate disbursementDate;
    private LocalDate expectedMaturityDate;
    private Integer termInMonths;
    private BigDecimal interestRatePerPeriod;
    private BigDecimal annualInterestRate;
    private String repaymentFrequency;
    private Integer numberOfRepayments;
    private BigDecimal emiAmount;
    private String loanStatus;

    // Client Information
    private Long clientId;
    private String clientName;
    private String clientAccountNumber;
    private String clientMobileNumber;
    private String clientEmailAddress;
    private String clientAddress;
    private LocalDate clientDateOfBirth;
    private String clientGender;

    // Financial Information
    private BigDecimal totalInterestCharges;
    private BigDecimal totalFeeCharges;
    private BigDecimal totalPenaltyCharges;
    private BigDecimal totalCharges;
    private BigDecimal totalRepaymentAmount;
    private BigDecimal effectiveInterestRate;
    private BigDecimal apr;

    // Repayment Schedule
    private List<RepaymentScheduleData> repaymentSchedule;

    // Family Members
    private List<FamilyMemberData> familyMembers;

    // Office Information
    private String officeName;
    private String groupName;
    private LocalDate expectedDisbursementDate;
    private BigDecimal interestRate;
    private String clientMobileNo;

    // Company/Lender Information
    private String companyName;
    private String companyAddress;
    private String companyContactNumber;
    private String companyEmailAddress;
    private String companyWebsite;
    private String companyRegistrationNumber;
    private String companyLicenseNumber;

    // Document Information
    private String documentType;
    private LocalDate generationDate;
    private String templateVersion;
    private String regulatoryCompliance;
}
