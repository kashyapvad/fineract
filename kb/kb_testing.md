# Fineract Backend Testing & Validation Framework

## Critical Testing Guardrails [P0]

### RULE: Tenant Context Test Setup [P0]
CONTEXT: All unit tests must properly initialize tenant context for multi-tenant operations
REQUIREMENT: Tests must setup and teardown ThreadLocalContextUtil context properly
FAIL IF:
- Tests run without tenant context setup
- Missing teardown in @AfterEach methods
- Tests share context state between executions
- Thread local leaks between tests
VERIFICATION: Check ThreadLocalContextUtil usage in test setup/teardown
REFERENCES:
  - PATTERN_EXAMPLE: `ExternalEventServiceTest.java:105-124` (proper setup with business dates)
  - CLEANUP_PATTERN: `CustomDateTimeProviderTest.java:39-42` (reset in teardown)
  - CONTEXT_SETUP: `WithTenantContextExtension.java:33-45` (reusable extension)
  - VERIFICATION_CMD: `grep -rn "@BeforeEach\|@AfterEach" fineract-provider/src/test/java/ | grep -A5 -B5 ThreadLocalContextUtil`

### RULE: Validation Exception Testing [P0]
CONTEXT: All data validation scenarios must be tested with proper exception assertions
REQUIREMENT: Use PlatformApiDataValidationException testing patterns for validation failures
FAIL IF:
- Validation tests don't verify specific error codes and messages
- Missing boundary value testing for constraints
- Validation bypassed in test scenarios
- Exception assertions incomplete or generic
VERIFICATION: Check PlatformApiDataValidationException usage in tests
REFERENCES:
  - PATTERN_EXAMPLE: `DelinquencyActionParseAndValidatorTest.java:405-415` (proper validation testing)
  - ASSERTION_PATTERN: `CreditAllocationsValidatorTest.java:119-130` (detailed exception verification)
  - ERROR_CHECKING: `LoanReAgingValidatorTest.java:84-94` (specific validation scenarios)

### RULE: Command Handler Test Coverage [P0]
CONTEXT: All command handlers must have comprehensive test coverage
REQUIREMENT: Test success paths, validation failures, authorization, and error handling
FAIL IF:
- Command handlers lack unit tests
- Missing authorization test scenarios
- Business rule validation not tested
- Error handling paths untested
VERIFICATION: Ensure all @CommandType handlers have corresponding test classes
REFERENCES:
  - PATTERN_EXAMPLE: `SynchronousCommandProcessingServiceTest.java:87-132` (command execution testing)
  - HANDLER_EXAMPLE: `ValidCommandHandler.java:32-38` (test command handler)
  - PROVIDER_TEST: `CommandHandlerProviderStepDefinitions.java:30-48` (BDD testing approach)

## Security Testing Framework [P0]

### RULE: Authorization Test Patterns [P0]
CONTEXT: All API endpoints and services must test authorization scenarios
REQUIREMENT: Test both authorized and unauthorized access patterns
FAIL IF:
- API tests don't verify permission requirements
- Missing authorization failure test cases
- User role/permission validation untested
- Security bypass scenarios not covered
VERIFICATION: Check @PreAuthorize annotations have corresponding test coverage
REFERENCES:
  - PATTERN_EXAMPLE: `BusinessDateApiTest.java:67-95` (permission testing)
  - AUTH_FAILURE: `BusinessDateApiTest.java:106-114` (unauthorized access testing)
  - USER_VALIDATION: `SpringSecurityPlatformSecurityContextTest.java:71-81` (user authentication testing)

### RULE: Security Context Testing [P0]
CONTEXT: Security context must be properly mocked and validated in tests
REQUIREMENT: Use proper security context setup for authentication/authorization tests
FAIL IF:
- Security context not properly mocked
- Authentication state not validated
- User principal verification missing
- Security exceptions not tested
VERIFICATION: Check SecurityContext and Authentication mocking patterns
REFERENCES:
  - CONTEXT_MOCK: `SpringSecurityPlatformSecurityContextTest.java:44-69` (security context mocking)
  - AUTH_TESTING: `BusinessDateApiTest.java:57-83` (authenticated user testing)

## Data Validation Testing [P0]

### RULE: Comprehensive Validator Testing [P0]
CONTEXT: All data validators must test edge cases, constraints, and business rules
REQUIREMENT: Test null values, boundary conditions, format validation, and business constraints
FAIL IF:
- Validators lack comprehensive test coverage
- Edge cases not tested (null, empty, boundary values)
- Business rule validation scenarios missing
- Error message verification incomplete
VERIFICATION: Ensure all DataValidator implementations have test classes
REFERENCES:
  - BOUNDARY_TESTING: `LoanReAgingValidatorTest.java:92-104` (external ID length validation)
  - NULL_TESTING: Business date validation scenarios
  - FORMAT_TESTING: Date format and locale validation patterns

