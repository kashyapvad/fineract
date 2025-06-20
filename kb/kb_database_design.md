# Fineract Database Design Patterns

> **Note:** This file contains database design rules for schema architecture and Java entity patterns.
> For migration writing rules, see `kb_database_migrations.md`

## Multi-Tenant Database Rules

### RULE: Tenant Context Isolation
CONTEXT: Complete data separation between tenants in multi-tenant architecture
REQUIREMENT: All database operations must be scoped to current tenant context with no data leakage
FAIL IF:
- Database queries executed without tenant context validation
- Tenant isolation bypassed for administrative operations
- Cross-tenant data access in single query
- Missing tenant validation in repository methods
- Default tenant context used in production environments
VERIFICATION: Check ThreadLocalContextUtil usage and tenant scoping in queries
REFERENCES: ThreadLocalContextUtil implementation, tenant-scoped repository methods

### RULE: Tenant Database Schema Management
CONTEXT: Isolated database schemas for each tenant ensuring data privacy
REQUIREMENT: Each tenant must have dedicated database schema with proper access controls
FAIL IF:
- Shared tables between multiple tenants
- Missing schema-level access controls
- Tenant schema creation not automated
- Schema naming conventions not followed
- Cross-schema references causing data integrity issues
VERIFICATION: Check tenant schema creation and access control patterns
REFERENCES: Tenant schema setup scripts, database schema configurations

### RULE: Tenant Connection Pool Management
CONTEXT: Efficient database connection management per tenant
REQUIREMENT: Database connection pools must be properly managed per tenant with appropriate sizing
FAIL IF:
- Connection pools not properly configured per tenant
- Connection leaks affecting tenant performance
- Missing connection pool monitoring and alerting
- Connection pool sizes not optimized for tenant load
- Connection timeout configurations not appropriate
VERIFICATION: Check HikariCP configuration and connection pool management
REFERENCES: Database configuration files, connection pool settings

### RULE: API Resource Permission Pattern
CONTEXT: All API resource endpoints require corresponding permissions in the database for authorization
REQUIREMENT: When creating new API resources, corresponding permissions must be added to migration files
FAIL IF:
- API resource created without corresponding READ/CREATE/UPDATE/DELETE permissions
- Permission codes don't match the entity naming convention (e.g., CLIENT_KYC, CLIENT_CREDIT_REPORT)
- Missing PULL or custom action permissions for specialized operations
- Command handler @CommandType action doesn't have matching permission with same action_name
- Permissions not grouped correctly under "portfolio" or appropriate grouping
- can_maker_checker not properly set (usually false for extension features)
VERIFICATION: Check that all API resources have matching permissions in m_permission table and command handler actions have corresponding permissions
REFERENCES:
  - PATTERN_EXAMPLE: `9000_create_extend_kyc_tables.xml:451-489` (CLIENT_KYC permissions standard template)
  - PATTERN_EXAMPLE: `9001_create_extend_credit_report_table.xml:452-490` (CLIENT_CREDIT_REPORT permissions)  
  - PATTERN_EXAMPLE: `ClientKycApiResource.java:89-92` (permission validation in API endpoint)
  - IMPLEMENTATION: `ClientKycApiResource.java:118-121` (validateHasReadPermission usage)
  - VERIFICATION_CMD: `grep -rn "validateHasReadPermission\|validateHasCreatePermission" fineract-provider/src/main/java/ | grep -o '"[^"]*"' | sort -u`
  - VERIFICATION_CMD: `grep -rn "entity_name.*CLIENT_" fineract-provider/src/main/resources/db/changelog/`
  - ANTI_PATTERN: API resources without corresponding database permissions causing 403 errors

