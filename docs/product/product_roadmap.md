AssistX — Product Roadmap
Document ID: NUDGE-ROADMAP-001
Version: 0.1
Status: Approved
Last updated: 2026-10-29
Related documents: PRODUCT_BRIEF.md, SRS.md, MVP_SCOPE.md, SPRINT_PLAN.md
Instruction for AI agents: This roadmap defines the intended development order and milestone exit gates, not permission to implement every milestone in one large change. Execute only the currently approved sprint/task. Do not skip a gate because a later feature appears easy. Preserve unresolved decisions as placeholders and report blockers explicitly.

1. Purpose
This roadmap turns the AssistX v0.1 product scope into a sequence of reviewable milestones. It is designed for a solo developer working with AI coding agents, with emphasis on small changes, test evidence, and preventing architecture and feature scope from expanding without approval.

AssistX is one installable native Android app containing:
- a floating assistant;
- an in-app Studio/settings interface;
- shared state, preferences, and local diagnostics.
The roadmap is outcome-based. It does not assign calendar dates or promise a delivery duration because the implementation baseline, selected UI toolkit, unresolved product decisions, and measured device behavior are not yet known.
2. Roadmap Principles
1. Resolve high-impact decisions before dependent code. Do not guess the primary action, minimum Android version, or other decisions that could alter permissions or architecture.
2. Build the smallest vertical slice first. A working, testable behavior is preferred over broad scaffolding without verified behavior.
3. Separate requirements from implementation. The SRS defines required behavior; architecture documents define the technical approach; the sprint plan defines bounded work.
4. Test each milestone before moving forward. Passing a build is not proof that runtime behavior works on the device.
5. Use platform evidence. Verify Android restrictions through current official documentation and actual-device tests where the result is device-dependent.
6. No hidden scope expansion. Extra actions, libraries, frameworks, cloud services, and nonessential refactors require review.
7. Do not fake confidence. Unverified cases remain open and are identified in the test report.
8. Maintain a single source of truth. When an approved decision changes, update affected documents and traceability records.
3. Current Baseline and Open Inputs
Known direction
- One installable Android application.
- Native Android with Kotlin.
- Reliability-first MVP.
- No backend or cloud dependency.
- Floating button and Studio interface share one application package.
- Overlay availability and Accessibility state must be represented separately.
- Runtime recovery must be bounded and must respect Android permission controls.
- First test target: Realme Narzo 30 4G, model RMX2156, Android 12, realme UI 3.0, build RMX2156_11_F.19, as reported by the product owner.
Inputs not yet approved
ID	Item	Needed by
R-OPEN-01	Exact primary floating-button action	Before action-dependent implementation
R-OPEN-02	Trigger gesture and timing	Before interaction implementation
R-OPEN-03	Minimum supported Android version	Before project configuration and compatibility claims
R-OPEN-04	UI toolkit: Compose or Views	Before final UI implementation
R-OPEN-05	Application ID/package name	Before release configuration; may use a clearly temporary local ID in an early spike if approved
R-OPEN-06	Distribution method	Before release/signing decisions
R-OPEN-07	Device coverage beyond the target phone	Before compatibility claims
R-OPEN-08	Dragging and position persistence	Before floating-button interaction is finalized
R-OPEN-09	Reliability measurement period and targets	Before final reliability sign-off
R-OPEN-10	Diagnostic retention limits	Before diagnostics are considered release-ready


A sprint can proceed while an unrelated decision is open, but it must not make a permanent or difficult-to-reverse choice that depends on that decision.
4. Milestone Overview
Milestone	Name	Main outcome	Depends on
M0	Product baseline and decisions	Scope, requirements, and high-impact choices reviewed	Existing product brief, SRS, and scope draft
M1	Architecture and test design	Approved system design, state model, platform constraints, and test approach	M0 decisions needed for architecture
M2	Repository and app foundation	Reproducible Android project build and baseline tests	M1
M3	Floating overlay vertical slice	One permission-aware floating button with safe attach/remove behavior	M2; approved basic action/interaction design
M4	State and lifecycle management	Separate desired state from observed runtime health; bounded recovery foundation	M3; state/lifecycle specification
M5	Accessibility integration, if required	Approved action works with accurate Accessibility state handling	Owner-approved action and verified platform approach
M6	Studio and diagnostics	Status/recovery UI and useful bounded local event records	M4; M5 where relevant
M7	Reliability and device validation	Required scenarios run; failures fixed or explicitly documented	M3–M6
M8	Release candidate and handoff	Build, instructions, limitations, and release checks complete	M7; distribution/signing decisions