### RULE: JSON Command Validation [P0]
CONTEXT: All JsonCommand processing must validate input data thoroughly
REQUIREMENT: Test malformed JSON, missing required fields, invalid formats
FAIL IF:
- JSON deserialization not tested
- Missing field validation incomplete
- Invalid data type handling untested
- Locale and format validation missing
VERIFICATION: Check JsonCommand usage in validator tests
REFERENCES:
  - COMMAND_TESTING: `DelinquencyActionParseAndValidatorTest.java:383-402` (JSON command creation)
  - FORMAT_VALIDATION: Date format and locale testing patterns

## Service Layer Testing [P0]

### RULE: Business Logic Test Coverage [P0]
CONTEXT: All business services must have comprehensive unit test coverage
REQUIREMENT: Test business logic paths, error scenarios, and edge cases
FAIL IF:
- Service methods lack unit tests
- Business rule validation not tested
- Integration scenarios incomplete
- Transactional behavior not verified
VERIFICATION: Ensure service classes have corresponding test coverage
REFERENCES:
  - SERVICE_TESTING: External event service testing patterns
  - MOCK_USAGE: Proper service dependency mocking

### RULE: Repository Layer Testing [P0]
CONTEXT: Repository operations must be tested for data integrity and constraints
REQUIREMENT: Test data persistence, retrieval, and constraint validation
FAIL IF:
- Repository methods lack test coverage
- Database constraint validation missing
- Data integrity scenarios untested
- Query optimization not verified
VERIFICATION: Check repository test coverage and data integrity testing

## Integration Testing Framework [P0]

### RULE: End-to-End Workflow Testing [P0]
CONTEXT: Critical business workflows must have integration test coverage
REQUIREMENT: Test complete workflows from API to database with realistic data
FAIL IF:
- Critical workflows lack integration tests
- Cross-service interactions untested
- Data consistency across services not verified
- Performance impact not measured
VERIFICATION: Ensure integration-tests module covers core workflows

### RULE: Database Transaction Testing [P0]
CONTEXT: Transactional behavior must be tested for consistency and rollback scenarios
REQUIREMENT: Test transaction boundaries, rollback scenarios, and data consistency
FAIL IF:
- Transaction rollback scenarios untested
- Data consistency validation missing
- Concurrent access scenarios not covered
- Deadlock prevention not verified
VERIFICATION: Check @Transactional behavior in integration tests

## Test Quality Standards [P0]

### RULE: Test Isolation and Independence [P0]
CONTEXT: Tests must be independent and not rely on execution order
REQUIREMENT: Each test must setup its own data and clean up properly
FAIL IF:
- Tests depend on other tests' data
- Shared state between tests
- Inconsistent test results based on execution order
- Database state leakage between tests
VERIFICATION: Run tests in random order and verify consistency

### RULE: Meaningful Test Names and Documentation [P0]
CONTEXT: Test methods must clearly describe what is being tested
REQUIREMENT: Use descriptive test names that explain the scenario and expected outcome
FAIL IF:
- Generic test method names (test1, testMethod)
- Missing test documentation for complex scenarios
- Unclear test assertions
- Missing setup context documentation
VERIFICATION: Review test method naming conventions and documentation

## Performance Testing Requirements [P0]

### RULE: Critical Path Performance Testing [P0]
CONTEXT: Performance-critical operations must have benchmark tests
REQUIREMENT: Test response times, throughput, and resource usage
FAIL IF:
- No performance baselines established
- Critical operations lack performance tests
- Memory leaks not detected
- Database query performance not validated
VERIFICATION: Implement performance test suites for critical operations

### RULE: Load Testing for Scalability [P0]
CONTEXT: Multi-tenant operations must be tested under load
REQUIREMENT: Test concurrent user scenarios and resource contention
FAIL IF:
- Concurrent access scenarios untested
- Resource contention not validated
- Tenant isolation under load not verified
- System limits not established
VERIFICATION: Implement load testing scenarios for multi-tenant operations

## Test Automation and CI/CD [P0]

### RULE: Automated Test Execution [P0]
CONTEXT: All tests must run automatically in CI/CD pipeline
REQUIREMENT: Tests must be deterministic and pass consistently
FAIL IF:
- Flaky tests that fail intermittently
- Tests require manual intervention
- Environment-dependent test failures
- Missing test coverage reporting
VERIFICATION: Monitor CI/CD test execution and failure rates

### RULE: Test Coverage Monitoring [P0]
CONTEXT: Maintain high test coverage across all critical components
REQUIREMENT: Establish and maintain minimum coverage thresholds
FAIL IF:
- Coverage below established thresholds
- New code lacks adequate test coverage
- Critical paths not covered by tests
- Coverage reporting not integrated in CI
VERIFICATION: Implement coverage monitoring and enforce thresholds
