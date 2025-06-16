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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * Application context initializer that loads environment variables from .env file.
 *
 * This initializer is part of the extend module and runs early in the Spring Boot startup process to load environment
 * variables from a .env file before other configurations are processed. This ensures credit bureau provider
 * configuration variables are available when needed.
 */
@Slf4j
public class DotEnvApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String DOT_ENV_FILE = ".env";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        try {
            Path envFile = Paths.get(DOT_ENV_FILE);

            if (Files.exists(envFile)) {
                Properties envProperties = loadEnvFile(envFile);

                if (!envProperties.isEmpty()) {
                    PropertiesPropertySource propertySource = new PropertiesPropertySource("dotenv-extend", envProperties);
                    environment.getPropertySources().addFirst(propertySource);

                    log.info("Successfully loaded {} environment variables from .env file for extend module", envProperties.size());
                } else {
                    log.debug(".env file is empty");
                }
            } else {
                log.debug(".env file not found at: {}", envFile.toAbsolutePath());
            }

        } catch (Exception e) {
            log.warn("Failed to load .env file for extend module: {}", e.getMessage());
            log.debug("Full error details:", e);
        }
    }

    private Properties loadEnvFile(Path envFile) throws IOException {
        Properties properties = new Properties();

        Files.lines(envFile).forEach(line -> {
            line = line.trim();

            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#")) {
                return;
            }

            // Parse KEY=VALUE format
            int equalIndex = line.indexOf('=');
            if (equalIndex > 0) {
                String key = line.substring(0, equalIndex).trim();
                String value = line.substring(equalIndex + 1).trim();

                // Remove quotes if present
                if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }

                properties.setProperty(key, value);
                log.debug("Loaded environment variable for extend module: {}", key);
            }
        });

        return properties;
    }
}
