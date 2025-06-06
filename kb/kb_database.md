# Fineract Database Implementation Patterns

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
