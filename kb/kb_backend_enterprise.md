# Fineract Backend Enterprise-Grade Robustness Rules

## Command Pattern & Transaction Management [P0]

### RULE: Command Handler Validation Pattern [P0]
CONTEXT: All command handlers must implement comprehensive validation before processing
REQUIREMENT: Validate command data, business rules, and authorization before execution
FAIL IF:
- Command handlers skip validation steps
- Business rules not enforced at command level
- Missing authorization checks before processing
- Command execution without proper error handling
VERIFICATION: Check all @CommandType annotated classes for validation patterns
REFERENCES:
  - PATTERN_EXAMPLE: `CreateSmsCommandHandler.java:40-46` (command processing pattern)
  - HANDLER_REGISTRATION: `CommandHandlerProvider.java:64-76` (handler discovery)
  - VALIDATION_EXAMPLE: Command validation in service layer before processing
  - VERIFICATION_CMD: `grep -r "@CommandType" fineract-provider/src/main/java/ | head -10`

### RULE: Transactional Boundary Management [P0]
CONTEXT: Command handlers must define clear transaction boundaries
REQUIREMENT: Use @Transactional appropriately with proper isolation and rollback rules
FAIL IF:
- Missing @Transactional annotations on command handlers
- Incorrect transaction propagation settings
- Missing rollback conditions for business exceptions
- Long-running transactions without proper timeout
VERIFICATION: Check @Transactional usage on command handlers
REFERENCES:
  - TRANSACTION_EXAMPLE: `CreateSmsCommandHandler.java:39` (@Transactional usage)
  - ROLLBACK_PATTERN: Exception handling with rollback scenarios
  - ISOLATION_LEVELS: Proper isolation level configuration

### RULE: Command Result Consistency [P0]
CONTEXT: All command handlers must return consistent CommandProcessingResult
REQUIREMENT: Return proper result with entity ID, status, and change indicators
FAIL IF:
- Inconsistent CommandProcessingResult structure
- Missing entity ID in results
- Incomplete change tracking
- Missing error information in failure cases
VERIFICATION: Check CommandProcessingResult usage patterns
REFERENCES:
  - RESULT_PATTERN: CommandProcessingResult creation patterns
  - SUCCESS_RESULT: Successful command result structure
  - ERROR_RESULT: Error handling in command results

## Data Integrity & Validation [P0]

### RULE: Multi-Layer Validation Pattern [P0]
CONTEXT: Implement validation at API, service, and domain layers
REQUIREMENT: Each layer must validate data appropriate to its responsibility level
FAIL IF:
- Single layer validation only
- Missing business rule validation at service layer
- Domain constraints not enforced at entity level
- API layer validation bypassed
VERIFICATION: Check validation implementation across layers
REFERENCES:
  - API_VALIDATION: API layer data validation patterns
  - SERVICE_VALIDATION: Business rule validation in services
  - DOMAIN_VALIDATION: Entity-level constraint validation
  - VALIDATOR_EXAMPLE: `DelinquencyActionParseAndValidator.java` patterns

### RULE: Comprehensive Error Response Pattern [P0]
CONTEXT: All validation failures must provide detailed, actionable error information
REQUIREMENT: Use PlatformApiDataValidationException with specific error codes and parameters
FAIL IF:
- Generic error messages without context
- Missing field-specific error information
- No error codes for programmatic handling
- Incomplete validation error aggregation
VERIFICATION: Check PlatformApiDataValidationException usage patterns
REFERENCES:
  - ERROR_PATTERN: `PlatformApiDataValidationException.java:32-57` (error structure)
  - VALIDATION_AGGREGATION: Multiple error collection patterns
  - ERROR_CODES: Standardized error code usage

### RULE: Input Sanitization & Security [P0]
CONTEXT: All external input must be sanitized and validated for security
REQUIREMENT: Implement input sanitization, SQL injection prevention, and XSS protection
FAIL IF:
- Raw user input passed to database queries
- Missing input length validation
- No special character sanitization
- Unvalidated file uploads or data imports
VERIFICATION: Check input sanitization in API controllers and services
REFERENCES:
  - SANITIZATION_PATTERN: Input cleaning before processing
  - SQL_PROTECTION: Parameterized query usage
  - FILE_UPLOAD_SECURITY: Secure file handling patterns

## Performance & Scalability [P0]

