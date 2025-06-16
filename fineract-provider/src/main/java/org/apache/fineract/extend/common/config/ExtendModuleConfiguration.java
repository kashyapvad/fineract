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
package org.apache.fineract.extend.common.config;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.extend.common.provider.CreditBureauProviderFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for extend module components.
 *
 * This configuration handles the optional nature of credit bureau providers by providing appropriate beans based on
 * whether a CreditBureauProviderFactory is available.
 */
@Configuration
@Slf4j
public class ExtendModuleConfiguration {

    /**
     * Provides an empty Optional CreditBureauProviderFactory when the actual credit bureau provider is not available.
     */
    @Bean
    @ConditionalOnMissingBean(CreditBureauProviderFactory.class)
    public Optional<CreditBureauProviderFactory> emptyCreditBureauProviderFactory() {
        log.info("No CreditBureauProviderFactory available - credit bureau features disabled");
        return Optional.empty();
    }

    /**
     * Wraps the actual CreditBureauProviderFactory in an Optional when it is available.
     */
    @Bean
    @ConditionalOnBean(CreditBureauProviderFactory.class)
    public Optional<CreditBureauProviderFactory> availableCreditBureauProviderFactory(CreditBureauProviderFactory factory) {
        log.info("DEBUG-CONFIG: Creating Optional<CreditBureauProviderFactory> with actual factory: {}",
                factory.getClass().getSimpleName());
        return Optional.of(factory);
    }
}
