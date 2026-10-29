AssistX — Sprint Plan and Implementation Backlog
Document ID: NUDGE-SPRINT-001
Version: 0.1
Status: Approved Backlog
Last updated: 2026-10-29
Product: AssistX — one installable native Android app
Related documents: PRODUCT_BRIEF.md, SRS.md, MVP_SCOPE.md, PRODUCT_ROADMAP.md, SYSTEM_ARCHITECTURE.md, MODULE_AND_PACKAGE_STRUCTURE.md, STATE_AND_LIFECYCLE.md, PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md, FAILURE_AND_RECOVERY_MATRIX.md, TEST_STRATEGY.md, ACCEPTANCE_TESTS.md, REAL_DEVICE_TEST_MATRIX.md, AGENTS.md, AI_AGENT_WORKFLOW.md, SPRINT_TASK_TEMPLATE.md
Instruction for AI agents: This is the planned backlog, not authorization to execute all tasks in sequence automatically. Work only on the task explicitly assigned by the owner or approved workflow. Before implementation, copy the task into a task-specific brief using SPRINT_TASK_TEMPLATE.md. Preserve unresolved decisions. A task may be marked Ready only when its prerequisites and acceptance criteria are sufficiently clear.

1. Purpose
This plan turns AssistX v0.1 into a sequence of small, reviewable tasks. Every task should have one primary objective, a clear scope boundary, measurable acceptance criteria, and relevant tests.

The plan prioritizes:
1. resolving product choices that affect permissions and architecture;
2. establishing a reproducible Android project;
3. implementing one reliable floating overlay;
4. modelling state and lifecycle explicitly;
5. adding Accessibility only if the approved action requires it;
6. building a minimal Studio and local diagnostics;
7. validating failure behavior on the target phone;
8. preparing a release candidate only after the evidence is reviewed.
AssistX is one installable app. The floating assistant and Studio are internal components, not separate applications.
2. How to Use This Backlog
- Select one task at a time.
- Create a task-specific file from SPRINT_TASK_TEMPLATE.md for the selected task.
- Read AGENTS.md and AI_AGENT_WORKFLOW.md before coding.
- Inspect the repository and preserve existing user work.
- Confirm prerequisite tasks and decisions are complete.
- Implement only the selected task.
- Add/update tests in the same task wherever practical.
- Run relevant checks and report actual evidence.
- Update task status and docs only when supported by results.
- Review and accept the task before assigning dependent work.
Do not ask an agent to “finish Sprint 1” if the sprint contains multiple independent tasks. Assign a task ID or a small explicitly named group of tasks.
3. Status and Priority Rules
Status values
- Not Started
- Blocked
- Ready
- In Progress
- Review Required
- Passed
- Deferred
- Rejected
Priority
- P0: blocks core MVP development or acceptance.
- P1: needed for a complete, maintainable, release-ready MVP.
- P2: optional or explicitly deferred.
A task is Passed only after required acceptance criteria and tests have evidence. Writing tests without executing them is not a pass. A build passing is not proof that Android lifecycle behavior works.
4. Mandatory Decision Gate Before Code
The following items can materially affect permissions, architecture, or test design. They must be resolved or handled by an explicitly approved limited technical spike before tasks depending on them become Ready.
Decision ID	Decision	Required before
D-01	Exact primary button action	Any action-specific implementation
D-02	Trigger gesture and timing	Floating interaction implementation
D-03	Whether Accessibility is required	Accessibility manifest/service and action implementation
D-04	Minimum supported Android version and target SDK strategy	Final project configuration and platform-sensitive implementation
D-05	UI toolkit: Compose or Views	Studio/floating UI implementation
D-06	Application ID/package name	Release configuration; a temporary local ID may be used only if clearly documented and accepted
D-07	Whether button dragging and position persistence are included	Floating interaction implementation
D-08	Distribution method	Release/signing and Play policy decisions
D-09	Whether automatic startup after reboot is required for v0.1	Boot/startup implementation
D-10	Retry/cooldown policy	Production recovery implementation
D-11	Diagnostic retention bounds	Diagnostics implementation/release
D-12	Reliability-test duration/targets	Final reliability sign-off


For each decision, record the chosen option, rationale, date, and affected documents in DECISION_LOG.md and, for technical choices, ARCHITECTURE_DECISIONS.md.

