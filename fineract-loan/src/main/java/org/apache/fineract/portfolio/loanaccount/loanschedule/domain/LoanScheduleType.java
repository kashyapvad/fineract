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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

@Getter
@RequiredArgsConstructor
public enum LoanScheduleType {

    CUMULATIVE("Cumulative"), //
    PROGRESSIVE("Progressive"); //

    private final String humanReadableName;

    public static List<EnumOptionData> getValuesAsEnumOptionDataList() {
        return Arrays.stream(values()).map(v -> new EnumOptionData((long) (v.ordinal() + 1), v.name(), v.getHumanReadableName())).toList();
    }

    public EnumOptionData asEnumOptionData() {
        return new EnumOptionData((long) this.ordinal(), this.name(), this.humanReadableName);
    }

    public static LoanScheduleType fromEnumOptionData(EnumOptionData enumOptionData) {
        if (enumOptionData == null) {
            return null;
        }
        return valueOf(enumOptionData.getCode());
    }
}
