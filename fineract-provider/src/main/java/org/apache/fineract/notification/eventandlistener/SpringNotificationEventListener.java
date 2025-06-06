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
package org.apache.fineract.notification.eventandlistener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.notification.data.NotificationData;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Profile("!activeMqEnabled")
@RequiredArgsConstructor
@Slf4j
public class SpringNotificationEventListener implements ApplicationListener<NotificationEvent> {

    private final NotificationEventListener notificationEventListener;

    @Override
    public void onApplicationEvent(@NonNull final NotificationEvent event) {
        log.debug("Processing Spring notification event {}", event);
        try {
            ThreadLocalContextUtil.init(event.getContext());
            final NotificationData notificationData = event.getNotificationData();
            notificationEventListener.receive(notificationData);
        } finally {
            ThreadLocalContextUtil.reset();
        }
    }

}
