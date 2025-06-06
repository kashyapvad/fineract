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
package org.apache.fineract.infrastructure.dataqueries.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DatatableWriteService {

    @PreAuthorize(value = "hasAnyAuthority('ALL_FUNCTIONS', 'REGISTER_DATATABLE')")
    void registerDatatable(JsonCommand command);

    @PreAuthorize(value = "hasAnyAuthority('ALL_FUNCTIONS', 'REGISTER_DATATABLE')")
    void registerDatatable(String dataTableName, String applicationTableName, String entitySubType);

    @PreAuthorize(value = "hasAnyAuthority('ALL_FUNCTIONS', 'REGISTER_DATATABLE')")
    void registerDatatable(JsonCommand command, String permissionTable);

    @PreAuthorize(value = "hasAnyAuthority('ALL_FUNCTIONS', 'DEREGISTER_DATATABLE')")
    void deregisterDatatable(String datatable);

    CommandProcessingResult createDatatable(JsonCommand command);

    void updateDatatable(String datatableName, JsonCommand command);

    void deleteDatatable(String datatableName);

    CommandProcessingResult createNewDatatableEntry(String datatable, Long appTableId, JsonCommand command);

    CommandProcessingResult createNewDatatableEntry(String datatable, Long appTableId, String json);

    CommandProcessingResult updateDatatableEntryOneToOne(String datatable, Long appTableId, JsonCommand command);

    CommandProcessingResult updateDatatableEntryOneToMany(String datatable, Long appTableId, Long datatableId, JsonCommand command);

    CommandProcessingResult deleteDatatableEntries(String datatable, Long appTableId, JsonCommand command);

    CommandProcessingResult deleteDatatableEntry(String datatable, Long appTableId, Long datatableId, JsonCommand command);

    CommandProcessingResult createPPIEntry(String datatable, Long appTableId, JsonCommand command);

}
