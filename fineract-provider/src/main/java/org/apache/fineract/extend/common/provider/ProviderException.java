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
package org.apache.fineract.extend.common.provider;

/**
 * Exception thrown by credit bureau providers.
 */
public class ProviderException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String providerName;
    private final String errorCode;
    private final boolean retryable;

    public ProviderException(String providerName, String errorCode, String message, boolean retryable) {
        super(message);
        this.providerName = providerName;
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    public ProviderException(String providerName, String errorCode, String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.providerName = providerName;
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
