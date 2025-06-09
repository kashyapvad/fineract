# Fineract Backend Knowledge Base Index

## Overview
This Knowledge Base provides architectural guardrails and development guidelines for the Apache Fineract backend platform - a mature core banking solution built with Java 21, Spring Boot 3.x, and multi-tenant architecture.

## PROJECT STRUCTURE - CRITICAL FOR NAVIGATION

### TWO SEPARATE CODEBASES
This project consists of **TWO COMPLETELY SEPARATE CODEBASES** that must be handled differently:

#### 🔧 BACKEND CODEBASE (Java/Spring Boot)
- **Location**: `/Users/kash/Documents/GitHub/fineract/` (main working directory)
- **Technology**: Java 21, Spring Boot 3.x, PostgreSQL, Gradle
- **File Types**: `.java`, `.xml`, `.sql`, `.properties`, `.gradle`
- **Key Directories**:
  - `fineract-provider/` - Main Spring Boot application
  - `fineract-core/` - Core domain models
  - `fineract-loan/`, `fineract-savings/` - Feature modules
  - `fineract-db/` - Database schemas and migrations
  - `kb/` - Knowledge base documentation (this directory)

#### 🎨 FRONTEND CODEBASE (Angular/TypeScript)
- **Location**: `/Users/kash/Documents/GitHub/fineract-web/` (separate directory!)
- **Technology**: Angular, TypeScript, Material UI, SCSS
- **File Types**: `.ts`, `.html`, `.scss`, `.json`, `package.json`
- **Key Directories**:
  - `src/app/` - Angular components and services
  - `src/app/extend/` - Custom frontend modules
  - Root level has `package.json`, `angular.json`, etc.

### NAVIGATION RULES FOR AI ASSISTANTS
- **Backend Tasks**: Stay in `/fineract/` directory, use `gradle` commands
- **Frontend Tasks**: Navigate to `/fineract-web/` directory, use `npm`/`ng` commands
- **File Identification**: Check file extensions (`.java` = backend, `.ts` = frontend)
- **Build Tools**: Never mix Gradle (backend) with npm (frontend)
- **Formatting**: Use Prettier from `fineract-web/` directory for TypeScript/Angular files

## Priority-Based Quick Start
- **P0 (Critical)**: [kb_critical.md](./kb_critical.md) - Security, tenant isolation, data integrity
- **P1 (Important)**: [kb_java_backend.md](./kb_java_backend.md) - Architecture patterns, performance
- **P2 (Recommended)**: [kb_customization.md](./kb_customization.md) - Code style, optimization

## Cross-Project Integration
For multi-project tasks: **[kb_cross_project.md](./kb_cross_project.md)** - Dependencies with Android & Web

## Knowledge Base Structure

### 🎯 Core Architecture [P0]
- **[kb_critical.md](./kb_critical.md)** (15 rules)
  - Essential architectural guardrails
  - Multi-tenant data isolation patterns
  - Command-Query Separation (CQS) enforcement
  - Spring Boot configuration best practices
  - **Use when**: Starting new features, architectural decisions, code reviews

### ☕ Java Backend Patterns [P1]
- **[kb_java_backend.md](./kb_java_backend.md)** (20 rules)
  - Spring Boot service layer patterns
  - Command handler implementations
  - Repository and entity patterns
  - Transaction boundary management
  - Error handling and validation
  - **Use when**: Implementing services, commands, business logic

### 🏢 Enterprise Backend Robustness [P0]
- **[kb_backend_enterprise.md](./kb_backend_enterprise.md)** (25 rules)
  - Enterprise-grade command pattern validation
  - Comprehensive transaction management
  - Performance & scalability patterns
  - Error handling & resilience
  - Monitoring & observability
  - **Use when**: Production-ready features, enterprise deployment, robustness requirements

### 🧪 Testing & Validation Framework [P0]
- **[kb_testing.md](./kb_testing.md)** (15 rules)
  - Tenant context test setup patterns
  - Validation exception testing
  - Security & authorization testing
  - Service layer test coverage
  - Integration testing framework
  - **Use when**: Writing tests, test coverage validation, quality assurance