If a decision is unresolved, agents may continue unrelated safe work but must not silently commit to an architecture that depends on it.
5. Sprint Overview
Sprint	Name	Main outcome	Exit gate
S0	Requirements and technical decisions	Scope is reviewed; architecture-critical decisions are recorded	Owner accepts scope and decisions needed for implementation
S1	Project foundation	Reproducible one-app Android project and baseline test workflow	Clean build and baseline checks pass
S2	State model	Desired state, observed states, and status mapping are testable	State invariants have unit tests
S3	Floating overlay vertical slice	Permission-aware button attachment/removal with duplicate prevention	Core overlay cases pass on target device
S4	Runtime and recovery	Enable/disable lifecycle and bounded recovery are implemented	Recovery tests pass and no uncontrolled retry path remains
S5	Primary action / Accessibility (conditional)	The owner-approved action works using only required capabilities	Action and failure cases verified, or Accessibility task is formally not applicable
S6	Studio and diagnostics	Minimal status/settings UI and bounded local event history	UI states match real observations and diagnostics are useful
S7	Reliability validation	Acceptance and target-device tests executed	Results, defects, and limitations reviewed
S8	Release candidate	Repeatable build/install instructions and release checks	Owner approves distribution


Sprints are logical work batches, not calendar commitments. They may be split further. Dependencies may be resequenced only with a documented reason; exit criteria must not be dropped silently.
6. S0 — Requirements and Technical Decisions
Objective: Make the first implementation tasks sufficiently specific.
NUDGE-001 — Review and approve MVP scope
- Priority: P0
- Depends on: Existing PRODUCT_BRIEF.md, SRS.md, MVP_SCOPE.md
- Objective: Confirm the reliability-first scope and explicit exclusions.
- Deliverable: Owner-reviewed product brief, SRS, and MVP scope.
- Acceptance criteria:
  - One installable app is confirmed.
  - v0.1 included and excluded features are understood.
  - The primary action is identified as approved or explicitly unresolved.
  - Unresolved choices remain visible; no agent interpretation is treated as approval.
- Tests/evidence: Review checklist and decision references; no code test required.
- Status: Passed (Approved via questionnaire)
NUDGE-002 — Decide primary action and gesture
- Priority: P0
- Depends on: NUDGE-001
- Objective: Approve the exact floating-button action and triggering gesture.
- Deliverable: Recorded decision in DECISION_LOG.md, with SRS/scope updates.
- Acceptance criteria:
  - Action and gesture are stated precisely.
  - Required Android capability/permission has been investigated.
  - Failure behavior is defined at a product level.
- Tests/evidence: Decision record and official platform references, as applicable.
- Blocker rule: Do not implement action-specific APIs until approved.
- Status: Passed (Approved: Double-tap lock screen, long-press home/back)
NUDGE-003 — Decide Android support and UI direction
- Priority: P0
- Depends on: NUDGE-001
- Objective: Approve the minimum supported Android version, target-SDK strategy, and UI toolkit.
- Deliverable: Decision records and updated technical constraints.
- Acceptance criteria:
  - Minimum Android version is explicit.
  - Target SDK strategy is based on current Android requirements.
  - Compose or Views is selected with a short rationale.
- Tests/evidence: Decision record and current official Android documentation references.
- Status: Passed (Approved: minSdk 28, targetSdk 34, Jetpack Compose)
NUDGE-004 — Record app identity and distribution direction
- Priority: P1
- Depends on: NUDGE-001
- Objective: Record package/application ID and intended distribution method.
- Deliverable: Decision record.
- Acceptance criteria:
  - Package ID is finalized or a temporary development ID is explicitly accepted.
  - Distribution method is identified as personal APK, private distribution, or Play Store.
  - Any resulting signing, policy, or privacy implications are listed.
- Tests/evidence: Decision record; release checks occur later.
- Status: Passed (Approved: Double-tap lock screen, long-press home/back)
NUDGE-005 — Create decision and traceability registers
- Priority: P0
- Depends on: NUDGE-001
- Objective: Provide a controlled log for product decisions and requirement-to-test mapping.
- Deliverable: DECISION_LOG.md and REQUIREMENTS_TRACEABILITY.md.
- Acceptance criteria:
  - Each open decision has an ID, owner/status, and affected artifacts.
  - Each Must requirement from the SRS can be mapped to planned implementation work and verification.
  - No requirement or test ID is invented without a corresponding entry.
