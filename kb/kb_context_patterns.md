# Fineract Context-Aware Development Patterns

## Development Context Recognition Patterns

### PATTERN: New Command Handler Development
CONTEXT_TRIGGERS:
- Creating new files with `@CommandType` annotation
- Implementing `NewCommandSourceHandler` interface
- Command-related package structures (`*.handler.*`, `*.command.*`)

SUGGESTED_WORKFLOW:
1. **Foundation Check**: Review `kb_critical.md` - Tenant Context Validation
2. **Implementation Guide**: Review `kb_backend_enterprise.md` - Command Handler Validation Pattern
3. **Testing Strategy**: Review `kb_testing.md` - Command Handler Test Coverage
4. **Validation**: Implement multi-layer validation per `kb_backend_enterprise.md`

REFERENCE_EXAMPLES:
- `CreateSmsCommandHandler.java:30-47` (basic pattern)
- `ApplyAddtionalSharesCommandHandler.java:40-47` (entity-specific pattern)
- `ValidCommandHandler.java:32-38` (test implementation)

CHECKLIST:
- [ ] @CommandType annotation with entity/action
- [ ] @Transactional with appropriate isolation
- [ ] Input validation before processing
- [ ] Proper CommandProcessingResult return
- [ ] Unit tests with tenant context setup
- [ ] Authorization validation tests

### PATTERN: API Endpoint Development
CONTEXT_TRIGGERS:
- Creating new REST controllers
- Adding @RequestMapping or HTTP method annotations
- API package structures (`*.api.*`, `*.rest.*`)

SUGGESTED_WORKFLOW:
1. **Security First**: Review `kb_security.md` - API Authentication Patterns
2. **Validation**: Review `kb_testing.md` - Authorization Test Patterns
3. **Enterprise Standards**: Review `kb_backend_enterprise.md` - Comprehensive Authorization Patterns
4. **Implementation**: Follow existing API patterns in codebase

REFERENCE_EXAMPLES:
- `BusinessDateApiResource.java` (permission checking patterns)
- API validation and error handling patterns
- Authentication and authorization test examples

CHECKLIST:
- [ ] @PreAuthorize annotations on sensitive endpoints
- [ ] Input validation with proper error responses
- [ ] Tenant context verification
- [ ] Comprehensive test coverage (auth + unauth scenarios)
- [ ] API documentation updates
- [ ] Error response standardization

### PATTERN: Database Entity/Repository Development
CONTEXT_TRIGGERS:
- Creating new JPA entities
- Repository interface implementations
- Database migration files (Liquibase)

SUGGESTED_WORKFLOW:
1. **Multi-Tenant Design**: Review `kb_critical.md` - Database Tenant Isolation
2. **Entity Patterns**: Review `kb_java_backend.md` - Repository patterns
3. **Migration Strategy**: Review `kb_database.md` - Liquibase patterns
4. **Testing**: Review `kb_testing.md` - Repository Layer Testing

REFERENCE_EXAMPLES:
- Existing entity classes with tenant awareness
- Repository implementations with tenant filtering
- Liquibase migration examples

CHECKLIST:
- [ ] Tenant isolation in entity design
- [ ] Proper database constraints
- [ ] Liquibase migration scripts
- [ ] Repository test coverage
- [ ] Data integrity validation
- [ ] Performance optimization (indexes, queries)

### PATTERN: Service Layer Development
CONTEXT_TRIGGERS:
- Creating service implementations
- Business logic methods
- Transaction boundary definitions

SUGGESTED_WORKFLOW:
1. **Architecture**: Review `kb_critical.md` - Service Layer Boundaries
2. **Enterprise Patterns**: Review `kb_backend_enterprise.md` - Business Logic Test Coverage
3. **Transaction Management**: Review `kb_backend_enterprise.md` - Transactional Boundary Management
4. **Testing**: Review `kb_testing.md` - Service Layer Testing

REFERENCE_EXAMPLES:
- Service implementations with proper transaction boundaries
- Business logic validation patterns
- Service test examples with mocking

CHECKLIST:
- [ ] Clear service boundaries
- [ ] Proper transaction annotations
- [ ] Business rule validation
- [ ] Comprehensive unit tests
- [ ] Integration test coverage
- [ ] Error handling patterns

### PATTERN: Validation Implementation
CONTEXT_TRIGGERS:
- Creating validator classes
- Data validation logic
- Exception handling for validation failures

SUGGESTED_WORKFLOW:
1. **Validation Framework**: Review `kb_testing.md` - Comprehensive Validator Testing
2. **Error Handling**: Review `kb_backend_enterprise.md` - Comprehensive Error Response Pattern
3. **Testing Patterns**: Review `kb_testing.md` - Validation Exception Testing

