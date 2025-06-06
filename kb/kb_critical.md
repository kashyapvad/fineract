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
