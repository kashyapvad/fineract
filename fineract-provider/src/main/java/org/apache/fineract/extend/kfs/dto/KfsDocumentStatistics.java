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

/**
 * DTO for KFS document statistics. Used for API responses containing document statistics information.
 */
public class KfsDocumentStatistics {

    private long totalDocuments;
    private long generatedCount;
    private long processingCount;
    private long deliveredCount;
    private long acknowledgedCount;
    private long failedCount;
    private long cancelledCount;
    private long totalFileSize;
    private double averageFileSize;
    private long documentsGeneratedToday;
    private long documentsDeliveredToday;

    // Default constructor
    public KfsDocumentStatistics() {}

    // Getters and Setters
    public long getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(long totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public long getGeneratedCount() {
        return generatedCount;
    }

    public void setGeneratedCount(long generatedCount) {
        this.generatedCount = generatedCount;
    }

    public long getProcessingCount() {
        return processingCount;
    }

    public void setProcessingCount(long processingCount) {
        this.processingCount = processingCount;
    }

    public long getDeliveredCount() {
        return deliveredCount;
    }

    public void setDeliveredCount(long deliveredCount) {
        this.deliveredCount = deliveredCount;
    }

    public long getAcknowledgedCount() {
        return acknowledgedCount;
    }

    public void setAcknowledgedCount(long acknowledgedCount) {
        this.acknowledgedCount = acknowledgedCount;
    }

    public long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(long failedCount) {
        this.failedCount = failedCount;
    }

    public long getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(long cancelledCount) {
        this.cancelledCount = cancelledCount;
    }

    public long getTotalFileSize() {
        return totalFileSize;
    }

    public void setTotalFileSize(long totalFileSize) {
        this.totalFileSize = totalFileSize;
    }

    public double getAverageFileSize() {
        return averageFileSize;
    }

    public void setAverageFileSize(double averageFileSize) {
        this.averageFileSize = averageFileSize;
    }

    public long getDocumentsGeneratedToday() {
        return documentsGeneratedToday;
    }

    public void setDocumentsGeneratedToday(long documentsGeneratedToday) {
        this.documentsGeneratedToday = documentsGeneratedToday;
    }

    public long getDocumentsDeliveredToday() {
        return documentsDeliveredToday;
    }

    public void setDocumentsDeliveredToday(long documentsDeliveredToday) {
        this.documentsDeliveredToday = documentsDeliveredToday;
    }
}