- Tests/evidence: Manual consistency review.
- Status: Ready
NUDGE-006 — Finalize architecture-critical platform analysis
- Priority: P0
- Depends on: NUDGE-002, NUDGE-003; NUDGE-004 if release policy affects the decision
- Objective: Verify current Android constraints for the approved action and proposed runtime model.
- Deliverable: Reviewed PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md and ARCHITECTURE_DECISIONS.md.
- Acceptance criteria:
  - Required special permissions are listed with a reason.
  - Accessibility necessity is established or ruled out.
  - Foreground-service/reboot behavior is not assumed without evidence.
  - Target-SDK-dependent rules are identified.
- Tests/evidence: Official Android references and a concise decision record.
- Status: Blocked — depends on action/SDK decisions
S0 exit gate: Tasks NUDGE-001, NUDGE-002, NUDGE-003, and NUDGE-006 must be accepted before implementing the primary action or permission-sensitive runtime architecture. NUDGE-004 may remain open until release setup if it does not block the local foundation.
7. S1 — Project Foundation
Objective: Establish a minimal, reproducible Android project. Do not implement all product features in this sprint.
NUDGE-007 — Inspect or create the Android project baseline
- Priority: P0
- Depends on: S0 decisions (Completed)
- Objective: Confirm the repository's actual project structure and establish one installable app module.
- Deliverable: Documented baseline structure; existing work preserved.
- Acceptance criteria:
  - One application package is planned.
  - Existing code/user changes are inspected before modification.
  - No separate Studio app or unnecessary Gradle modules are created.
  - SDK and UI choices match recorded decisions.
- Tests/evidence: Repository inspection report.
- Status: Passed

NUDGE-008 — Establish reproducible build configuration
- Priority: P0
- Depends on: NUDGE-007
- Objective: Ensure the application can be built using the project wrapper and documented configuration.
- Deliverable: Minimal buildable app and confirmed build command.
- Acceptance criteria:
  - Gradle wrapper/toolchain is recorded.
  - Application builds successfully in the available environment.
  - No secrets or machine-specific credentials are committed.
  - Build output/generated files are handled by .gitignore.
- Tests/evidence: `./gradlew.bat assembleDebug` succeeds.
- Status: Passed

NUDGE-009 — Establish test source sets and baseline checks
- Priority: P0
- Depends on: NUDGE-008
- Objective: Configure the minimum test setup needed for the first state/overlay implementation.
- Deliverable: A small baseline test and documented test commands.
- Acceptance criteria:
  - A test task can be executed.
  - The result is recorded.
  - Instrumentation setup is identified if not yet available.
  - The documentation distinguishes unit from device tests.
- Tests/evidence: `./gradlew.bat testDebugUnitTest` executed (24 tasks, PASS).
- Status: Passed

NUDGE-010 — Add initial resources and entry screen
- Priority: P1
- Depends on: NUDGE-008, NUDGE-003
- Objective: Provide a minimal application entry point that opens Studio.
- Deliverable: Simple, navigable first screen using the selected UI toolkit.
- Acceptance criteria:
  - App launches on the selected test environment.
  - User-visible strings/resources follow project conventions.
  - No fake active state or unnecessary service startup is introduced.
- Tests/evidence: Compose `MainActivity` and `StudioScreen` packaged in `app-debug.apk`.
- Status: Passed
S1 exit gate: The app builds and launches; baseline tests can run; the repository workflow is documented. (Passed)
8. S2 — State Model
Objective: Make important state distinctions testable before wiring them into Android services.
NUDGE-011 — Implement explicit state types
- Priority: P0
- Depends on: NUDGE-006, NUDGE-008
- Objective: Implement the smallest state representation needed for desired state, overlay permission/attachment, runtime, and conditional Accessibility.
- Deliverable: `StateTypes.kt` in package `com.nudge.assist.state`.
- Acceptance criteria:
  - Desired state is separate from observed runtime/overlay state.
  - Accessibility state is separate and conditional.
  - Unknown/unobserved conditions can be represented.
  - No single isRunning boolean replaces the state model.
- Tests/evidence: `StateModelTest` (3 tests, PASS).
- Status: Passed

