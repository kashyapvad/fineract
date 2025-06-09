# Fineract Backend Critical Architectural Guardrails

## Multi-Tenant Architecture Rules

### RULE: Tenant Context Validation [P0]
CONTEXT: Every operation must operate within proper tenant context for data isolation
REQUIREMENT: All service methods must validate ThreadLocalContextUtil.getTenant() before processing
FAIL IF:
- Service methods proceed without tenant context validation
- Database operations executed without tenant verification
- Cross-tenant data access attempted
- Business operations bypass tenant isolation
- Scheduled tasks execute without proper tenant initialization
VERIFICATION: Check ThreadLocalContextUtil.getTenant() usage in service entry points
REFERENCES:
  - PATTERN_EXAMPLE: `SchedulerJobListener.java:54-68` (proper tenant context setup with cleanup)
  - PATTERN_EXAMPLE: `JobStarter.java:82-95` (context initialization for async operations)
  - IMPLEMENTATION: `ThreadLocalContextUtil.java:42-57` (tenant context management API)
  - TEST_PATTERN: `WithTenantContextExtension.java:33-45` (test context setup)
  - VERIFICATION_CMD: `grep -rn "ThreadLocalContextUtil.getTenant()" fineract-provider/src/main/java/ | head -10`
  - ANTI_PATTERN: Service methods accessing repositories without checking tenant context first

### RULE: Database Tenant Isolation [P0]
CONTEXT: Absolute data separation between tenants for regulatory compliance
REQUIREMENT: All database queries must include tenant-specific filtering or use tenant-aware repositories
FAIL IF:
- Queries return data across multiple tenants
- Repository methods lack tenant filtering
- Native SQL queries bypass tenant isolation
- Data transfer objects expose cross-tenant information
- Cache keys not tenant-specific
VERIFICATION: Verify repository query methods include tenant filtering
REFERENCES:
  - PATTERN_EXAMPLE: `TomcatJdbcDataSourcePerTenantService.java:56-75` (tenant-aware datasource routing)
  - PATTERN_EXAMPLE: `TenantAwareTenantIdentifierFilter.java:115-125` (tenant context setup from request)
  - IMPLEMENTATION: `RoutingDataSourceServiceFactory.java:35-45` (datasource selection by tenant)
  - VERIFICATION_CMD: `find fineract-provider -name "*Repository.java" -exec grep -l "tenant" {} \;`
  - VERIFICATION_CMD: `grep -rn "jdbcTemplateFactory.create(tenant)" fineract-core/src/main/java/`
  - ANTI_PATTERN: Direct JDBC queries without tenant-specific datasource selection

### RULE: Tenant Context Lifecycle Management
CONTEXT: Proper initialization and cleanup of tenant context across threads
REQUIREMENT: Tenant context must be properly initialized, maintained, and reset across operation boundaries
FAIL IF:
- ThreadLocalContextUtil not initialized in background tasks
- Tenant context leaked between operations
- Async operations missing tenant context propagation
- Batch processes lack proper tenant isolation
- Context not reset after operation completion
VERIFICATION: Check ThreadLocalContextUtil.init(), reset() in async operations and batch jobs
REFERENCES:
  - PATTERN_EXAMPLE: `SchedulerTriggerListener.java:52-70` (proper context lifecycle in batch jobs)
  - PATTERN_EXAMPLE: `TenantDatabaseUpgradeService.java:195-213` (context management in migrations)
  - PATTERN_EXAMPLE: `SchedulerJobListener.java:111-116` (context cleanup in finally block)
  - IMPLEMENTATION: `ThreadLocalContextUtil.java:85-95` (context reset and cleanup methods)
  - VERIFICATION_CMD: `grep -rn "ThreadLocalContextUtil.reset()" fineract-provider/src/main/java/`
  - VERIFICATION_CMD: `grep -A5 -B5 "finally" fineract-provider/src/main/java/ | grep -A10 -B10 ThreadLocal`
  - ANTI_PATTERN: Async operations without try-finally blocks for context cleanup

