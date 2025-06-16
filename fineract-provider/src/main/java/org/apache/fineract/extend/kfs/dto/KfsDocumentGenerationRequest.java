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
package org.apache.fineract.extend.kfs.dto;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for KFS document generation API. This is the API-facing request object that gets converted to internal
 * requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KfsDocumentGenerationRequest {

    private Long loanId;
    private Long clientId;
    private Long eirCalculationId;
    private Long templateId;
    private String templateName;
    private String templateVersion;
    private String format;
    private String deliveryMethod;
    private String customerName;
    private String customerMobile;
    private String customerEmail;
    private BigDecimal principalAmount;
    private BigDecimal netDisbursementAmount;
    private BigDecimal chargesDueAtDisbursement;
    private BigDecimal emiAmount;
    private Integer tenureInMonths;
    private Boolean preview;
    private Map<String, Object> customFields;

    // Helper methods
    public boolean isPreview() {
        return preview != null && preview;
    }

    public String getFormat() {
        return format != null ? format.toUpperCase() : "PDF";
    }

    public String getDeliveryMethod() {
        return deliveryMethod != null ? deliveryMethod.toUpperCase() : "DOWNLOAD";
    }
}
