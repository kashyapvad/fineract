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
package org.apache.fineract.extend.kfs.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for KfsTemplate entities. Provides standard CRUD operations and custom query methods for KFS template
 * management.
 */
@Repository
public interface KfsTemplateRepository extends JpaRepository<KfsTemplate, Long> {

    /**
     * Find template by name and version combination. Used to ensure unique template name-version pairs.
     */
    @Query("SELECT t FROM KfsTemplate t WHERE t.templateName = :templateName AND t.templateVersion = :templateVersion")
    Optional<KfsTemplate> findByNameAndVersion(@Param("templateName") String templateName,
            @Param("templateVersion") String templateVersion);

    /**
     * Find all templates with the specified name (all versions).
     */
    List<KfsTemplate> findByTemplateName(String templateName);

    /**
     * Find all templates that are marked as active versions.
     */
    List<KfsTemplate> findByIsActiveVersionTrue();

    /**
     * Find templates by template type.
     */
    List<KfsTemplate> findByTemplateType(String templateType);

    /**
     * Find the latest version of a template by name. Assumes version strings can be compared lexicographically or
     * numerically. Note: JPQL doesn't support LIMIT, so we use setMaxResults() in the service layer
     */
    @Query("SELECT t FROM KfsTemplate t WHERE t.templateName = :templateName ORDER BY t.templateVersion DESC")
    List<KfsTemplate> findByTemplateNameOrderByVersionDesc(@Param("templateName") String templateName);

    /**
     * Find the latest version of a template by name. Returns the template with the highest version number for the given
     * name.
     */
    @Query(value = "SELECT * FROM kfs_template t WHERE t.template_name = :templateName ORDER BY t.template_version DESC LIMIT 1", nativeQuery = true)
    Optional<KfsTemplate> findLatestVersionByName(@Param("templateName") String templateName);

    /**
     * Find active version of a template by name.
     */
    @Query("SELECT t FROM KfsTemplate t WHERE t.templateName = :templateName AND t.isActiveVersion = true")
    Optional<KfsTemplate> findActiveVersionByName(@Param("templateName") String templateName);

    /**
     * Delete template by name and version. Returns the number of deleted records.
     */
    @Modifying
    @Query("DELETE FROM KfsTemplate t WHERE t.templateName = :templateName AND t.templateVersion = :templateVersion")
    long deleteByNameAndVersion(@Param("templateName") String templateName, @Param("templateVersion") String templateVersion);

    /**
     * Count templates with the specified name.
     */
    long countByTemplateName(String templateName);

    /**
     * Check if template exists with specified name and version.
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM KfsTemplate t WHERE t.templateName = :templateName AND t.templateVersion = :templateVersion")
    boolean existsByNameAndVersion(@Param("templateName") String templateName, @Param("templateVersion") String templateVersion);

    /**
     * Find active templates ordered by template name.
     */
    List<KfsTemplate> findByIsActiveVersionTrueOrderByTemplateName();

    /**
     * Find the latest active template by ID (most recently created).
     */
    Optional<KfsTemplate> findFirstByIsActiveVersionTrueOrderByIdDesc();
}