### RULE: Business Date Context Management
CONTEXT: Financial operations must operate with correct business date context
REQUIREMENT: All financial calculations must use ThreadLocalContextUtil.getBusinessDate() for consistency
FAIL IF:
- Current system date used instead of business date
- Business date not validated before financial operations
- Date calculations ignore business date context
- COB processes use incorrect date references
- Interest calculations bypass business date validation
VERIFICATION: Check business date usage in financial calculation services
REFERENCES:
  - PATTERN_EXAMPLE: `TenantAwareTenantIdentifierFilter.java:118-120` (business date initialization)
  - PATTERN_EXAMPLE: `JobStarter.java:92-94` (business date context for batch jobs)
  - IMPLEMENTATION: `ThreadLocalContextUtil.java:68-75` (business date management API)
  - VERIFICATION_CMD: `grep -rn "getBusinessDate\|setBusinessDates" fineract-provider/src/main/java/`
  - VERIFICATION_CMD: `find . -name "*.java" -exec grep -l "LocalDate.now()" {} \; | head -5`
  - ANTI_PATTERN: Using `LocalDate.now()` or `new Date()` instead of business date context

### RULE: Cross-Tenant Data Access Prevention
CONTEXT: Preventing accidental or malicious cross-tenant data exposure
REQUIREMENT: All API endpoints and services must enforce tenant boundary validation
FAIL IF:
- Resource IDs accessible across tenant boundaries
- Service methods accept cross-tenant entity references
- API responses include cross-tenant data
- Foreign key relationships span tenant boundaries
- Search operations return cross-tenant results
VERIFICATION: Validate API endpoint tenant enforcement and service boundary checks
REFERENCES:
  - PATTERN_EXAMPLE: `TenantAwareBasicAuthenticationFilter.java:125-135` (tenant enforcement in filters)
  - PATTERN_EXAMPLE: `ExternalEventConfigurationValidationService.java:69-70` (tenant-scoped operations)
  - IMPLEMENTATION: `TenantIdentifierLoggingConverter.java:28-30` (tenant context logging)
  - VERIFICATION_CMD: `grep -rn "getTenant()" fineract-provider/src/main/java/ | grep -v test`
  - VERIFICATION_CMD: `find . -name "*Controller.java" -exec grep -L "tenant" {} \;`
  - ANTI_PATTERN: API controllers that don't validate tenant context before data access

## Fork Safety Rules

### RULE: Upstream File Modification Prohibition [P0]
CONTEXT: Fork maintenance requires avoiding upstream file modifications to prevent merge conflicts and maintain upgrade path
REQUIREMENT: Custom functionality must be implemented through extension patterns, not upstream file modifications
FAIL IF:
- Any upstream Apache Fineract core file modified for custom functionality
- Infrastructure files like CommandWrapperBuilder, CommandHandlerProvider modified
- Core service files modified to add custom business logic
- Configuration files in upstream packages modified for custom features
- Entity files modified to add custom fields or relationships
- Existing API controllers modified to add custom endpoints
VERIFICATION: Check git history of upstream files for custom modifications
REFERENCES:
  - FORK_SAFE_PATTERN: `ExtendCommandWrapperBuilder.java` (custom builder instead of modifying CommandWrapperBuilder)
  - FORK_SAFE_PATTERN: `org.apache.fineract.extend.*` packages for all custom functionality
  - FORK_SAFE_PATTERN: Custom service implementations extending upstream interfaces
  - FORK_SAFE_PATTERN: Custom configuration classes instead of modifying upstream config
  - VERIFICATION_CMD: `git log --oneline --since="1 month ago" -- fineract-core/ | grep -v "Merge\|Update"`
  - VERIFICATION_CMD: `find fineract-provider/src/main/java/org/apache/fineract -name "*.java" -not -path "*/extend/*" -exec git log --oneline -1 {} \;`
  - ANTI_PATTERN: Modifying any file outside of `org.apache.fineract.extend.*` packages for custom functionality
  - EXCEPTION: Only modify upstream files for critical bug fixes or security patches that will be contributed back

## Command-Query Separation Rules

### RULE: Command Handler Implementation [P1]
CONTEXT: State-changing operations must follow CQS pattern for consistency and auditability
REQUIREMENT: All state-changing operations must implement NewCommandSourceHandler interface
FAIL IF:
- State changes executed outside command handlers
- Business logic scattered across multiple handlers
- Command handlers mixed with query logic
- Direct repository calls bypass command pattern
- Audit trail not captured for state changes
VERIFICATION: Check NewCommandSourceHandler implementations for state-changing operations
REFERENCES:
  - PATTERN_EXAMPLE: `CreateSmsCommandHandler.java:32-47` (standard command handler pattern)
  - PATTERN_EXAMPLE: `CreateCodeCommandHandler.java:32-47` (transactional command handler)
  - PATTERN_EXAMPLE: `ReleaseInteropTransferHandler.java:32-49` (financial operation handler)
  - INTERFACE: `NewCommandSourceHandler.java:24-27` (command handler interface)
  - PROVIDER: `CommandHandlerProvider.java:80-95` (handler registration and lookup)
  - VERIFICATION_CMD: `find . -name "*CommandHandler.java" -exec grep -l "NewCommandSourceHandler" {} \;`
  - VERIFICATION_CMD: `grep -rn "@CommandType" fineract-provider/src/main/java/ | head -10`
  - ANTI_PATTERN: Business logic in controllers or direct service calls without command handlers

