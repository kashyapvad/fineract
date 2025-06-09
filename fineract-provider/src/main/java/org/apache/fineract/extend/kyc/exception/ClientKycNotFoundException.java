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
package org.apache.fineract.extend.kyc.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when client KYC details are not found.
 */
public class ClientKycNotFoundException extends AbstractPlatformResourceNotFoundException {

    private static final String ERROR_MESSAGE_CODE = "error.msg.client.kyc.not.found";

    public ClientKycNotFoundException(Long clientId) {
        super(ERROR_MESSAGE_CODE, "Client KYC details with client identifier " + clientId + " does not exist", clientId);
    }

    public ClientKycNotFoundException(Long kycId, boolean byKycId) {
        super(ERROR_MESSAGE_CODE, "Client KYC details with identifier " + kycId + " does not exist", kycId);
    }
}