NUDGE-012 — Implement derived status mapping
- Priority: P0
- Depends on: NUDGE-011
- Objective: Derive the user-facing status from the underlying state dimensions.
- Deliverable: `StatusEvaluator.kt` pure mapping function and `UserFacingStatus` enum.
- Acceptance criteria:
  - Missing overlay permission cannot map to Active.
  - Required-but-disabled Accessibility cannot map to fully ready.
  - Unknown observations do not become success states.
  - Secondary component details are not lost.
- Tests/evidence: `StatusEvaluatorTest` (11 tests, PASS).
- Status: Passed

NUDGE-013 — Test state invariants and transitions
- Priority: P0
- Depends on: NUDGE-011, NUDGE-012
- Objective: Protect state semantics before platform integration.
- Deliverable: `StateCoordinator.kt` and `StateCoordinatorTest.kt`.
- Acceptance criteria:
  - Explicit Disable suppresses recovery decisions.
  - An enable request does not automatically mean startup succeeded.
  - Overlay and Accessibility states can vary independently.
  - Conflicting/stale observations are handled according to the approved policy.
- Tests/evidence: `StateCoordinatorTest` (6 tests, PASS). Overall 22 unit tests passing.
- Status: Passed
S2 exit gate: Core state mapping and invariants have passing unit-test evidence. (Passed)
9. S3 — Floating Overlay Vertical Slice
Objective: Deliver one permission-aware floating button with predictable attach/remove behavior.
NUDGE-014 — Implement overlay permission adapter
- Priority: P0
- Depends on: NUDGE-006, NUDGE-013
- Objective: Encapsulate checking the current overlay permission and routing the user to settings.
- Deliverable: Narrow permission-check/settings-navigation component.
- Acceptance criteria:
  - Permission state can be GRANTED, REQUIRED, or UNKNOWN.
  - Returning from settings causes a current re-check.
  - The component does not assume a grant.
  - No permission prompt/settings loop occurs.
- Tests/evidence: Unit/instrumentation coverage where practical; target-device check.
- Status: Not Started
NUDGE-015 — Implement overlay attach/remove manager
- Priority: P0
- Depends on: NUDGE-014, NUDGE-011
- Objective: Encapsulate the platform window operations for the floating button.
- Deliverable: Overlay manager and minimal floating-button presentation.
- Acceptance criteria:
  - One overlay may be attached at a time.
  - Attachment/removal outcomes are surfaced.
  - Partial initialization and repeated removal are safe.
  - No unrelated action or gesture is added.
- Tests/evidence: Tests using a controllable adapter plus actual-device tests.
- Status: Not Started
NUDGE-016 — Verify the overlay vertical slice on device
- Priority: P0
- Depends on: NUDGE-015
- Objective: Validate granted/missing permission and repeated attach/remove behavior.
- Deliverable: Test record for AT-005 through AT-010, as applicable.
- Acceptance criteria:
  - At least one documented successful show/remove cycle.
  - Missing permission case is truthful.
  - Repeated enable/disable does not create duplicates.
  - Failures have usable diagnostic evidence.
- Tests/evidence: Real-device results and bugs tracked.
- Status: Not Started
S3 exit gate: The one-overlay invariant and permission failure path are verified on the target device.
10. S4 — Runtime and Recovery
Objective: Coordinate user intent and component lifecycle without unbounded recovery.
NUDGE-017 — Implement minimal preferences persistence
- Priority: P0
- Depends on: NUDGE-013; persistence choice documented
- Objective: Persist only the approved preferences needed by v0.1.
- Deliverable: Small preferences adapter/repository.
- Acceptance criteria:
  - Desired state can be saved and loaded.
  - Safe defaults are defined.
  - Persisted runtime health is not treated as current truth.
  - No database/library is added without justification.
- Tests/evidence: Unit/integration tests for defaults, round-trip, and malformed/missing values.
- Status: Not Started
NUDGE-018 — Implement enable/disable coordination
- Priority: P0
- Depends on: NUDGE-015, NUDGE-017
- Objective: Connect user intent to overlay/runtime operations through one coordinated path.
- Deliverable: Minimal runtime coordinator or equivalent.
- Acceptance criteria:
  - Enable checks prerequisites and records actual outcomes.
  - Disable records user intent and requests cleanup.
  - Repeated start/stop calls are safe.
  - UI rendering does not create lifecycle side effects.