### RULE: Query Optimization Patterns [P0]
CONTEXT: Database queries must be optimized for performance and scalability
REQUIREMENT: Use pagination, indexing, and query optimization techniques
FAIL IF:
- Missing pagination in list operations
- N+1 query problems in entity relationships
- Missing database indexes on query columns
- Inefficient query patterns or full table scans
VERIFICATION: Check query patterns and pagination implementation
REFERENCES:
  - PAGINATION_PATTERN: Page-based data retrieval
  - INDEX_OPTIMIZATION: Database index usage
  - QUERY_EFFICIENCY: Efficient query design patterns

### RULE: Caching Strategy Implementation [P0]
CONTEXT: Implement appropriate caching for frequently accessed data
REQUIREMENT: Cache static data, configuration, and frequently accessed entities
FAIL IF:
- No caching for static reference data
- Missing cache invalidation strategies
- Inappropriate cache TTL settings
- Cache stampede scenarios not handled
VERIFICATION: Check caching implementation and strategy
REFERENCES:
  - CACHE_PATTERN: Cache usage patterns
  - INVALIDATION_STRATEGY: Cache invalidation approaches
  - CACHE_CONFIGURATION: Cache TTL and size configuration

### RULE: Resource Management & Monitoring [P0]
CONTEXT: Implement proper resource management and monitoring
REQUIREMENT: Monitor resource usage, implement circuit breakers, and handle resource exhaustion
FAIL IF:
- No resource usage monitoring
- Missing circuit breaker patterns
- Unhandled resource exhaustion scenarios
- Memory leaks in long-running operations
VERIFICATION: Check resource management and monitoring implementation
REFERENCES:
  - MONITORING_PATTERN: Resource usage monitoring
  - CIRCUIT_BREAKER: Failure handling patterns
  - RESOURCE_CLEANUP: Proper resource disposal

## Error Handling & Resilience [P0]

### RULE: Comprehensive Exception Handling [P0]
CONTEXT: Implement comprehensive exception handling with proper error categorization
REQUIREMENT: Handle technical, business, and security exceptions appropriately
FAIL IF:
- Generic exception handling without categorization
- Missing retry mechanisms for transient failures
- Unhandled exception scenarios
- Exception information leakage to external users
VERIFICATION: Check exception handling patterns across all layers
REFERENCES:
  - EXCEPTION_HIERARCHY: Proper exception classification
  - RETRY_PATTERN: Transient failure handling
  - ERROR_LOGGING: Secure error logging practices

### RULE: Graceful Degradation Patterns [P0]
CONTEXT: System must degrade gracefully under failure conditions
REQUIREMENT: Implement fallback mechanisms and graceful service degradation
FAIL IF:
- No fallback mechanisms for service failures
- Complete system failure on component unavailability
- Missing timeout configurations
- No graceful shutdown procedures
VERIFICATION: Check resilience patterns and fallback implementations
REFERENCES:
  - FALLBACK_PATTERN: Service degradation strategies
  - TIMEOUT_CONFIGURATION: Proper timeout settings
  - SHUTDOWN_GRACEFUL: Clean shutdown procedures

## Security & Compliance [P0]

### RULE: Comprehensive Authorization Patterns [P0]
CONTEXT: Implement fine-grained authorization at all access points
REQUIREMENT: Use @PreAuthorize annotations and validate permissions consistently
FAIL IF:
- Missing @PreAuthorize annotations on sensitive operations
- Inconsistent permission checking patterns
- Authorization bypassed in internal calls
- Missing audit trail for authorization decisions
VERIFICATION: Check @PreAuthorize usage and authorization patterns
REFERENCES:
  - AUTH_ANNOTATION: `@PreAuthorize` usage patterns
  - PERMISSION_CHECK: Manual permission validation
  - AUDIT_TRAIL: Authorization decision logging
  - SECURITY_CONTEXT: `SpringSecurityPlatformSecurityContext.java` patterns

### RULE: Audit Trail & Compliance [P0]
CONTEXT: Maintain comprehensive audit trails for all business operations
REQUIREMENT: Log all business transactions, user actions, and system events
FAIL IF:
- Missing audit trails for business operations
- Incomplete user action logging
- No data access audit logs
- Missing compliance-required event tracking
VERIFICATION: Check audit logging implementation
REFERENCES:
  - AUDIT_LOGGING: Business operation audit patterns
  - USER_ACTION_LOG: User activity tracking
  - COMPLIANCE_LOG: Regulatory compliance logging

