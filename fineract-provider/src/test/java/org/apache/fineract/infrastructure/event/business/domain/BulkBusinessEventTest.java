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
package org.apache.fineract.infrastructure.event.business.domain;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientActivateBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientCreateBusinessEvent;
import org.apache.fineract.portfolio.client.domain.Client;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BulkBusinessEventTest {

    @Test
    public void testConstructorWorksForSameAggregateId() {
        // given
        List<BusinessEvent<?>> events = List.of(new ClientCreateBusinessEvent(client(1L)), new ClientActivateBusinessEvent(client(1L)));

        // when
        BulkBusinessEvent bulkEvent = new BulkBusinessEvent(events);

        // then
        Assertions.assertEquals(1L, bulkEvent.getAggregateRootId());
        Assertions.assertEquals(events, bulkEvent.get());
        Assertions.assertEquals(BulkBusinessEvent.TYPE, bulkEvent.getType());
        Assertions.assertEquals("Bulk", bulkEvent.getCategory());
    }

    @Test
    public void testConstructorThrowsExceptionForDifferentAggregateId() {
        // given
        List<BusinessEvent<?>> events = List.of(new ClientCreateBusinessEvent(client(1L)), new ClientActivateBusinessEvent(client(2L)));

        // when/then
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> new BulkBusinessEvent(events));
        Assertions.assertEquals("The business events are related to multiple aggregate roots which is not allowed", exception.getMessage());
    }

    @Test
    public void testConstructorWorksForNullAggregateId() {
        // given
        List<BusinessEvent<?>> events = List.of(new ClientCreateBusinessEvent(client(1L)), new ClientActivateBusinessEvent(client(null)));

        // when
        BulkBusinessEvent bulkEvent = new BulkBusinessEvent(events);

        // then
        Assertions.assertEquals(1L, bulkEvent.getAggregateRootId());
        Assertions.assertEquals(events, bulkEvent.get());
    }

    private Client client(Long id) {
        Client client = mock(Client.class);
        given(client.getId()).willReturn(id);
        return client;
    }
}
