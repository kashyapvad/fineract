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
package org.apache.fineract.extend.creditbureau.domain;

/**
 * Enumeration representing different types of credit bureau reports.
 *
 * Based on Decentro API capabilities: - FULL_REPORT: Complete credit report with detailed financial history -
 * CREDIT_SCORE: Credit score only report - DATA_PULL: Customer data pull for verification
 */
public enum CreditBureauReportType {

    /**
     * Full credit bureau report with comprehensive financial history. Includes credit score, payment history, account
     * details, and other financial data.
     */
    FULL_REPORT("FULL_REPORT", "Full Credit Report"),

    /**
     * Credit score only report. Provides just the credit score without detailed history.
     */
    CREDIT_SCORE("CREDIT_SCORE", "Credit Score Report"),

    /**
     * Customer data pull for verification purposes. Used to verify customer information against credit bureau
     * databases.
     */
    DATA_PULL("DATA_PULL", "Customer Data Pull"),

    /**
     * Manual credit report entry. Used for manually created credit reports with custom data entry. This type allows
     * users to create credit reports without external provider integration.
     */
    MANUAL_ENTRY("MANUAL_ENTRY", "Manual Credit Report Entry");

    private final String code;
    private final String description;

    CreditBureauReportType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the code representation of the report type.
     *
     * @return the report type code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Returns the human-readable description of the report type.
     *
     * @return the report type description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the CreditBureauReportType enum from the provided code.
     *
     * @param code
     *            the report type code
     * @return the corresponding CreditBureauReportType
     * @throws IllegalArgumentException
     *             if code is not valid
     */
    public static CreditBureauReportType fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (CreditBureauReportType type : CreditBureauReportType.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid credit bureau report type code: " + code);
    }

    /**
     * Checks if this report type includes credit score.
     *
     * @return true if report type includes credit score data
     */
    public boolean includesCreditScore() {
        return this == FULL_REPORT || this == CREDIT_SCORE || this == MANUAL_ENTRY;
    }

    /**
     * Checks if this report type includes detailed financial history.
     *
     * @return true if report type includes detailed financial data
     */
    public boolean includesDetailedHistory() {
        return this == FULL_REPORT;
    }

    /**
     * Checks if this report type is primarily for verification.
     *
     * @return true if report type is for verification purposes
     */
    public boolean isVerificationPurpose() {
        return this == DATA_PULL;
    }

    /**
     * Checks if this report type is manually created.
     *
     * @return true if report type is manually created without external provider
     */
    public boolean isManualEntry() {
        return this == MANUAL_ENTRY;
    }
}
