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
package org.apache.fineract.extend.creditbureau.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * Exception thrown when a client credit bureau record is not found.
 */
public class ClientCreditBureauNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ClientCreditBureauNotFoundException(final Long reportId, final Long clientId) {
        super("error.msg.client.credit.bureau.report.not.found",
                "Credit bureau report with identifier " + reportId + " for client " + clientId + " does not exist", reportId, clientId);
    }

    public ClientCreditBureauNotFoundException(final Long reportId) {
        super("error.msg.client.credit.bureau.report.not.found", "Credit bureau report with identifier " + reportId + " does not exist",
                reportId);
    }

    public ClientCreditBureauNotFoundException(final Long reportId, final EmptyResultDataAccessException e) {
        super("error.msg.client.credit.bureau.report.not.found", "Credit bureau report with identifier " + reportId + " does not exist",
                reportId, e);
    }
}