### RULE: Data Protection & Privacy [P0]
CONTEXT: Implement comprehensive data protection and privacy controls
REQUIREMENT: Encrypt sensitive data, implement data masking, and control data access
FAIL IF:
- Sensitive data stored in plain text
- Missing data masking in logs
- No data retention policies
- Uncontrolled data export capabilities
VERIFICATION: Check data protection implementation
REFERENCES:
  - ENCRYPTION_PATTERN: Sensitive data encryption
  - DATA_MASKING: PII masking in logs
  - RETENTION_POLICY: Data lifecycle management

## Monitoring & Observability [P0]

### RULE: Comprehensive Logging Framework [P0]
CONTEXT: Implement structured logging with proper context and correlation
REQUIREMENT: Use structured logging with correlation IDs and proper log levels
FAIL IF:
- Unstructured log messages
- Missing correlation IDs for request tracing
- Inappropriate log levels
- Sensitive data in log messages
VERIFICATION: Check logging implementation and structure
REFERENCES:
  - STRUCTURED_LOGGING: Log message structure patterns
  - CORRELATION_ID: Request tracing implementation
  - LOG_LEVELS: Appropriate logging level usage
  - TENANT_LOGGING: `TenantIdentifierLoggingConverter.java:29-31` (tenant context in logs)

### RULE: Health Check & Metrics [P0]
CONTEXT: Implement comprehensive health checks and metrics collection
REQUIREMENT: Monitor system health, performance metrics, and business metrics
FAIL IF:
- No health check endpoints
- Missing performance metrics
- Unmonitored business metrics
- No alerting on critical thresholds
VERIFICATION: Check health check and metrics implementation
REFERENCES:
  - HEALTH_CHECK: System health monitoring
  - PERFORMANCE_METRICS: Response time and throughput monitoring
  - BUSINESS_METRICS: Business operation metrics

### RULE: Distributed Tracing [P0]
CONTEXT: Implement distributed tracing for multi-service operations
REQUIREMENT: Trace requests across service boundaries with proper context propagation
FAIL IF:
- No distributed tracing implementation
- Missing trace context propagation
- Incomplete operation coverage
- No trace correlation with logs
VERIFICATION: Check distributed tracing implementation
REFERENCES:
  - TRACE_PROPAGATION: Context propagation patterns
  - OPERATION_TRACING: Service operation tracing
  - TRACE_CORRELATION: Log and trace correlation

## Configuration & Environment Management [P0]

### RULE: Environment-Specific Configuration [P0]
CONTEXT: Implement proper configuration management for different environments
REQUIREMENT: Use externalized configuration with environment-specific overrides
FAIL IF:
- Hardcoded configuration values
- Same configuration across all environments
- Missing configuration validation
- Sensitive configuration in source code
VERIFICATION: Check configuration management implementation
REFERENCES:
  - CONFIG_EXTERNALIZATION: External configuration patterns
  - ENVIRONMENT_OVERRIDE: Environment-specific settings
  - CONFIG_VALIDATION: Configuration value validation

### RULE: Secret Management [P0]
CONTEXT: Implement secure secret management and rotation
REQUIREMENT: Use secure secret storage with automatic rotation capabilities
FAIL IF:
- Secrets in configuration files
- No secret rotation strategy
- Plaintext secret storage
- Missing secret access auditing
VERIFICATION: Check secret management implementation
REFERENCES:
  - SECRET_STORAGE: Secure secret storage patterns
  - SECRET_ROTATION: Automatic rotation implementation
  - SECRET_AUDIT: Secret access monitoring

## Deployment & Operations [P0]

### RULE: Zero-Downtime Deployment [P0]
CONTEXT: Support zero-downtime deployments with proper versioning
REQUIREMENT: Implement database migrations, backward compatibility, and rolling deployments
FAIL IF:
- Breaking database schema changes
- Incompatible API changes without versioning
- No rollback capabilities
- Missing deployment health checks
VERIFICATION: Check deployment strategies and compatibility
REFERENCES:
  - SCHEMA_MIGRATION: Database migration patterns
  - API_VERSIONING: Backward compatibility strategies
  - ROLLBACK_STRATEGY: Deployment rollback procedures

### RULE: Operational Readiness [P0]
CONTEXT: Ensure operational readiness with proper tooling and procedures
REQUIREMENT: Implement operational tooling, runbooks, and incident response procedures
FAIL IF:
- No operational dashboards
- Missing incident response procedures
- Inadequate troubleshooting tools
- No automated recovery procedures
VERIFICATION: Check operational tooling and procedures
REFERENCES:
  - OPERATIONAL_DASHBOARD: System monitoring dashboards
  - INCIDENT_RESPONSE: Incident handling procedures
  - AUTOMATED_RECOVERY: Self-healing capabilities
