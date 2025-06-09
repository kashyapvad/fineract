# Fineract Java Backend Implementation Patterns

## Spring Boot Service Layer Rules

### RULE: Service Interface Abstraction
CONTEXT: Consistent service layer architecture with proper interface-based design
REQUIREMENT: All business services must implement interfaces with clear contract definitions
FAIL IF:
- Service implementations exposed directly without interfaces
- Business logic embedded in controllers or repositories
- Service contracts not clearly defined
- Multiple implementations without proper qualifier usage
- Interface methods not properly documented
VERIFICATION: Check service interface definitions and implementation patterns
REFERENCES: Service interface implementations, Spring configuration

### RULE: Constructor Dependency Injection
CONTEXT: Immutable service dependencies and clear dependency management
REQUIREMENT: All services must use constructor injection with final fields for dependencies
FAIL IF:
- Field injection (@Autowired on fields) used instead of constructor injection
- Mutable service dependencies (non-final fields)
- Circular dependencies between services
- Optional dependencies not properly handled
- Service instantiation using 'new' operator
VERIFICATION: Check service constructor patterns and dependency declarations
REFERENCES:
  - PATTERN_EXAMPLE: `SmsCampaignDomainServiceImpl.java:74-85` (@Service with @RequiredArgsConstructor)
  - PATTERN_EXAMPLE: `LoanAccountDomainServiceJpa.java:128-156` (multiple final dependencies)
  - PATTERN_EXAMPLE: `ConfigJobParameterServiceImpl.java:42-51` (interface injection)
  - MAPSTRUCT_CONFIG: `MapstructMapperConfig.java:29-30` (constructor injection strategy)
  - VERIFICATION_CMD: `grep -rn "@RequiredArgsConstructor" fineract-provider/src/main/java/ | head -10`
  - VERIFICATION_CMD: `find . -name "*.java" -exec grep -l "@Autowired.*private" {} \;`
  - ANTI_PATTERN: Field injection with @Autowired instead of constructor injection

### RULE: Optional Bean Configuration
CONTEXT: Graceful handling of optional external dependencies and feature toggles
REQUIREMENT: Optional external services and configurable features must use proper conditional bean configuration patterns
FAIL IF:
- Optional external dependencies injected directly without conditional configuration
- Feature toggles implemented with runtime checks instead of conditional beans
- Missing fallback implementations for optional services
- Optional beans not properly wrapped in Optional<T> for null safety
- External service availability not validated at startup
- Optional dependencies causing application startup failures when unavailable
VERIFICATION: Check conditional bean configurations and optional dependency handling
REFERENCES:
  - PATTERN_EXAMPLE: `ExtendModuleConfiguration.java:24-26` (@ConditionalOnMissingBean with Optional.empty())
  - PATTERN_EXAMPLE: `ExtendModuleConfiguration.java:32-35` (@ConditionalOnBean with Optional.of())
  - PATTERN_EXAMPLE: `CreditBureauProviderFactory.java:42` (@ConditionalOnProperty for feature enablement)
  - PATTERN_EXAMPLE: `DecentroProvider.java:61` (@ConditionalOnProperty with matchIfMissing = false)
  - PATTERN_EXAMPLE: `TwoFactorServiceImpl.java:48` (@ConditionalOnProperty("fineract.security.2fa.enabled"))
  - PATTERN_EXAMPLE: `ExternalEventKafkaConfiguration.java:38` (@ConditionalOnProperty with havingValue)
  - PATTERN_EXAMPLE: `TemplateConfiguration.java:35-37` (@ConditionalOnMissingBean for default implementations)
  - USE_CASES: External payment providers, credit bureau integrations, messaging systems, cloud services
  - ANTI_PATTERN: Direct @Autowired injection of optional services without conditional configuration
  - ANTI_PATTERN: Runtime null checks instead of Optional<T> bean patterns
  - ANTI_PATTERN: Feature flags in business logic instead of conditional bean registration
  - VERIFICATION_CMD: `grep -rn "@ConditionalOnProperty" fineract-provider/src/main/java/ | head -10`
  - VERIFICATION_CMD: `grep -rn "@ConditionalOnMissingBean" fineract-provider/src/main/java/ | head -10`
  - CONFIGURATION_PATTERN: Use matchIfMissing = false for optional features to prevent accidental enablement

