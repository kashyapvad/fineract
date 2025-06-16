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

import java.util.Map;
import lombok.Data;

/**
 * DTO for KFS generation statistics.
 */
@Data
public class KfsGenerationStatistics {

    private Long totalGenerated;
    private Long successfulGenerations;
    private Long failedGenerations;
    private Long generationsToday;
    private Double successRate;
    private Double averageGenerationTime;
    private Long activeTemplates;
    private Map<String, Integer> templateUsageStats;

    public KfsGenerationStatistics() {
        this.totalGenerated = 0L;
        this.successfulGenerations = 0L;
        this.failedGenerations = 0L;
        this.generationsToday = 0L;
        this.successRate = 0.0;
        this.averageGenerationTime = 0.0;
        this.activeTemplates = 0L;
    }

    // Getters and Setters
    public long getTotalGenerations() {
        return totalGenerated;
    }

    public void setTotalGenerations(long totalGenerations) {
        this.totalGenerated = totalGenerations;
    }

    public long getSuccessfulGenerations() {
        return successfulGenerations;
    }

    public void setSuccessfulGenerations(long successfulGenerations) {
        this.successfulGenerations = successfulGenerations;
    }

    public long getFailedGenerations() {
        return failedGenerations;
    }

    public void setFailedGenerations(long failedGenerations) {
        this.failedGenerations = failedGenerations;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getAverageGenerationTime() {
        return averageGenerationTime;
    }

    public void setAverageGenerationTime(double averageGenerationTime) {
        this.averageGenerationTime = averageGenerationTime;
    }

    public long getActiveTemplates() {
        return activeTemplates;
    }

    public void setActiveTemplates(long activeTemplates) {
        this.activeTemplates = activeTemplates;
    }

    public Map<String, Integer> getTemplateUsageStats() {
        return templateUsageStats;
    }

    public void setTemplateUsageStats(Map<String, Integer> templateUsageStats) {
        this.templateUsageStats = templateUsageStats;
    }
}
