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

import org.apache.fineract.extend.common.dto.CreditBureauProviderRequest;
import org.apache.fineract.extend.common.dto.CreditBureauProviderResponse;
import org.apache.fineract.extend.common.dto.CreditScoreProviderRequest;
import org.apache.fineract.extend.common.dto.CreditScoreProviderResponse;
import org.apache.fineract.extend.common.dto.CustomerDataProviderRequest;
import org.apache.fineract.extend.common.dto.CustomerDataProviderResponse;

/**
 * Provider interface for credit bureau operations.
 *
 * This interface abstracts all external credit bureau provider implementations allowing seamless switching between
 * providers (Decentro, Experian, Equifax, etc.) without changing business logic.
 *
 * Implementation Strategy: - Each provider (Decentro, Experian, etc.) implements this interface - Provider-specific
 * response parsing handled in implementation - Business services depend only on this interface - Provider selection via
 * configuration/environment variables
 */
public interface CreditBureauProvider {

    /**
     * Get the provider name identifier. Used for logging, configuration, and provider selection.
     *
     * @return Provider name (e.g., "DECENTRO", "EXPERIAN", "EQUIFAX")
     */
    String getProviderName();

    /**
     * Check if this provider is available and properly configured. Used for health checks and provider validation.
     *
     * @return true if provider is available and configured
     */
    boolean isAvailable();

    /**
     * Generate a comprehensive credit report for a customer.
     *
     * @param request
     *            Provider-agnostic request containing customer details
     * @return Standardized response with credit report data
     * @throws ProviderException
     *             if credit report generation fails
     */
    CreditBureauProviderResponse generateCreditReport(CreditBureauProviderRequest request);

    /**
     * Fetch credit score only (lighter operation than full report).
     *
     * @param request
     *            Provider-agnostic request containing customer details
     * @return Standardized response with credit score data
     * @throws ProviderException
     *             if credit score fetch fails
     */
    CreditScoreProviderResponse fetchCreditScore(CreditScoreProviderRequest request);

    /**
     * Pull customer data for document verification purposes.
     *
     * @param request
     *            Provider-agnostic request containing customer and document details
     * @return Standardized response with customer data and verification status
     * @throws ProviderException
     *             if customer data pull fails
     */
    CustomerDataProviderResponse pullCustomerData(CustomerDataProviderRequest request);

    /**
     * Get supported report types for this provider. Different providers may support different types of reports.
     *
     * @return Array of supported report type codes
     */
    String[] getSupportedReportTypes();

    /**
     * Get supported document types for customer data pull. Different providers may support different document types.
     *
     * @return Array of supported document type codes
     */
    String[] getSupportedDocumentTypes();

    /**
     * Validate provider-specific configuration. Called during application startup to ensure provider is properly
     * configured.
     *
     * @throws ProviderConfigurationException
     *             if configuration is invalid
     */
    void validateConfiguration();

    /**
     * Get provider-specific rate limits. Used for request throttling and capacity planning.
     *
     * @return Rate limit information (requests per minute, daily limits, etc.)
     */
    ProviderRateLimits getRateLimits();
}
