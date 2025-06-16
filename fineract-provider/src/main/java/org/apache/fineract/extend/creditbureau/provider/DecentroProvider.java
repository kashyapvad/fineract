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
package org.apache.fineract.extend.creditbureau.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.extend.common.dto.CreditBureauProviderRequest;
import org.apache.fineract.extend.common.dto.CreditBureauProviderResponse;
import org.apache.fineract.extend.common.dto.CreditScoreProviderRequest;
import org.apache.fineract.extend.common.dto.CreditScoreProviderResponse;
import org.apache.fineract.extend.common.dto.CustomerDataProviderRequest;
import org.apache.fineract.extend.common.dto.CustomerDataProviderResponse;
import org.apache.fineract.extend.common.provider.CreditBureauProvider;
import org.apache.fineract.extend.common.provider.ProviderConfigurationException;
import org.apache.fineract.extend.common.provider.ProviderException;
import org.apache.fineract.extend.common.provider.ProviderRateLimits;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Decentro provider implementation for credit bureau operations.
 *
 * This implementation handles Decentro-specific API calls, request/response mapping, and error handling while
 * conforming to the provider-agnostic interface.
 *
 * Activated when: credit.bureau.provider=DECENTRO
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "credit.bureau.provider.enabled", havingValue = "true", matchIfMissing = false)
public class DecentroProvider implements CreditBureauProvider {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${credit.bureau.decentro.base-url}")
    private String decentroBaseUrl;

    @Value("${credit.bureau.decentro.client-id}")
    private String decentroClientId;

    @Value("${credit.bureau.decentro.client-secret}")
    private String decentroClientSecret;

    @Value("${credit.bureau.decentro.module-secret}")
    private String decentroModuleSecret;

    @Value("${credit.bureau.decentro.provider-secret}")
    private String decentroProviderSecret;

    // Decentro API endpoints
    private static final String CREDIT_REPORT_ENDPOINT = "/v2/financial_services/credit_bureau/credit_report/summary";
    private static final String CREDIT_SCORE_ENDPOINT = "/v2/bytes/credit-score";
    private static final String CUSTOMER_DATA_PULL_ENDPOINT = "/v2/financial_services/data/pull";

    @Override
    public String getProviderName() {
        return "DECENTRO";
    }

