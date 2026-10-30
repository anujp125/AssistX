# AssistX Documentation Index

Welcome to the **AssistX** internal engineering and product documentation. This directory provides structured documentation covering the complete lifecycle of AssistX—from product definition and architecture to security constraints, development workflows, testing, and operations.

For a high-level overview of the application, architecture diagrams, and quickstart instructions, refer to the root [README.md](../README.md).

---

## Documentation Categories

```
docs/
├── product/                 # Vision, requirements, MVP scope, and decision checklist
├── architecture/            # System architecture, module structure, and state engine
├── design/                  # Metro UI specifications and styling guidelines
├── platform-and-security/   # Android permissions, accessibility constraints & UPI safety
├── testing/                 # Strategy, acceptance criteria, device test matrix & failure injection
├── development/             # Agent guidelines, execution playbooks, sprint plans & DoD
├── operations-and-release/  # Diagnostics, telemetry, recovery matrix & release planning
├── project-management/      # Requirements traceability and change control
└── reference/               # Architecture decision log and pre-development questionnaire
```

---

## 1. Product (`docs/product/`)
Documents defining product vision, scope boundaries, and core functional specifications.

- [**Product Brief**](product/product_brief.md): Executive summary, problem statement, user personas, and target outcomes.
- [**Software Requirements Specification (SRS)**](product/SRS.md): Comprehensive functional and non-functional requirements (`FR-001` through `NFR-012`).
- [**MVP Scope**](product/MVP_scope.md): In-scope vs. out-of-scope boundaries for MVP v0.1.0 release.
- [**Product Roadmap**](product/product_roadmap.md): Phased roadmap across MVP, post-MVP, and future horizons.
- [**Feature Catalog**](product/feature_catalog.md): Complete index of assistive features, gestures, and settings.
- [**User Stories**](product/user_stories.md): User personas, epics, and acceptance scenarios.
- [**Product Decisions Checklist**](product/PRODUCT_DECISIONS_CHECKLIST.md): Approved decision register (`D-001` through `D-025`).

## 2. Architecture (`docs/architecture/`)
Deep-dive technical specifications of the Android subsystem architecture, state machine, and data flow.

- [**System Architecture**](architecture/system_architecture.md): High-level block diagram, process model, and architectural patterns.
- [**Architecture Decisions**](architecture/architecture_decisions.md): Architecture Decision Records (`ADR-001` through `ADR-010`).
- [**Module & Package Structure**](architecture/module&packages.md): Codebase organization, package boundaries, and class responsibilities.
- [**State & Lifecycle**](architecture/state&lifecycle.md): Formal state engine, transition diagrams, and background service lifecycle.

## 3. Design (`docs/design/`)
User interface guidelines, design philosophy, and typography rules.

- [**UI/UX Specifications**](design/ui_ux_specifications.md): Screen layouts, component hierarchies, and interactive states.
- [**UI Style Guide**](design/ui-style-guide.md): 0dp Windows Phone Metro aesthetic, WCAG AA contrast, and typography standards.

## 4. Platform & Security (`docs/platform-and-security/`)
Android platform constraints, sandbox requirements, permission handling, and payment security.

- [**Permissions & Platform Constraints**](platform-and-security/permissions&platform_constraints.md): Detailed permission audit, OEM background killing mitigation, and Android 8–14 compatibility.
- [**UPI Security & Accessibility Design**](platform-and-security/upi-security-and-accessibility-design.md): Payment Safety Mode, accessibility detachment protocols, and zero-overlay security.

## 5. Testing (`docs/testing/`)
Verification strategies, test execution matrix, automated test suites, and hardware validation.