- Tests/evidence: Unit tests with controlled operation outcomes; integration test.
- Status: Not Started
NUDGE-019 — Approve and implement bounded recovery policy
- Priority: P0
- Depends on: NUDGE-018, NUDGE-006
- Objective: Implement a documented, bounded policy for eligible failures only.
- Deliverable: Recovery policy with reviewed retry/cooldown/reset settings.
- Acceptance criteria:
  - Retry values are centralized and documented, not scattered magic constants.
  - Disablement prevents recovery.
  - Known permission blockers prevent futile retries.
  - Outcomes distinguish success, failure, blocked, and unknown.
  - Retry exhaustion leads to a recovery-required status.
- Tests/evidence: Deterministic unit tests for eligibility, retries, cooldowns, and exhaustion.
- Blocker rule: Do not set production retry values without a documented engineering proposal and review.
- Status: Ready (Approved: 3 retries, 1s/2s/5s backoff, 60s reset)
NUDGE-020 — Validate lifecycle coordination
- Priority: P0
- Depends on: NUDGE-018, NUDGE-019
- Objective: Verify rapid enable/disable, partial startup, and conflicting operations.
- Deliverable: Integration/unit test evidence.
- Acceptance criteria:
  - Old asynchronous outcomes do not override newer user intent.
  - No duplicate overlay/start operation is created.
  - Partial failures are reconciled or marked unknown.
  - Automatic recovery does not run after user Disable.
- Tests/evidence: Tests for AT-004, AT-019 through AT-021 as applicable.
- Status: Not Started
S4 exit gate: State and recovery logic passes deterministic tests; at least the relevant lifecycle cases have device/integration evidence.
11. S5 — Primary Action and Accessibility (Conditional)
Objective: Implement only the approved primary action, using the least-privileged viable capability.
NUDGE-021 — Confirm action API and permission requirements
- Priority: P0
- Depends on: NUDGE-002, NUDGE-006
- Objective: Ensure the exact primary action can be implemented under the selected SDK and permission model.
- Deliverable: Technical decision record and updated SRS/platform constraints.
- Acceptance criteria:
  - Action and trigger are precise.
  - Required Android capability is identified.
  - API/SDK availability is verified.
  - Failure and unavailable-action behavior is defined.
  - If Accessibility is unnecessary, the Accessibility implementation is explicitly marked not applicable.
- Tests/evidence: Official documentation references and a minimal proof/analysis where needed.
- Status: Ready (Approved: GLOBAL_ACTION_LOCK_SCREEN, GLOBAL_ACTION_HOME/BACK)
NUDGE-022 — Implement Accessibility state observation (if required)
- Priority: P0 if applicable; otherwise NOT APPLICABLE
- Depends on: NUDGE-021, NUDGE-013
- Objective: Represent Accessibility enabled/connected state without conflating it with overlay state.
- Deliverable: Minimal, narrowly configured Accessibility integration.
- Acceptance criteria:
  - The service exists only if required.
  - State reflects current evidence, allowing UNKNOWN where needed.
  - Disabled and disconnected conditions are distinguished where detectable.
  - No unrelated accessibility event/content access is requested.
- Tests/evidence: Unit/state tests and device tests.
- Status: Blocked — conditional
NUDGE-023 — Implement and verify the approved primary action
- Priority: P0
- Depends on: NUDGE-021; NUDGE-022 if Accessibility is required
- Objective: Make the approved floating-button action work through the approved gesture.
- Deliverable: Action handler, status/error handling, tests, and user guidance.
- Acceptance criteria:
  - Only the approved action/gesture is implemented.
  - Required capability is checked.
  - Failure is not falsely reported as success.
  - No fallback to unapproved Device Administrator or another sensitive permission.
- Tests/evidence: Unit/instrumentation tests and target-device acceptance test AT-015/AT-018 as applicable.
- Status: Blocked — conditional
S5 exit gate: The action is approved and verified, or a documented owner-approved decision establishes that the conditional Accessibility/action work is not part of v0.1.
12. S6 — Studio and Diagnostics
Objective: Make current state, blockers, and recent diagnostic events understandable.
NUDGE-024 — Implement minimal Studio status screen
- Priority: P0
- Depends on: NUDGE-012, NUDGE-018
- Objective: Display desired state, runtime/overlay status, and available recovery guidance.
- Deliverable: Studio status screen using the selected UI toolkit.
- Acceptance criteria:
  - UI is driven by actual state observations.
  - Enable/disable is explicit.
  - Missing overlay permission has actionable guidance.
  - Secondary component status remains inspectable.
  - Closing Studio does not own or stop an otherwise healthy runtime.
