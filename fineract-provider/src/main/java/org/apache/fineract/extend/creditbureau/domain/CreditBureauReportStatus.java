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
 * Enumeration representing the status of a credit bureau report.
 *
 * This enum tracks the current state of credit bureau API calls: - SUCCESS: Report generated successfully - FAILURE:
 * Report generation failed - PENDING: Report generation in progress
 */
public enum CreditBureauReportStatus {

    /**
     * Report generated successfully. The API call completed and returned valid credit bureau data.
     */
    SUCCESS("SUCCESS", "Report generated successfully"),

    /**
     * Report generation failed. The API call failed due to technical issues, invalid data, or service unavailability.
     */
    FAILURE("FAILURE", "Report generation failed"),

    /**
     * Report generation in progress. The API call has been initiated but not yet completed.
     */
    PENDING("PENDING", "Report generation pending");

    private final String code;
    private final String description;

    CreditBureauReportStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the code representation of the report status.
     *
     * @return the report status code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Returns the human-readable description of the report status.
     *
     * @return the report status description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the CreditBureauReportStatus enum from the provided code.
     *
     * @param code
     *            the report status code
     * @return the corresponding CreditBureauReportStatus
     * @throws IllegalArgumentException
     *             if code is not valid
     */
    public static CreditBureauReportStatus fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (CreditBureauReportStatus status : CreditBureauReportStatus.values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid credit bureau report status code: " + code);
    }

    /**
     * Checks if the report status indicates success.
     *
     * @return true if status is SUCCESS
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * Checks if the report status indicates failure.
     *
     * @return true if status is FAILURE
     */
    public boolean isFailure() {
        return this == FAILURE;
    }

    /**
     * Checks if the report status indicates pending.
     *
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Checks if the report status indicates completion (either success or failure).
     *
     * @return true if status is SUCCESS or FAILURE
     */
    public boolean isCompleted() {
        return this == SUCCESS || this == FAILURE;
    }
}