### RULE: Query vs Command Separation
CONTEXT: Clear separation between read and write operations for performance and scalability
REQUIREMENT: Read operations must not be implemented in command handlers
FAIL IF:
- Command handlers return detailed business data
- Query operations trigger state changes
- Read-only services implement command interfaces
- Business queries mixed with command processing
- Report generation in command handlers
VERIFICATION: Ensure command handlers only return success/failure status
REFERENCES: Command handler return types, query service implementations

### RULE: Transaction Boundary Management
CONTEXT: Proper transaction scope for data consistency and performance
REQUIREMENT: Command handlers must define clear transaction boundaries with appropriate isolation
FAIL IF:
- Transactions span multiple business operations
- Long-running transactions lock resources unnecessarily
- Nested transactions create deadlock potential
- Read operations wrapped in unnecessary transactions
- Transaction rollback scenarios not properly handled
VERIFICATION: Check @Transactional annotations and transaction scope in command handlers
REFERENCES: Transaction management configurations, command handler transaction annotations

### RULE: Command Audit Trail
CONTEXT: Financial operations require complete audit trails for regulatory compliance
REQUIREMENT: All commands must generate audit entries with sufficient detail for compliance
FAIL IF:
- State changes not recorded in audit log
- Audit entries lack sufficient detail for reconstruction
- User context missing from audit trails
- Batch operations bypass audit requirements
- Command failures not properly logged
VERIFICATION: Verify audit log generation in command handlers
REFERENCES: Audit logging implementations, command processing audit trails

## Spring Boot Configuration Rules

### RULE: Dependency Injection Patterns
CONTEXT: Consistent service layer architecture using Spring's dependency injection
REQUIREMENT: All services must use constructor injection with proper interface abstractions
FAIL IF:
- Field injection used instead of constructor injection
- Concrete classes injected instead of interfaces
- Circular dependencies between services
- Service instantiation using 'new' operator
- Missing @Service, @Component annotations
VERIFICATION: Check service class constructor injection and interface usage
REFERENCES:
  - PATTERN_EXAMPLE: `SmsReadPlatformServiceImpl.java:47-55` (@Service with @RequiredArgsConstructor)
  - PATTERN_EXAMPLE: `SmsCampaignDomainServiceImpl.java:74-85` (final fields with constructor injection)
  - PATTERN_EXAMPLE: `ConfigJobParameterServiceImpl.java:42-51` (interface injection pattern)
  - PATTERN_EXAMPLE: `LoanAccountDomainServiceJpa.java:128-156` (multiple dependencies injection)
  - MAPSTRUCT_CONFIG: `MapstructMapperConfig.java:29-30` (constructor injection strategy)
  - VERIFICATION_CMD: `grep -rn "@RequiredArgsConstructor" fineract-provider/src/main/java/ | head -10`
  - VERIFICATION_CMD: `find . -name "*.java" -exec grep -l "@Autowired.*private" {} \;`
  - ANTI_PATTERN: Field injection with @Autowired on private fields instead of constructor injection

### RULE: HTTP Interceptor Chain Configuration
CONTEXT: Consistent request/response processing across all API endpoints
REQUIREMENT: HTTP interceptors must be properly ordered and configured for tenant isolation and security
FAIL IF:
- Interceptor chain order incorrect
- Tenant context not set before business logic
- Authentication bypassed for protected endpoints
- Error handling not consistent across endpoints
- Progress tracking missing for long operations
VERIFICATION: Check HTTP_INTERCEPTORS provider configuration and order
REFERENCES: Interceptor implementations, HTTP configuration