- Tests/evidence: UI/state tests and RT-007/AT-012.
- Status: Not Started
NUDGE-025 — Implement permission and recovery guidance
- Priority: P0
- Depends on: NUDGE-014, NUDGE-024; NUDGE-022 if applicable
- Objective: Provide clear, specific next steps for known blockers.
- Deliverable: Permission/recovery messages and settings navigation.
- Acceptance criteria:
  - Overlay permission and Accessibility conditions use different messages.
  - Opening settings is not treated as permission success.
  - Unsupported recovery is described honestly.
  - No repetitive settings-opening loop.
- Tests/evidence: AT-006, AT-007, AT-008, AT-016 as applicable.
- Status: Not Started
NUDGE-026 — Implement diagnostic event model and recorder
- Priority: P0
- Depends on: NUDGE-005, NUDGE-018
- Objective: Record relevant local state transitions and operation outcomes.
- Deliverable: Diagnostic event type and local recording mechanism.
- Acceptance criteria:
  - Events distinguish component, event type, source, time, and outcome where available.
  - Events represent observed facts, not assumed causes.
  - Logging is best-effort and does not normally break core behavior.
  - No unapproved network transmission occurs.
- Tests/evidence: Unit/integration tests for event shape and failure handling.
- Status: Not Started
NUDGE-027 — Add bounded retention and recent-event view
- Priority: P1
- Depends on: NUDGE-026, NUDGE-024
- Objective: Enforce retention bounds and give the user a simple way to review recent events.
- Deliverable: Bounded diagnostics storage and minimal recent-events UI, if retained in the approved MVP.
- Acceptance criteria:
  - Retention/size limit is documented and approved.
  - Old events are cleaned up deterministically.
  - Storage failure does not create an infinite logging loop.
  - The user can inspect relevant recent events without cloud/backend access.
- Tests/evidence: AT-026, AT-027, RT-019 as applicable.
- Status: Ready (Approved: Room DB, max 500 records or 2 MB)
S6 exit gate: Studio status is truthful, recovery guidance is actionable, and diagnostics are useful, bounded, and local.
13. S7 — Reliability and Validation
Objective: Establish product behavior through actual evidence.
NUDGE-028 — Complete automated acceptance/regression tests
- Priority: P0
- Depends on: NUDGE-013, NUDGE-020, applicable S5/S6 tasks
- Objective: Run relevant automated tests and fill in acceptance results.
- Deliverable: Updated ACCEPTANCE_TESTS.md, test reports, and traceability matrix.
- Acceptance criteria:
  - Applicable automated test cases are executed.
  - Failures are fixed or logged.
  - Flaky/unrun cases are explicit.
  - Requirement-to-test links are complete for Must requirements.
- Tests/evidence: Actual command output and case results.
- Status: Not Started
NUDGE-029 — Complete target-device test matrix
- Priority: P0
- Depends on: NUDGE-016, NUDGE-020, applicable S5/S6 tasks
- Objective: Execute the applicable physical-device cases on the target Realme phone.
- Deliverable: Completed REAL_DEVICE_TEST_MATRIX.md run record.
- Acceptance criteria:
  - Permission scenarios and repeated overlay lifecycle are tested.
  - Studio open/close, switching apps, lock/unlock, and approved action are tested.
  - Accessibility tests are performed only if applicable.
  - Process death, force-stop, and reboot are documented separately.
  - Results distinguish verified causes from hypotheses.
- Tests/evidence: Device model/build, app commit, steps, results, and logs.
- Status: Not Started
NUDGE-030 — Investigate and fix critical/high defects
- Priority: P0
- Depends on: NUDGE-028, NUDGE-029
- Objective: Resolve defects that block the approved core workflow or make status misleading.
- Deliverable: Small bug-fix tasks with reproduction and regression tests.
- Acceptance criteria:
  - Each defect has a reproduction case or clearly documented reason it could not be reproduced.
  - Fixes are tied to relevant requirements.
  - Regression tests are added where feasible.
  - Unresolved issues are captured in KNOWN_ISSUES.md.
