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
package org.apache.fineract.extend.creditbureau.handler;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.extend.creditbureau.service.ClientCreditBureauWritePlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Command handler for pulling credit bureau reports from external providers.
 *
 * This handler processes commands to pull credit reports from configured providers (like Decentro) and stores the
 * results in the credit bureau data tables.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "credit.bureau.provider.enabled", havingValue = "true", matchIfMissing = false)
@CommandType(entity = "CLIENT_CREDIT_REPORT", action = "PULL")
public class PullCreditBureauReportCommandHandler implements NewCommandSourceHandler {

    private final ClientCreditBureauWritePlatformService clientCreditBureauWritePlatformService;

    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        return this.clientCreditBureauWritePlatformService.pullCreditReport(command);
    }
}
