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
package org.apache.fineract.infrastructure.campaigns.constants;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public enum CampaignType {

    INVALID(0, "campaignType.invalid"), //
    SMS(1, "campaignType.sms"), //
    NOTIFICATION(2, "campaignType.notification"); //

    private final Integer value;
    private final String code;

    CampaignType(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static CampaignType fromInt(final Integer typeValue) {
        if (typeValue == null) {
            return INVALID;
        }

        switch (typeValue) {
            case 0:
                return INVALID;
            case 1:
                return SMS;
            case 2:
                return NOTIFICATION;
            default:
                return INVALID;
        }
    }

    public static EnumOptionData campaignType(final Integer campaignTypeId) {
        return campaignType(CampaignType.fromInt(campaignTypeId));
    }

    public static EnumOptionData campaignType(final CampaignType campaignType) {
        EnumOptionData optionData = new EnumOptionData(INVALID.getValue().longValue(), INVALID.getCode(), "Invalid");
        switch (campaignType) {
            case INVALID:
                optionData = new EnumOptionData(INVALID.getValue().longValue(), INVALID.getCode(), "Invalid");
            break;
            case SMS:
                optionData = new EnumOptionData(SMS.getValue().longValue(), SMS.getCode(), "SMS");
            break;
            case NOTIFICATION:
                optionData = new EnumOptionData(NOTIFICATION.getValue().longValue(), NOTIFICATION.getCode(), "NOTIFICATION");
            break;
        }
        return optionData;
    }

    public boolean isSms() {
        return this.equals(SMS);
    }
}