### RULE: Conditional Bean Configuration
CONTEXT: Environment-specific service configuration and feature toggles
REQUIREMENT: Conditional beans must be properly configured with clear conditions and fallbacks
FAIL IF:
- Multiple beans of same type without qualifiers
- Conditional logic in business code instead of configuration
- Missing default implementations for conditional features
- Environment-specific logic scattered across services
- Feature toggles not properly externalized
VERIFICATION: Check @ConditionalOn* annotations and bean qualifier usage
REFERENCES: Configuration classes, conditional bean definitions

## Data Integrity & Security Rules

### RULE: Input Validation and Sanitization [P0]
CONTEXT: Preventing malicious input and ensuring data integrity
REQUIREMENT: All API inputs must be validated and sanitized before processing
FAIL IF:
- Raw user input used in database queries
- Missing validation annotations on command parameters
- Business logic accepts invalid data ranges
- File uploads not properly validated
- JSON input not schema-validated
VERIFICATION: Check validation annotations and input sanitization in API controllers
REFERENCES:
  - PATTERN_EXAMPLE: `SmsCampaignValidator.java` (input validation implementation)
  - PATTERN_EXAMPLE: `GroupingTypesDataValidator.java` (JSON validation pattern)
  - PATTERN_EXAMPLE: `LoanApplicationValidator.java` (business rule validation)
  - VERIFICATION_CMD: `find . -name "*Validator.java" -exec grep -l "fromApiJsonHelper\|JsonCommand" {} \;`
  - VERIFICATION_CMD: `grep -rn "JsonCommand.*validate" fineract-provider/src/main/java/ | head -5`
  - VERIFICATION_CMD: `find . -name "*Controller.java" -exec grep -l "@Valid\|@RequestBody" {} \;`
  - ANTI_PATTERN: Controllers accepting JsonCommand without calling validator.validate() first

### RULE: Authentication Integration
CONTEXT: Consistent authentication and authorization across all endpoints
REQUIREMENT: All protected endpoints must integrate with configured authentication mechanism
FAIL IF:
- Business logic bypasses authentication checks
- User context not available in service methods
- Permission checks inconsistent across similar operations
- Authentication failures not properly handled
- Session management not secure
VERIFICATION: Check authentication integration in API controllers and services
REFERENCES: Authentication service usage, permission checking implementations

### RULE: Environment Variable Management [P0]
CONTEXT: Centralized environment variable management for configuration consistency
REQUIREMENT: All environment variables must be managed through env.md which automatically syncs with .env
FAIL IF:
- Environment variables added directly to .env without documentation
- Configuration values hardcoded instead of using environment variables
- Missing environment variables not documented in env.md
- Environment-specific configurations scattered across multiple files
- Sensitive configuration values committed to version control
VERIFICATION: Check that all environment variables are documented in env.md and properly used in configuration
REFERENCES:
  - CONFIGURATION_FILE: `env.md` (symlinked to .env for automatic synchronization)
  - PATTERN: Add new environment variables to env.md, they will automatically appear in .env
  - PATTERN: Read current environment configuration from env.md instead of .env
  - VERIFICATION_CMD: `grep -rn "System.getenv\|@Value.*\${" fineract-provider/src/main/java/ | head -10`
  - VERIFICATION_CMD: `diff env.md .env` (should show no differences due to symlink)
  - ANTI_PATTERN: Directly editing .env file or hardcoding configuration values

### RULE: No Mock Data or Mock Implementations [P0]
CONTEXT: Production financial platform requires real implementations and authentic data processing
REQUIREMENT: All services must implement actual business logic without mock data, mock responses, or placeholder implementations
FAIL IF:
- Mock data used in any service implementations
- Placeholder TODO comments left in production code
- Fake responses returned from business services
- Mock API integrations used instead of real external connections
- Test data generators used in production services
- Development-only code paths left in production builds
VERIFICATION: Check for mock data, fake responses, and TODO comments in production code
REFERENCES:
  - VERIFICATION_CMD: `grep -rn "mock\|Mock\|MOCK\|fake\|Fake\|FAKE" fineract-provider/src/main/java/ --exclude-dir=test`
  - VERIFICATION_CMD: `grep -rn "TODO" fineract-provider/src/main/java/ --exclude-dir=test`
  - VERIFICATION_CMD: `grep -rn "placeholder\|dummy\|test.*data" fineract-provider/src/main/java/ --exclude-dir=test`
  - ANTI_PATTERN: Services returning mock responses or fake data
  - ANTI_PATTERN: External integrations with mock implementations
  - ANTI_PATTERN: TODO comments in production service implementations
  - EXCEPTION: Test classes and test resources are exempt from this rule

