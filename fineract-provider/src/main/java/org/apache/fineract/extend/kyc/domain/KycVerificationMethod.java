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
package org.apache.fineract.extend.kyc.domain;

/**
 * Enumeration representing the method used for KYC verification.
 *
 * This enum defines how the client's KYC documents were verified: - API: Verified through automated credit bureau or
 * KYC API calls - MANUAL: Manually verified by a user/staff member
 */
public enum KycVerificationMethod {

    /**
     * Verification performed through API calls to credit bureau or KYC services. This is automated verification using
     * external services like Decentro.
     */
    API("API", "Verified through API"),

    /**
     * Verification performed manually by a staff member or authorized user. This involves human verification of
     * documents and details.
     */
    MANUAL("MANUAL", "Verified manually");

    private final String code;
    private final String description;

    KycVerificationMethod(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the code representation of the verification method.
     *
     * @return the verification method code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Returns the human-readable description of the verification method.
     *
     * @return the verification method description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the KycVerificationMethod enum from the provided code.
     *
     * @param code
     *            the verification method code
     * @return the corresponding KycVerificationMethod
     * @throws IllegalArgumentException
     *             if code is not valid
     */
    public static KycVerificationMethod fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (KycVerificationMethod method : KycVerificationMethod.values()) {
            if (method.getCode().equalsIgnoreCase(code)) {
                return method;
            }
        }

        throw new IllegalArgumentException("Invalid KYC verification method code: " + code);
    }

    /**
     * Checks if the verification method is API-based.
     *
     * @return true if verification method is API
     */
    public boolean isApiVerification() {
        return this == API;
    }

    /**
     * Checks if the verification method is manual.
     *
     * @return true if verification method is MANUAL
     */
    public boolean isManualVerification() {
        return this == MANUAL;
    }
}
