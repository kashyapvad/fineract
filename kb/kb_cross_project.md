# Cross-Project Integration Guide

## Priority Legend
- **[P0]**: Critical - Security, data integrity, architectural foundations
- **[P1]**: Important - Performance, maintainability, best practices
- **[P2]**: Recommended - Code style, optimization, nice-to-have

## Common Multi-Project Scenarios

### **Scenario 1: API Development (Backend + Mobile + Web)**
**Task**: Adding new financial service endpoint
**Projects Involved**: Backend (primary), Android (consumer), Web (consumer)

**Load Order**:
1. **Backend**: `kb_critical.md`, `kb_java_backend.md`, `kb_security.md`
2. **Android**: `kb_critical.md`, `kb_android_architecture.md`, `kb_data_layer.md`
3. **Web**: `kb_critical.md`, `kb_angular_architecture.md`

**Critical Cross-Dependencies**:
- Backend API changes → Android repository layer updates → Web service layer updates
- Authentication changes → All three projects must align
- Data model changes → Database, mobile storage, and web forms must sync

### **Scenario 2: Authentication Enhancement (All Projects)**
**Task**: Implementing two-factor authentication
**Projects Involved**: Backend (auth provider), Android (auth consumer), Web (auth consumer)

**Load Order**:
1. **Backend**: `kb_critical.md` [P0 rules], `kb_security.md`
2. **Android**: `kb_critical.md` [P0 rules], `kb_security.md`
3. **Web**: `kb_critical.md` [P0 rules], `kb_security.md`

**Critical Cross-Dependencies**:
- Backend JWT/OAuth2 changes → Mobile token storage → Web token management
- Session management consistency across all platforms
- Security encryption standards must align

### **Scenario 3: Offline Sync Feature (Backend + Android)**
**Task**: Implementing offline transaction sync
**Projects Involved**: Backend (sync endpoint), Android (offline storage)

**Load Order**:
1. **Backend**: `kb_critical.md`, `kb_database.md`, `kb_java_backend.md`
2. **Android**: `kb_critical.md`, `kb_android_architecture.md`, `kb_data_layer.md`

**Critical Cross-Dependencies**:
- Backend tenant isolation [P0] → Android local data segmentation [P0]
- Backend command patterns → Android sync command patterns
- Database transaction boundaries → Mobile transaction boundaries

### **Scenario 4: UI Feature Implementation (Android + Web)**
**Task**: Adding client management feature
**Projects Involved**: Android (mobile UI), Web (web UI)

**Load Order**:
1. **Android**: `kb_critical.md`, `kb_android_architecture.md`, `kb_compose_ui.md`
2. **Web**: `kb_critical.md`, `kb_angular_architecture.md`, `kb_ui_components.md`

**Critical Cross-Dependencies**:
- Consistent API consumption patterns
- Similar validation logic
- Aligned user experience patterns

## Priority-Based Loading Strategy

### **For P0 (Critical) Tasks**
**Security/Data Integrity Issues**:
```
ALWAYS LOAD:
- backend/kb_critical.md [P0 rules only]
- android/kb_critical.md [P0 rules only]
- web/kb_critical.md [P0 rules only]
```

### **For P1 (Important) Tasks**
**Architecture/Performance Work**:
```
LOAD BASED ON PRIMARY PROJECT:
Primary Backend: kb_critical.md + kb_java_backend.md
Primary Android: kb_critical.md + kb_android_architecture.md
Primary Web: kb_critical.md + kb_angular_architecture.md
```

### **For P2 (Recommended) Tasks**
**Code Style/Optimization**:
```
LOAD ONLY PRIMARY PROJECT FILES:
Single project focus, refer to others only if needed
```

## Rule Dependencies Matrix

### **Backend → Android Dependencies**
| Backend Rule | Android Impact | Android KB Files |
|--------------|----------------|------------------|
| Tenant Context Validation [P0] | Local data segmentation [P0] | kb_critical.md, kb_data_layer.md |
| Command Handler Implementation [P1] | Offline sync commands [P1] | kb_android_architecture.md |
| Input Validation [P0] | Form validation [P0] | kb_security.md |

### **Backend → Web Dependencies**
| Backend Rule | Web Impact | Web KB Files |
|--------------|------------|--------------|
| Authentication State [P0] | Angular auth service [P0] | kb_critical.md, kb_security.md |
| API Design [P1] | HTTP service patterns [P1] | kb_angular_architecture.md |
| Error Handling [P1] | Angular error interceptors [P1] | kb_performance.md |

### **Android → Web Dependencies**
| Android Rule | Web Impact | Web KB Files |
|--------------|------------|--------------|
| Offline-First [P0] | PWA/Caching strategy [P1] | kb_performance.md |
| MVVM Patterns [P1] | Angular component patterns [P1] | kb_angular_architecture.md |
| Material Design [P2] | Angular Material consistency [P2] | kb_ui_components.md |

## Quick Decision Tree

```
TASK TYPE?
├── Security/Auth → Load ALL kb_critical.md [P0 rules]
├── API Changes → Load Backend kb_critical.md + Consumer kb_architecture.md
├── UI Feature → Load Primary UI kb + Secondary UI kb_architecture.md
├── Data Model → Load Backend kb_database.md + Consumer kb_data_layer.md
└── Performance → Load Primary kb_performance.md + Related kb_critical.md [P1 rules]
```

## LLM Workflow Optimization

### **Step 1: Identify Primary Project**
- Where is the main implementation happening?
- Load primary project's critical and specific KB files

### **Step 2: Identify Secondary Projects**
- Which other projects are impacted?
- Load only relevant KB files (usually kb_critical.md + one specific file)

### **Step 3: Check Cross-Dependencies**
- Use the dependencies matrix above
- Focus on P0 rules first, then P1, skip P2 unless specifically needed

### **Step 4: Verify Consistency**
- Ensure changes align with cross-project rules
- Check that security [P0] and architecture [P1] patterns are consistent

## Common Anti-Patterns to Avoid

1. **Loading All KB Files**: Leads to context overload and confusion
2. **Ignoring Cross-Dependencies**: Creates inconsistent implementations
3. **Mixing Priority Levels**: P0 security rules mixed with P2 style rules
4. **Single Project Focus**: Missing critical dependencies from other projects
5. **Sequential Loading**: Load related KB files together, not one at a time