### RULE: Common Provider Service Pattern
CONTEXT: Centralized external provider integration to eliminate duplication across extend modules
REQUIREMENT: External provider operations must be centralized in common service layer with consistent error handling
FAIL IF:
- Provider availability checks duplicated across multiple services
- Provider factory access patterns repeated in different modules
- Reference ID generation logic scattered across services
- Provider response validation inconsistent between modules
- Error handling patterns not standardized for provider operations
VERIFICATION: Check provider service usage and centralization patterns
REFERENCES:
  - PATTERN_EXAMPLE: `ExtendProviderService.java:42-55` (centralized provider availability validation)
  - PATTERN_EXAMPLE: `ExtendProviderService.java:98-125` (standardized customer data pull with error handling)
  - PATTERN_EXAMPLE: `ExtendProviderService.java:127-154` (standardized credit report generation)
  - PATTERN_EXAMPLE: `ClientKycWritePlatformServiceImpl.java:64` (using common service for validation)
  - PATTERN_EXAMPLE: `ClientCreditBureauWritePlatformServiceImpl.java:87` (using common service for validation)
  - USAGE_PATTERN: Services inject `ExtendProviderService` instead of `Optional<CreditBureauProviderFactory>`
  - CENTRALIZATION_BENEFIT: Single point of change for provider logic updates
  - CONSISTENCY_BENEFIT: Standardized error messages and validation patterns
  - VERIFICATION_CMD: `grep -rn "ExtendProviderService" fineract-provider/src/main/java/org/apache/fineract/extend/ | head -10`
  - ANTI_PATTERN: Direct provider factory usage in business services
  - ANTI_PATTERN: Duplicated provider availability validation logic

### RULE: Service Method Transaction Boundaries
CONTEXT: Proper transaction scope management for data consistency
REQUIREMENT: Service methods must define appropriate transaction boundaries with correct propagation
FAIL IF:
- Long-running transactions spanning multiple business operations
- Missing @Transactional annotations on state-changing methods
- Incorrect transaction propagation settings
- Read-only operations wrapped in write transactions
- Transaction rollback scenarios not properly handled
VERIFICATION: Check @Transactional annotations and transaction configuration
REFERENCES: Service method transaction annotations, transaction management configuration

### RULE: Repository Wrapper Pattern
CONTEXT: Consistent data access patterns with proper error handling
REQUIREMENT: Data access must use repository wrapper pattern for entity validation and error handling
FAIL IF:
- Direct repository usage in service methods
- Missing entity existence validation
- Inconsistent error handling across data access
- Repository methods not properly abstracted
- Entity not found scenarios handled inconsistently
VERIFICATION: Check repository wrapper implementations and usage patterns
REFERENCES:
  - PATTERN_EXAMPLE: `ReportRepositoryWrapper.java:37-50` (standard wrapper with findOneThrowExceptionIfNotFound)
  - PATTERN_EXAMPLE: `CodeValueRepositoryWrapper.java:37-66` (wrapper with multiple find methods)
  - PATTERN_EXAMPLE: `ChargeRepositoryWrapper.java:44-58` (wrapper with business validation)
  - PATTERN_EXAMPLE: `GLAccountRepositoryWrapper.java:37-48` (wrapper with multiple lookup strategies)
  - VERIFICATION_CMD: `find . -name "*RepositoryWrapper.java" | head -10`
  - VERIFICATION_CMD: `grep -rn "findOneWithNotFoundDetection\|findOneThrowExceptionIfNotFound" fineract-provider/src/main/java/ | head -5`
  - ANTI_PATTERN: Services directly calling repository.findById() without wrapper validation

### RULE: Service Layer Error Handling
CONTEXT: Consistent exception handling and user-friendly error messages
REQUIREMENT: Service methods must handle exceptions with appropriate business context and user messaging
FAIL IF:
- Technical exceptions propagated to presentation layer
- Business rule violations not properly communicated
- Missing validation error details
- Exception messages not internationalized
- Error context information lost in exception handling
VERIFICATION: Check exception handling patterns in service methods
REFERENCES: Exception handling implementations, error message configurations

