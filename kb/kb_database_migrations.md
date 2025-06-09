# Fineract Database Migration Patterns
## XML Database Migration Rules [P0]

### RULE: Apache RAT License Header Compliance
CONTEXT: All XML migration files must pass Apache RAT license validation during build
REQUIREMENT: Every XML database migration file must contain proper Apache license header
FAIL IF:
- XML files missing Apache Software Foundation license header comment block
- License header format doesn't match APACHE_LICENSETEXT.md template
- License header positioned incorrectly (must be after XML declaration, before root element)
- License header contains formatting errors or truncated text
- Migration files bypassed in RAT excludes without justification
VERIFICATION: Run `./gradlew rat` to validate license headers in all XML files
REFERENCES: 
  - PATTERN_EXAMPLE: `0001_initial_schema.xml:1-20` (proper Apache license header format)
  - PATTERN_EXAMPLE: `9000_create_extend_kyc_tables.xml:1-20` (license header after XML declaration)
  - BUILD_VALIDATION: `build.gradle:rat` task configuration for license validation
  - LICENSE_TEMPLATE: `APACHE_LICENSETEXT.md` canonical license text format
  - VERIFICATION_CMD: `./gradlew rat | grep -A5 -B5 "FAIL"`

### RULE: Liquibase XML Schema Declaration
CONTEXT: Proper XML schema validation for Liquibase changesets
REQUIREMENT: All migration XML files must declare correct Liquibase namespace and schema location
FAIL IF:
- Missing or incorrect xmlns declarations for Liquibase namespace
- Wrong schema location URL for Liquibase XSD validation
- Schema version mismatch with project's Liquibase version
- Multiple conflicting namespace declarations
- Missing xsi:schemaLocation attribute
VERIFICATION: Validate XML against Liquibase XSD schema
REFERENCES:
  - PATTERN_EXAMPLE: `0001_initial_schema.xml:21-24` (correct Liquibase namespace declarations)
  - PATTERN_EXAMPLE: `9000_create_extend_kyc_tables.xml:21-24` (schema location declaration)
  - SCHEMA_VALIDATION: Liquibase XSD validation against current version
  - VERIFICATION_CMD: `xmllint --schema liquibase-4.3.xsd *.xml`

### RULE: Database-Specific ChangeSet Context
CONTEXT: Multi-database compatibility through proper context usage
REQUIREMENT: Database-specific migrations must use proper context attributes to target appropriate databases
FAIL IF:
- Missing context attribute for database-specific changesets
- Using generic changesets for database-specific features (JSON vs JSONB)
- MySQL-specific syntax used without context="mysql"
- PostgreSQL-specific syntax used without context="postgresql"
- Data type conflicts between MySQL and PostgreSQL contexts
VERIFICATION: Check context usage in changesets and test against both MySQL and PostgreSQL
REFERENCES:
  - PATTERN_EXAMPLE: `9000_create_extend_kyc_tables.xml:25-35` (MySQL context with JSON and DATETIME(6) types)
  - PATTERN_EXAMPLE: `9000_create_extend_kyc_tables.xml:147-157` (PostgreSQL context with JSONB and TIMESTAMP)
  - CONTEXT_VALIDATION: Database-specific changeset context attribute usage
  - VERIFICATION_CMD: `grep -rn 'context="mysql\|context="postgresql"' fineract-provider/src/main/resources/sql/migrations/`

### RULE: ChangeSet ID and Author Requirements
CONTEXT: Unique identification and tracking of database changes
REQUIREMENT: Every changeset must have unique ID and proper author attribution
FAIL IF:
- Missing or empty changeset id attribute
- Duplicate changeset IDs within same changelog file
- Missing or generic author attribute (must be "fineract")
- ChangeSet IDs not sequential or properly organized
- Missing changeset without proper identification
VERIFICATION: Check for duplicate IDs and proper attribution across migration files
REFERENCES:
  - PATTERN_EXAMPLE: `0001_initial_schema.xml:26-28` (proper changeset ID and author attribution)
  - PATTERN_EXAMPLE: `9000_create_extend_kyc_tables.xml:22-24` (sequential changeset numbering)
  - ID_VALIDATION: Unique changeset ID verification within changelog files
  - VERIFICATION_CMD: `grep -rn 'changeSet.*id=' fineract-provider/src/main/resources/sql/migrations/ | sort`

### RULE: Foreign Key Constraint Naming
CONTEXT: Consistent and descriptive foreign key constraint naming
REQUIREMENT: All foreign key constraints must follow naming convention and avoid conflicts
FAIL IF:
- Generic constraint names like "fk1", "fk2" without descriptive context
- Constraint names exceeding database-specific length limits
- Duplicate constraint names across different tables
- Missing constraintName attribute in addForeignKeyConstraint
- Constraint names not following FK_[table]_[reference] pattern
VERIFICATION: Check constraint naming patterns and uniqueness
REFERENCES:
  - PATTERN_EXAMPLE: `0001_initial_schema.xml:2847-2853` (FK_m_loan_m_client foreign key naming)
  - PATTERN_EXAMPLE: `0001_initial_schema.xml:3156-3162` (FK_m_savings_account_m_client naming pattern)
  - CONSTRAINT_NAMING: FK_[source_table]_[target_table] naming convention
  - VERIFICATION_CMD: `grep -rn "constraintName.*FK_" fineract-provider/src/main/resources/sql/migrations/`

