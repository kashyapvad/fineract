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
package org.apache.fineract.extend.common.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Provider-agnostic response DTO for credit score operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditScoreProviderResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String referenceId;
    private String providerTransactionId;
    private boolean success;
    private String responseCode;
    private String message;
    private Integer creditScore;
    private String creditRating;
    private boolean scoreFound;
    private JsonNode rawProviderResponse;
    private CreditBureauProviderResponse.ErrorInformation error;
    private CreditBureauProviderResponse.ProviderMetadata providerMetadata;
}
