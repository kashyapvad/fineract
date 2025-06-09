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
package org.apache.fineract.extend.common.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.extend.common.dto.CreditBureauProviderRequest;
import org.apache.fineract.extend.common.dto.CreditBureauProviderResponse;
import org.apache.fineract.extend.common.dto.CustomerDataProviderRequest;
import org.apache.fineract.extend.common.dto.CustomerDataProviderResponse;
import org.apache.fineract.extend.common.provider.CreditBureauProvider;
import org.apache.fineract.extend.common.provider.CreditBureauProviderFactory;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.springframework.stereotype.Service;

/**
 * Common service for handling external provider operations across extend modules.
 * 
 * This service extracts common provider logic to reduce duplication between
 * KYC and Credit Bureau services while maintaining clean separation of concerns.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExtendProviderService {

    private final Optional<CreditBureauProviderFactory> providerFactory;

    /**
     * Validates that a provider is available and configured.
     * 
     * @throws PlatformServiceUnavailableException if provider is not available
     */
    public void validateProviderAvailable() {
        if (!isProviderAvailable()) {
            throw new PlatformServiceUnavailableException(
                "error.msg.creditbureau.provider.not.available",
                "Credit bureau provider is not configured or unavailable. Please contact system administrator to configure external credit bureau services.",
                "provider", "not_configured"
            );
        }
    }

    /**
     * Checks if a provider is available without throwing exceptions.
     * 
     * @return true if provider is configured and available
     */
    public boolean isProviderAvailable() {
        return providerFactory.isPresent() && providerFactory.get().isProviderAvailable();
    }

    /**
     * Gets the configured provider name safely.
     * 
     * @return provider name if available
     * @throws PlatformServiceUnavailableException if provider not available
     */
    public String getProviderName() {
        validateProviderAvailable();
        return providerFactory.get().getConfiguredProviderName();
    }

    /**
     * Gets the provider instance safely.
     * 
     * @return provider instance
     * @throws PlatformServiceUnavailableException if provider not available
     */
    public CreditBureauProvider getProvider() {
        validateProviderAvailable();
        return providerFactory.get().getProvider();
    }

    /**
     * Executes customer data pull with proper error handling.
     * 
     * @param request the customer data request
     * @return provider response
     * @throws RuntimeException if the operation fails
     */
    public CustomerDataProviderResponse pullCustomerData(CustomerDataProviderRequest request) {
        try {
            log.debug("Executing customer data pull for client: {}", request.getClientId());
            
            final CustomerDataProviderResponse response = getProvider().pullCustomerData(request);
            
            if (!response.isSuccess()) {
                log.warn("Customer data pull failed for client {}: {}", request.getClientId(), response.getMessage());
                throw new RuntimeException("Customer data pull failed: " + response.getMessage());
            }
            
            log.info("Successfully pulled customer data for client: {}", request.getClientId());
            return response;
            
        } catch (Exception e) {
            log.error("Error during customer data pull for client {}: {}", request.getClientId(), e.getMessage(), e);
            throw new RuntimeException("Customer data pull failed: " + e.getMessage(), e);
        }
    }

    /**
     * Executes credit report generation with proper error handling.
     * 
     * @param request the credit bureau request
     * @return provider response
     * @throws RuntimeException if the operation fails
     */
    public CreditBureauProviderResponse generateCreditReport(CreditBureauProviderRequest request) {
        try {
            log.debug("Executing credit report generation for client: {}", request.getClientId());
            
            final CreditBureauProviderResponse response = getProvider().generateCreditReport(request);
            
            if (!response.isSuccess()) {
                log.warn("Credit report generation failed for client {}: {}", request.getClientId(), response.getMessage());
                throw new RuntimeException("Credit report generation failed: " + response.getMessage());
            }
            
            log.info("Successfully generated credit report for client: {}", request.getClientId());
            return response;
            
        } catch (Exception e) {
            log.error("Error during credit report generation for client {}: {}", request.getClientId(), e.getMessage(), e);
            throw new RuntimeException("Credit report generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a standardized reference ID for provider requests.
     * 
     * @param prefix operation prefix (e.g., "KYC", "CREDIT_REPORT")
     * @param clientId client identifier
     * @return formatted reference ID
     */
    public String createReferenceId(String prefix, Long clientId) {
        return String.format("FINERACT_%s_%d_%d", prefix, clientId, System.currentTimeMillis());
    }

    /**
     * Validates provider response and throws appropriate exceptions.
     * 
     * @param response provider response to validate
     * @param operation operation name for error messages
     * @throws RuntimeException if response indicates failure
     */
    public void validateProviderResponse(Object response, String operation) {
        if (response instanceof CustomerDataProviderResponse customerResponse) {
            if (!customerResponse.isSuccess()) {
                throw new RuntimeException(operation + " failed: " + customerResponse.getMessage());
            }
        } else if (response instanceof CreditBureauProviderResponse creditResponse) {
            if (!creditResponse.isSuccess()) {
                throw new RuntimeException(operation + " failed: " + creditResponse.getMessage());
            }
        }
    }
} 