### RULE: JPA Audit Field Column Naming Consistency [CRITICAL]
CONTEXT: JPA entities using AbstractAuditableWithUTCDateTimeCustom require specific audit column names defined in AuditableFieldsConstants
REQUIREMENT: Database migrations for entities extending AbstractAuditableWithUTCDateTimeCustom must use JPA expected column names, not legacy naming
FAIL IF:
- Using legacy audit columns (createdby_id, created_date, lastmodifiedby_id, lastmodified_date) with AbstractAuditableWithUTCDateTimeCustom
- Migration creates columns that don't match JPA @Column annotations in audit base class
- Mismatch between database schema and entity mapping causing "Unknown column" SQL errors
- New tables created with legacy naming instead of current JPA standard
VERIFICATION: Check entity inheritance and corresponding migration column names match exactly
REFERENCES:
  - JPA_EXPECTED_COLUMNS: created_by, created_on_utc, last_modified_by, last_modified_on_utc
  - LEGACY_COLUMNS: createdby_id, created_date, lastmodifiedby_id, lastmodified_date (used in 0001_initial_schema.xml)
  - MIGRATION_HISTORY: Core tables migrated from legacy to JPA naming in 0020_add_audit_entries.xml
  - CONSTANTS_FILE: AuditableFieldsConstants.java defines CREATED_BY_DB_FIELD = "created_by", CREATED_DATE_DB_FIELD = "created_on_utc"
  - BASE_CLASS: AbstractAuditableWithUTCDateTimeCustom.java uses AuditableFieldsConstants for @Column names
  - CURRENT_PATTERN: All new tables since 0020 use JPA naming (created_by, created_on_utc, etc.)
  - ANTI_PATTERN_EXAMPLE: Entity extends AbstractAuditableWithUTCDateTimeCustom but migration uses createdby_id column
  - VERIFICATION_CMD: `grep -rn "extends AbstractAuditableWithUTCDateTimeCustom" --include="*.java"`
  - VERIFICATION_CMD: `grep -rn "createdby_id\|created_date\|lastmodifiedby_id\|lastmodified_date" fineract-provider/src/main/resources/db/changelog/tenant/parts/90*.xml`
  - ERROR_EXAMPLE: "Unknown column 't1.created_by' in 'field list'" when JPA expects created_by but database has createdby_id
RULE_CLARIFICATION:
  - NEW TABLES: Always use JPA naming (created_by, created_on_utc, last_modified_by, last_modified_on_utc)
  - EXISTING CORE TABLES: Already migrated to JPA naming in migration 0020
  - LEGACY COLUMNS: Only exist in pre-0020 migration files and some reports that reference old column names
SOLUTION: Use JPA expected column names in all new migration files

### RULE: Command Handler Registration [CRITICAL]
CONTEXT: API endpoints using command processing framework require registered command handlers to prevent UnsupportedCommandException
REQUIREMENT: Every command created by ExtendCommandWrapperBuilder must have a corresponding command handler registered with @CommandType annotation
FAIL IF:
- API endpoint creates CommandWrapper but no corresponding @CommandType handler exists
- Command entity/action combination not registered in any @CommandType annotation
- CommandWrapper action name doesn't match any handler's @CommandType action
- Service implementation method exists but no command handler delegates to it
- Missing @Service annotation on command handler class
VERIFICATION: Check that every action in ExtendCommandWrapperBuilder has matching @CommandType(entity="ENTITY_NAME", action="ACTION_NAME")
REFERENCES: 
  - PATTERN_EXAMPLE: `ExtendCommandWrapperBuilder.java:175-195` (createClientKyc command wrapper)
  - PATTERN_EXAMPLE: `CreateClientKycCommandHandler.java:37` (@CommandType annotation with entity/action)
  - PATTERN_EXAMPLE: `VerifyClientKycApiCommandHandler.java:37` (API verification command handler)
  - HANDLER_INTERFACE: `NewCommandSourceHandler.java` (command handler interface requirement)
  - FRAMEWORK_DOCS: Command framework architecture, handler registration pattern

## Connection Pool and Performance Rules

### RULE: HikariCP Configuration Optimization
CONTEXT: Optimal database connection pool performance for high-load scenarios
REQUIREMENT: HikariCP must be configured with appropriate settings for expected load patterns
FAIL IF:
- Connection pool size not tuned for expected concurrent users
- Connection timeout settings causing user experience issues
- Missing connection pool metrics and monitoring
- Connection validation queries not optimized
- Pool configuration not environment-specific
VERIFICATION: Check HikariCP configuration parameters and performance metrics
REFERENCES: Database configuration properties, connection pool monitoring

### RULE: Database Query Performance
CONTEXT: Efficient database queries supporting high transaction volumes
REQUIREMENT: Database queries must be optimized with proper indexing and execution plans
FAIL IF:
- Missing database indexes for frequently queried columns
- N+1 query problems in ORM usage
- Inefficient JOIN operations affecting performance
- Missing query execution plan analysis
- Database queries not properly parameterized
VERIFICATION: Check query performance and database index usage
REFERENCES: Database query implementations, index definitions

### RULE: Transaction Management
CONTEXT: Proper transaction boundaries and isolation levels
REQUIREMENT: Database transactions must be properly scoped with appropriate isolation levels
FAIL IF:
- Transaction boundaries spanning multiple business operations
- Incorrect transaction isolation levels causing data inconsistency
- Missing transaction rollback handling
- Long-running transactions affecting system performance
- Transaction deadlock scenarios not properly handled
VERIFICATION: Check transaction configuration and management patterns
REFERENCES: Transaction management configuration, service transaction boundaries