Conditional milestone: M5 may be reduced or omitted if the approved v0.1 action does not require Accessibility. Do not add an Accessibility Service solely because similar apps use one.
5. Milestone Specifications and Exit Gates
M0 — Product baseline and decisions
Objective: Ensure the first implementation has a sufficiently unambiguous scope.

Work products
- Reviewed PRODUCT_BRIEF.md.
- Reviewed SRS.md.
- Approved MVP_SCOPE.md.
- Recorded owner decisions in DECISION_LOG.md.
- Identified requirements that are blocked by unanswered questions.
Exit criteria
- The MVP's purpose and excluded features are accepted.
- The primary button action and its required capability are decided, or a limited technical spike is explicitly authorized to compare options.
- High-impact Android permission implications are identified.
- No agent is expected to infer a product decision from an unanswered placeholder.
Do not begin: full implementation of the primary action or permission-sensitive architecture based on a guess.

Review questions
- Is the proposed primary action definitely part of v0.1?
- Does it require Accessibility, device-admin privileges, or another special capability?
- What is the intended minimum Android version and distribution method?
M1 — Architecture and test design
Objective: Convert the approved requirements into a design small enough to implement incrementally.

Work products
- SYSTEM_ARCHITECTURE.md
- MODULE_AND_PACKAGE_STRUCTURE.md
- STATE_AND_LIFECYCLE.md
- PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md
- FAILURE_AND_RECOVERY_MATRIX.md
- TEST_STRATEGY.md
- Initial ACCEPTANCE_TESTS.md
- Initial REQUIREMENTS_TRACEABILITY.md
- Relevant entries in ARCHITECTURE_DECISIONS.md
Exit criteria
- Component responsibilities and dependencies are explicit.
- Desired user state is separated from observed runtime state.
- Overlay and Accessibility health are modeled independently.
- Startup, shutdown, interruption, permission loss, and recovery paths are described.
- Recovery policy has retry bounds and duplicate-prevention rules.
- Testable acceptance criteria exist for the first vertical slice.
- Permission and background-execution claims that affect the design have been checked against current official Android documentation.
Do not begin: speculative abstractions, extra application modules, or large dependency additions without a concrete need.
M2 — Repository and application foundation
Objective: Create a reproducible development baseline.

Work products
- Android project and application configuration.
- Minimal package structure.
- Version-controlled project files.
- Baseline build and test commands documented in README.md.
- .gitignore and initial AGENTS.md.
- Basic CI/build verification where feasible.
Exit criteria
- The project builds in the documented environment.
- Baseline tests execute.
- No secrets, generated build output, local databases, or machine-specific configuration are accidentally committed.
- The repository instructions tell agents to inspect before editing and to work only on the assigned task.
- The minimum SDK, target SDK, package ID, and UI toolkit choices are explicit or correctly marked as temporary and unresolved.
Do not begin: implementing several product features in one change or adding an unneeded backend.
M3 — Floating overlay vertical slice
Objective: Prove that one floating button can be shown and removed predictably.

Work products
- Overlay permission check and appropriate user guidance.
- Minimal floating-button view.
- Safe overlay attachment/removal.
- Duplicate-overlay prevention.
- Basic tests for state and overlay-management logic where testable.
- Manual test record for permission granted/denied and repeated enable/disable.
Exit criteria
- With permission available and AssistX enabled, one floating button appears.
- With required permission unavailable, AssistX does not falsely report the overlay as active.
- Repeated start requests do not create multiple buttons.
- Disabling AssistX removes the overlay when allowed and reports failures usefully.
- Closing Studio does not itself become a requirement for the overlay's continued operation.
- The implementation follows the approved button interaction scope.
Do not begin: advanced gestures, customization, extra quick actions, themes, or a radial menu.
M4 — State and lifecycle management
Objective: Make runtime conditions explicit and make normal interruption handling safe.