## Command Handler Implementation Rules

### RULE: Command Handler Single Responsibility
CONTEXT: Clear separation of command handling logic for maintainability
REQUIREMENT: Each command handler must handle exactly one command type with focused business logic
FAIL IF:
- Command handlers processing multiple command types
- Business logic scattered across multiple handlers for same operation
- Command handlers containing query logic
- Cross-cutting concerns mixed with business logic
- Command validation logic embedded in handlers
VERIFICATION: Check command handler implementations and scope
REFERENCES:
  - PATTERN_EXAMPLE: `CreateSmsCommandHandler.java:32-47` (single responsibility command handler)
  - PATTERN_EXAMPLE: `CreateCodeCommandHandler.java:32-47` (focused command processing)
  - PATTERN_EXAMPLE: `InlineJobExecuteHandler.java:32-48` (single command type handling)
  - ANNOTATION: `@CommandType(entity = "SMS", action = "CREATE")` (clear command mapping)
  - VERIFICATION_CMD: `find . -name "*CommandHandler.java" -exec grep -l "@CommandType" {} \;`
  - VERIFICATION_CMD: `grep -rn "@CommandType" fineract-provider/src/main/java/ | head -10`
  - ANTI_PATTERN: Command handlers with multiple @CommandType annotations or mixed business logic

### RULE: Command Validation and Preprocessing
CONTEXT: Input validation and data preparation before command execution
REQUIREMENT: Command handlers must validate inputs and prepare data before executing business logic
FAIL IF:
- Business logic executed without input validation
- Command data not properly deserialized
- Missing required field validation
- Business rule validation embedded in processing logic
- Invalid command data causing system errors
VERIFICATION: Check command validation patterns and error handling
REFERENCES: Command handler validation logic, input processing patterns

### RULE: Command Result Consistency
CONTEXT: Standardized command execution results for API consistency
REQUIREMENT: Command handlers must return standardized result objects with consistent structure
FAIL IF:
- Command handlers returning inconsistent result formats
- Business data embedded in command results
- Missing operation success/failure indicators
- Error details not properly structured
- Audit information missing from results
VERIFICATION: Check command result object patterns and consistency
REFERENCES: Command result implementations, API response formats

### RULE: Command Audit Integration
CONTEXT: Complete audit trail for all state-changing operations
REQUIREMENT: Command handlers must integrate with audit logging for compliance and traceability
FAIL IF:
- State changes not recorded in audit log
- Audit entries missing required business context
- User information not captured in audit trails
- Command failures not properly audited
- Bulk operations bypassing individual audit entries
VERIFICATION: Check audit logging integration in command handlers
REFERENCES: Audit logging implementations, command audit patterns

### RULE: Financial Data Audit Trail Implementation
CONTEXT: Comprehensive audit trails for financial operations to meet regulatory compliance requirements
REQUIREMENT: All financial state changes must generate detailed audit entries with before/after state capture
FAIL IF:
- Update operations missing before/after state logging
- Delete operations not capturing complete record state before deletion
- Audit entries missing critical business context (user, client, operation type)
- Financial data changes not logged with sufficient detail for compliance
- Audit trail format inconsistent across different operations
VERIFICATION: Check audit trail implementation in financial service operations
REFERENCES:
  - PATTERN_EXAMPLE: `ClientKycWritePlatformServiceImpl.java:370-378` (update audit with before/after state)
  - PATTERN_EXAMPLE: `ClientKycWritePlatformServiceImpl.java:450-458` (delete audit with complete record state)
  - PATTERN_EXAMPLE: `ClientCreditBureauWritePlatformServiceImpl.java:370-378` (credit report update audit)
  - PATTERN_EXAMPLE: `ClientCreditBureauWritePlatformServiceImpl.java:220-228` (credit report delete audit)
  - AUDIT_FORMAT: "AUDIT: OPERATION_TYPE - User: {} | Client: {} | Entity ID: {} | Command: {} | Before: [{}] | After: [{}]"
  - COMPLIANCE_REQUIREMENT: All financial data changes must be auditable for regulatory review
  - STATE_CAPTURE: Before and after states captured in structured format for comparison
  - VERIFICATION_CMD: `grep -rn "AUDIT:" fineract-provider/src/main/java/org/apache/fineract/extend/ | head -10`
  - ANTI_PATTERN: Financial operations without comprehensive audit trails
  - ANTI_PATTERN: Audit entries missing critical business context