### RULE: DRY Principle for Service Layer Logic [P1]

CONTEXT: Common service operations must be consolidated to avoid code duplication and ensure consistency
REQUIREMENT: Duplicate service logic must be extracted into reusable methods with proper abstraction
FAIL IF:

- Similar API call patterns duplicated across multiple services
- Common data transformation logic repeated in multiple methods
- Identical error handling patterns not consolidated
- Template/dropdown data loading logic duplicated unnecessarily
- Entity mapping logic scattered across multiple services
- Validation patterns repeated instead of using shared validators
  VERIFICATION: Check for duplicated service logic and ensure consolidation
  REFERENCES:
  - PATTERN_EXAMPLE: `mapToClientCreditBureauData()` - consolidated entity-to-DTO mapping logic
  - PATTERN_EXAMPLE: Shared validation services instead of per-service validation logic
  - CONSOLIDATION_PATTERN: Single method handling multiple use cases with optional parameters
  - TEMPLATE_AVOIDANCE: Avoid unnecessary template API endpoints that provide unused dropdown data
  - MAPPING_PATTERN: Centralized entity-to-DTO mapping methods with configurable field mapping
  - VALIDATION_PATTERN: Shared validator classes for common patterns (PAN, Aadhaar, mobile)
  - VERIFICATION_CMD: `grep -rn "mapTo.*Data\|template\|Template" fineract-provider/src/main/java/ | grep -v test`
  - ANTI_PATTERN: Multiple services with 70%+ identical logic for similar operations
  - ANTI_PATTERN: Template API endpoints that load unused dropdown data
  - ANTI_PATTERN: Scattered entity mapping logic across multiple service implementations

### RULE: Error Handling with User Privacy
CONTEXT: Consistent error responses that don't expose sensitive system information
REQUIREMENT: All error responses must be user-friendly without exposing internal system details
FAIL IF:
- Stack traces exposed to end users
- Database constraint violations shown directly
- System paths or configuration exposed in errors
- Technical error messages not translated
- Sensitive data included in error logs
VERIFICATION: Check exception handling and error response formatting
REFERENCES: Exception handler implementations, error response formatters

### RULE: Database Constraint Error Handling [P0]

CONTEXT: Database constraint violations must be converted to user-friendly error messages following Fineract patterns
REQUIREMENT: All service methods must catch DataIntegrityViolationException and convert to PlatformDataIntegrityException with meaningful messages
FAIL IF:

- DataIntegrityViolationException thrown directly to API layer without conversion
- Generic exception handling used instead of specific constraint error handling
- Database constraint names exposed directly to users
- Error handling not following upstream Fineract patterns (AppUserWritePlatformServiceImpl, etc.)
- Missing handleDataIntegrityIssues() method in custom service implementations
- JpaSystemException and PersistenceException not properly caught and converted
  VERIFICATION: Check exception handling in @Transactional service methods
  REFERENCES:
  - PATTERN_EXAMPLE: `AppUserWritePlatformServiceImpl.java:137-142` (DataIntegrityViolationException handling)
  - PATTERN_EXAMPLE: `AppUserWritePlatformServiceImpl.java:177-182` (JpaSystemException handling with ExceptionUtils.getRootCause)
  - PATTERN_EXAMPLE: `ClientCreditBureauWritePlatformServiceImpl.handleDataIntegrityIssues()` (custom constraint handling)
  - UPSTREAM_PATTERN: `catch (final DataIntegrityViolationException dve) { throw handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve); }`
  - UPSTREAM_PATTERN: `catch (final JpaSystemException dve) { Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()); throw handleDataIntegrityIssues(command, throwable, dve); }`
  - ERROR_CONVERSION: `PlatformDataIntegrityException("error.msg.code", "User message", "field", "Technical details")`
  - VERIFICATION_CMD: `grep -rn "DataIntegrityViolationException" fineract-provider/src/main/java/ | grep -v test`
  - VERIFICATION_CMD: `grep -rn "handleDataIntegrityIssues" fineract-provider/src/main/java/ | head -10`
  - ANTI_PATTERN: `catch (Exception e) { throw e; }` without specific constraint handling
  - ANTI_PATTERN: Exposing database constraint names like "uk_extend_credit_score_report_model" directly to users
  - ANTI_PATTERN: Not catching JpaSystemException and PersistenceException in addition to DataIntegrityViolationException