### RULE: Index Naming and Performance
CONTEXT: Consistent index naming for database performance optimization
REQUIREMENT: All database indexes must follow naming convention and target appropriate columns
FAIL IF:
- Generic index names without descriptive context
- Missing indexes on foreign key columns
- Index names exceeding database length limits
- Duplicate index definitions
- Missing performance-critical indexes on frequently queried columns
VERIFICATION: Check index naming patterns and performance impact
REFERENCES:
  - PATTERN_EXAMPLE: `0001_initial_schema.xml:4523-4526` (idx_m_loan_client_id index naming)
  - PATTERN_EXAMPLE: `0001_initial_schema.xml:4789-4792` (idx_m_savings_account_client_id naming pattern)
  - INDEX_NAMING: idx_[table_name]_[column_name] naming convention
  - VERIFICATION_CMD: `grep -rn "indexName.*idx_" fineract-provider/src/main/resources/sql/migrations/`

### RULE: PreConditions and Rollback Safety
CONTEXT: Safe migration execution with proper validation and rollback capability
REQUIREMENT: Destructive or conditional migrations must include preConditions and rollback instructions
FAIL IF:
- Destructive operations without preConditions to check existence
- Missing rollback instructions for reversible operations
- PreConditions with incorrect expectedResult values
- Operations that cannot be safely rolled back without proper documentation
- Missing failOnError and onFail attributes in preConditions
VERIFICATION: Test migration rollback scenarios and preCondition validation
REFERENCES: 
  - PATTERN_EXAMPLE: `9000_create_extend_kyc_tables.xml:22-35` (changeSet with preConditions)
  - PATTERN_EXAMPLE: `0001_initial_schema.xml:26-45` (failOnError and onFail usage)
  - LIQUIBASE_DOCS: Liquibase preConditions documentation

### RULE: Data Type Consistency Across Databases
CONTEXT: Ensuring data type compatibility between MySQL and PostgreSQL
REQUIREMENT: Data types must be compatible across supported database systems
FAIL IF:
- Using MySQL-specific data types in generic changesets
- PostgreSQL-specific data types used without proper context
- Inconsistent precision/scale for DECIMAL types across databases
- Missing database-specific type mappings for JSON/JSONB
- Timestamp precision inconsistencies between databases
VERIFICATION: Test migrations against both MySQL and PostgreSQL databases
REFERENCES: Database-specific migration examples
COMMON MAPPINGS:
- MySQL: JSON → PostgreSQL: JSONB
- MySQL: DATETIME(6) → PostgreSQL: TIMESTAMP WITH TIME ZONE
- MySQL: INT → PostgreSQL: INTEGER
- MySQL: DECIMAL(19,6) → PostgreSQL: DECIMAL(19,6)

### RULE: XML Formatting and Spotless Compliance
CONTEXT: Consistent XML formatting to pass Spotless checks during build
REQUIREMENT: All XML files must follow Spotless formatting rules
FAIL IF:
- Inconsistent indentation (must use 4 spaces, not tabs)
- Missing trailing newlines at end of file
- Trailing whitespace on any lines
- Incorrect XML attribute formatting and spacing
- XML elements not properly aligned
VERIFICATION: Run `./gradlew spotlessCheck` to validate formatting
REFERENCES: build.gradle Spotless configuration
FORMATTING RULES:
- Use 4 spaces for indentation
- End files with single newline
- No trailing whitespace
- Consistent attribute formatting
- Proper XML element alignment

### RULE: Custom Change Class Implementation
CONTEXT: Proper implementation of custom Liquibase change classes
REQUIREMENT: Custom change classes must be properly implemented and referenced
FAIL IF:
- Custom change class referenced but not found in classpath
- Missing required interface implementations (CustomTaskChange)
- Custom change class not properly registered with Spring context
- Incorrect package naming or class path references
- Missing error handling in custom change execution
VERIFICATION: Verify custom change classes exist and implement required interfaces
REFERENCES: 
  - PATTERN_EXAMPLE: `0007_x_extend_tenant_ro_passwords.xml:25-30` (customChange class implementation)
  - PATTERN_EXAMPLE: `TenantPasswordEncryptionTask.java` (CustomTaskChange interface implementation)
  - PATTERN_EXAMPLE: `TenantReadOnlyPasswordEncryptionTask.java` (custom change execution example)

### RULE: Check Constraint SQL Escaping
CONTEXT: Proper SQL escaping in check constraints to avoid XML parsing errors
REQUIREMENT: All SQL operators in check constraints must be properly XML-escaped
FAIL IF:
- Using < or > operators without XML entity escaping
- Unescaped ampersand characters in SQL conditions
- Missing CDATA sections for complex SQL expressions
- Inconsistent escaping between MySQL and PostgreSQL contexts
- SQL syntax errors due to improper XML character handling
VERIFICATION: Validate XML parsing and SQL syntax in check constraints
REFERENCES: 
  - PATTERN_EXAMPLE: `9002_create_extend_credit_score_table.xml:254-258` (proper XML entity escaping with &gt; and &lt;)
  - PATTERN_EXAMPLE: `9002_create_extend_credit_score_table.xml:262-266` (database-specific SQL with dbms attribute)
  - XML_DOCS: XML entity escaping documentation