Work products
- Desired-state storage and observed-state model.
- Lifecycle coordinator or equivalent minimal mechanism, if justified by the design.
- Bounded recovery logic.
- Structured state/event logging.
- Unit tests for transitions, invariants, and duplicate-prevention behavior.
- Tests or a manual procedure for process interruption where practical.
Exit criteria
- Active is not inferred solely from a saved preference.
- User disablement suppresses automatic recovery.
- Repeated lifecycle events do not create duplicate services or overlays.
- Recovery attempts have clear conditions, limits, and recorded outcomes.
- Permission loss and runtime interruption produce distinct states where detectable.
- Platform-impossible recovery paths are reported as requiring user intervention.
Do not begin: aggressive keep-alive loops or attempts to circumvent Android restrictions.
M5 — Accessibility integration (conditional)
Objective: Implement only the owner-approved action that requires Accessibility and make its availability observable.

Preconditions
- The primary action and gesture are approved.
- The requirement for Accessibility is verified.
- Manifest/service configuration and relevant platform constraints have been reviewed.
- The behavior can be implemented without bypassing system controls.
Work products
- Accessibility Service integration if needed.
- Enabled-versus-connected status handling to the extent supported by Android.
- User guidance for disabled or disconnected conditions.
- Tests for the state model and action logic where feasible.
- Device tests for enabling, disabling, reconnecting, and using the approved action.
Exit criteria
- No Accessibility component exists unless justified by an approved feature.
- The app distinguishes enabled settings from runtime connection where detectable.
- AssistX does not claim it can silently turn Accessibility back on.
- Accessibility failure is not misreported as overlay failure.
- The approved action is tested on the target device.
Do not begin: additional Accessibility-driven functions or unrelated system actions.
M6 — Studio and local diagnostics
Objective: Give the user an accurate view of status and a practical way to investigate failures.

Work products
- Minimal Studio status screen.
- Enable/disable control.
- Permission and recovery guidance.
- Recent diagnostic-event view, if retained in v0.1.
- Local preferences and bounded diagnostic storage.
- Privacy/permission explanation.
- Tests for state presentation and storage logic.
Exit criteria
- Status labels match the state model.
- Required next steps are clear to the user.
- The interface differentiates overlay availability from Accessibility availability.
- Diagnostics can distinguish relevant observed events, errors, and recovery outcomes.
- Retention/size limits are defined and tested.
- No unapproved network transmission or analytics dependency exists.
Do not begin: account systems, cloud sync, dashboards requiring a backend, or advanced UI customization.
M7 — Reliability and real-device validation
Objective: Validate the product against its actual failure modes, not just successful startup.

Work products
- Completed ACCEPTANCE_TESTS.md results.
- Completed REAL_DEVICE_TEST_MATRIX.md results.
- Failure-injection results for scenarios that can be tested safely.
- Reproducible bug reports for failures.
- KNOWN_ISSUES.md and COMPATIBILITY_AND_LIMITATIONS.md updates.
- Performance and resource-observation notes.
- Updated traceability mapping.
Required scenario categories
- Fresh install and first-run setup.
- Overlay permission granted and denied/revoked.
- Enable/disable repetition.
- Open/close Studio.
- App switching and screen lock/unlock.
- Accessibility enabled, disabled, and disconnected, when relevant.
- Runtime interruption and permitted recovery.
- Process death and force-stop, distinguishing their different platform semantics.
- Device reboot and the resulting startup behavior.
- Long-duration observation for unexpected disappearance, duplicate instances, battery impact, and log growth.
Exit criteria
- The approved acceptance suite has been executed in the relevant environments.
- Failures are fixed or logged as known issues with severity and workaround.
- No critical open defect blocks the approved core workflow.
- Unsupported recovery guarantees are not claimed.
- The owner can review what passed, failed, was not tested, and remains uncertain.
A failure-injection scenario must not be marked passed if it was never executed. Where a scenario cannot be automated or safely induced, document the manual method or the limitation.
M8 — Release candidate and handoff
Objective: Produce a build that can be installed and evaluated using repeatable instructions.

Work products
- Release build configuration appropriate to the chosen distribution method.
- INSTALLATION_AND_SETUP.md
- TROUBLESHOOTING.md
- PRIVACY_AND_PERMISSIONS.md
- BUILD_AND_RELEASE.md
- RELEASE_CHECKLIST.md
- Updated README.md and CHANGELOG.md
- Version identifier and known limitations.
- License decision if the repository is to be distributed publicly.
Exit criteria
- The release build can be produced through the documented process.
- Installation and permission setup instructions have been followed successfully.
- Required tests for the release candidate pass.
- Sensitive signing material and secrets are not committed.
- Known limitations and target-device validation results are stated accurately.
- The product owner explicitly approves distribution.
Do not begin: public distribution until the owner selects a distribution method, package ID, signing approach, and applicable license/ownership decisions.
6. Dependencies and Critical Path
The intended critical path is:
Product decisions
    ↓