### RULE: Database Connection Monitoring
CONTEXT: Proactive monitoring and alerting for database connectivity issues
REQUIREMENT: Database connection health must be monitored with appropriate alerting
FAIL IF:
- Missing database connection health checks
- Connection pool exhaustion not properly monitored
- Database performance metrics not tracked
- Missing alerting for database connectivity issues
- Connection leak detection not implemented
VERIFICATION: Check database monitoring and alerting configurations
REFERENCES: Health check implementations, monitoring configuration

## Data Integrity and Consistency Rules

### RULE: Foreign Key Constraint Management
CONTEXT: Data integrity through proper relationship constraints
REQUIREMENT: Database relationships must be enforced through foreign key constraints
FAIL IF:
- Missing foreign key constraints for entity relationships
- Cascade operations not properly configured
- Data integrity violations not prevented at database level
- Orphaned records not properly handled
- Constraint violations not properly reported
VERIFICATION: Check foreign key constraint definitions and cascade settings
REFERENCES: Entity relationship definitions, database constraint configurations

### RULE: Data Validation at Database Level
CONTEXT: Data consistency through database-level validation rules
REQUIREMENT: Critical business rules must be enforced at database level through constraints
FAIL IF:
- Business rule validation only at application level
- Missing check constraints for data ranges and formats
- Null constraints not properly defined for required fields
- Data validation rules not consistent across environments
- Validation error messages not user-friendly
VERIFICATION: Check database constraint definitions and validation rules
REFERENCES: Database constraint implementations, validation rule configurations

### RULE: Audit Trail Implementation
CONTEXT: Complete audit history for compliance and troubleshooting
REQUIREMENT: All data modifications must be tracked through comprehensive audit trails
FAIL IF:
- Missing audit records for critical data changes
- Audit trail not capturing sufficient context information
- Audit data not properly secured and immutable
- Audit record retention policies not properly implemented
- Audit query performance not optimized
VERIFICATION: Check audit trail implementation and data capture
REFERENCES: Audit entity definitions, audit trail recording mechanisms

### RULE: Data Archival and Retention
CONTEXT: Long-term data management and compliance with retention policies
REQUIREMENT: Data archival must be implemented with proper retention policies and compliance requirements
FAIL IF:
- Missing data archival procedures for historical records
- Data retention policies not properly implemented
- Archived data not accessible for compliance requirements
- Data purging affecting referential integrity
- Archival processes affecting system performance
VERIFICATION: Check data archival procedures and retention policy implementation
REFERENCES: Data archival scripts, retention policy configurations

## Critical Data Integrity Rules

### RULE: Permission-Entity Naming Consistency Across Components [CRITICAL]
CONTEXT: API resources, command handlers, and database permissions must use consistent entity naming to prevent authorization failures
REQUIREMENT: Entity names must be identical across ExtendCommandWrapperBuilder, API resources RESOURCE_NAME_FOR_PERMISSIONS, command handler @CommandType annotations, and database migration permissions
FAIL IF:
- ExtendCommandWrapperBuilder uses different entity name than command handler @CommandType
- API resource RESOURCE_NAME_FOR_PERMISSIONS differs from migration permission entity_name
- Command wrapper entity parameter doesn't match any database permission entity_name
- Inconsistent naming between related components causing permission validation failures
- Entity name changes in one component without updating all related components
VERIFICATION: Cross-reference entity names between command wrappers, handlers, API resources, and migration permissions
REFERENCES:
  - PATTERN_EXAMPLE: `9001_create_extend_credit_report_table.xml:453-481` (entity_name = "CLIENT_CREDIT_REPORT" in migration)
  - PATTERN_EXAMPLE: `CreateClientCreditBureauReportCommandHandler.java:37` (@CommandType entity = "CLIENT_CREDIT_REPORT")
  - PATTERN_EXAMPLE: `ExtendCommandWrapperBuilder.java:261` (entity = "CLIENT_CREDIT_REPORT" in command wrapper)
  - PATTERN_EXAMPLE: `ClientCreditBureauApiResource.java:67` (RESOURCE_NAME_FOR_PERMISSIONS = "CLIENT_CREDIT_REPORT")
  - VERIFICATION_CMD: `grep -rn "CLIENT_CREDIT" fineract-provider/src/main/java/org/apache/fineract/extend/ | grep -E "(CommandType|RESOURCE_NAME|entity.*=)"`
  - VERIFICATION_CMD: `grep -rn "entity_name.*CLIENT" fineract-provider/src/main/resources/db/changelog/tenant/parts/90*.xml`