REFERENCE_EXAMPLES:
- `DelinquencyActionParseAndValidator.java` (comprehensive validation)
- `LoanReAgingValidator.java` (business rule validation)
- Validation test patterns with PlatformApiDataValidationException

CHECKLIST:
- [ ] Multi-layer validation implementation
- [ ] Specific error codes and messages
- [ ] Boundary value testing
- [ ] Business rule validation
- [ ] Comprehensive test coverage
- [ ] Error aggregation patterns

## Context-Sensitive Code Review Guidelines

### REVIEW_PATTERN: Command Handler Review
TRIGGERS: Pull requests containing `@CommandType` classes
FOCUS_AREAS:
- Tenant context validation
- Transaction boundary appropriateness
- Input validation completeness
- Test coverage adequacy
- Authorization checks

AUTOMATED_CHECKS:
```bash
# Verify @CommandType has corresponding tests
grep -r "@CommandType" --include="*.java" | sed 's/.*\/\([^/]*\)\.java.*/\1Test.java/' | xargs -I {} find . -name "{}"

# Check for tenant context in tests
grep -l "ThreadLocalContextUtil" src/test/java/**/*Test.java

# Verify @Transactional usage
grep -l "@CommandType" src/main/java/**/*.java | xargs grep -L "@Transactional"
```

### REVIEW_PATTERN: API Endpoint Review
TRIGGERS: Pull requests containing REST controller changes
FOCUS_AREAS:
- @PreAuthorize annotation presence
- Input validation patterns
- Error response consistency
- Test coverage for auth scenarios

AUTOMATED_CHECKS:
```bash
# Check for @PreAuthorize on public endpoints
grep -r "@RequestMapping\|@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping" --include="*.java" -A5 | grep -B5 -A5 "public.*(" | grep -v "@PreAuthorize"

# Verify API test coverage
find src/test/java -name "*ApiTest.java" -o -name "*ControllerTest.java"
```

### REVIEW_PATTERN: Database Changes Review
TRIGGERS: Pull requests containing entity or migration changes
FOCUS_AREAS:
- Tenant isolation patterns
- Migration script quality
- Index optimization
- Data integrity constraints

AUTOMATED_CHECKS:
```bash
# Check for tenant-aware queries
grep -r "createQuery\|createNativeQuery" --include="*.java" | grep -v "tenant"

# Verify migration files
find . -name "*.xml" -path "*/db/changelog/*" | head -5
```

## Proactive Development Guidance

### GUIDANCE: Pre-Development Checklist
CONTEXT: Before starting new feature development

RECOMMENDED_SEQUENCE:
1. **Architecture Review** (5 min)
   - Read relevant sections of `kb_critical.md`
   - Understand tenant isolation requirements
   - Review command/query separation needs

2. **Pattern Selection** (10 min)
   - Choose appropriate patterns from `kb_backend_enterprise.md`
   - Select testing strategy from `kb_testing.md`
   - Plan transaction boundaries

3. **Security Planning** (5 min)
   - Review authorization requirements
   - Plan permission validation
   - Consider audit trail needs

4. **Testing Strategy** (10 min)
   - Plan test coverage approach
   - Set up tenant context patterns
   - Design validation test scenarios

### GUIDANCE: Code Quality Gates
CONTEXT: Automated quality checks during development

CRITICAL_CHECKS (P0):
- All command handlers have @Transactional
- All public API methods have @PreAuthorize
- All tests setup proper tenant context
- All validation uses PlatformApiDataValidationException

IMPORTANT_CHECKS (P1):
- Service methods have proper error handling
- Database queries include tenant filtering
- Tests cover both success and failure scenarios
- Documentation updated for new features

### GUIDANCE: Performance Considerations
CONTEXT: Performance-critical code development

MONITORING_POINTS:
- Database query efficiency
- Transaction boundary optimization
- Caching strategy implementation
- Resource usage patterns

REFERENCE_BENCHMARKS:
- API response times < 200ms for CRUD operations
- Database query execution < 50ms
- Memory usage patterns within acceptable limits
- Thread pool utilization optimization

## Integration with Development Tools

### IDE Integration Patterns
SETUP_INSTRUCTIONS:
1. Configure code templates based on KB patterns
2. Set up automated KB rule validation
3. Enable context-sensitive help references
4. Configure automated test generation

### CI/CD Integration Patterns
PIPELINE_INTEGRATION:
1. Automated KB compliance checking
2. Context-aware test execution
3. Performance benchmark validation
4. Security pattern verification

### Documentation Generation
AUTO_DOCUMENTATION:
1. Generate API documentation with security annotations
2. Create test coverage reports with KB compliance
3. Maintain architecture decision records
4. Update deployment guides automatically