Approved scope and requirements
    ↓
Architecture + state/lifecycle + platform constraints
    ↓
Project foundation
    ↓
Floating overlay vertical slice
    ↓
State/lifecycle and bounded recovery
    ↓
Accessibility integration, only if required
    ↓
Studio + diagnostics
    ↓
Target-device reliability validation
    ↓
Release candidate
Testing is not a single final phase. Each implementation milestone has its own tests and exit gate. M7 consolidates end-to-end and device-level validation.

Some documentation and repository setup can occur in parallel, but implementation work must not depend on unresolved high-impact decisions.
7. How Milestones Become Sprints
Each milestone will be broken down in SPRINT_PLAN.md into small tasks. Every task must have:
- a unique task ID;
- one primary objective;
- clear in-scope and out-of-scope work;
- prerequisites;
- expected artifacts or files, where known;
- acceptance criteria;
- test commands or manual test steps;
- rollback/regression considerations for lifecycle-sensitive changes;
- documentation changes required;
- evidence of completion and unresolved issues.
Avoid tasks described only as “build the runtime,” “make it reliable,” or “implement all UI.” Split such work into testable slices.

A task should be reduced further if it changes several unrelated subsystems, adds a major dependency, mixes architecture redesign with feature work, or cannot have a clear pass/fail outcome.
8. Progress Tracking
Use the following status values consistently:
- Not Started — work has not begun.
- Ready — dependencies and acceptance criteria are clear.
- In Progress — implementation is underway.
- Blocked — an unresolved decision, external dependency, or technical issue prevents progress.
- Review Required — implementation and self-checks are complete; owner/reviewer review is needed.
- Passed — exit criteria have evidence and the milestone is accepted.
- Deferred — explicitly postponed with a reason.
- Rejected — proposed work was not approved.
Status must reflect actual evidence, not agent confidence. Mark a milestone Passed only after its exit criteria are met and the relevant results are recorded.
9. Change and Replanning Rules
When a requirement, platform constraint, or discovered implementation fact changes the roadmap, the agent must:
1. identify the affected milestone and tasks;
2. explain the reason and evidence;
3. assess effects on scope, architecture, permissions, privacy, battery, and tests;
4. propose a revised sequence;
5. update the decision log and affected documents after approval.
The roadmap may be reordered to handle a verified dependency or risk, but agents may not remove exit criteria or silently mark incomplete work as passed.

A bug fix that restores approved behavior may be added to the current or next sprint. Any new feature, added sensitive permission, networking capability, or material architecture change requires explicit approval.
10. Risks to Track
ID	Risk	Mitigation
RK-01	Root cause of the existing app's failure is unknown.	Instrument observable states and test failure scenarios; avoid assumptions.
RK-02	Android/Realme background restrictions limit runtime persistence.	Verify official platform requirements and test on the target device.
RK-03	Primary action choice changes permission/architecture needs.	Resolve before dependent implementation.
RK-04	AI agents expand scope or refactor unrelated code.	Use AGENTS.md, task templates, and change-control rules.
RK-05	Build tests pass but device lifecycle fails.	Require device-level validation for lifecycle/permission behavior.
RK-06	Diagnostics grow without bound or expose unnecessary details.	Define local retention limits and minimize recorded data.
RK-07	App claims unsupported recovery guarantees.	Require platform-aware acceptance criteria and truthful user messaging.
RK-08	Incomplete documentation becomes inconsistent.	Maintain traceability and update affected documents as decisions change.


11. Roadmap Approval
- Product owner: Anuj Pawar (@anujp125)
- Roadmap status: Approved
- Approved target milestone for initial implementation: Milestone M1 (MVP v0.1 Core Reliability & Groundwork)
- Review date: 2026-10-29
- Accepted deviations: None
- Notes: Approved via project questionnaire. Milestones M0 & M1 unblocked.
This roadmap is a controlled draft until reviewed. Approved scope and platform constraints remain mandatory even when the sprint order changes.