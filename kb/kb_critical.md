# Fineract Backend Critical Architectural Guardrails

## Multi-Tenant Architecture Rules

**Note: Multi-tenant architecture is handled automatically by the Fineract framework through CommandHandlers and the upstream codebase. Custom code should focus on business logic rather than tenant management.**

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
**Note: Detailed transaction patterns are covered in `kb_backend_enterprise.md` - Transactional Boundary Management [P0]**

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
**Note: Detailed constructor injection patterns are covered in `kb_java_backend.md` - Constructor Dependency Injection**

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
