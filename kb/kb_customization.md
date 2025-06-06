# Fineract Customization and Extension Patterns

## Custom Module Development Rules

### RULE: Custom Module Structure
CONTEXT: Extending Fineract with custom business logic while maintaining upgradeability
REQUIREMENT: Custom modules must follow established patterns and maintain separation from core code
FAIL IF:
- Custom code mixed with core Fineract modules
- Missing custom module namespace and packaging
- Custom modules not following Spring Boot auto-configuration patterns
- Custom entities not properly extending BaseEntity
- Custom modules bypassing standard dependency injection
VERIFICATION: Check custom module structure and separation from core
REFERENCES: custom/ directory structure, module packaging patterns

### RULE: Custom Entity Implementation
CONTEXT: Adding new business entities while maintaining data integrity and audit trails
REQUIREMENT: Custom entities must extend BaseEntity and follow established JPA patterns
FAIL IF:
- Custom entities not extending BaseEntity
- Missing audit fields (created/modified timestamps, version, etc.)
- Custom entities not implementing tenant isolation
- Missing proper JPA annotations and relationships
- Custom entities not following naming conventions
VERIFICATION: Check custom entity implementations and inheritance
REFERENCES: BaseEntity class, custom entity examples in custom/ directory

### RULE: Custom API Controller Patterns
CONTEXT: Exposing custom business logic through REST APIs with consistent patterns
REQUIREMENT: Custom controllers must follow established API patterns and security requirements
FAIL IF:
- Custom controllers not following REST conventions
- Missing authentication and authorization checks
- Custom APIs not implementing proper error handling
- Missing API documentation and OpenAPI specifications
- Custom endpoints not following URL naming conventions
VERIFICATION: Check custom controller implementations and API patterns
REFERENCES: Core controller patterns, API documentation standards

### RULE: Custom Service Layer Implementation
CONTEXT: Implementing custom business logic with proper transaction management
REQUIREMENT: Custom services must implement interfaces and follow transaction patterns
FAIL IF:
- Custom services not implementing proper interfaces
- Missing transaction annotations and boundaries
- Custom business logic not properly validated
- Custom services not following dependency injection patterns
- Missing proper error handling and logging
VERIFICATION: Check custom service implementations and transaction management
REFERENCES: Service interface patterns, transaction management examples

### RULE: Custom Repository Patterns
CONTEXT: Data access patterns for custom entities with proper query optimization
REQUIREMENT: Custom repositories must extend standard repository interfaces with optimized queries
FAIL IF:
- Custom repositories not extending JpaRepository or similar
- Missing custom query methods with proper JPQL
- Custom repositories not implementing tenant-aware queries
- Missing query optimization and indexing considerations
- Custom repositories not following naming conventions
VERIFICATION: Check custom repository implementations and query patterns
REFERENCES: Repository interface examples, query optimization patterns

## Plugin and Extension Rules

### RULE: Plugin Architecture Compliance
CONTEXT: Developing plugins that integrate seamlessly with Fineract core
REQUIREMENT: Plugins must follow established architecture patterns and lifecycle management
FAIL IF:
- Plugins not implementing required interfaces
- Missing plugin metadata and configuration
- Plugins not following lifecycle management patterns
- Plugin dependencies not properly declared
- Plugins affecting core system stability
VERIFICATION: Check plugin implementation and integration patterns
REFERENCES: Plugin interface definitions, plugin lifecycle management

### RULE: Event-Driven Extension Points
CONTEXT: Extending functionality through event listeners and publishers
REQUIREMENT: Custom extensions must use event-driven patterns for loose coupling
FAIL IF:
- Custom logic tightly coupled to core business processes
- Missing event publishing for significant business operations
- Event handlers not properly implementing error handling
- Events not following established payload patterns
- Missing event ordering and dependency management
VERIFICATION: Check event publishing and handling implementations
REFERENCES: Event publishing patterns, event handler examples

### RULE: Configuration Extension Patterns
CONTEXT: Adding custom configuration while maintaining environment consistency
REQUIREMENT: Custom configuration must follow externalization patterns and validation
FAIL IF:
- Custom configuration hardcoded in application code
- Missing validation for custom configuration properties
- Custom configuration not following naming conventions
- Missing environment-specific configuration handling
- Custom configuration not properly documented
VERIFICATION: Check custom configuration implementation and validation
REFERENCES: Configuration property patterns, validation examples