- [**Test Strategy**](testing/test_strategy.md): Multi-tier test pyramid (Unit, Robolectric, Real-Device, CI).
- [**Acceptance Tests**](testing/acceptance_tests.md): Verifiable acceptance criteria for all user stories.
- [**Real Device Test Matrix**](testing/real_device_test.md): Physical device validation procedures, gestures, and OEM-specific edge cases.
- [**Failure Injection Plan**](testing/failure_injection_plan.md): Crash resilience, permission revoking, and recovery testing.
- [**Known Issues**](testing/known_issues.md): Tracked edge cases, limitations, and platform quirks.
- [**Bug Report Template**](testing/bug_report_template.md): Standardized format for defects and hardware anomalies.

## 6. Development (`docs/development/`)
Engineering guidelines, coding conventions, sprint execution, and AI agent workflows.

- [**Central Agent Guidelines**](development/agents.md): Rules for AI coding assistants and autonomous pair programming.
- [**AssistX Agent Execution Playbook**](development/ASSISTX_AGENT_EXECUTION_PLAYBOOK.md): Step-by-step implementation and verification playbook.
- [**AI Agent Workflow**](development/ai_agent_workflow.md): Phased development protocol and governance.
- [**Coding Standards**](development/coding_standards.md): Kotlin style guide, architecture patterns, and naming conventions.
- [**Definition of Done (DoD)**](development/definition_of_done.md): Release gates, verification thresholds, and sign-off criteria.
- [**Sprint Plan**](development/sprint_plan.md): Sprint 1 backlog, task breakdowns (`ASSISTX-001` to `ASSISTX-034`), and timeline.
- [**Sprint Task Template**](development/sprint_task_template.md): Template for sprint task specifications and execution logs.

## 7. Operations & Release (`docs/operations-and-release/`)
Runtime telemetry, diagnostics storage, error recovery, and release management.

- [**Data & Diagnostics**](operations-and-release/data&diagnostics.md): Room telemetry database (`assistx_diagnostics.db`), DataStore schema, and local-only logging.
- [**Failure & Recovery Matrix**](operations-and-release/failure&recovery.md): System failure modes, recovery policies, and self-healing mechanisms.

## 8. Project Management (`docs/project-management/`)
Traceability, scope governance, and change management.

- [**Requirements Traceability**](project-management/requirements_tracebility.md): Forward and backward traceability matrix mapping requirements to test suites.
- [**Change Control**](project-management/change_control.md): Protocol for managing scope adjustments, ADR amendments, and schema migrations.

## 9. Reference (`docs/reference/`)
Supporting historical documentation and decision records.

- [**Decision Log**](reference/decision_log.md): Historical record of approved project decisions (`D-001` through `D-025`).
- [**Pre-Development Questionnaire**](reference/QUESTIONNAIRE.md): Baseline clarification responses and design options.

---

## Quick Reference: Where to Look

| Task / Topic | Recommended Document Path |
| :--- | :--- |
| **Understanding what AssistX does** | [`docs/product/product_brief.md`](product/product_brief.md) |
| **Understanding app architecture & services** | [`docs/architecture/system_architecture.md`](architecture/system_architecture.md) |
| **Metro UI styling & color contrast** | [`docs/design/ui-style-guide.md`](design/ui-style-guide.md) |
| **Android Accessibility & Overlay permissions**| [`docs/platform-and-security/permissions&platform_constraints.md`](platform-and-security/permissions&platform_constraints.md) |
| **Payment safety & UPI compliance** | [`docs/platform-and-security/upi-security-and-accessibility-design.md`](platform-and-security/upi-security-and-accessibility-design.md) |
| **Running tests & device validation** | [`docs/testing/test_strategy.md`](testing/test_strategy.md) & [`docs/testing/real_device_test.md`](testing/real_device_test.md) |
| **Agent conventions & sprint tasks** | [`docs/development/agents.md`](development/agents.md) & [`docs/development/sprint_plan.md`](development/sprint_plan.md) |
| **Local diagnostics database & events** | [`docs/operations-and-release/data&diagnostics.md`](operations-and-release/data&diagnostics.md) |
| **Decision records & approved choices** | [`docs/product/PRODUCT_DECISIONS_CHECKLIST.md`](product/PRODUCT_DECISIONS_CHECKLIST.md) |

