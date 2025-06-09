package org.apache.fineract.extend.common.config;

import java.util.Optional;

import org.apache.fineract.extend.common.provider.CreditBureauProviderFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the Extend module that provides optional beans
 * for external service integrations like credit bureau providers.
 */
@Configuration
public class ExtendModuleConfiguration {

    /**
     * Provides an empty Optional CreditBureauProviderFactory when the actual
     * credit bureau provider is not configured/available.
     * This allows KYC functionality to work independently without external dependencies.
     */
    @Bean
    @ConditionalOnMissingBean(CreditBureauProviderFactory.class)
    public Optional<CreditBureauProviderFactory> optionalCreditBureauProviderFactory() {
        return Optional.empty();
    }

    /**
     * Wraps the actual CreditBureauProviderFactory in an Optional when it is available.
     */
    @Bean
    @ConditionalOnBean(CreditBureauProviderFactory.class)
    public Optional<CreditBureauProviderFactory> availableCreditBureauProviderFactory(CreditBureauProviderFactory factory) {
        return Optional.of(factory);
    }
} 