    @Override
    public boolean isAvailable() {
        try {
            validateConfiguration();
            return true;
        } catch (Exception e) {
            log.warn("Decentro provider not available: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public CreditBureauProviderResponse generateCreditReport(CreditBureauProviderRequest request) {
        log.info("Generating credit report via Decentro for client {}", request.getClientId());

        try {
            // Build Decentro-specific request
            final Map<String, Object> decentroRequest = buildCreditReportRequest(request);

            // Make API call
            final String endpoint = decentroBaseUrl + CREDIT_REPORT_ENDPOINT;
            final JsonNode response = makeDecentroApiCall(endpoint, decentroRequest);

            // Map to provider-agnostic response
            return mapCreditReportResponse(request.getReferenceId(), response);

        } catch (Exception e) {
            log.error("Failed to generate credit report via Decentro: {}", e.getMessage(), e);
            throw new ProviderException(getProviderName(), "CREDIT_REPORT_FAILED", "Failed to generate credit report: " + e.getMessage(), e,
                    isRetryableError(e));
        }
    }

    @Override
    public CreditScoreProviderResponse fetchCreditScore(CreditScoreProviderRequest request) {
        log.info("Fetching credit score via Decentro for client {}", request.getClientId());

        try {
            // Build Decentro-specific request
            final Map<String, Object> decentroRequest = buildCreditScoreRequest(request);

            // Make API call
            final String endpoint = decentroBaseUrl + CREDIT_SCORE_ENDPOINT;
            final JsonNode response = makeDecentroApiCall(endpoint, decentroRequest);

            // Map to provider-agnostic response
            return mapCreditScoreResponse(request.getReferenceId(), response);

        } catch (Exception e) {
            log.error("Failed to fetch credit score via Decentro: {}", e.getMessage(), e);
            throw new ProviderException(getProviderName(), "CREDIT_SCORE_FAILED", "Failed to fetch credit score: " + e.getMessage(), e,
                    isRetryableError(e));
        }
    }

    @Override
    public CustomerDataProviderResponse pullCustomerData(CustomerDataProviderRequest request) {
        log.info("Pulling customer data via Decentro for client {}", request.getClientId());

        try {
            // Build Decentro-specific request
            final Map<String, Object> decentroRequest = buildCustomerDataRequest(request);

            // Make API call
            final String endpoint = decentroBaseUrl + CUSTOMER_DATA_PULL_ENDPOINT;
            final JsonNode response = makeDecentroApiCall(endpoint, decentroRequest);

            // Map to provider-agnostic response
            return mapCustomerDataResponse(request.getReferenceId(), response, request.getDocumentType());

        } catch (Exception e) {
            log.error("Failed to pull customer data via Decentro: {}", e.getMessage(), e);
            throw new ProviderException(getProviderName(), "CUSTOMER_DATA_FAILED", "Failed to pull customer data: " + e.getMessage(), e,
                    isRetryableError(e));
        }
    }

    @Override
    public String[] getSupportedReportTypes() {
        return new String[] { "FULL_REPORT", "CREDIT_SCORE", "DATA_PULL" };
    }

    @Override
    public String[] getSupportedDocumentTypes() {
        return new String[] { "Aadhaar", "Driving_License", "PAN", "Passport", "VoterID" };
    }

    @Override
    public void validateConfiguration() {
        if (StringUtils.isBlank(decentroBaseUrl)) {
            throw new ProviderConfigurationException("Decentro base URL is not configured");
        }
        if (StringUtils.isBlank(decentroClientId)) {
            throw new ProviderConfigurationException("Decentro client ID is not configured");
        }
        if (StringUtils.isBlank(decentroClientSecret)) {
            throw new ProviderConfigurationException("Decentro client secret is not configured");
        }
        if (StringUtils.isBlank(decentroModuleSecret)) {
            throw new ProviderConfigurationException("Decentro module secret is not configured");
        }
        if (StringUtils.isBlank(decentroProviderSecret)) {
            throw new ProviderConfigurationException("Decentro provider secret is not configured");
        }
    }

    @Override
    public ProviderRateLimits getRateLimits() {
        return ProviderRateLimits.builder().requestsPerMinute(60).requestsPerHour(3600).requestsPerDay(86400).concurrentRequests(10)
                .timeoutMs(30000L).build();
    }

    /**
     * Build Decentro-specific credit report request.
     */
    private Map<String, Object> buildCreditReportRequest(CreditBureauProviderRequest request) {
        final Map<String, Object> decentroRequest = new HashMap<>();

        decentroRequest.put("reference_id", request.getReferenceId());
        decentroRequest.put("consent", request.getConsent());
        decentroRequest.put("name", request.getCustomerName());

        if (StringUtils.isNotBlank(request.getMobileNumber())) {
            decentroRequest.put("mobile", request.getMobileNumber());
        }

        if (StringUtils.isNotBlank(request.getPanNumber())) {
            decentroRequest.put("pan", request.getPanNumber());
        }

        // Add address information if available
        if (request.getAddress() != null) {
            if (StringUtils.isNotBlank(request.getAddress().getAddressType())) {
                decentroRequest.put("address_type", request.getAddress().getAddressType());
            }
            if (StringUtils.isNotBlank(request.getAddress().getPincode())) {
                decentroRequest.put("pincode", request.getAddress().getPincode());
            }
        }

        return decentroRequest;
    }

    /**
     * Build Decentro-specific credit score request.
     */
    private Map<String, Object> buildCreditScoreRequest(CreditScoreProviderRequest request) {
        final Map<String, Object> decentroRequest = new HashMap<>();

        decentroRequest.put("reference_id", request.getReferenceId());

        if (StringUtils.isBlank(request.getMobileNumber())) {
            throw new ProviderException(getProviderName(), "MOBILE_REQUIRED", "Mobile number is required for credit score fetch", false);
        }
        decentroRequest.put("mobile", request.getMobileNumber());

        return decentroRequest;
    }

    /**
     * Build Decentro-specific customer data request.
     */
    private Map<String, Object> buildCustomerDataRequest(CustomerDataProviderRequest request) {
        final Map<String, Object> decentroRequest = new HashMap<>();

        decentroRequest.put("reference_id", request.getReferenceId());
        decentroRequest.put("consent", request.getConsent());
        decentroRequest.put("name", request.getCustomerName());

        if (StringUtils.isNotBlank(request.getMobileNumber())) {
            decentroRequest.put("mobile", request.getMobileNumber());
        }

        if (StringUtils.isNotBlank(request.getDocumentType()) && StringUtils.isNotBlank(request.getDocumentNumber())) {
            decentroRequest.put("document_type", request.getDocumentType());
            decentroRequest.put("document_number", request.getDocumentNumber());
        }

        return decentroRequest;
    }

    /**
     * Make HTTP call to Decentro API.
     */
    private JsonNode makeDecentroApiCall(String endpoint, Map<String, Object> requestPayload) throws Exception {
        final HttpHeaders headers = createDecentroHeaders();
        final HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestPayload, headers);

        try {
            final ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            } else {
                throw new ProviderException(getProviderName(), "HTTP_ERROR", "HTTP error: " + response.getStatusCode(), false);
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ProviderException(getProviderName(), "HTTP_ERROR",
                    "HTTP error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e, isRetryableHttpError(e));
        } catch (ResourceAccessException e) {
            throw new ProviderException(getProviderName(), "NETWORK_ERROR", "Network error: " + e.getMessage(), e, true);
        }
    }

    /**
     * Create Decentro-specific HTTP headers.
     */
    private HttpHeaders createDecentroHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("client_id", decentroClientId);
        headers.set("client_secret", decentroClientSecret);
        headers.set("module_secret", decentroModuleSecret);
        headers.set("provider_secret", decentroProviderSecret);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("User-Agent", "Fineract-KYC-Service/1.0");
        return headers;
    }

    /**
     * Map Decentro credit report response to provider-agnostic format.
     */
    private CreditBureauProviderResponse mapCreditReportResponse(String referenceId, JsonNode response) {
        // Implementation continues in next part due to length...
        return CreditBureauProviderResponse.builder().referenceId(referenceId)
                .success("success_credit_report".equals(getResponseCode(response))).responseCode(getResponseCode(response))
                .message(getResponseMessage(response)).creditScore(extractCreditScore(response)).creditRating(extractCreditRating(response))
                .providerTransactionId(extractProviderTxnId(response)).reportSummary(extractReportSummary(response))
                .rawProviderResponse(response).build();
    }

    /**
     * Map Decentro credit score response to provider-agnostic format.
     */
    private CreditScoreProviderResponse mapCreditScoreResponse(String referenceId, JsonNode response) {
        boolean success = "success_credit_score".equals(getResponseCode(response));
        boolean scoreFound = !("error_credits_score_not_found".equals(getResponseCode(response)));

        return CreditScoreProviderResponse.builder().referenceId(referenceId).success(success).responseCode(getResponseCode(response))
                .message(getResponseMessage(response)).creditScore(extractCreditScore(response)).creditRating(extractCreditRating(response))
                .scoreFound(scoreFound).providerTransactionId(extractProviderTxnId(response)).rawProviderResponse(response).build();
    }

    /**
     * Map Decentro customer data response to provider-agnostic format.
     */
    private CustomerDataProviderResponse mapCustomerDataResponse(String referenceId, JsonNode response, String documentType) {
        boolean success = "success_customer_data_pull".equals(getResponseCode(response));

        Map<String, Boolean> verificationResults = new HashMap<>();
        if (success && StringUtils.isNotBlank(documentType)) {
            // Apply the same verification logic we had before
            boolean verified = extractDetailedVerificationResult(response.get("data"), documentType);

            // Normalize document type: trim whitespace and convert to uppercase for comparison
            final String normalizedDocumentType = StringUtils.trimToEmpty(documentType).toUpperCase();

            switch (normalizedDocumentType) {
                case "PAN":
                    verificationResults.put("panVerified", verified);
                break;
                case "AADHAAR":
                case "AADHAR":
                    verificationResults.put("aadhaarVerified", verified);
                break;
                case "DRIVING_LICENSE":
                case "DRIVING_LICENCE":
                case "DL":
                    verificationResults.put("drivingLicenseVerified", verified);
                break;
                case "VOTERID":
                case "VOTER_ID":
                case "VOTER ID":
                    verificationResults.put("voterIdVerified", verified);
                break;
                case "PASSPORT":
                    verificationResults.put("passportVerified", verified);
                break;
                default:
                    log.warn("Unknown document type for verification: {}", normalizedDocumentType);
            }
        }

        return CustomerDataProviderResponse.builder().referenceId(referenceId).success(success).responseCode(getResponseCode(response))
                .message(getResponseMessage(response)).verified(success).verificationResults(verificationResults)
                .providerTransactionId(extractProviderTxnId(response)).rawProviderResponse(response).build();
    }

    // Helper methods for extracting data from Decentro responses
    private String getResponseCode(JsonNode response) {
        return response.has("responseCode") ? response.get("responseCode").asText() : null;
    }

    private String getResponseMessage(JsonNode response) {
        return response.has("message") ? response.get("message").asText() : null;
    }

    private String extractProviderTxnId(JsonNode response) {
        return response.has("decentroTxnId") ? response.get("decentroTxnId").asText() : null;
    }

    private Integer extractCreditScore(JsonNode response) {
        if (response != null && response.has("data")) {
            JsonNode data = response.get("data");
            if (data.has("creditScore")) {
                return data.get("creditScore").asInt();
            }
        }
        return null;
    }

    private String extractCreditRating(JsonNode response) {
        if (response != null && response.has("data")) {
            JsonNode data = response.get("data");
            if (data.has("creditRating")) {
                return data.get("creditRating").asText();
            }
        }
        return null;
    }

    private String extractReportSummary(JsonNode response) {
        // Same logic as before
        if (response == null) return null;

        StringBuilder summary = new StringBuilder();
        if (response.has("responseCode")) {
            summary.append("Response Code: ").append(response.get("responseCode").asText()).append("\n");
        }
        if (response.has("message")) {
            summary.append("Message: ").append(response.get("message").asText()).append("\n");
        }

        return summary.length() > 0 ? summary.toString().trim() : null;
    }

    private boolean extractDetailedVerificationResult(JsonNode data, String documentType) {
        // Same logic as before for detailed verification
        if (data == null) return false;

        if (data.has("verified")) {
            return data.get("verified").asBoolean();
        }

        // Check for customer data presence as fallback
        if (data.has("name") || data.has("mobile") || data.has("address")) {
            return true;
        }

        return false;
    }

    private boolean isRetryableError(Exception e) {
        return e instanceof ResourceAccessException
                || (e instanceof HttpServerErrorException && ((HttpServerErrorException) e).getStatusCode().is5xxServerError());
    }

    private boolean isRetryableHttpError(Exception e) {
        if (e instanceof HttpServerErrorException) {
            return ((HttpServerErrorException) e).getStatusCode().is5xxServerError();
        }
        return false;
    }
}
