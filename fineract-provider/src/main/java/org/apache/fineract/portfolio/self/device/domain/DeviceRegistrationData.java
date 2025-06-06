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
package org.apache.fineract.portfolio.self.device.domain;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.portfolio.client.data.ClientData;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class DeviceRegistrationData {

    public Long id;
    public ClientData clientData;
    public String registrationId;
    public OffsetDateTime updatedOnDate;

    public static DeviceRegistrationData instance(final Long id, final ClientData clientData, final String registrationId,
            final OffsetDateTime updatedOnDate) {
        return new DeviceRegistrationData().setId(id).setClientData(clientData).setRegistrationId(registrationId)
                .setUpdatedOnDate(updatedOnDate);
    }

}
