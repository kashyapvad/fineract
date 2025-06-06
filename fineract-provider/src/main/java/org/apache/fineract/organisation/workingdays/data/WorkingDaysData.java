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
package org.apache.fineract.organisation.workingdays.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

@Data
@NoArgsConstructor
public class WorkingDaysData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String recurrence;

    private EnumOptionData repaymentRescheduleType;

    private Boolean extendTermForDailyRepayments;

    private Boolean extendTermForRepaymentsOnHolidays;

    // template date
    @SuppressWarnings("unused")
    private Collection<EnumOptionData> repaymentRescheduleOptions;

    public WorkingDaysData(Long id, String recurrence, EnumOptionData repaymentRescheduleType, Boolean extendTermForDailyRepayments,
            Boolean extendTermForRepaymentsOnHolidays) {
        this.id = id;
        this.recurrence = recurrence;
        this.repaymentRescheduleType = repaymentRescheduleType;
        this.repaymentRescheduleOptions = null;
        this.extendTermForDailyRepayments = extendTermForDailyRepayments;
        this.extendTermForRepaymentsOnHolidays = extendTermForRepaymentsOnHolidays;
    }

    public WorkingDaysData(Long id, String recurrence, EnumOptionData repaymentRescheduleType,
            Collection<EnumOptionData> repaymentRescheduleOptions, Boolean extendTermForDailyRepayments,
            Boolean extendTermForRepaymentsOnHolidays) {
        this.id = id;
        this.recurrence = recurrence;
        this.repaymentRescheduleType = repaymentRescheduleType;
        this.repaymentRescheduleOptions = repaymentRescheduleOptions;
        this.extendTermForDailyRepayments = extendTermForDailyRepayments;
        this.extendTermForRepaymentsOnHolidays = extendTermForRepaymentsOnHolidays;
    }

    public WorkingDaysData(WorkingDaysData data, Collection<EnumOptionData> repaymentRescheduleOptions) {
        this.id = data.id;
        this.recurrence = data.recurrence;
        this.repaymentRescheduleType = data.repaymentRescheduleType;
        this.repaymentRescheduleOptions = repaymentRescheduleOptions;
        this.extendTermForDailyRepayments = data.extendTermForDailyRepayments;
        this.extendTermForRepaymentsOnHolidays = data.extendTermForRepaymentsOnHolidays;
    }
}
