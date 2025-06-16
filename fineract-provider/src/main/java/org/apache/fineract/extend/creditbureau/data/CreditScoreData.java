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
package org.apache.fineract.extend.creditbureau.data;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for individual credit scores within a credit report.
 *
 * This DTO represents detailed credit score information from the ClientCreditScoreDetails entity.
 */
@Data
@NoArgsConstructor
public class CreditScoreData implements Serializable {

    private static final long serialVersionUID = 1L;

    // Score Identification
    private String scoreModel;
    private String scoreVersion;
    private String scoreName;

    // Score Value and Date
    private Integer creditScore;
    private LocalDate scoreDate;

    // Score Metadata
    private Integer scoreRangeMin;
    private Integer scoreRangeMax;
    private BigDecimal scorePercentile;

    // Scoring Elements (factors that influenced the score)
    private JsonNode scoringElements;
    private String scoreReason;

    // Provider Specific Data
    private String providerScoreId;
    private JsonNode providerMetadata;

    /**
     * Constructor for creating credit score data.
     */
    public CreditScoreData(String scoreModel, String scoreVersion, String scoreName, Integer creditScore, LocalDate scoreDate,
            Integer scoreRangeMin, Integer scoreRangeMax, BigDecimal scorePercentile, JsonNode scoringElements, String scoreReason,
            String providerScoreId, JsonNode providerMetadata) {
        this.scoreModel = scoreModel;
        this.scoreVersion = scoreVersion;
        this.scoreName = scoreName;
        this.creditScore = creditScore;
        this.scoreDate = scoreDate;
        this.scoreRangeMin = scoreRangeMin;
        this.scoreRangeMax = scoreRangeMax;
        this.scorePercentile = scorePercentile;
        this.scoringElements = scoringElements;
        this.scoreReason = scoreReason;
        this.providerScoreId = providerScoreId;
        this.providerMetadata = providerMetadata;
    }

    /**
     * Gets a human-readable score rating based on the score value.
     */
    public String getScoreRating() {
        if (creditScore == null) {
            return "UNKNOWN";
        }

        if (creditScore >= 750) {
            return "EXCELLENT";
        } else if (creditScore >= 700) {
            return "GOOD";
        } else if (creditScore >= 650) {
            return "FAIR";
        } else if (creditScore >= 600) {
            return "POOR";
        } else {
            return "VERY_POOR";
        }
    }

    /**
     * Gets score model and version as a combined identifier.
     */
    public String getScoreModelVersion() {
        if (scoreVersion != null && !scoreVersion.trim().isEmpty()) {
            return scoreModel + " " + scoreVersion;
        }
        return scoreModel;
    }

    /**
     * Gets the display name for this score (prioritizes scoreName, falls back to model+version).
     */
    public String getDisplayName() {
        if (scoreName != null && !scoreName.trim().isEmpty()) {
            return scoreName;
        }
        return getScoreModelVersion();
    }

    /**
     * Checks if this score is within the normal expected range.
     */
    public boolean isScoreInValidRange() {
        if (creditScore == null) {
            return false;
        }

        // Use provided score range if available
        if (scoreRangeMin != null && scoreRangeMax != null) {
            return creditScore >= scoreRangeMin && creditScore <= scoreRangeMax;
        }

        // Default range validation (most credit scores are between 300-900)
        return creditScore >= 300 && creditScore <= 900;
    }

    /**
     * Checks if this score has scoring elements data.
     */
    public boolean hasScoringElements() {
        return scoringElements != null && scoringElements.isArray() && scoringElements.size() > 0;
    }

    /**
     * Checks if this score has provider-specific metadata.
     */
    public boolean hasProviderMetadata() {
        return providerMetadata != null && !providerMetadata.isEmpty();
    }
}
