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
package org.apache.fineract.extend.kfs.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.extend.kfs.dto.FamilyMemberData;
import org.apache.fineract.extend.kfs.dto.KfsDocumentData;
import org.apache.fineract.extend.kfs.dto.KfsDocumentGenerationRequest;
import org.apache.fineract.extend.kfs.dto.RepaymentScheduleData;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientFamilyMembers;
import org.apache.fineract.portfolio.client.domain.ClientFamilyMembersRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for mapping loan and client data to KFS document format. Extracts real data from database and
 * transforms it for KFS document generation.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KfsDataMappingServiceImpl implements KfsDataMappingService {

    private static final Logger log = LoggerFactory.getLogger(KfsDataMappingServiceImpl.class);

    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ClientFamilyMembersRepository clientFamilyMembersRepository;
    private final LoanReadPlatformService loanReadPlatformService;

    @Override
    public KfsDocumentData mapLoanDataToKfsFormat(KfsDocumentGenerationRequest request) {
        this.context.authenticatedUser();

        validateRequiredDataAvailability(request);

        // Retrieve loan entity with all associations
        Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(request.getLoanId());

        // Retrieve client entity
        Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(loan.getClientId());

        // Retrieve family members for relationship data
        List<FamilyMemberData> familyMembers = getFamilyMembersData(client.getId());

        // Get formatted repayment schedule from loan installments
        List<RepaymentScheduleData> repaymentSchedule = buildRepaymentScheduleFromLoan(loan);

        // Calculate financial metrics
        BigDecimal totalInterestCharges = loan.getSummary().getTotalInterestCharged();
        BigDecimal totalFeeCharges = loan.getSummary().getTotalFeeChargesCharged();
        BigDecimal totalPenaltyCharges = loan.getSummary().getTotalPenaltyChargesCharged();
        BigDecimal totalCharges = totalFeeCharges.add(totalPenaltyCharges);
        BigDecimal totalRepaymentAmount = loan.getSummary().getTotalExpectedRepayment();

        // Calculate APR and EIR
        BigDecimal apr = calculateApr(loan);
        BigDecimal effectiveInterestRate = calculateEffectiveInterestRate(loan);

        return KfsDocumentData.builder()
                // Loan Information
                .loanId(loan.getId()).loanAccountNumber(loan.getAccountNumber()).loanProductName(loan.getLoanProduct().getName())
                .loanAmount(loan.getPrincipal().getAmount()).netDisbursalAmount(loan.getNetDisbursalAmount())
                .disbursementDate(loan.getDisbursementDate()).expectedMaturityDate(loan.getExpectedMaturityDate())
                .termInMonths(calculateTermInMonths(loan))
                .interestRatePerPeriod(loan.getLoanRepaymentScheduleDetail().getNominalInterestRatePerPeriod())
                .annualInterestRate(loan.getLoanRepaymentScheduleDetail().getAnnualNominalInterestRate())
                .repaymentFrequency(getRepaymentFrequencyText(loan)).numberOfRepayments(loan.getNumberOfRepayments())
                .emiAmount(calculateEmiAmount(loan)).loanStatus(loan.getStatus().toString())

                // Client Information
                .clientId(client.getId()).clientName(client.getDisplayName()).clientAccountNumber(client.getAccountNumber())
                .clientMobileNumber(client.getMobileNo()).clientMobileNo(client.getMobileNo()).clientEmailAddress(client.getEmailAddress())
                .clientAddress(buildClientAddress(client)).clientDateOfBirth(client.getDateOfBirth())
                .clientGender(client.getGender() != null ? client.getGender().getLabel() : null)

                // Office Information
                .officeName(loan.getOffice() != null ? loan.getOffice().getName() : "Main Office")
                .groupName(loan.getGroup() != null ? loan.getGroup().getName() : null)
                .expectedDisbursementDate(loan.getExpectedDisbursementDate())
                .interestRate(loan.getLoanRepaymentScheduleDetail().getAnnualNominalInterestRate())

                // Financial Information
                .totalInterestCharges(totalInterestCharges).totalFeeCharges(totalFeeCharges).totalPenaltyCharges(totalPenaltyCharges)
                .totalCharges(totalCharges).totalRepaymentAmount(totalRepaymentAmount).effectiveInterestRate(effectiveInterestRate).apr(apr)

                // Repayment Schedule
                .repaymentSchedule(repaymentSchedule)

                // Family Members
                .familyMembers(familyMembers)

                // Company Information (retrieved from system configuration)
                .companyName(getCompanyName()).companyAddress(getCompanyAddress()).companyContactNumber(getCompanyContactNumber())
                .companyEmailAddress(getCompanyEmailAddress()).companyWebsite(getCompanyWebsite())
                .companyRegistrationNumber(getCompanyRegistrationNumber()).companyLicenseNumber(getCompanyLicenseNumber())

                // Document Information
                .documentType("Key Facts Statement").generationDate(LocalDate.now()).templateVersion("N/A").regulatoryCompliance("N/A")
                .build();
    }

    @Override
    public void validateRequiredDataAvailability(KfsDocumentGenerationRequest request) {
        if (request.getLoanId() == null) {
            throw new PlatformDataIntegrityException("error.msg.kfs.loan.id.required", "Loan ID is required for KFS document generation");
        }

        // Validate loan exists - using findOneWithNotFoundDetection which throws exception if not found
        try {
            this.loanRepositoryWrapper.findOneWithNotFoundDetection(request.getLoanId());
        } catch (Exception e) {
            throw new PlatformDataIntegrityException("error.msg.kfs.loan.not.found", "Loan with ID " + request.getLoanId() + " not found",
                    e);
        }

        // Validate loan is in valid state for KFS generation
        Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(request.getLoanId());
        if (!isLoanValidForKfsGeneration(loan)) {
            throw new PlatformDataIntegrityException("error.msg.kfs.loan.invalid.state",
                    "Loan is not in valid state for KFS document generation. Loan must be approved or active.");
        }
    }

    private boolean isLoanValidForKfsGeneration(Loan loan) {
        return loan.getStatus().isApproved() || loan.getStatus().isActive() || loan.getStatus().isClosedObligationsMet();
    }

    private Integer calculateTermInMonths(Loan loan) {
        if (loan.getLoanRepaymentScheduleDetail().getRepaymentPeriodFrequencyType().isMonthly()) {
            return loan.getTermFrequency();
        } else if (loan.getLoanRepaymentScheduleDetail().getRepaymentPeriodFrequencyType().isWeekly()) {
            return (int) Math.ceil(loan.getTermFrequency() / 4.0);
        } else if (loan.getLoanRepaymentScheduleDetail().getRepaymentPeriodFrequencyType().isDaily()) {
            return (int) Math.ceil(loan.getTermFrequency() / 30.0);
        }
        return loan.getTermFrequency();
    }

    private String getRepaymentFrequencyText(Loan loan) {
        String frequency = loan.getLoanRepaymentScheduleDetail().getRepaymentPeriodFrequencyType().toString();
        Integer every = loan.getLoanRepaymentScheduleDetail().getRepayEvery();

        if (every == 1) {
            return frequency;
        } else {
            return "Every " + every + " " + frequency + "s";
        }
    }

    private BigDecimal calculateEmiAmount(Loan loan) {
        if (loan.getFixedEmiAmount() != null) {
            return loan.getFixedEmiAmount();
        }

        // Calculate average EMI from repayment schedule
        if (loan.getRepaymentScheduleInstallments() != null && !loan.getRepaymentScheduleInstallments().isEmpty()) {
            BigDecimal totalEmi = BigDecimal.ZERO;
            int count = 0;

            for (var installment : loan.getRepaymentScheduleInstallments()) {
                if (installment.getInstallmentNumber() > 0) {
                    totalEmi = totalEmi.add(installment.getTotalOutstanding(loan.getCurrency()).getAmount());
                    count++;
                }
            }

            if (count > 0) {
                return totalEmi.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            }
        }

        return BigDecimal.ZERO;
    }

    private String buildClientAddress(Client client) {
        StringBuilder address = new StringBuilder();

        // Build address from client address if available
        if (client.getOffice() != null) {
            address.append(client.getOffice().getName());
        }

        return address.length() > 0 ? address.toString() : "N/A";
    }

    private BigDecimal calculateApr(Loan loan) {
        // Calculate APR based on total cost of loan
        BigDecimal principal = loan.getPrincipal().getAmount();
        BigDecimal totalCost = loan.getSummary().getTotalExpectedCostOfLoan();
        BigDecimal termInYears = BigDecimal.valueOf(calculateTermInMonths(loan)).divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);

        if (principal.compareTo(BigDecimal.ZERO) > 0 && termInYears.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal apr = totalCost.subtract(principal).divide(principal, 4, RoundingMode.HALF_UP)
                    .divide(termInYears, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            return apr.setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calculateEffectiveInterestRate(Loan loan) {
        // For now, return the nominal rate - can be enhanced with complex EIR calculation
        return loan.getLoanRepaymentScheduleDetail().getAnnualNominalInterestRate();
    }

    // Company information methods - these should be retrieved from system configuration
    private String getCompanyName() {
        return "N/A";
    }

    private String getCompanyAddress() {
        return "N/A";
    }

    private String getCompanyContactNumber() {
        return "N/A";
    }

    private String getCompanyEmailAddress() {
        return "N/A";
    }

    private String getCompanyWebsite() {
        return "N/A";
    }

    private String getCompanyRegistrationNumber() {
        return "N/A";
    }

    private String getCompanyLicenseNumber() {
        return "N/A";
    }

    private List<RepaymentScheduleData> buildRepaymentScheduleFromLoan(Loan loan) {
        return loan.getRepaymentScheduleInstallments().stream().filter(installment -> !installment.isRecalculatedInterestComponent())
                .map(installment -> RepaymentScheduleData.builder().installmentNumber(installment.getInstallmentNumber())
                        .dueDate(installment.getDueDate()).principalAmount(installment.getPrincipal(loan.getCurrency()).getAmount())
                        .interestAmount(installment.getInterestCharged(loan.getCurrency()).getAmount())
                        .totalAmount(installment.getTotalOutstanding(loan.getCurrency()).getAmount())
                        .outstandingBalance(calculateOutstandingBalance(loan, installment.getInstallmentNumber())).build())
                .toList();
    }

    private BigDecimal calculateOutstandingBalance(Loan loan, Integer installmentNumber) {
        BigDecimal totalPrincipal = loan.getPrincipal().getAmount();
        BigDecimal paidPrincipal = loan.getRepaymentScheduleInstallments().stream()
                .filter(inst -> inst.getInstallmentNumber() <= installmentNumber)
                .map(inst -> inst.getPrincipal(loan.getCurrency()).getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalPrincipal.subtract(paidPrincipal);
    }

    /**
     * Retrieve and map family members data for the client
     */
    private List<FamilyMemberData> getFamilyMembersData(Long clientId) {
        try {
            List<ClientFamilyMembers> familyMembers = this.clientFamilyMembersRepository.findAll().stream()
                    .filter(fm -> fm.getClient().getId().equals(clientId)).collect(Collectors.toList());

            return familyMembers.stream().map(this::mapToFamilyMemberData).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not retrieve family members for client {}: {}", clientId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Map ClientFamilyMembers entity to FamilyMemberData DTO
     */
    private FamilyMemberData mapToFamilyMemberData(ClientFamilyMembers familyMember) {
        return FamilyMemberData.builder().id(familyMember.getId()).firstName(familyMember.getFirstName())
                .middleName(familyMember.getMiddleName()).lastName(familyMember.getLastName())
                .relationship(familyMember.getRelationship() != null ? familyMember.getRelationship().getLabel() : null)
                .gender(familyMember.getGender() != null ? familyMember.getGender().getLabel() : null)
                .dateOfBirth(familyMember.getDateOfBirth()).age(familyMember.getAge()).qualification(familyMember.getQualification())
                .profession(familyMember.getProfession() != null ? familyMember.getProfession().getLabel() : null)
                .mobileNumber(familyMember.getMobileNumber()).isDependent(familyMember.getIsDependent())
                .maritalStatus(familyMember.getMaritalStatus() != null ? familyMember.getMaritalStatus().getLabel() : null).build();
    }
}