## Data Access and Repository Rules

### RULE: JPA Entity Relationship Management
CONTEXT: Proper entity relationships and data integrity constraints
REQUIREMENT: JPA entities must properly define relationships with appropriate cascading and fetch strategies
FAIL IF:
- Missing foreign key constraints for data integrity
- Incorrect cascade settings causing data inconsistency
- Eager loading causing performance issues
- Bidirectional relationships not properly managed
- Entity lifecycle events not handled correctly
VERIFICATION: Check JPA entity definitions and relationship mappings
REFERENCES: Entity class definitions, JPA relationship annotations

### RULE: JPA AttributeConverter Implementation
CONTEXT: Proper JPA AttributeConverter patterns for custom type conversion
REQUIREMENT: JPA AttributeConverters must have no-argument constructors and cannot use Spring dependency injection
FAIL IF:
- AttributeConverter classes annotated with @Component or @Service
- AttributeConverter using @Autowired or @RequiredArgsConstructor for dependencies
- AttributeConverter relying on Spring context for initialization
- Missing no-argument constructor in AttributeConverter implementation
- Complex dependencies required in AttributeConverter (should be self-contained)
VERIFICATION: Check AttributeConverter implementations for proper constructor patterns
REFERENCES:
  - PATTERN_EXAMPLE: `JsonAttributeConverter.java` (no-arg constructor with direct ObjectMapper instantiation)
  - PATTERN_EXAMPLE: `ExternalIdConverter.java` (simple converter without dependencies)
  - PATTERN_EXAMPLE: `LoanStatusConverter.java` (enum converter without Spring injection)
  - VERIFICATION_CMD: `find . -name "*Converter.java" -exec grep -l "@Component\|@Service\|@Autowired" {} \;`
  - VERIFICATION_CMD: `grep -rn "@Converter" fineract-provider/src/main/java/ | head -10`
  - ANTI_PATTERN: AttributeConverter with @Component/@RequiredArgsConstructor causing JPA instantiation failures
  - ERROR_SIGNATURE: "No default constructor for entity" or "Could not instantiate converter" during JPA initialization

### RULE: Query Method Naming Conventions
CONTEXT: Consistent and self-documenting repository method names
REQUIREMENT: Repository query methods must follow Spring Data naming conventions with clear intent
FAIL IF:
- Repository method names not descriptive of query intent
- Inconsistent naming patterns across repositories
- Method parameters not clearly indicating query criteria
- Complex queries implemented as method names instead of @Query
- Missing method documentation for complex queries
VERIFICATION: Check repository interface method definitions
REFERENCES: Repository interface implementations, query method patterns

### RULE: Native Query Usage Guidelines
CONTEXT: Performance optimization while maintaining database independence
REQUIREMENT: Native queries must be used judiciously with proper documentation and database compatibility
FAIL IF:
- Native queries used unnecessarily instead of JPQL
- Database-specific syntax without compatibility considerations
- Missing documentation for complex native queries
- Native queries not properly parameterized
- Performance benefits not justified for native query usage
VERIFICATION: Check native query usage and documentation
REFERENCES: Repository native query implementations, query optimization patterns

### RULE: Repository Method Parameter Validation
CONTEXT: Data integrity and security through input validation
REQUIREMENT: Repository methods must validate parameters to prevent invalid data access
FAIL IF:
- Repository methods accepting null parameters without validation
- Missing parameter constraints for business rules
- SQL injection vulnerabilities in dynamic queries
- Improper parameter binding in native queries
- Invalid parameter combinations not detected
VERIFICATION: Check repository method parameter validation
REFERENCES: Repository parameter validation patterns, query parameter handling

