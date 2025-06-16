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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for KFS Document Generation Statistics Provides aggregated metrics for KFS document generation activities
 */
public class KfsDocumentGenerationStatistics {

    @JsonProperty("totalGenerationsRequested")
    private Long totalGenerationsRequested;

    @JsonProperty("successfulGenerations")
    private Long successfulGenerations;

    @JsonProperty("failedGenerations")
    private Long failedGenerations;

    @JsonProperty("averageGenerationTimeMs")
    private BigDecimal averageGenerationTimeMs;

    @JsonProperty("totalDocumentsGenerated")
    private Long totalDocumentsGenerated;

    @JsonProperty("documentsGeneratedToday")
    private Long documentsGeneratedToday;

    @JsonProperty("documentsGeneratedThisMonth")
    private Long documentsGeneratedThisMonth;

    @JsonProperty("mostUsedTemplate")
    private String mostUsedTemplate;

    @JsonProperty("lastGenerationDate")
    private LocalDate lastGenerationDate;

    @JsonProperty("totalFileSizeBytes")
    private Long totalFileSizeBytes;

    // Constructors
    public KfsDocumentGenerationStatistics() {}

    public KfsDocumentGenerationStatistics(Long totalGenerationsRequested, Long successfulGenerations, Long failedGenerations,
            BigDecimal averageGenerationTimeMs, Long totalDocumentsGenerated, Long documentsGeneratedToday,
            Long documentsGeneratedThisMonth, String mostUsedTemplate, LocalDate lastGenerationDate, Long totalFileSizeBytes) {
        this.totalGenerationsRequested = totalGenerationsRequested;
        this.successfulGenerations = successfulGenerations;
        this.failedGenerations = failedGenerations;
        this.averageGenerationTimeMs = averageGenerationTimeMs;
        this.totalDocumentsGenerated = totalDocumentsGenerated;
        this.documentsGeneratedToday = documentsGeneratedToday;
        this.documentsGeneratedThisMonth = documentsGeneratedThisMonth;
        this.mostUsedTemplate = mostUsedTemplate;
        this.lastGenerationDate = lastGenerationDate;
        this.totalFileSizeBytes = totalFileSizeBytes;
    }

    // Getters and Setters
    public Long getTotalGenerationsRequested() {
        return totalGenerationsRequested;
    }

    public void setTotalGenerationsRequested(Long totalGenerationsRequested) {
        this.totalGenerationsRequested = totalGenerationsRequested;
    }

    public Long getSuccessfulGenerations() {
        return successfulGenerations;
    }

    public void setSuccessfulGenerations(Long successfulGenerations) {
        this.successfulGenerations = successfulGenerations;
    }

    public Long getFailedGenerations() {
        return failedGenerations;
    }

    public void setFailedGenerations(Long failedGenerations) {
        this.failedGenerations = failedGenerations;
    }

    public BigDecimal getAverageGenerationTimeMs() {
        return averageGenerationTimeMs;
    }

    public void setAverageGenerationTimeMs(BigDecimal averageGenerationTimeMs) {
        this.averageGenerationTimeMs = averageGenerationTimeMs;
    }

    public Long getTotalDocumentsGenerated() {
        return totalDocumentsGenerated;
    }

    public void setTotalDocumentsGenerated(Long totalDocumentsGenerated) {
        this.totalDocumentsGenerated = totalDocumentsGenerated;
    }

    public Long getDocumentsGeneratedToday() {
        return documentsGeneratedToday;
    }

    public void setDocumentsGeneratedToday(Long documentsGeneratedToday) {
        this.documentsGeneratedToday = documentsGeneratedToday;
    }

    public Long getDocumentsGeneratedThisMonth() {
        return documentsGeneratedThisMonth;
    }

    public void setDocumentsGeneratedThisMonth(Long documentsGeneratedThisMonth) {
        this.documentsGeneratedThisMonth = documentsGeneratedThisMonth;
    }

    public String getMostUsedTemplate() {
        return mostUsedTemplate;
    }

    public void setMostUsedTemplate(String mostUsedTemplate) {
        this.mostUsedTemplate = mostUsedTemplate;
    }

    public LocalDate getLastGenerationDate() {
        return lastGenerationDate;
    }

    public void setLastGenerationDate(LocalDate lastGenerationDate) {
        this.lastGenerationDate = lastGenerationDate;
    }

    public Long getTotalFileSizeBytes() {
        return totalFileSizeBytes;
    }

    public void setTotalFileSizeBytes(Long totalFileSizeBytes) {
        this.totalFileSizeBytes = totalFileSizeBytes;
    }
}