- Tests/evidence: Linked bug reports, commits/PRs, and executed results.
- Status: Not Started
NUDGE-031 — Measure resource use and long-duration behavior
- Priority: P1
- Depends on: NUDGE-029; approved measurement period/targets
- Objective: Observe battery/memory/log growth and intermittent runtime behavior over the agreed period.
- Deliverable: Test report with method, duration, observations, and limitations.
- Acceptance criteria:
  - Duration and observation method are recorded.
  - Results are compared to approved targets if targets exist.
  - Failures are captured with context.
  - No claim of guaranteed persistence is made from limited evidence.
- Tests/evidence: RT-020 and any agreed measurement records.
- Status: Blocked — owner measurement target required
S7 exit gate: Required tests are executed; critical defects are resolved; limitations and exceptions are reviewed and documented.
14. S8 — Release Candidate and Handoff
Objective: Make the approved build reproducible and safe to install/distribute.
NUDGE-032 — Complete installation and troubleshooting docs
- Priority: P1
- Depends on: NUDGE-029
- Objective: Document setup, permission grants, common failure states, and recovery steps from verified behavior.
- Deliverable: INSTALLATION_AND_SETUP.md, TROUBLESHOOTING.md, PRIVACY_AND_PERMISSIONS.md, COMPATIBILITY_AND_LIMITATIONS.md.
- Acceptance criteria:
  - Instructions match the actual app.
  - Device-specific guidance is evidence-backed.
  - Unsupported behaviors and user-required recovery are stated.
  - Privacy text matches stored data and app behavior.
- Tests/evidence: Follow instructions from a clean installation.
- Status: Not Started
NUDGE-033 — Validate release build and signing approach
- Priority: P1
- Depends on: NUDGE-004, NUDGE-008, NUDGE-030
- Objective: Produce and verify the intended release artifact.
- Deliverable: Reproducible release build instructions and artifact verification.
- Acceptance criteria:
  - Distribution method and package identity are confirmed.
  - Signing material is not committed.
  - Release build succeeds using the documented process.
  - Required tests for the release candidate have been run.
- Tests/evidence: Build output and package/install verification.
- Status: Blocked — distribution/package decisions required
NUDGE-034 — Complete release checklist and owner sign-off
- Priority: P1
- Depends on: NUDGE-032, NUDGE-033
- Objective: Decide whether v0.1 is ready for distribution.
- Deliverable: Completed RELEASE_CHECKLIST.md, changelog, and owner decision.
- Acceptance criteria:
  - Critical acceptance criteria pass or have explicitly rejected exceptions.
  - No unresolved critical defect blocks the core workflow.
  - Known limitations and unrun tests are visible.
  - The owner approves distribution.
- Tests/evidence: Release checklist and recorded approval.
- Status: Not Started
S8 exit gate: No public/private distribution occurs until the owner approves the target artifact, packaging, signing, documentation, and known limitations.
15. Task Status Table
Update this table as tasks are assigned and accepted. It is a tracking aid; detailed acceptance evidence belongs in task reports and the test documents.
Task	Title	Priority	Depends on	Initial status
NUDGE-001	Review and approve MVP scope	P0	Product docs	Ready for owner review
NUDGE-002	Decide primary action and gesture	P0	NUDGE-001	Blocked
NUDGE-003	Decide Android support and UI direction	P0	NUDGE-001	Blocked
NUDGE-004	Record app identity and distribution	P1	NUDGE-001	Blocked
NUDGE-005	Create decision and traceability registers	P0	NUDGE-001	Ready
NUDGE-006	Finalize architecture-critical platform analysis	P0	NUDGE-002, 003	Blocked
NUDGE-007	Inspect/create project baseline	P0	NUDGE-003	Not Started
NUDGE-008	Reproducible build configuration	P0	NUDGE-007	Not Started
NUDGE-009	Test source sets and baseline checks	P0	NUDGE-008	Not Started
NUDGE-010	Initial resources and entry screen	P1	NUDGE-008, 003	Not Started
NUDGE-011	Explicit state types	P0	NUDGE-006, 008	Not Started
NUDGE-012	Derived status mapping	P0	NUDGE-011	Not Started
NUDGE-013	State invariant tests	P0	NUDGE-011, 012	Not Started
NUDGE-014	Overlay permission adapter	P0	NUDGE-006, 013	Not Started
NUDGE-015	Overlay attach/remove manager	P0	NUDGE-014, 011	Not Started
NUDGE-016	Overlay device validation	P0	NUDGE-015	Not Started
NUDGE-017	Preferences persistence	P0	NUDGE-013	Not Started
NUDGE-018	Enable/disable coordination	P0	NUDGE-015, 017	Not Started
NUDGE-019	Bounded recovery policy	P0	NUDGE-018, 006	Blocked
NUDGE-020	Lifecycle coordination validation	P0	NUDGE-018, 019	Not Started
NUDGE-021	Confirm action API/permissions	P0	NUDGE-002, 006	Blocked
NUDGE-022	Accessibility state observation	Conditional P0	NUDGE-021, 013	Blocked
NUDGE-023	Implement approved primary action	Conditional P0	NUDGE-021, 022 if needed	Blocked
NUDGE-024	Minimal Studio status screen	P0	NUDGE-012, 018	Not Started
NUDGE-025	Permission/recovery guidance	P0	NUDGE-014, 024	Not Started
NUDGE-026	Diagnostic event model/recorder	P0	NUDGE-005, 018	Not Started
NUDGE-027	Bounded retention/recent-event view	P1	NUDGE-026, 024	Blocked
NUDGE-028	Automated acceptance/regression tests	P0	Core feature tasks	Not Started
NUDGE-029	Target-device test matrix	P0	NUDGE-016, 020, applicable features	Not Started
NUDGE-030	Fix critical/high defects	P0	NUDGE-028, 029	Not Started
NUDGE-031	Resource and long-duration test	P1	NUDGE-029	Blocked
NUDGE-032	Installation/troubleshooting docs	P1	NUDGE-029	Not Started
NUDGE-033	Release build/signing validation	P1	NUDGE-004, 008, 030	Blocked
NUDGE-034	Release checklist/owner sign-off	P1	NUDGE-032, 033	Not Started