### 🎯 Context-Aware Development Patterns [P1]
- **[kb_context_patterns.md](./kb_context_patterns.md)** (Smart Guidance)
  - Development context recognition
  - Proactive workflow suggestions
  - Context-sensitive code review guidelines
  - Automated quality gates
  - Tool integration patterns
  - **Use when**: Starting new features, code reviews, automated development workflows

### 🗄️ Database Architecture [P0]
- **[kb_database_design.md](./kb_database_design.md)** (16 rules)
  - Multi-tenant database strategies
  - Schema design patterns
  - Java entity and connection patterns
  - **Use when**: Designing new entities, schema architecture, Java database code

- **[kb_database_migrations.md](./kb_database_migrations.md)** (18 rules)
  - XML migration patterns [P0]
  - Liquibase migration practices
  - Tenant migration strategies
  - **Use when**: Writing migration scripts, database schema changes

### 🔐 Security Implementation [P0]
- **[kb_security.md](./kb_security.md)** (15 rules)
  - OAuth2/Basic authentication patterns
  - Two-factor authentication (2FA)
  - Tenant-aware security filters
  - Authorization boundary enforcement
  - **Use when**: Authentication features, security reviews, access control

### 🔧 Customization & Extensions
- **[kb_customization.md](./kb_customization.md)** (10 rules)
  - Plugin architecture patterns
  - Extension point implementations
  - Module customization strategies
  - Hook and event systems
  - **Use when**: Building extensions, customizing modules, plugin development

### 🚀 Deployment & Operations
- **[kb_deployment.md](./kb_deployment.md)** (12 rules)
  - Docker containerization
  - Environment configuration
  - Production deployment patterns
  - Performance optimization
  - **Use when**: DevOps tasks, environment setup, production deployments

### 📜 Open Source Compliance
- **[kb_open_source.md](./kb_open_source.md)** (8 rules)
  - Apache License 2.0 compliance
  - Contribution guidelines
  - Code quality standards
  - Community practices
  - **Use when**: Contributing code, license compliance, community interaction

## Quick Navigation by Development Context

### 🆕 New Feature Development
1. Start with: `kb_critical.md` → `kb_java_backend.md`
2. Enterprise robustness: `kb_backend_enterprise.md`
3. Testing strategy: `kb_testing.md`
4. For data design: `kb_database_design.md`
5. For migrations: `kb_database_migrations.md`
6. For security features: `kb_security.md`

### 🔒 Security Features
1. Primary: `kb_security.md`
2. Foundation: `kb_critical.md` (tenant isolation)
3. Database: `kb_database_design.md` (data protection)

### 🏗️ Architecture Changes
1. Essential: `kb_critical.md`
2. Enterprise patterns: `kb_backend_enterprise.md`
3. Implementation: `kb_java_backend.md`
4. Testing coverage: `kb_testing.md`
5. Deployment: `kb_deployment.md`

### 🔧 Customization Work
1. Core: `kb_customization.md`
2. Architecture: `kb_critical.md`
3. Implementation: `kb_java_backend.md`

### 🚀 Production Deployment
1. Primary: `kb_deployment.md`
2. Security: `kb_security.md`
3. Database: `kb_database_design.md` (performance)
4. Migrations: `kb_database_migrations.md` (deployment scripts)

## Enhanced Knowledge Base Statistics
- **Total Rules**: ~130 architectural guardrails
- **Critical Rules**: 30 (must follow)
- **Implementation Rules**: 75 (code quality)
- **Testing Rules**: 15 (quality assurance)
- **Operational Rules**: 20 (deployment/maintenance)
- **🆕 Context Patterns**: Smart guidance system with real-time intelligence
- **🆕 Automated Quality Gates**: Executable validation commands
- **🆕 Proactive Workflows**: Context-aware development guidance

## Usage Instructions
1. **Before coding**: Read relevant KB files for your development context
2. **During development**: Use RULE format for validation checks
3. **Code reviews**: Reference specific rules in feedback
4. **Architecture decisions**: Consult kb_critical.md first

## AI Development Assistant Instructions
See [prompt.md](./prompt.md) for specific guidance on using this KB with AI-assisted development.

---
*Last Updated: December 2024*
*Fineract Version: 1.11+*
*Architecture: Multi-tenant Spring Boot Platform*
