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

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Factory for managing credit bureau provider selection and instantiation.
 *
 * Supports multiple providers with environment-based selection: - credit.bureau.provider=DECENTRO (default) -
 * credit.bureau.provider=EXPERIAN - credit.bureau.provider=EQUIFAX
 *
 * Usage: 1. Add new provider implementation of CreditBureauProvider interface 2. Add @ConditionalOnProperty annotation
 * with provider name 3. Set environment variable credit.bureau.provider=PROVIDER_NAME 4. Factory automatically selects
 * and validates provider
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "credit.bureau.provider.enabled", havingValue = "true", matchIfMissing = false)
public class CreditBureauProviderFactory {

    private final List<CreditBureauProvider> availableProviders;

    @Value("${credit.bureau.provider}")
    private String configuredProviderName;

    private CreditBureauProvider activeProvider;

    @PostConstruct
    public void initializeProvider() {
        log.info("Initializing CreditBureauProviderFactory with {} available providers", availableProviders.size());

        if (availableProviders.isEmpty()) {
            log.warn("No credit bureau providers are available");
            return;
        }

        // Find provider matching configured name
        final Optional<CreditBureauProvider> matchingProvider = availableProviders.stream()
                .filter(provider -> configuredProviderName.equalsIgnoreCase(provider.getProviderName())).findFirst();

        if (matchingProvider.isPresent()) {
            this.activeProvider = matchingProvider.get();
            log.info("Successfully configured credit bureau provider: {}", configuredProviderName);
        } else {
            log.error("Configured provider '{}' not found among available providers: {}", configuredProviderName,
                    availableProviders.stream().map(CreditBureauProvider::getProviderName).collect(Collectors.toList()));
        }
    }

    /**
     * Get the active credit bureau provider.
     *
     * @return The configured and validated provider instance
     * @throws ProviderConfigurationException
     *             if no provider is configured
     */
    public CreditBureauProvider getProvider() {
        if (activeProvider == null) {
            throw new ProviderConfigurationException("No active credit bureau provider configured");
        }
        return activeProvider;
    }

    /**
     * Get the configured provider name.
     *
     * @return The name of the configured provider
     */
    public String getConfiguredProviderName() {
        return configuredProviderName;
    }

    /**
     * Check if a provider is available and properly configured.
     *
     * @return true if active provider is available
     */
    public boolean isProviderAvailable() {
        return activeProvider != null && activeProvider.isAvailable();
    }

    /**
     * Get provider rate limits for the active provider.
     *
     * @return Rate limits information
     */
    public ProviderRateLimits getProviderRateLimits() {
        return getProvider().getRateLimits();
    }

    /**
     * Get supported report types for the active provider.
     *
     * @return Array of supported report types
     */
    public String[] getSupportedReportTypes() {
        return getProvider().getSupportedReportTypes();
    }

    /**
     * Get supported document types for the active provider.
     *
     * @return Array of supported document types
     */
    public String[] getSupportedDocumentTypes() {
        return getProvider().getSupportedDocumentTypes();
    }

    /**
     * Switch to a different provider (runtime switching). Used for testing or emergency provider switching.
     *
     * @param providerName
     *            The name of the provider to switch to
     * @throws ProviderConfigurationException
     *             if provider not found or invalid
     */
    public void switchProvider(String providerName) {
        log.info("Attempting to switch credit bureau provider from {} to {}",
                activeProvider != null ? activeProvider.getProviderName() : "none", providerName);

        Optional<CreditBureauProvider> provider = availableProviders.stream()
                .filter(p -> providerName.equalsIgnoreCase(p.getProviderName())).findFirst();

        if (provider.isPresent()) {
            CreditBureauProvider newProvider = provider.get();

            try {
                newProvider.validateConfiguration();
                activeProvider = newProvider;
                configuredProviderName = providerName;

                log.info("Successfully switched to credit bureau provider: {}", newProvider.getProviderName());
            } catch (Exception e) {
                log.error("Failed to switch to provider {}: {}", providerName, e.getMessage());
                throw new ProviderConfigurationException("Failed to switch to provider " + providerName + ": " + e.getMessage(), e);
            }
        } else {
            throw new ProviderConfigurationException("Provider not found: " + providerName);
        }
    }
}
