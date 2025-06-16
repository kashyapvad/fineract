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

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Data Transfer Object for family member information in KFS documents. Contains family member details including
 * relationship hierarchy for KFS generation.
 */
@Data
@Builder
@Jacksonized
public class FamilyMemberData {

    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String relationship;
    private String gender;
    private LocalDate dateOfBirth;
    private Long age;
    private String qualification;
    private String profession;
    private String mobileNumber;
    private Boolean isDependent;
    private String maritalStatus;

    /**
     * Get full name of family member
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();

        if (firstName != null) {
            fullName.append(firstName);
        }

        if (middleName != null && !middleName.trim().isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(middleName);
        }

        if (lastName != null && !lastName.trim().isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }

        return fullName.toString();
    }

    /**
     * Get relationship priority for KFS document generation Lower number = higher priority
     */
    public int getRelationshipPriority() {
        if (relationship == null) {
            return 999; // Lowest priority
        }

        return switch (relationship.toLowerCase()) {
            case "father" -> 1;
            case "mother" -> 2;
            case "husband" -> 3;
            case "wife" -> 4;
            case "brother" -> 5;
            case "sister" -> 6;
            default -> 999; // Other relationships have lowest priority
        };
    }
}
