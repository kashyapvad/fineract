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
package org.apache.fineract.infrastructure.campaigns.sms.constants;

import lombok.Getter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

@Getter
public enum SmsCampaignTriggerType {

    INVALID(-1, "triggerType.invalid"), //
    DIRECT(1, "triggerType.direct"), //
    SCHEDULE(2, "triggerType.schedule"), //
    TRIGGERED(3, "triggerType.triggered"); //

    private final Integer value;
    private final String code;

    SmsCampaignTriggerType(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public static SmsCampaignTriggerType fromInt(final Integer v) {
        if (v == null) {
            return INVALID;
        }

        return switch (v) {
            case 1 -> DIRECT;
            case 2 -> SCHEDULE;
            case 3 -> TRIGGERED;
            default -> INVALID;
        };
    }

    public static EnumOptionData triggerType(final Integer triggerTypeId) {
        return triggerType(SmsCampaignTriggerType.fromInt(triggerTypeId));
    }

    public static EnumOptionData triggerType(final SmsCampaignTriggerType triggerType) {
        return switch (triggerType) {
            case DIRECT -> new EnumOptionData(DIRECT.getValue().longValue(), DIRECT.getCode(), "Direct");
            case SCHEDULE -> new EnumOptionData(SCHEDULE.getValue().longValue(), SCHEDULE.getCode(), "Schedule");
            case TRIGGERED -> new EnumOptionData(TRIGGERED.getValue().longValue(), TRIGGERED.getCode(), "Triggered");
            default -> new EnumOptionData(INVALID.getValue().longValue(), INVALID.getCode(), "Invalid");
        };
    }

}