### RULE: Custom Workflow Implementation
CONTEXT: Implementing custom business workflows and approval processes
REQUIREMENT: Custom workflows must integrate with existing workflow engine and patterns
FAIL IF:
- Custom workflows bypassing established workflow patterns
- Missing workflow state management and persistence
- Custom workflows not implementing proper authorization
- Missing workflow audit trails and history
- Custom workflows not following transaction boundaries
VERIFICATION: Check custom workflow implementation and integration
REFERENCES: Workflow engine patterns, state management examples

## Integration and Compatibility Rules

### RULE: Core API Compatibility
CONTEXT: Maintaining compatibility with core Fineract APIs during customization
REQUIREMENT: Custom modifications must not break existing API contracts
FAIL IF:
- Custom changes breaking existing API endpoints
- Modified response formats affecting client compatibility
- Custom validation rules conflicting with core business rules
- Custom modifications affecting standard tenant operations
- Missing backward compatibility considerations
VERIFICATION: Check API compatibility and contract adherence
REFERENCES: API contract definitions, compatibility testing patterns

### RULE: Database Schema Extension
CONTEXT: Adding custom database schema while maintaining migration compatibility
REQUIREMENT: Custom schema changes must follow Liquibase patterns and not conflict with core schema
FAIL IF:
- Custom schema changes not using Liquibase changesets
- Custom tables not following naming conventions
- Custom schema modifications affecting core table structures
- Missing rollback procedures for custom schema changes
- Custom schema not properly documented
VERIFICATION: Check custom schema implementation and migration patterns
REFERENCES: Liquibase changeset examples, schema naming conventions

### RULE: Custom Security Integration
CONTEXT: Implementing custom security features while maintaining core security model
REQUIREMENT: Custom security implementations must integrate with existing authentication and authorization
FAIL IF:
- Custom security bypassing established authentication mechanisms
- Custom authorization not integrating with role-based access control
- Custom security implementations creating vulnerabilities
- Missing security audit trails for custom operations
- Custom security not following tenant isolation requirements
VERIFICATION: Check custom security implementation and integration
REFERENCES: Security integration patterns, authentication examples

### RULE: Performance Impact Assessment
CONTEXT: Ensuring custom modifications do not degrade system performance
REQUIREMENT: Custom implementations must be performance-tested and optimized
FAIL IF:
- Custom code causing performance degradation
- Missing performance testing for custom features
- Custom database queries not optimized
- Custom operations not implementing proper caching
- Missing monitoring and metrics for custom functionality
VERIFICATION: Check performance impact and optimization of custom code
REFERENCES: Performance testing patterns, optimization guidelines

## Deployment and Lifecycle Rules

### RULE: Custom Module Packaging
CONTEXT: Proper packaging and deployment of custom modules
REQUIREMENT: Custom modules must be packaged separately and deployed through established mechanisms
FAIL IF:
- Custom modules not properly packaged as separate artifacts
- Missing dependency declarations for custom modules
- Custom modules not following versioning conventions
- Custom deployment not following established procedures
- Missing rollback procedures for custom module deployments
VERIFICATION: Check custom module packaging and deployment procedures
REFERENCES: Module packaging examples, deployment procedures

### RULE: Environment Promotion Patterns
CONTEXT: Moving custom code through development, staging, and production environments
REQUIREMENT: Custom code must follow established promotion and testing procedures
FAIL IF:
- Custom code deployed without proper testing in staging
- Missing environment-specific configuration for custom features
- Custom code promotion not following approval workflows
- Missing automated testing for custom functionality
- Custom code affecting environment stability
VERIFICATION: Check environment promotion procedures and testing coverage
REFERENCES: Environment promotion workflows, testing procedures

### RULE: Custom Code Documentation
CONTEXT: Maintaining comprehensive documentation for custom implementations
REQUIREMENT: Custom code must be thoroughly documented with usage examples and integration guides
FAIL IF:
- Custom code lacking proper documentation
- Missing API documentation for custom endpoints
- Custom configuration not properly documented
- Missing integration examples and usage guides
- Custom code not following documentation standards
VERIFICATION: Check documentation completeness and quality
REFERENCES: Documentation standards, example documentation

### RULE: Upgrade Compatibility Planning
CONTEXT: Ensuring custom code remains compatible during Fineract core upgrades
REQUIREMENT: Custom implementations must be designed for upgrade compatibility and maintainability
FAIL IF:
- Custom code tightly coupled to specific core versions
- Missing upgrade testing procedures for custom code
- Custom modifications preventing core system upgrades
- Missing migration procedures for custom data and configuration
- Custom code not following deprecation and versioning policies
VERIFICATION: Check upgrade compatibility and migration procedures
REFERENCES: Upgrade procedures, compatibility guidelines
