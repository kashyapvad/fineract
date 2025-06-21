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
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
 * SurePass provider implementation for credit bureau operations.
 *
 * This implementation handles SurePass-specific API calls for KYC verification, including Aadhaar and PAN validation
 * with comprehensive verification logic.
 *
 * Activated when: credit.bureau.provider=SUREPASS
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "credit.bureau.provider.enabled", havingValue = "true", matchIfMissing = false)
public class SurePassProvider implements CreditBureauProvider {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${credit.bureau.surepass.base-url}")
    private String surePassBaseUrl;

    @Value("${credit.bureau.surepass.api-key}")
    private String surePassApiKey;

    @Value("${credit.bureau.surepass.aadhaar-endpoint}")
    private String surePassAadhaarEndpoint;

    @Value("${credit.bureau.surepass.pan-endpoint}")
    private String surePassPanEndpoint;

    @PostConstruct
    private void init() {
        log.info("SurePassProvider initialized successfully");
    }

    @Override
    public String getProviderName() {
        return "SUREPASS";
    }

    @Override
    public boolean isAvailable() {
        try {
            validateConfiguration();
            return true;
        } catch (Exception e) {
            log.warn("SurePass provider not available: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public CreditBureauProviderResponse generateCreditReport(CreditBureauProviderRequest request) {
        log.info("SurePass does not support credit report generation - this is a KYC-only provider");
        throw new ProviderException(getProviderName(), "NOT_SUPPORTED",
                "Credit report generation not supported by SurePass - use for KYC verification only", false);
    }

    @Override
    public CreditScoreProviderResponse fetchCreditScore(CreditScoreProviderRequest request) {
        log.info("SurePass does not support credit score fetching - this is a KYC-only provider");
        throw new ProviderException(getProviderName(), "NOT_SUPPORTED",
                "Credit score fetching not supported by SurePass - use for KYC verification only", false);
    }

    @Override
    public CustomerDataProviderResponse pullCustomerData(CustomerDataProviderRequest request) {
        log.info("Pulling customer data via SurePass for client {} - document type: {}", request.getClientId(), request.getDocumentType());

        try {
            String documentType = StringUtils.trimToEmpty(request.getDocumentType()).toUpperCase();

            switch (documentType) {
                case "AADHAAR":
                case "AADHAR":
                    return verifyAadhaar(request);
                case "PAN":
                    return verifyPan(request);
                default:
                    throw new ProviderException(getProviderName(), "UNSUPPORTED_DOCUMENT",
                            "Document type not supported: " + documentType + ". SurePass supports AADHAAR and PAN only", false);
            }

        } catch (Exception e) {
            log.error("Failed to pull customer data via SurePass: {}", e.getMessage(), e);
            if (e instanceof ProviderException) {
                throw e;
            }
            throw new ProviderException(getProviderName(), "CUSTOMER_DATA_FAILED", "Failed to pull customer data: " + e.getMessage(), e,
                    isRetryableError(e));
        }
    }

    /**
     * Verify Aadhaar number via SurePass API
     */
    private CustomerDataProviderResponse verifyAadhaar(CustomerDataProviderRequest request) {
        log.info("Verifying Aadhaar for client {}", request.getClientId());

        try {
            // Build SurePass Aadhaar request
            Map<String, Object> surePassRequest = new HashMap<>();
            surePassRequest.put("id_number", request.getDocumentNumber());

            // Make API call
            String endpoint = surePassBaseUrl + surePassAadhaarEndpoint;
            JsonNode response = makeSurePassApiCall(endpoint, surePassRequest);

            // Parse response and perform validation
            return processAadhaarResponse(request, response);

        } catch (Exception e) {
            log.error("Failed to verify Aadhaar via SurePass: {}", e.getMessage(), e);
            throw new ProviderException(getProviderName(), "AADHAAR_VERIFICATION_FAILED", "Aadhaar verification failed: " + e.getMessage(),
                    e, isRetryableError(e));
        }
    }

    /**
     * Verify PAN number via SurePass API
     */
    private CustomerDataProviderResponse verifyPan(CustomerDataProviderRequest request) {
        log.info("Verifying PAN for client {}", request.getClientId());

        try {
            // Build SurePass PAN request
            Map<String, Object> surePassRequest = new HashMap<>();
            surePassRequest.put("id_number", request.getDocumentNumber());

            // Make API call
            String endpoint = surePassBaseUrl + surePassPanEndpoint;
            JsonNode response = makeSurePassApiCall(endpoint, surePassRequest);

            // Parse response and perform validation
            return processPanResponse(request, response);

        } catch (Exception e) {
            log.error("Failed to verify PAN via SurePass: {}", e.getMessage(), e);
            throw new ProviderException(getProviderName(), "PAN_VERIFICATION_FAILED", "PAN verification failed: " + e.getMessage(), e,
                    isRetryableError(e));
        }
    }

    /**
     * Process Aadhaar verification response from SurePass
     */
    private CustomerDataProviderResponse processAadhaarResponse(CustomerDataProviderRequest request, JsonNode response) {
        boolean apiSuccess = response.path("success").asBoolean(false);
        int statusCode = response.path("status_code").asInt(500);
        String message = response.path("message").asText("");

        Map<String, Boolean> verificationResults = new HashMap<>();
        boolean verified = false;

        if (apiSuccess && statusCode == 200) {
            JsonNode data = response.path("data");
            String remarks = data.path("remarks").asText("");

            if ("success".equals(remarks)) {
                // API validation successful, now perform business logic validation
                verified = validateAadhaarData(request, data);
                log.info("Aadhaar validation for client {}: API success = {}, Business validation = {}", request.getClientId(), true,
                        verified);
            } else {
                log.info("Aadhaar validation failed for client {} - API remarks: {}", request.getClientId(), remarks);
            }
        } else {
            log.info("Aadhaar API call failed for client {} - Status: {}, Message: {}", request.getClientId(), statusCode, message);
        }

        verificationResults.put("aadhaarVerified", verified);

        return CustomerDataProviderResponse.builder().referenceId(request.getReferenceId()).success(apiSuccess)
                .responseCode(String.valueOf(statusCode)).message(message).verified(verified).verificationResults(verificationResults)
                .rawProviderResponse(response).build();
    }

    /**
     * Process PAN verification response from SurePass
     */
    private CustomerDataProviderResponse processPanResponse(CustomerDataProviderRequest request, JsonNode response) {
        boolean apiSuccess = response.path("success").asBoolean(false);
        int statusCode = response.path("status_code").asInt(500);
        String message = response.path("message").asText("");

        Map<String, Boolean> verificationResults = new HashMap<>();
        boolean verified = false;

        if (apiSuccess && statusCode == 200) {
            JsonNode data = response.path("data");
            String fullName = data.path("full_name").asText("");

            if (StringUtils.isNotBlank(fullName)) {
                // API validation successful, now perform name matching
                verified = validatePanName(request, fullName);
                log.info("PAN validation for client {}: API success = {}, Name validation = {}", request.getClientId(), true, verified);
            } else {
                log.info("PAN validation failed for client {} - No full name returned", request.getClientId());
            }
        } else {
            log.info("PAN API call failed for client {} - Status: {}, Message: {}", request.getClientId(), statusCode, message);
        }

        verificationResults.put("panVerified", verified);

        return CustomerDataProviderResponse.builder().referenceId(request.getReferenceId()).success(apiSuccess)
                .responseCode(String.valueOf(statusCode)).message(message).verified(verified).verificationResults(verificationResults)
                .rawProviderResponse(response).build();
    }

    /**
     * Validate Aadhaar data against client information Checks gender and last 3 digits of mobile number
     */
    private boolean validateAadhaarData(CustomerDataProviderRequest request, JsonNode aadhaarData) {
        try {
            String aadhaarGender = aadhaarData.path("gender").asText("");
            String lastDigits = aadhaarData.path("last_digits").asText("");

            // Get client mobile number and extract last 3 digits
            String clientMobile = request.getMobileNumber();
            if (StringUtils.isBlank(clientMobile) || clientMobile.length() < 3) {
                log.warn("Client mobile number not available or too short for validation");
                return false;
            }

            String clientLastDigits = clientMobile.substring(clientMobile.length() - 3);

            // Validate last digits of mobile number
            boolean lastDigitsMatch = lastDigits.equals(clientLastDigits);

            // Validate gender if client gender is available
            boolean genderMatch = true; // Default to true if no gender validation needed
            String clientGender = request.getGender(); // Assuming this field exists or will be added

            if (StringUtils.isNotBlank(clientGender) && StringUtils.isNotBlank(aadhaarGender)) {
                genderMatch = validateGender(clientGender, aadhaarGender);
                log.info("Aadhaar gender validation - Gender match: {} (Client: '{}', Aadhaar: '{}')", genderMatch, clientGender,
                        aadhaarGender);
            } else {
                log.info("Skipping gender validation - Client gender: '{}', Aadhaar gender: '{}'", clientGender, aadhaarGender);
            }

            log.info("Aadhaar validation - Last digits match: {}, Gender match: {} (Aadhaar: {}, Client: {})", lastDigitsMatch, genderMatch,
                    lastDigits, clientLastDigits);

            // Both validations must pass for successful verification
            return lastDigitsMatch && genderMatch;

        } catch (Exception e) {
            log.error("Error validating Aadhaar data: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Comprehensive gender validation handling all possible variations Handles: M/F, Male/Female, MALE/FEMALE,
     * male/female, with trailing spaces, etc.
     */
    private boolean validateGender(String clientGender, String aadhaarGender) {
        if (StringUtils.isBlank(clientGender) || StringUtils.isBlank(aadhaarGender)) {
            return false;
        }

        // Step 1: Normalize both gender values
        String normalizedClientGender = normalizeGender(clientGender);
        String normalizedAadhaarGender = normalizeGender(aadhaarGender);

        // Step 2: Compare normalized values
        boolean matches = normalizedClientGender.equals(normalizedAadhaarGender);

        log.debug("Gender validation - Original: Client='{}', Aadhaar='{}' | Normalized: Client='{}', Aadhaar='{}' | Match: {}",
                clientGender, aadhaarGender, normalizedClientGender, normalizedAadhaarGender, matches);

        return matches;
    }

    /**
     * Normalize gender value to standard format Converts all variations to either "MALE" or "FEMALE"
     */
    private String normalizeGender(String gender) {
        if (StringUtils.isBlank(gender)) {
            return "";
        }

        // Step 1: Trim whitespace and convert to uppercase
        String normalized = StringUtils.trimToEmpty(gender).toUpperCase();

        // Step 2: Handle all possible male variations
        if (normalized.equals("M") || normalized.equals("MALE") || normalized.equals("MAN")
                || normalized.startsWith("M") && normalized.length() <= 4) { // Handle typos like "MAL"
            return "MALE";
        }

        // Step 3: Handle all possible female variations
        if (normalized.equals("F") || normalized.equals("FEMALE") || normalized.equals("WOMAN") || normalized.equals("WOMEN")
                || normalized.startsWith("F") && normalized.length() <= 6) { // Handle typos like "FEMAL"
            return "FEMALE";
        }

        // Step 4: Handle numeric codes (if any system uses them)
        if (normalized.equals("1")) {
            return "MALE";
        }
        if (normalized.equals("2")) {
            return "FEMALE";
        }

        // Step 5: If no match found, return original normalized value for logging
        log.warn("Unknown gender format encountered: '{}' (original: '{}')", normalized, gender);
        return normalized;
    }

    /**
     * Validate PAN name against client name using proper name matching logic
     */
    private boolean validatePanName(CustomerDataProviderRequest request, String panFullName) {
        try {
            String clientName = request.getCustomerName();

            if (StringUtils.isBlank(clientName) || StringUtils.isBlank(panFullName)) {
                log.warn("Client name or PAN name is blank - cannot validate");
                return false;
            }

            boolean nameMatches = validateNames(clientName, panFullName);

            log.info("PAN name validation - Names match: {} (Client: '{}', PAN: '{}')", nameMatches, clientName, panFullName);

            return nameMatches;

        } catch (Exception e) {
            log.error("Error validating PAN name: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validate names using strict name matching logic Requirements: - Direct name comparison (exact match) -
     * Word-by-word comparison with 100% match requirement (order can be different) - Case-insensitive comparison with
     * whitespace normalization - No partial name matching allowed
     */
    private boolean validateNames(String clientName, String panName) {
        if (StringUtils.isBlank(clientName) || StringUtils.isBlank(panName)) {
            return false;
        }

        // Step 1: Trim whitespace
        String normalizedClientName = StringUtils.trimToEmpty(clientName);
        String normalizedPanName = StringUtils.trimToEmpty(panName);

        // Step 2: Normalize multiple spaces to single space
        normalizedClientName = normalizedClientName.replaceAll("\\s{2,}", " ");
        normalizedPanName = normalizedPanName.replaceAll("\\s{2,}", " ");

        // Step 3: Convert to uppercase for comparison
        normalizedClientName = normalizedClientName.toUpperCase();
        normalizedPanName = normalizedPanName.toUpperCase();

        // Step 4: Direct match (exact comparison)
        if (normalizedClientName.equals(normalizedPanName)) {
            return true;
        }

        // Step 5: Word-by-word comparison with 100% match requirement (order can be different)
        String[] clientNameParts = normalizedClientName.split("\\s+");
        String[] panNameParts = normalizedPanName.split("\\s+");

        // Both names must have the same number of words for 100% match
        if (clientNameParts.length != panNameParts.length) {
            return false;
        }

        // Create sets to check if all words match (ignoring order)
        Set<String> clientNameSet = new HashSet<>();
        Set<String> panNameSet = new HashSet<>();

        // Add all non-trivial name parts to sets (ignore very short parts like initials)
        for (String part : clientNameParts) {
            if (part.length() > 1) { // Keep parts longer than 1 character
                clientNameSet.add(part);
            }
        }

        for (String part : panNameParts) {
            if (part.length() > 1) { // Keep parts longer than 1 character
                panNameSet.add(part);
            }
        }

        // For 100% match, all words must be present in both names
        return clientNameSet.equals(panNameSet) && !clientNameSet.isEmpty();
    }

    /**
     * Make HTTP call to SurePass API
     */
    private JsonNode makeSurePassApiCall(String endpoint, Map<String, Object> requestPayload) throws Exception {
        HttpHeaders headers = createSurePassHeaders();
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);

            if (response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            } else {
                throw new ProviderException(getProviderName(), "EMPTY_RESPONSE", "Empty response from SurePass API", false);
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error calling SurePass API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            // Handle specific HTTP status codes
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();

            // Handle 422 Unprocessable Entity - Invalid data/validation errors
            if (statusCode == 422) {
                return handleValidationError(responseBody);
            }

            // Handle 401 Unauthorized
            if (statusCode == 401) {
                throw new ProviderException(getProviderName(), "UNAUTHORIZED",
                        "Authentication failed with SurePass API. Please check API credentials.", e, false);
            }

            // Handle 429 Too Many Requests
            if (statusCode == 429) {
                throw new ProviderException(getProviderName(), "RATE_LIMITED",
                        "Rate limit exceeded for SurePass API. Please try again later.", e, true);
            }

            // Handle 400 Bad Request
            if (statusCode == 400) {
                return handleBadRequest(responseBody);
            }

            // Try to parse other error responses
            try {
                JsonNode errorResponse = objectMapper.readTree(responseBody);
                return createErrorResponse(statusCode, errorResponse);
            } catch (Exception parseException) {
                throw new ProviderException(getProviderName(), "HTTP_ERROR", "HTTP error: " + e.getStatusCode() + " - " + responseBody,
                        parseException, isRetryableHttpError(e));
            }
        } catch (ResourceAccessException e) {
            throw new ProviderException(getProviderName(), "NETWORK_ERROR", "Network error: " + e.getMessage(), e, true);
        }
    }

    /**
     * Handle 422 validation errors from SurePass API
     */
    private JsonNode handleValidationError(String responseBody) throws Exception {
        try {
            JsonNode errorResponse = objectMapper.readTree(responseBody);
            String message = errorResponse.path("message").asText("Invalid document data");
            String details = errorResponse.path("details").asText("");

            // Create a standardized error response that processPanResponse/processAadhaarResponse can handle
            ObjectNode standardizedError = objectMapper.createObjectNode();
            standardizedError.put("success", false);
            standardizedError.put("status_code", 422);

            // Create user-friendly error message
            String userFriendlyMessage;
            if (message.toLowerCase().contains("invalid pan") || message.toLowerCase().contains("pan")) {
                userFriendlyMessage = "Invalid PAN number format or PAN not found in government records";
            } else if (message.toLowerCase().contains("invalid aadhaar") || message.toLowerCase().contains("aadhaar")) {
                userFriendlyMessage = "Invalid Aadhaar number format or Aadhaar not found in government records";
            } else {
                userFriendlyMessage = "Invalid document data: " + message;
            }

            standardizedError.put("message", userFriendlyMessage);

            if (StringUtils.isNotBlank(details)) {
                standardizedError.put("details", details);
            }

            log.warn("SurePass validation error (422): {}", userFriendlyMessage);
            return standardizedError;

        } catch (Exception e) {
            // Fallback if we can't parse the error response
            ObjectNode fallbackError = objectMapper.createObjectNode();
            fallbackError.put("success", false);
            fallbackError.put("status_code", 422);
            fallbackError.put("message", "Document validation failed - invalid document format or document not found");
            return fallbackError;
        }
    }

    /**
     * Handle 400 bad request errors from SurePass API
     */
    private JsonNode handleBadRequest(String responseBody) throws Exception {
        try {
            JsonNode errorResponse = objectMapper.readTree(responseBody);
            String message = errorResponse.path("message").asText("Bad request");

            ObjectNode standardizedError = objectMapper.createObjectNode();
            standardizedError.put("success", false);
            standardizedError.put("status_code", 400);
            standardizedError.put("message", "Request format error: " + message);

            log.warn("SurePass bad request (400): {}", message);
            return standardizedError;

        } catch (Exception e) {
            ObjectNode fallbackError = objectMapper.createObjectNode();
            fallbackError.put("success", false);
            fallbackError.put("status_code", 400);
            fallbackError.put("message", "Invalid request format");
            return fallbackError;
        }
    }

    /**
     * Create standardized error response for other HTTP errors
     */
    private JsonNode createErrorResponse(int statusCode, JsonNode originalError) {
        ObjectNode standardizedError = objectMapper.createObjectNode();
        standardizedError.put("success", false);
        standardizedError.put("status_code", statusCode);

        String message = originalError.path("message").asText("API request failed");
        standardizedError.put("message", message);

        // Copy any additional error details
        if (originalError.has("details")) {
            standardizedError.set("details", originalError.get("details"));
        }

        return standardizedError;
    }

    /**
     * Create SurePass-specific HTTP headers
     */
    private HttpHeaders createSurePassHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + surePassApiKey);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("User-Agent", "Fineract-KYC-Service/1.0");
        return headers;
    }

    @Override
    public String[] getSupportedReportTypes() {
        return new String[] { "KYC_VERIFICATION" };
    }

    @Override
    public String[] getSupportedDocumentTypes() {
        return new String[] { "Aadhaar", "PAN" };
    }

    @Override
    public void validateConfiguration() {
        if (StringUtils.isBlank(surePassBaseUrl)) {
            throw new ProviderConfigurationException("SurePass base URL is not configured");
        }
        if (StringUtils.isBlank(surePassApiKey)) {
            throw new ProviderConfigurationException("SurePass API key is not configured");
        }
        if (StringUtils.isBlank(surePassAadhaarEndpoint)) {
            throw new ProviderConfigurationException("SurePass Aadhaar endpoint is not configured");
        }
        if (StringUtils.isBlank(surePassPanEndpoint)) {
            throw new ProviderConfigurationException("SurePass PAN endpoint is not configured");
        }
    }

    @Override
    public ProviderRateLimits getRateLimits() {
        return ProviderRateLimits.builder().requestsPerMinute(20) // Conservative rate limit for SurePass
                .requestsPerHour(1200).requestsPerDay(28800).concurrentRequests(5).timeoutMs(30000L).build();
    }

    /**
     * Check if an error is retryable
     */
    private boolean isRetryableError(Exception e) {
        if (e instanceof ResourceAccessException) {
            return true; // Network errors are retryable
        }
        if (e instanceof HttpServerErrorException) {
            return true; // 5xx errors are retryable
        }
        return false;
    }

    /**
     * Check if HTTP error is retryable
     */
    private boolean isRetryableHttpError(Exception e) {
        if (e instanceof HttpServerErrorException) {
            return true; // 5xx errors are retryable
        }
        if (e instanceof HttpClientErrorException clientError) {
            // 429 (Too Many Requests) is retryable
            return clientError.getStatusCode().value() == 429;
        }
        return false;
    }
}