COMPONENT ALIGNMENT CHECKLIST:
  - ✅ Migration permissions: entity_name values in INSERT INTO m_permission statements
  - ✅ Command handlers: @CommandType(entity = "ENTITY_NAME") annotations
  - ✅ Command wrappers: ExtendCommandWrapperBuilder entity parameter in CommandWrapper constructor
  - ✅ API resources: RESOURCE_NAME_FOR_PERMISSIONS constant for validateHasReadPermission() calls
  - ✅ API endpoints: Permission validation using correct entity name
  - ANTI_PATTERN: `ExtendCommandWrapperBuilder.java` using "CLIENT_CREDIT_BUREAU" while others use "CLIENT_CREDIT_REPORT"

### RULE: Complete API-Command-Permission Coverage [CRITICAL]
CONTEXT: Every API endpoint that processes commands must have complete permission and handler coverage to prevent runtime failures
REQUIREMENT: All API endpoints using commandsSourceWritePlatformService must have corresponding permissions and command handlers for every possible action
FAIL IF:
- API endpoint accepts command but no handler registered for entity/action combination
- API endpoint validates permissions that don't exist in migration files
- Command action in ExtendCommandWrapperBuilder without corresponding @CommandType handler
- Permission exists in migration but no API endpoint or handler uses it
- API endpoint missing permission validation before command processing
VERIFICATION: Audit API endpoints, command handlers, command wrappers, and migration permissions for complete coverage
REFERENCES:
  - PATTERN_EXAMPLE: `9000_create_extend_kyc_tables.xml:280` (CREATE_CLIENT_KYC permission definition)
  - PATTERN_EXAMPLE: `ClientKycApiResource.java:135-138` (API endpoint with permission validation and command processing)
  - PATTERN_EXAMPLE: `ExtendCommandWrapperBuilder.java:175-195` (createClientKyc command wrapper method)
  - PATTERN_EXAMPLE: `CreateClientKycCommandHandler.java:37` (@CommandType entity="CLIENT_KYC" action="CREATE")
  - COVERAGE_MATRIX: Map API endpoints → Command wrappers → Command handlers → Permissions
  - VERIFICATION_PATTERN: Every command action must have handler and permission
  - API_PATTERN: `commandsSourceWritePlatformService.logCommandSource(commandRequest)` requires handler
  - HANDLER_PATTERN: `@CommandType(entity = "ENTITY", action = "ACTION")` must match command wrapper action
  - PERMISSION_PATTERN: `action_name = "ACTION"` in migration must match handler and wrapper
COVERAGE_VERIFICATION_CHECKLIST:
  - ✅ API POST/PUT/DELETE endpoints have corresponding command wrappers
  - ✅ Command wrappers have matching command handler @CommandType annotations  
  - ✅ Command handlers have corresponding permissions in migration files
  - ✅ API endpoints validate permissions before creating command wrappers
  - ✅ No orphaned permissions without corresponding API functionality

### RULE: Child Entity Permission Inheritance Pattern [ARCHITECTURAL]
CONTEXT: Child entities (1-many relationships) should inherit permissions from parent entities rather than having separate permission sets
REQUIREMENT: Child entities accessed only through parent entity operations should use parent entity permissions to avoid permission proliferation
FAIL IF:
- Creating separate permissions for child entities that are always accessed through parent
- Child entity operations not properly scoped to parent entity permissions
- Permission explosion with separate CRUD permissions for every child entity
- Child entity permissions not properly linked to parent entity context
- Missing parent entity validation when operating on child entities
VERIFICATION: Check that child entities use parent permissions and are properly scoped
REFERENCES:
  - PATTERN_EXAMPLE: `9002_create_extend_credit_score_table.xml` (no permission creation for child entity)
  - PATTERN_EXAMPLE: `9001_create_extend_credit_report_table.xml:453-481` (parent entity permissions only)
  - PATTERN_EXAMPLE: `ClientCreditBureauApiResource.java:108-115` (parent permission validation for child access)
  - PATTERN_EXAMPLE: `ClientCreditScoreDetails.java:25-35` (child entity with @ManyToOne parent relationship)
  - CHILD_ENTITY_PATTERN: Child entities managed through parent entity API endpoints
  - ANTI_PATTERN: Separate permission sets for every child entity relationship
INHERITANCE_PATTERNS:
  - ✅ Credit Report (parent) → Credit Scores (children) use CLIENT_CREDIT_REPORT permissions
  - ✅ Client (parent) → KYC Details (child) use CLIENT_KYC permissions  
  - ✅ Child operations via parent API: `/clients/{id}/extend/creditreport/reports/{reportId}`
  - ❌ Separate permissions: CLIENT_CREDIT_SCORE_READ, CLIENT_CREDIT_SCORE_CREATE, etc. 