## Business Logic and Domain Rules

### RULE: Domain Service Encapsulation
CONTEXT: Business logic organization and reusability across operations
REQUIREMENT: Complex business logic must be encapsulated in domain services with clear responsibilities
FAIL IF:
- Business logic scattered across multiple service classes
- Domain rules embedded in command handlers or controllers
- Business calculations not properly abstracted
- Domain services with unclear responsibilities
- Business logic duplicated across different operations
VERIFICATION: Check domain service implementations and business logic organization
REFERENCES: Domain service patterns, business logic implementations

### RULE: Business Rule Validation
CONTEXT: Consistent enforcement of business rules across all operations
REQUIREMENT: Business rules must be validated consistently with clear error messaging
FAIL IF:
- Business rules enforced inconsistently across operations
- Rule validation logic scattered across multiple layers
- Business rule violations not properly communicated
- Missing validation for critical business constraints
- Rule validation bypassed in bulk operations
VERIFICATION: Check business rule validation implementations
REFERENCES: Business rule validation patterns, constraint checking logic

### RULE: Calculation and Financial Logic
CONTEXT: Accurate financial calculations with proper precision and rounding
REQUIREMENT: Financial calculations must use appropriate data types and follow consistent rounding rules
FAIL IF:
- Floating-point arithmetic used for financial calculations
- Inconsistent rounding strategies across calculations
- Currency precision not properly maintained
- Interest calculations not properly documented
- Financial formulas not unit tested
VERIFICATION: Check financial calculation implementations and precision handling
REFERENCES: Financial calculation services, interest computation logic

### RULE: Entity State Management
CONTEXT: Proper entity lifecycle management and state transitions
REQUIREMENT: Entity state changes must follow defined workflows with proper validation
FAIL IF:
- Invalid state transitions allowed without validation
- Entity lifecycle events not properly handled
- State change audit trails incomplete
- Concurrent state modifications not properly managed
- Business workflows not properly enforced
VERIFICATION: Check entity state management and workflow implementations
REFERENCES: Entity state management patterns, workflow validation logic

## Configuration and Integration Rules

### RULE: External Service Integration
CONTEXT: Consistent patterns for integrating with external systems and APIs
REQUIREMENT: External service integrations must use proper abstraction and error handling
FAIL IF:
- External service calls not properly abstracted
- Missing timeout and retry configurations
- External service failures not gracefully handled
- Service integration not properly tested
- External dependencies not properly documented
VERIFICATION: Check external service integration patterns and configurations
REFERENCES: External service implementations, integration configuration

### RULE: Configuration Property Management
CONTEXT: Externalized configuration with proper validation and documentation
REQUIREMENT: Application configuration must be externalized with validation and clear documentation
FAIL IF:
- Configuration values hardcoded in business logic
- Missing validation for critical configuration properties
- Configuration changes requiring code deployment
- Environment-specific logic scattered across application
- Configuration properties not properly documented
VERIFICATION: Check configuration property usage and validation
REFERENCES: Configuration classes, property validation patterns

### RULE: Event Publishing and Handling
CONTEXT: Loose coupling through domain events and proper event handling
REQUIREMENT: Domain events must be published for significant business operations with proper handling
FAIL IF:
- Business operations not publishing relevant domain events
- Event handlers not properly decoupled from event publishers
- Missing error handling in event processing
- Event ordering not properly maintained
- Event processing not idempotent
VERIFICATION: Check domain event publishing and handling patterns
REFERENCES: Event publishing implementations, event handler patterns

### RULE: Caching Strategy Implementation
CONTEXT: Performance optimization through appropriate caching with proper invalidation
REQUIREMENT: Caching must be implemented strategically with proper key management and invalidation
FAIL IF:
- Cache keys not tenant-aware for multi-tenant data
- Missing cache invalidation for data updates
- Inappropriate caching of frequently changing data
- Cache configuration not optimized for usage patterns
- Cache-related security vulnerabilities
VERIFICATION: Check caching implementation and invalidation strategies
REFERENCES: Cache configuration, cache key management patterns
