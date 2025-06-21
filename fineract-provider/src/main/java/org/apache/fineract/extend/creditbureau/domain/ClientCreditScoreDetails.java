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
package org.apache.fineract.extend.creditbureau.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.extend.converter.PostgresJsonbConverter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

/**
 * Entity representing individual credit scores within a credit report.
 *
 * This entity stores individual credit scores from different scoring models (e.g., ERS, FICO, CIBIL Score) that are
 * part of a comprehensive credit report. Each credit report can have multiple scores from different models/versions.
 *
 * Examples of score models: - Decentro: ERS 4.0, FICO 2.1 - CIBIL: TransUnion CIBIL Score 2.0, CIBIL Score 3.0 -
 * Equifax: Equifax Risk Score 3.0
 */
@Entity
@Table(name = "m_extend_client_credit_score", uniqueConstraints = @UniqueConstraint(name = "uk_extend_credit_score_report_model", columnNames = {
        "credit_report_id", "score_model" }))
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class ClientCreditScoreDetails extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_report_id", nullable = false)
    private ClientCreditReportDetails creditReport;

    // Score Identification
    @Column(name = "score_model", nullable = false, length = 50)
    private String scoreModel;

    @Column(name = "score_version", length = 20)
    private String scoreVersion;

    @Column(name = "score_name", length = 100)
    private String scoreName;

    // Score Value and Date
    @Column(name = "credit_score", nullable = false)
    private Integer creditScore;

    @Column(name = "score_date")
    private LocalDate scoreDate;

    // Score Metadata
    @Column(name = "score_range_min")
    private Integer scoreRangeMin;

    @Column(name = "score_range_max")
    private Integer scoreRangeMax;

    @Column(name = "score_percentile", precision = 5, scale = 2)
    private BigDecimal scorePercentile;

    // Scoring Elements (factors that influenced the score)
    @Convert(converter = PostgresJsonbConverter.class)
    @Column(name = "scoring_elements", columnDefinition = "JSONB")
    private JsonNode scoringElements;

    @Column(name = "score_reason", columnDefinition = "TEXT")
    private String scoreReason;

    // Provider Specific Data
    @Column(name = "provider_score_id", length = 255)
    private String providerScoreId;

    /**
     * Provider-specific metadata about the scoring calculation. This can include confidence scores, model versions,
     * bureau-specific flags, etc.
     */
    @Convert(converter = PostgresJsonbConverter.class)
    @Column(name = "provider_metadata", columnDefinition = "JSONB")
    private JsonNode providerMetadata;

    /**
     * Static factory method for creating credit score details.
     */
    public static ClientCreditScoreDetails createScore(ClientCreditReportDetails creditReport, String scoreModel, String scoreVersion,
            String scoreName, Integer creditScore, LocalDate scoreDate) {

        return new ClientCreditScoreDetails().setCreditReport(creditReport).setScoreModel(scoreModel).setScoreVersion(scoreVersion)
                .setScoreName(scoreName).setCreditScore(creditScore).setScoreDate(scoreDate);
    }

    /**
     * Static factory method for creating credit score details from Decentro API response.
     */
    public static ClientCreditScoreDetails createFromDecentroResponse(ClientCreditReportDetails creditReport, JsonNode scoreDetailsNode) {

        String scoreType = scoreDetailsNode.path("type").asText();
        String scoreVersion = scoreDetailsNode.path("version").asText();
        String scoreName = scoreDetailsNode.path("name").asText();
        Integer scoreValue = scoreDetailsNode.path("value").asInt();
        JsonNode scoringElements = scoreDetailsNode.path("scoringElements");

        return new ClientCreditScoreDetails().setCreditReport(creditReport).setScoreModel(scoreType).setScoreVersion(scoreVersion)
                .setScoreName(scoreName).setCreditScore(scoreValue).setScoreDate(LocalDate.now())
                .setScoringElements(scoringElements.isArray() ? scoringElements : null);
    }

    /**
     * Sets score range information.
     */
    public ClientCreditScoreDetails withScoreRange(Integer minScore, Integer maxScore) {
        this.scoreRangeMin = minScore;
        this.scoreRangeMax = maxScore;
        return this;
    }

    /**
     * Sets score percentile information.
     */
    public ClientCreditScoreDetails withScorePercentile(BigDecimal percentile) {
        this.scorePercentile = percentile;
        return this;
    }

    /**
     * Sets scoring elements that influenced this score.
     */
    public ClientCreditScoreDetails withScoringElements(JsonNode scoringElements) {
        this.scoringElements = scoringElements;
        return this;
    }

    /**
     * Sets score reason/explanation.
     */
    public ClientCreditScoreDetails withScoreReason(String scoreReason) {
        this.scoreReason = scoreReason;
        return this;
    }

    /**
     * Sets provider-specific metadata.
     */
    public ClientCreditScoreDetails withProviderMetadata(String providerScoreId, JsonNode providerMetadata) {
        this.providerScoreId = providerScoreId;
        this.providerMetadata = providerMetadata;
        return this;
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

    // Convenience methods for accessing related data
    public Long getCreditReportId() {
        return this.creditReport != null ? this.creditReport.getId() : null;
    }

    public Long getClientId() {
        return this.creditReport != null ? this.creditReport.getClientId() : null;
    }

    public String getCreditBureauProvider() {
        return this.creditReport != null ? this.creditReport.getCreditBureauProvider() : null;
    }
}