### RULE: Check Constraint Implementation Failures [CRITICAL]
CONTEXT: Check constraints in Liquibase addCheckConstraint operations frequently cause migration failures
REQUIREMENT: Check constraints must be implemented with proper XML escaping and data validation
FAIL IF:
- Using addCheckConstraint with unescaped comparison operators (>=, <=, <, >)
- Applying constraints to tables with existing data that violates conditions
- Missing preConditions to validate existing data before constraint application
- Using generic addCheckConstraint without database-specific context
- Constraint conditions that conflict with existing data integrity
VERIFICATION: Test migration against database with existing data, validate XML parsing
REFERENCES: 
  - ANTI_PATTERN: Using addCheckConstraint with unescaped operators (causes XML parsing errors)
  - PATTERN_EXAMPLE: `9002_create_extend_credit_score_table.xml:242-266` (correct preConditions with SQL check)
  - PATTERN_EXAMPLE: `9002_create_extend_credit_score_table.xml:254-266` (proper database-specific SQL implementation)
  - CORRECT_APPROACH: Use preConditions + database-specific SQL instead of addCheckConstraint
  - XML_ESCAPING: Failed migration examples, XML entity escaping rules

### RULE: Command Handler Registration [CRITICAL]
CONTEXT: API endpoints using command processing framework require registered command handlers to prevent UnsupportedCommandException
REQUIREMENT: Every command created by ExtendCommandWrapperBuilder must have a corresponding command handler registered with @CommandType annotation
FAIL IF:
- API endpoint creates CommandWrapper but no corresponding @CommandType handler exists
- Command entity/action combination not registered in any @CommandType annotation
## Liquibase Migration Rules

### RULE: Migration Script Structure
CONTEXT: Consistent and maintainable database schema evolution
REQUIREMENT: Liquibase changesets must follow consistent structure with proper documentation
FAIL IF:
- Migration scripts without proper changeset identification
- Missing rollback instructions for schema changes
- Database-specific SQL without compatibility considerations
- Changeset dependencies not properly managed
- Migration scripts not properly documented
VERIFICATION: Check Liquibase changeset structure and documentation
REFERENCES: Liquibase XML/SQL files, migration documentation

### RULE: Schema Version Control
CONTEXT: Coordinated database schema changes across environments
REQUIREMENT: Database schema changes must be version controlled with proper change tracking
FAIL IF:
- Schema changes deployed without version control
- Missing database version tracking across environments
- Schema drift between development and production
- Changeset execution order not properly maintained
- Database state not properly tracked
VERIFICATION: Check Liquibase change tracking and version management
REFERENCES: Database changelog tables, version control integration

### RULE: Data Migration Safety
CONTEXT: Safe data transformations without data loss or corruption
REQUIREMENT: Data migration scripts must include validation and rollback procedures
FAIL IF:
- Data migration without backup procedures
- Missing data validation after migration execution
- Destructive changes without proper rollback capability
- Data migration affecting system performance
- Migration execution not properly logged
VERIFICATION: Check data migration safety procedures and validation
REFERENCES: Data migration scripts, rollback procedures

### RULE: Environment-Specific Migrations
CONTEXT: Different migration strategies for different environments
REQUIREMENT: Migration scripts must handle environment-specific requirements appropriately
FAIL IF:
- Production migrations not properly tested in staging
- Development-specific changes affecting production
- Missing environment variable usage in migrations
- Migration execution timing not considered for production
- Environment-specific data not properly handled
VERIFICATION: Check environment-specific migration configurations
REFERENCES: Environment configuration files, migration execution procedures

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
### RULE: Tenant Data Migration Patterns
CONTEXT: Safe and consistent tenant data migration across environments
REQUIREMENT: Tenant data migrations must be isolated and reversible with proper validation
FAIL IF:
- Data migration affecting multiple tenants simultaneously
- Missing rollback procedures for failed migrations
- Tenant data not properly validated after migration
- Migration scripts not tenant-aware
- Data integrity checks missing in migration process
VERIFICATION: Check tenant migration scripts and validation procedures
REFERENCES: Tenant migration implementations, data validation scripts

### RULE: API Resource Permission Pattern
CONTEXT: All API resource endpoints require corresponding permissions in the database for authorization
REQUIREMENT: When creating new API resources, corresponding permissions must be added to migration files
FAIL IF:
- API resource created without corresponding READ/CREATE/UPDATE/DELETE permissions
- Permission codes don't match the entity naming convention (e.g., CLIENT_KYC, CLIENT_CREDIT_REPORT)
- Missing PULL or custom action permissions for specialized operations
- Command handler @CommandType action doesn't have matching permission with same action_name
- Permissions not grouped correctly under "portfolio" or appropriate grouping