16. Per-Task Execution Contract
Before assigning any task:
1. Copy its entry into a task-specific file using SPRINT_TASK_TEMPLATE.md.
2. Read AGENTS.md and AI_AGENT_WORKFLOW.md.
3. Confirm prerequisites have passed.
4. Fill in acceptance criteria and test commands.
5. Mark the task Ready only when any blocking decision is resolved.
6. Assign only that task or a deliberately small group of closely related tasks.
After implementation:
1. Review the diff.
2. Execute relevant checks.
3. Report exact results and unrun tests.
4. Update status and requirement-to-test traceability.
5. Request review before marking dependent work ready.
17. Backlog Change Rules
- Do not silently reorder tasks if the change weakens an exit gate.
- A dependency may be changed when implementation evidence or verified platform requirements justify it; record the reason.
- New user-facing features, permissions, major dependencies, network capability, service strategies, or architecture changes require owner approval.
- Bug fixes should be separate tasks tied to a reproduced defect or clearly documented failure.
- Deferred tasks must retain a reason and must not be counted as completed.
- When a task is split, keep the parent objective and traceability visible and assign unique child task IDs.
- When a decision makes a conditional task unnecessary, mark it NOT APPLICABLE with a link to the approved decision; do not delete the traceability entry.
18. Overall v0.1 Definition of Done
AssistX v0.1 is ready for owner acceptance only when:
- the single-app product scope and primary action are approved;
- the application builds using documented steps;
- the floating overlay works under granted prerequisites and reports permission failures accurately;
- overlay and Accessibility state (if relevant) remain distinct;
- user intent, lifecycle, and recovery invariants pass tests;
- the Studio interface presents truthful status and actionable recovery;
- local diagnostics are bounded and privacy-conscious;
- applicable automated acceptance tests have been executed;
- the target-device test matrix is completed or exceptions are explicitly accepted;
- critical defects are resolved;
- setup, troubleshooting, privacy, compatibility, and release documents match actual behavior;
- the release artifact and distribution method are approved by the owner.
19. Owner Review and Approval
- Product owner: Anuj Pawar (@anujp125)
- Roadmap/backlog status: Approved
- Approved primary action: Double-tap lock screen (GLOBAL_ACTION_LOCK_SCREEN); Long-press home/back (GLOBAL_ACTION_HOME/BACK)
- Approved UI/SDK direction: Jetpack Compose with Material 3; minSdk 28; targetSdk 34; package com.nudge.assist
- Approved measurement targets: 8h soak test; <1% battery / 8h idle; <40MB RAM RSS; <150ms touch latency
- Review date: 2026-10-29
- Accepted deviations: None
- Notes: Backlog and sprint plan formally approved. Sprint S1 is Ready.